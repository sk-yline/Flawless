
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class Player{
    public Vector size;
    public Vector pos;
    public int mouse_dir = 0;
    public double max_speed_x = 450/Game.SCALE;
    public double max_speed_grav = 750/Game.SCALE;
    public double max_speed_slide = 375/Game.SCALE;
    public double max_speed_y = max_speed_grav;
    public double sustained_jump_acceleration = -375/Game.SCALE;
    public double initialJumpVelocity = -800 / Game.SCALE;
    public double accel_x = 1800/Game.SCALE;
    public double gravity = 1800/Game.SCALE;
    public double accel_y = gravity;
    public double velocity_x = 0;
    public double velocity_y = 0;
    public int direction = 1;
    public int atk_offset = (int) Math.round(Game.GRIDSIZE*2/Game.SCALE);
    public int hp = 1;
    public double facing = 1;
    public String state = "Idle";
    public int frame_ind = 0;

    

    public Player(Vector pos, Vector size){
        this.size = size;
        this.pos = pos;
        Game.collisionManager.collidable.add(new CollisionBox(pos, size, 0, "player"));
        Game.collisionManager.collidable.add(new CollisionBox(new Vector(pos.x + atk_offset, pos.y), new Vector(Game.GRIDSIZE*3/Game.SCALE, Game.GRIDSIZE*1.5/Game.SCALE), 0, "player_atk"));
        Game.collisionManager.collidable.get(Game.collisionManager.get_box("player_atk")).enable = false;
    }


    public void update_collision(){
        int player_ind = Game.collisionManager.get_box("player");
        Game.collisionManager.collidable.get(player_ind).pos = pos;
        Game.collisionManager.collidable.get(player_ind).frame_pos = pos.to_frame(Game.camera);
    }

    public void update_direction(Camera c, Mouse m){
        Vector frame_pos = pos.to_frame(c);
        //Calculate Direction using the formula of dot product cos(theta) = a.b / |a|*|b|;
        Vector edge = m.pos.minus(frame_pos);
        if (edge.y <= 0){
            mouse_dir = Math.round((float)(Math.acos(edge.dot(new Vector(1, 0)) / edge.magnitude()) * 180 / Math.PI));
        }
        else{
            mouse_dir = Math.round((float)(Math.acos(edge.dot(new Vector(-1, 0)) / edge.magnitude()) * 180 / Math.PI)) + 180;
        }
    }

    private double max_jump_duration = 0.15;
    private double jump_duration = 0;

    public boolean is_grounded(){
        int player_ind = Game.collisionManager.get_box("player");
        Vector col_pos = Game.collisionManager.collidable.get(player_ind).pos;
        Vector col_size = Game.collisionManager.collidable.get(player_ind).size;
        Line2D line_left = new Line2D.Double(new Point2D.Double(col_pos.x - col_size.x/2, col_pos.y), new Point2D.Double(col_pos.x - col_size.x/2, col_pos.y + col_size.y/2 + Game.GRIDSIZE * 0.75));
        Line2D line_right = new Line2D.Double(new Point2D.Double(col_pos.x + col_size.x/2, col_pos.y), new Point2D.Double(col_pos.x + col_size.x/2, col_pos.y + col_size.y/2 + Game.GRIDSIZE * 0.75));
        // if (Game.DEBUG){
        //     Game.rend.line_to_frame(line_left);
        //     Game.rend.line_to_frame(line_right);
        // }
        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("wall")){
                Rectangle2D rect = c.get_rect();
                if (Game.DEBUG){
                    Game.rend.rect_to_frame(rect);
                }
                if (line_left.intersects(rect) || line_right.intersects(rect)){
                    return true;
                }
            }
        }

        return false;
    }

    //The same as is_grounded, I'm just to lazy to add all the is_grounded functions with a parameter
    public boolean disable_double_jump(){
        int player_ind = Game.collisionManager.get_box("player");
        Vector col_pos = Game.collisionManager.collidable.get(player_ind).pos;
        Vector col_size = Game.collisionManager.collidable.get(player_ind).size;
        Line2D line_left = new Line2D.Double(new Point2D.Double(col_pos.x - col_size.x/2, col_pos.y), new Point2D.Double(col_pos.x - col_size.x/2, col_pos.y + col_size.y/2 + Game.GRIDSIZE * 0.25));
        Line2D line_right = new Line2D.Double(new Point2D.Double(col_pos.x + col_size.x/2, col_pos.y), new Point2D.Double(col_pos.x + col_size.x/2, col_pos.y + col_size.y/2 + Game.GRIDSIZE * 0.25));

        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("wall")){
                Rectangle2D rect = c.get_rect();
                if (Game.DEBUG){
                    Game.rend.rect_to_frame(rect);
                }
                if (line_left.intersects(rect) || line_right.intersects(rect)){
                    return true;
                }
            }
        }

        return false;
    }

    int wall_dir = 0;
    public boolean on_wall(){
        int player_ind = Game.collisionManager.get_box("player");
        Vector col_pos = Game.collisionManager.collidable.get(player_ind).pos;
        Vector col_size = Game.collisionManager.collidable.get(player_ind).size;
        Line2D line_left_up = new Line2D.Double(new Point2D.Double(col_pos.x - col_size.x/2 - Game.GRIDSIZE * 0.5, col_pos.y - col_size.y/2), new Point2D.Double(col_pos.x, col_pos.y - col_size.y/2));
        Line2D line_left_down = new Line2D.Double(new Point2D.Double(col_pos.x - col_size.x/2 - Game.GRIDSIZE * 0.5, col_pos.y + col_size.y/2), new Point2D.Double(col_pos.x, col_pos.y + col_size.y/2));
        Line2D line_right_up = new Line2D.Double(new Point2D.Double(col_pos.x + col_size.x/2 + Game.GRIDSIZE * 0.5, col_pos.y - col_size.y/2), new Point2D.Double(col_pos.x, col_pos.y - col_size.y/2));
        Line2D line_right_down = new Line2D.Double(new Point2D.Double(col_pos.x + col_size.x/2 + Game.GRIDSIZE * 0.5, col_pos.y + col_size.y/2), new Point2D.Double(col_pos.x, col_pos.y + col_size.y/2));

        // if (Game.DEBUG){
        //     Game.rend.line_to_frame(line_left_up);
        //     Game.rend.line_to_frame(line_left_down);
        //     Game.rend.line_to_frame(line_right_up);
        //     Game.rend.line_to_frame(line_right_down);
        // }

        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("wall")){
                Rectangle2D rect = c.get_rect();
                if (line_left_up.intersects(rect) || line_left_down.intersects(rect)){
                    wall_dir = 1;
                    return true;
                }
                else if (line_right_up.intersects(rect) || line_right_down.intersects(rect)){
                    wall_dir = -1;
                    return true;
                }
            }
        }

        return false;
    }

    public boolean can_double_jump = false;
    public final double double_jump_max = 0.3;
    public double double_jump_duration = 0;
    public double double_jump_acc = -1000/Game.SCALE;
    public double initial_double_jump_velocity = -600/Game.SCALE;
    public void double_jump(double delta){
        if (velocity_y != 0){
            velocity_y = initial_double_jump_velocity;
        }
        velocity_y += double_jump_acc * delta;
    }

    public double wall_jump_y_acc = -20000 /Game.SCALE;
    public double wall_jump_x_acc = 20000/Game.SCALE;
    public double wall_jump_duration = 0;
    private boolean wall_j = false;

    public void wall_jump(double delta){
        if (!wall_j){
            wall_j = true;
            velocity_y = 0;
            velocity_x = 0;
        }
        velocity_y += wall_jump_y_acc  * delta;
        velocity_x += wall_jump_x_acc * wall_dir * delta;
        facing = wall_dir * -1;
        if (wall_dir < 0  && velocity_x > 0){
            velocity_x *= -1;
        }
    }

    private boolean holding_jump = false;
    public double coyote_time = 0.2;

    public void jump(double delta)
    {
        if (jump_duration <= 0){
            holding_jump = false;
        }
        if (Game.keydown[KeyEvent.VK_SPACE]){
            if (is_grounded() || coyote_time > 0){
                Game.sound.get("jump").play();
                holding_jump = true;
                jump_duration = max_jump_duration;
                velocity_y = initialJumpVelocity;
                coyote_time = 0;
                change_state("Jump");
            }
        }
        else if (jump_duration > 0 && holding_jump){
            coyote_time = 0;
            velocity_y += sustained_jump_acceleration * delta;
            jump_duration -= delta;
            change_state("Jump");
        }

        if (can_double_jump){
            double_jump(delta);
            Game.sound.get("jump").play();
            change_state("DoubleJump");
            double_jump_duration = double_jump_max;
            can_double_jump = false;
        }
        else if (double_jump_duration > 0){
            double_jump(delta);
            change_state("DoubleJump");
        }
        // System.out.println(Game.keydown[KeyEvent.VK_SPACE]);
    }

    public void move(double delta){
        update_collision();

        Game.rend.g.drawString(Double.toString(velocity_y), 500, 100);
        Game.rend.g.drawString(Double.toString(velocity_x), 500, 120);
        

        double dx = velocity_x * delta;
        double dy = velocity_y * delta;
        CollisionBox previous_box = Game.collisionManager.collidable.get(Game.collisionManager.get_box("player"));
        //Have a bit of x offset to avoid moving into the wall and stuck at inside
        CollisionBox new_x_collision = new CollisionBox(new Vector(pos.x + dx + direction * 1, pos.y), previous_box.size, 0, "player");
        boolean move_x = true;
        if (Game.collisionManager.is_collide(new_x_collision, "wall")){
            move_x = false;
        }
        else if (new_x_collision.pos.x - new_x_collision.size.x/2 <= 0.1){
            move_x = false;
        }

        if (move_x){
            pos.x += dx;
            pos.x = (int) pos.x;
        }


        CollisionBox new_y_collision = new CollisionBox(new Vector(pos.x, pos.y + dy), previous_box.size, 0, "player");
        boolean move_y = true;
        if (Game.collisionManager.is_collide(new_y_collision, "wall")){
            move_y = false;
        }
        else if (new_y_collision.pos.y + new_y_collision.size.y/2 >= Game.levels.get(Game.current_level).height * Game.GRIDSIZE - 0.1){
            pos.y = 400;
        }

        if (move_y){
            pos.y += dy;
            pos.y = (int) pos.y;
        }
        else{
            if (is_grounded() && !holding_jump){
                for (CollisionBox c : Game.collisionManager.collidable){
                    if (c.type.equals("wall")){
                        CollisionBox pl = Game.collisionManager.collidable.get(Game.collisionManager.get_box("player"));
                        double player_bottom = pl.pos.y + pl.size.y/2 + dy;
                        double wall_top = c.pos.y - c.size.y/2;
                        if (wall_top - player_bottom < 1.2){
                            velocity_y *= 0;
                        }
                    }
                }
            }
        }
    }
    

    final double max_dash = 0.30;
    // final double max_dash_cd = 0.8;
    double dash_duration = 0;
    double dash_vel = 1000/Game.SCALE;
    // double dash_cd = 0;
    boolean can_dash = true;
    boolean dashing = false;

    public void dash(double delta){
        Game.rend.g.drawString(Double.toString(dash_duration), 500, 140);
        velocity_y = 0;
        velocity_x = dash_vel * direction;
        facing = direction;
        dash_duration -= delta;
    }

    final double max_attack = 0.2;
    boolean attacking = false;
    double attack_duration = 0;
    double attack_cd = 0;
    final double max_attack_cd = 0.4;
    public int attack_dir = -1;
    double attack_vel = 1400/Game.SCALE;

    public void attack(double delta){
        if (attack_dir == -1){
            attack_dir = mouse_dir;
            Game.rend.g.drawString(Integer.toString(attack_dir), 500, 160);
            velocity_y = attack_vel * Math.sin(Math.toRadians(attack_dir)) * -1;
            velocity_x = attack_vel * Math.cos(Math.toRadians(attack_dir));
            if (attack_dir <= 90 || attack_dir >= 270){
                facing = 1;
            }
            else{
                facing = -1;
            }
        }

        if (attack_duration <= 0){
            attacking = false;
            attack_cd = max_attack_cd;
            Game.collisionManager.collidable.get(Game.collisionManager.get_box("player_atk")).enable = false;
            attack_dir = -1;
            attack_frame = 0;
            return;
        }

        Game.collisionManager.update_player_attack(attack_dir);
        attack_duration -= delta;

        for (Enemy e : Game.enemies){
            CollisionBox attack_box = Game.collisionManager.collidable.get(Game.collisionManager.get_box("player_atk"));
            if (Game.collisionManager.collide(attack_box, e.box) && e.box.enable){
                if (e.type.equals("enemy_1")) {
                    e.change_state("Death");
                }
                else{
                    e.destroy();
                }

                can_double_jump = true;
                Game.hit_pause = Game.hit_pause_time;
                Game.sound.get("impact").play();
            }
        }
    }

    final double frame_pause = 0.1;
    double frame_pause_timer = 0;
    public int attack_frame = 0;
    public void change_state(String s){
        int max_frame = Game.player_frame.get(s);
        if (!state.equals(s)){
            frame_ind = -1;
            frame_pause_timer = 0;
        }
        state = s;
        if (frame_pause_timer == 0){
            if (state.equals("Dash")){
                if (frame_ind == -1){
                    frame_ind = 0;
                }
                if (dash_duration <= 0.12){
                    double frame_dash_pause = 0.03;
                    frame_ind++;
                    frame_ind %= max_frame;
                    frame_pause_timer = frame_dash_pause;
                }
            }
            else if (state.equals("Jump")){
                if (velocity_y < -10){
                    frame_ind = 0;
                }
                else if (velocity_y > 10){
                    frame_ind = 2;
                }
                else{
                    frame_ind = 1;
                }
                frame_pause_timer = frame_pause;
            }
            else if (state.equals("DoubleJump")){
                double dj_frame_pause = double_jump_max/max_frame;
                frame_ind++;
                frame_ind %= max_frame;
                frame_pause_timer = dj_frame_pause;
            }
            else if (state.equals("Attack")){
                state = "Jump";
                if (attack_duration <= 0.075){
                    double frame_attack_pause = 0.02;
                    attack_frame++;
                    if (attack_frame > 4){
                        attack_frame = 4;
                    }
                    frame_pause_timer = frame_attack_pause;
                }
                else if (attack_duration <= 0.2){
                    double frame_attack_pause = 0.05;
                    frame_ind++;
                    frame_ind %= max_frame;
                    frame_pause_timer = frame_attack_pause;
                }
                else if (attack_duration <= 0.3){
                    double frame_attack_pause = 0.1;
                    frame_ind++;
                    frame_ind %= max_frame;
                    frame_pause_timer = frame_attack_pause;
                }
                if (attack_dir <= 180){
                    frame_ind = 0;
                }
                else{
                    frame_ind = 2;
                }
            }
            else{
                frame_ind++;
                frame_ind %= max_frame;
                frame_pause_timer = frame_pause;
            }
        }
    }

    double walk_sound_interval = 0;

    public void update_postion(double delta){
        //Priority: Dash > Jump > Attack > Move > Normal

        //Attack
        if (Game.mousedown){
            if (!attacking && attack_cd == 0){
                Game.collisionManager.collidable.get(Game.collisionManager.get_box("player_atk")).enable = true;
                attack_duration = max_attack;
                attacking = true;
                attack(delta);
                dashing = false;
                dash_duration = 0;
                change_state("Attack");
                Game.sound.get("sword").play();
            }
        }

        if (attacking){
            attack(delta);
            change_state("Attack");
            velocity_x *= 0.9;
            velocity_y *= 0.9;
            dashing = false;
            dash_duration = 0;
        }
        
        attack_cd -= delta;
        if (attack_cd < 0){
            attack_cd = 0;
        }

        //Dash
        if (Game.keydown[KeyEvent.VK_SHIFT]){
            if (can_dash){
                dashing = true;
                dash_duration = max_dash;
                Game.sound.get("swosh").play();
                dash(delta);
                change_state("Dash");
            }
        }

        if (dash_duration > 0){
            dash(delta);
            change_state("Dash");
        }
        else if (dashing){
            dashing = false;
            can_dash = false;
        }

        if (!dashing && ! attacking){
            //Jump
            if (Game.keyhold[KeyEvent.VK_SPACE] && !wall_j)
            {
                jump(delta);
            }
            else{
                if (velocity_y < 0){     
                    velocity_y *= 0.5;
                }
                holding_jump = false;
                jump_duration = 0;
            }

            if (wall_jump_duration > 0 && wall_j){
                wall_jump(delta);
                wall_jump_duration -= delta;
            }

            else if (Game.keydown[KeyEvent.VK_SPACE] && on_wall()){
                wall_jump_duration = 0.1;
                Game.sound.get("jump").play();
                wall_jump(delta);
            }

            else{
                wall_j = false;
            }

            
            //Move
            if (Game.keyhold[KeyEvent.VK_A] && !wall_j){
                if (velocity_x > 0){
                    velocity_x = 0;
                }
                velocity_x -= accel_x * delta;
                direction = -1;
                if (on_wall()){
                    facing = wall_dir;
                    change_state("Wall");
                }
                else if (double_jump_duration > 0){
                    facing = direction;
                    change_state("DoubleJump");
                }
                else{
                    facing = direction;
                    change_state("Run");
                }
            }
            if (Game.keyhold[KeyEvent.VK_D] && !wall_j){
                if (velocity_x < 0){
                    velocity_x = 0;
                }
                velocity_x += accel_x * delta;
                direction = 1;
                if (on_wall()){
                    facing = wall_dir;
                    change_state("Wall");
                }
                else if (double_jump_duration > 0){
                    facing = direction;
                    change_state("DoubleJump");
                }
                else{
                    facing = direction;
                    change_state("Run");
                }
            }
            if (!Game.keyhold[KeyEvent.VK_A] && !Game.keyhold[KeyEvent.VK_D] && !wall_j){
                velocity_x = 0;
                if (!on_wall()){
                    change_state("Idle");
                }
            }

            if (!on_wall() && !is_grounded() && double_jump_duration <= 0){
                change_state("Jump");
            }

            if (on_wall()){
                change_state("Wall");
                facing = wall_dir;
            }

            //Normal gravity
            if (on_wall() && !wall_j){
                max_speed_y = max_speed_slide;
            }
            else{
                max_speed_y = max_speed_grav;
            }
            velocity_y += accel_y * delta;
            
            if (velocity_y > max_speed_y && !wall_j){
                velocity_y = max_speed_y;
            }
            if (Math.abs(velocity_x) > max_speed_x && !wall_j){
                velocity_x = max_speed_x * Math.signum(velocity_x);
            }

            if (is_grounded() && velocity_x != 0){
                if (walk_sound_interval == 0){
                    // Game.sound.get("walk").play();
                    walk_sound_interval = 0.15;
                }
            }
        }
        else {
            coyote_time = 0;
        }

        move(delta);
        coyote_time -= delta;
        if (coyote_time < 0){
            coyote_time = 0;
        }

        if (is_grounded()){
            can_dash = true;
            coyote_time = 0.2;
        }

        if (disable_double_jump()){
            can_double_jump = false;
        }

        frame_pause_timer -= delta;
        if (frame_pause_timer < 0){
            frame_pause_timer = 0;
        }
        
        walk_sound_interval -= delta;
        if (walk_sound_interval < 0){
            walk_sound_interval = 0;
        }

        double_jump_duration -= delta;
        if (double_jump_duration < 0){
            double_jump_duration = 0;
        }

        Game.rend.g.drawString(Boolean.toString(can_double_jump), 500, 180);
        Game.rend.g.drawString(Double.toString(double_jump_duration), 500, 200);

    }
    
    public void update(double delta){
        update_direction(Game.camera, Game.mouse);
        update_postion(delta);
        update_collision();
    }
}
