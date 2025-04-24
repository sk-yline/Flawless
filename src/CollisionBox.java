import java.awt.geom.Rectangle2D;
import java.util.*;

// CollisionBox represents a game object's physical boundaries for collision detection
// Can represent various game elements like players, enemies, walls, platforms, etc.
public class CollisionBox {
    // Position of the collision box's center in world coordinates
    public Vector pos;
    // Size of the collision box (width and height)
    public Vector size;
    // Rotation angle in degrees
    public double theta;
    // Type identifier (Player, Enemy, Wall, Platform, Bullet, Attack, etc.)
    public String type;
    // Position in screen coordinates (relative to camera)
    public Vector frame_pos = new Vector();
    // Flag indicating if this collision box is active for collision detection
    boolean enable = true;
    // Temporary disable flag
    boolean temp_shut = false;

    // Constructor initializes a collision box with position, size, rotation, and type
    public CollisionBox(Vector pos, Vector size, double theta, String type){
        this.pos = pos;
        this.size = size;
        this.theta = theta;
        this.type = type;
    }

    // Calculates the four vertices/corners of the collision box after rotation
    // Used for collision detection with Separating Axis Theorem (SAT)
    public ArrayList<Vector> get_verticies(){
        double half_w = size.x/2;
        double half_h = size.y/2;

        //Local un_rotated coordinates using (0,0) as center
        ArrayList<Vector> local_vert = new ArrayList<Vector>() {
            {
            add(new Vector(-half_w, -half_h)); //bottom-left 0
            add(new Vector(half_w, -half_h)); //bottom-right 1
            add(new Vector(half_w, half_h)); //top-right 2
            add(new Vector(-half_w, half_h)); //top-left 3
            }
        };

        // Calculate rotation using rotation matrix
        double cos_theta = Math.cos(theta * Math.PI / 180);
        double sin_theta = Math.sin(theta * Math.PI / 180);

        ArrayList<Vector> rotated_vert = new ArrayList<>();
        for (Vector i : local_vert){
            /*Rotation matrix:
            [cos, -sin
             sin, cos]
            */
            double x_rot = i.x * cos_theta - i.y * sin_theta;
            double y_rot = i.x * sin_theta + i.y * cos_theta;
            rotated_vert.add(new Vector(pos.x + x_rot, pos.y - y_rot));
        }

        return rotated_vert;
    }

    // Returns a Rectangle2D representation of this collision box
    // Used for simple collision checks and rendering debug visuals
    public Rectangle2D get_rect(){
        ArrayList<Vector> vert = get_verticies();
        return new Rectangle2D.Double(vert.get(3).x, vert.get(3).y, size.x, size.y);
    }

    // Timer for special collision objects like refill items
    final double sound_pause = 0.4;
    public double sound_timer = 0;
    
    // Updates refill type collision boxes
    // Handles enabling/disabling the collision box based on a timer
    public void update_ref(double delta){
        if (enable == false && sound_timer == 0){
            sound_timer = sound_pause;
        }

        sound_timer -= delta;
        if (sound_timer <= 0){
            sound_timer = 0;
        }
        if (sound_timer == 0){
            enable = true;
        }
    }

}
