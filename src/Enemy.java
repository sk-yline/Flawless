
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
    public CollisionBox attackbox;
    public String state = "Idle";
    public int frame = 0;

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
        this.attackbox = new CollisionBox(new Vector(pos.x + direction * 40/Game.SCALE, pos.y), new Vector(Game.GRIDSIZE * 3/Game.SCALE, Game.GRIDSIZE*2/Game.SCALE), 0, "enemy_atk");
        this.attackbox.enable = false;
        Game.collisionManager.collidable.add(attackbox);
    }

    public void destroy(){
        Game.removeables.add(box);
        Game.removeables.add(attackbox);
    }

    public int find_ind(){
        for (int i = 0; i<Game.collisionManager.collidable.size(); i++){
            if (Game.collisionManager.collidable.get(i).equals(this.box)){
                return i;
            }
        }
        return -1;
    }

    public int find_ind_atk(){
        for (int i = 0; i<Game.collisionManager.collidable.size(); i++){
            if (Game.collisionManager.collidable.get(i).equals(this.attackbox)){
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

    public void attack(double delta){
        pos.x += direction * 200/Game.SCALE * delta;
        update_pos();
        attackbox.pos = new Vector(pos.x + direction * 80/Game.SCALE, pos.y);
        attackbox.frame_pos = attackbox.pos.to_frame(Game.camera);
        
        boolean check_player = true;
        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("player_atk")){
                if (Game.collisionManager.collide(attackbox, c) && c.enable && attackbox.enable){
                    Game.sound.get("clash").play();
                    Game.player.velocity_x = direction * 800/Game.SCALE;
                    if (pos.x - size.x/2 <= border_left){
                        pos.x = border_left;
                    }
                    if (pos.x + size.x/2 >= border_right){
                        pos.x = border_right;
                    }
                    attack_duration = 0;
                    Game.player.attack_duration = 0;
                    c.enable = false;
                    check_player = false;
                    change_state("Idle");
                }
            }

        }

        if (check_player){
            for (CollisionBox c : Game.collisionManager.collidable){
                if (c.type.equals("player")){
                    if (Game.collisionManager.collide(attackbox, c) && attackbox.enable){
                        Game.player.hp -= 1;
                        Game.sound.get("impact").play();
                        attack_duration = 0;
                        Game.player.attack_duration = 0;
                    }
                }
            }
        }

        attack_duration -= delta;

        if (attack_duration <= 0){
            attacking = false;
            Game.collisionManager.collidable.get(find_ind_atk()).enable = false;
            attackbox.enable = false;
            attack_duration = 0;
            attack_cd = 0.7;
        }
        
        Game.rend.g.drawRect((int)(attackbox.frame_pos.x - attackbox.size.x/2), (int)(attackbox.frame_pos.y - attackbox.size.y/2), (int)(attackbox.size.x), (int)(attackbox.size.y));
    }

    final double frame_pause = 0.1;
    public int facing = -1;
    double frame_pause_timer = 0;    
    boolean died = false;
    final double cropse_duration = 10;
    double cropse_timer = cropse_duration;
    public void change_state(String s){
        int max_frame = Game.enemy_1_frame.get(s);
        if (!state.equals(s)){
            frame = 0;
        }
        state = s;
        if (!died){
            if (Game.player.pos.x < pos.x){
                facing = -1;
            }
            else{
                facing = 1;
            }
        }


        if (state.equals("Death") && !died){
            Game.collisionManager.collidable.get(find_ind()).enable = false;
            Game.collisionManager.collidable.get(find_ind_atk()).enable = false;
            box.enable = false;
            attackbox.enable = false;
            died = true;
        }

        if (frame_pause_timer == 0){
            if (state.equals("Attack")){
                if (attack_duration >= 0.2){
                    frame = 2;
                    frame_pause_timer = frame_pause;
                }
                else{
                    frame++;
                    frame %= max_frame;
                    frame_pause_timer = frame_pause;
                }
            }
            if (state.equals("Death")){
                frame++;
                if (frame >= max_frame){
                    frame = max_frame - 1;
                }
                frame_pause_timer = frame_pause;
            }
            else{
                frame++;
                frame %= max_frame;
                frame_pause_timer = frame_pause;
            }
        }
    }

    double velocity = 300/Game.SCALE;
    int direction = 0;
    boolean attacking = false;
    double attack_duration = 0;
    double attack_cd = 0;
    double wait_time = 0.2;
    double wait = wait_time;

    public void update_1(double delta){
        if (state.equals("Death") || died){
            change_state("Death");
            frame_pause_timer -= delta;
            if (frame_pause_timer < 0){
                frame_pause_timer = 0;
            }

            if (frame == Game.enemy_1_frame.get("Death") - 1){
                cropse_timer -= delta;
                if (cropse_timer < 0){
                    cropse_timer = 0;
                }
                if (cropse_timer == 0){
                    destroy();
                }
                Game.rend.g.drawString(Double.toString(cropse_timer), 1300, 200);
            }
            return;
        }
        if (Game.player.pos.x <= pos.x){
            direction = -1;
        }
        else{
            direction = 1;
        }
        boolean move = true;
        boolean idle = true;
        if (Math.abs(Game.player.pos.y - pos.y) < 70){
            if (Math.abs(Game.player.pos.x - pos.x) < 200/Game.SCALE){
                if (wait == 0){
                    if (!attacking && attack_cd == 0){
                        Game.collisionManager.collidable.get(find_ind_atk()).enable = true;
                        attackbox.enable = true;
                        attack_duration = 0.4;
                        attacking = true;
                        change_state("Attack");
                        attack(delta);
                    }
                    if (attacking){
                        change_state("Attack");
                        attack(delta);
                    }
                }

                wait -= delta;
                if (wait < 0){
                    wait = 0;
                }
                move = false;
                idle = false;
            }

            if (Math.abs(Game.player.pos.x - pos.x) < 600 && move){
                if (Game.player.pos.x < pos.x){
                    if (pos.x - size.x/2 > border_left){
                        pos.x -= velocity * delta;
                        wait = wait_time;
                        change_state("Run");
                        idle = false;
                    }
                    else{
                        change_state("Idle");
                    }
                }
                else if (Game.player.pos.x > pos.x && move){
                    if (pos.x + size.x/2 < border_right){
                        pos.x += velocity * delta;
                        wait = wait_time;
                        change_state("Run");
                        idle = false;
                    }
                    else {
                        change_state("Idle");
                    }
                }
            }
        }

        attack_cd -= delta;
        if (attack_cd < 0){
            attack_cd = 0;
        }

        frame_pause_timer -= delta;
        if (frame_pause_timer < 0){
            frame_pause_timer = 0;
        }
        if (idle){
            change_state("Idle");
        }
    }
 

    double bullet_cool = 0;
    final double bullet_cooldown = 1;
    double pre_cast = 0.5;
    final double pre_cast_delay = 0.5;

    public int player_dir = -1;
    
    public void update_2(double delta){
       if (bullet_cool == 0 && pre_cast == 0){
            Random rnd = new Random();
            int bullet_dir = find_angle();
            int drift = rnd.nextInt(5) - 2;
            Game.bullets.add(new Bullet(new Vector(pos.x, pos.y), bullet_dir + drift, "enemy"));
            bullet_cool = bullet_cooldown;
       }
       player_dir = find_angle();
    }

    public void update(double delta){
        if (type.equals("enemy_1")){

            update_1(delta);
            pre_cast -= delta;
            if (pre_cast < 0){
                pre_cast = 0;
            }
        }
        else if (type.equals("enemy_2")){
            if (see_player()){
                update_2(delta);
            }
            pre_cast -= delta;
            if (pre_cast < 0){
                pre_cast = 0;
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
