import java.util.*;

// CollisionManager handles collision detection and response between game objects
// Uses Separating Axis Theorem (SAT) for precise collision detection between rotated rectangles
public class CollisionManager{
    ArrayList<CollisionBox> collidable = new ArrayList<>();

    // Checks if two collision boxes are colliding using Separating Axis Theorem (SAT)
    public boolean collide(CollisionBox a, CollisionBox b){
        ArrayList<Vector> verticiesA = a.get_verticies();
        ArrayList<Vector> verticiesB = b.get_verticies();
        
        // Check for separation along axes perpendicular to edges of box A
        for (int i = 0; i<verticiesA.size(); i++){
            Vector va = verticiesA.get(i);
            Vector vb = verticiesA.get((i+1)%verticiesA.size());

            Vector edge = vb.minus(va);
            Vector axis = new Vector(edge.y, -edge.x); // Normal vector to the edge
            axis = axis.normalize();

            Vector projA = project_vect(verticiesA, axis);
            Vector projB = project_vect(verticiesB, axis);
            
            // If projections don't overlap along any axis, objects aren't colliding
            if (projA.x > projB.y || projB.x > projA.y){
                return false;
            }
        }

        // Check for separation along axes perpendicular to edges of box B
        for (int i = 0; i<verticiesB.size(); i++){
            Vector va = verticiesB.get(i);
            Vector vb = verticiesB.get((i+1)%verticiesB.size());

            Vector edge = vb.minus(va);
            Vector axis = new Vector(edge.y, -edge.x); // Perpendicular axis to the edge
            axis = axis.normalize();

            Vector projA = project_vect(verticiesA, axis);
            Vector projB = project_vect(verticiesB, axis);
            
            // If projections don't overlap along any axis, objects aren't colliding
            if (projA.x > projB.y || projB.x > projA.y){
                return false;
            }
        }

        // If no separating axis was found, the objects are colliding
        return true;
    }

    // Projects all vertices of an object onto an axis and returns min and max values
    // Returns a Vector where x is the min projection and y is the max projection
    public Vector project_vect(ArrayList<Vector> verticies, Vector axis){
        double min = 100000000;
        double max = -100000000;

        for (Vector v : verticies){
            double proj = v.dot(axis);

            if (proj < min) min = proj;
            if (proj > max) max = proj;
        }
        
        return new Vector(min, max);
    }

    // Checks if a collision box is colliding with any enabled collision box of the given type
    // Useful for checking if a player is colliding with walls, platforms, etc.
    public boolean is_collide(CollisionBox reference, String target){
        for (CollisionBox c : collidable){
            if (c.type.equals(target) && c.enable && reference.enable){
                if (collide(reference, c)){
                    return true;
                }
            }
        }
        return false;
    }

    // Finds the index of a collision box with the specified type
    // Returns -1 if no box with that type is found
    public int get_box(String target){
        for (int i = 0; i<collidable.size(); i++){
            if (collidable.get(i).type.equals(target)){
                return i;
            }
        }
        return -1;
    }

    // Updates screen positions for all collision boxes based on camera position
    // Should be called every frame to ensure collision boxes are correctly positioned
    public void update_pos(){
        for (CollisionBox c: collidable){
            c.frame_pos = c.pos.to_frame(Game.camera);
        }
    }

    // Updates the player attack collision box position and rotation
    // Called when the player is attacking to position the attack hitbox
    public void update_player_attack(double dir){
        int atk_ind = get_box("player_atk");
        collidable.get(atk_ind).pos.x = Game.player.pos.x + Game.player.atk_offset * Math.cos(Math.toRadians(dir));
        collidable.get(atk_ind).pos.y = Game.player.pos.y - Game.player.atk_offset * Math.sin(Math.toRadians(dir));
        collidable.get(atk_ind).theta = dir;
    }

    // Handles platform collision logic
    // Platforms can be walked on from above but passed through from below
    public void update_platform(){
        double player_bottom = Game.player.pos.y + Game.player.size.y/2;
        for (CollisionBox c : collidable){
            if (c.type.equals("platform")){
                double top = c.pos.y - c.size.y/2;
                // Enable platform collision when player is above the platform
                if (player_bottom < top - 4){
                    c.enable = true;
                }
                // Disable platform collision when player is below the platform
                if (player_bottom > top + 2){
                    c.enable = false;
                }
            }
        }
    }

    // Updates the refill objects like powerups or health pickups
    // Calls the update_ref method on all refill collision boxes
    public void update_refill(double delta){
        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("refill")){
                c.update_ref(delta);
            }
        }
    }

    // Main update method called every frame to handle all collision-related updates
    public void update(double delta){
        update_pos();
        update_platform();
        update_refill(delta);
    }
}