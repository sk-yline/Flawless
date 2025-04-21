import java.util.*;

public class CollisionManager{
    ArrayList<CollisionBox> collidable = new ArrayList<>();

    //Seperate Axis Therom (SAT) Collision Detection
    public boolean collide(CollisionBox a, CollisionBox b){
        ArrayList<Vector> verticiesA = a.get_verticies();
        ArrayList<Vector> verticiesB = b.get_verticies();
        for (int i = 0; i<verticiesA.size(); i++){
            Vector va = verticiesA.get(i);
            Vector vb = verticiesA.get((i+1)%verticiesA.size());

            Vector edge = vb.minus(va);
            Vector axis = new Vector(edge.y, -edge.x);
            axis = axis.normalize();

            Vector projA = project_vect(verticiesA, axis);
            Vector projB = project_vect(verticiesB, axis);
            if (projA.x > projB.y || projB.x > projA.y){
                return false;
            }
        }

        for (int i = 0; i<verticiesB.size(); i++){
            Vector va = verticiesB.get(i);
            Vector vb = verticiesB.get((i+1)%verticiesB.size());

            Vector edge = vb.minus(va);
            Vector axis = new Vector(edge.y, -edge.x);
            axis = axis.normalize();

            Vector projA = project_vect(verticiesA, axis);
            Vector projB = project_vect(verticiesB, axis);
            if (projA.x > projB.y || projB.x > projA.y){
                return false;
            }
        }

        return true;
    }

    //Vector(min, max)
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

    public int get_box(String target){
        for (int i = 0; i<collidable.size(); i++){
            if (collidable.get(i).type.equals(target)){
                return i;
            }
        }
        return -1;
    }

    public void update_pos(){
        for (CollisionBox c: collidable){
            c.frame_pos = c.pos.to_frame(Game.camera);
        }
    }

    public void update_player_attack(double dir){
        int atk_ind = get_box("player_atk");
        collidable.get(atk_ind).pos.x = Game.player.pos.x + Game.player.atk_offset * Math.cos(Math.toRadians(dir));
        collidable.get(atk_ind).pos.y = Game.player.pos.y - Game.player.atk_offset * Math.sin(Math.toRadians(dir));
        collidable.get(atk_ind).theta = dir;
    }

    public void update_platform(){
        double player_bottom = Game.player.pos.y + Game.player.size.y/2;
        for (CollisionBox c : collidable){
            if (c.type.equals("platform")){
                double top = c.pos.y - c.size.y/2;
                if (player_bottom < top - 4){
                    c.enable = true;
                }

                if (player_bottom > top + 2){
                    c.enable = false;
                }
            }
        }
    }

    public void update_refill(double delta){
        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("refill")){
                c.update_ref(delta);
            }
        }
    }

    public void update(double delta){
        update_pos();
        update_platform();
        update_refill(delta);
    }
}