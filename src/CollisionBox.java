import java.awt.geom.Rectangle2D;
import java.util.*;

public class CollisionBox {
    public Vector pos;
    public Vector size;
    public double theta;
    //Player, Enemy, Wall, Platform, Bullet, Attack
    public String type;
    public Vector frame_pos = new Vector();
    boolean enable = true;

    public CollisionBox(Vector pos, Vector size, double theta, String type){
        this.pos = pos;
        this.size = size;
        this.theta = theta;
        this.type = type;
    }

    public ArrayList<Vector> get_verticies(){
        double half_w = size.x/2;
        double half_h = size.y/2;

        //Local un_rotated corr. using (0,0) as center;
        ArrayList<Vector> local_vert = new ArrayList<Vector>() {
            {
            add(new Vector(-half_w, -half_h)); //bottom-left 0
            add(new Vector(half_w, -half_h)); //bottom-right 1
            add(new Vector(half_w, half_h)); //top-right 2
            add(new Vector(-half_w, half_h)); //top-left 3
            }
        };

        double cos_theta = Math.cos(theta * Math.PI / 180);
        double sin_theta = Math.sin(theta * Math.PI / 180);

        ArrayList<Vector> rotated_vert = new ArrayList<>();
        for (Vector i : local_vert){
            /*旋转矩阵
            [cos, -sin
             sin, cos]
            */
            double x_rot = i.x * cos_theta - i.y * sin_theta;
            double y_rot = i.x * sin_theta + i.y * cos_theta;
            rotated_vert.add(new Vector(pos.x + x_rot, pos.y - y_rot));
        }

        return rotated_vert;
    }

    public Rectangle2D get_rect(){
        ArrayList<Vector> vert = get_verticies();
        return new Rectangle2D.Double(vert.get(3).x, vert.get(3).y, size.x, size.y);
    }

}
