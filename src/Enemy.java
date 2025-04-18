
import java.awt.Color;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;


public class Enemy {
    public Vector size;
    public Vector pos;
    public String type;
    public double speed = 300/Game.SCALE;
    public double border_left = 0;
    public double border_right = 0;
    public CollisionBox box;

    public Enemy(Vector pos, Vector size, String type, CollisionBox box){ 
        this.type = type;
        this.size = size;
        this.pos = pos;
        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("wall")){
                Line2D bottom = new Line2D.Double(new Point2D.Double(pos.x, pos.y), new Point2D.Double(pos.x, pos.y + size.y/2 + 20));
                Rectangle2D rect = c.get_rect();
                if (bottom.intersects(rect)){
                    border_left = c.pos.x - c.size.x/2;
                    border_right = c.pos.x + c.size.x/2;
                    break;
                }
            }
        }
        this.box = box;
        Game.collisionManager.collidable.add(box);
    }

    public void destroy(){
        Game.removeables.add(box);
    }

    public int find_ind(){
        for (int i = 0; i<Game.collisionManager.collidable.size(); i++){
            if (Game.collisionManager.collidable.get(i).equals(this.box)){
                return i;
            }
        }
        return -1;
    }
    
    public void update_pos(){
        int enemy_ind = find_ind();
        if (enemy_ind != -1){
            Game.collisionManager.collidable.get(enemy_ind).pos = pos;
            Game.collisionManager.collidable.get(enemy_ind).frame_pos = pos.to_frame(Game.camera);
        }
    }

    public boolean see_player(){
        CollisionBox observer = new CollisionBox(pos, new Vector(size.x * 30, size.x * 26), 0, "see");
        observer.frame_pos = pos.to_frame(Game.camera);
        // Game.rend.g.setColor(Color.white);
        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("player")){
                if (Game.collisionManager.collide(observer, c)){
                    // Game.rend.g.setColor(Color.red);
                    // Game.rend.g.drawRect((int)(observer.frame_pos.x - observer.size.x/2), (int)(observer.frame_pos.y - observer.size.y/2), (int)(observer.size.x), (int)(observer.size.y));
                    return true;
                }
            }
        }

        // Game.rend.g.drawRect((int)(observer.frame_pos.x - observer.size.x/2), (int)(observer.frame_pos.y - observer.size.y/2), (int)(observer.size.x), (int)(observer.size.y));
        Game.rend.g.setColor(Color.green);
        return false;
    }

    public int find_angle(){
        Vector edge = Game.player.pos.minus(pos);
        if (edge.y <= 0){
            return Math.round((float)(Math.acos(edge.dot(new Vector(1, 0)) / edge.magnitude()) * 180 / Math.PI));
        }
        else{
            return Math.round((float)(Math.acos(edge.dot(new Vector(-1, 0)) / edge.magnitude()) * 180 / Math.PI)) + 180;
        }
    }


    public void update_1(double delta){

    }
 

    double bullet_cool = 0;
    final double bullet_cooldown = 1;
    double pre_cast = 0.5;
    final double pre_cast_delay = 0.5;
    
    public void update_2(double delta){
       if (bullet_cool == 0 && pre_cast == 0){
            Random rnd = new Random();
            int bullet_dir = find_angle();
            int drift = rnd.nextInt(5) - 2;
            Game.bullets.add(new Bullet(new Vector(pos.x, pos.y), bullet_dir + drift, "enemy"));
            bullet_cool = bullet_cooldown;
       }
    }

    public void update(double delta){
        if (see_player()){
            if (type.equals("enemy_1")){
                update_1(delta);
            }
            else if (type.equals("enemy_2")){
                update_2(delta);
                pre_cast -= delta;
                if (pre_cast < 0){
                    pre_cast = 0;
                }
            }
        }
        else{
            pre_cast = pre_cast_delay;
        }
        update_pos();
        bullet_cool -= delta;
        if (bullet_cool < 0){
            bullet_cool = 0;
        }

        // Game.rend.g.drawString(Double.toString(bullet_cool), 900, 100);
        // Game.rend.g.drawString(Double.toString(pre_cast), 900, 200);
    }
}
