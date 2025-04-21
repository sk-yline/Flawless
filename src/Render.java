import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import javax.imageio.ImageIO;

public class Render{
    public Graphics g;
    private final String basepath = System.getProperty("user.dir");
    public int width;
    public int height;
    public double parrallax = 0.1; //背景移动速度与camera的比例

    public Render (Graphics g, int width, int height){
        this.g = g;
        this.width = width;
        this.height = height;
    }

    public BufferedImage loadimg(String path){
        BufferedImage img = null;
        
        try {
            img = ImageIO.read(new File(basepath + path));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return img;
    }

    public Image scale(BufferedImage img, double s){
        int img_width = (int)Math.round(img.getWidth() * s / Game.SCALE);
        int img_height = (int)Math.round(img.getHeight() * s / Game.SCALE);
        return img.getScaledInstance(img_width, img_height, Image.SCALE_DEFAULT);
    }

    public void DrawBackground(Image img){
        int img_width = img.getWidth(null);
        int img_height = img.getHeight(null);
        
        int l = (int) (Game.camera.l * parrallax) % img_width;
        int r = l + width;
        int u = 0;
        int b = u + height;
        //l, r, u, b代表camera的四个边所对应到背景上的位置
        //dx前缀表示绘制到屏幕上的位置, sx前缀表示从图片上截图的区域
        if (r > img_width){
            int mid_point = img_width - l;
            g.drawImage(img, 0, 0, mid_point, height, l, u, img_width, b, null);
            g.drawImage(img, mid_point, 0, width, height, 0, u, width-mid_point, b, null);
        }
        else{
            g.drawImage(img, 0, 0, width, height, l, u, r, b, null);
        }        
    }

    public void DrawFixedBG(Image img){

        g.drawImage(img, 0, 0, width, height, null);
    }

    public void render_mouse(){
        Vector pos = Game.mouse.pos;
        Vector size = Game.mouse.size;
        g.drawOval((int)(pos.x - size.x/2), (int)(pos.y - size.x/2), (int)size.x, (int)size.x);
        g.fillOval((int)(pos.x - size.y/2), (int)(pos.y - size.y/2), (int)size.y, (int)size.y);
        if (Game.DEBUG) g.drawString("x: " + (int)pos.x + " y:" + (int)pos.y, 50, 50);
        if (Game.DEBUG) g.drawString("relative rotation: " + Game.player.mouse_dir, 50, 100);
    }

    public void line_to_frame(Line2D l){
        Vector p1 = new Vector(l.getX1(), l.getY1()).to_frame(Game.camera);
        Vector p2 = new Vector(l.getX2(), l.getY2()).to_frame(Game.camera);
        g.drawLine((int)p1.x, (int)p1.y, (int)p2.x, (int)p2.y);
    }

    public void rect_to_frame(Rectangle2D r){
        Vector p1 = new Vector(r.getX(), r.getY()).to_frame(Game.camera);
        g.drawRect((int)p1.x, (int)p1.y, (int)r.getWidth(), (int)r.getHeight());
    }

    public void render_collision_box(){
        for (CollisionBox c : Game.collisionManager.collidable){
            g.setColor(Color.GREEN);
            if (c.enable){
                if (c.type.equals("player_atk") || c.type.equals("bullet")){
                    ArrayList<Vector> box_vert = c.get_verticies();
                    for (int i  = 0; i < box_vert.size(); i++){
                        Vector v = box_vert.get(i).to_frame(Game.camera);
                        box_vert.get(i).x = v.x;
                        box_vert.get(i).y = v.y;
                    }

                    int[] x_vert = new int[4], y_vert = new int[4];
                    for (int i = 0; i<4; i++){
                        x_vert[i] = Math.round((float)box_vert.get(i).x);
                        y_vert[i] = Math.round((float) box_vert.get(i).y);
                    }

                    g.drawPolygon(x_vert, y_vert, 4);
                }

                else{
                    // if(Game.collisionManager.collide(Game.collisionManager.collidable.get(0), Game.collisionManager.collidable.get(1))){
                    //     g.setColor(Color.RED);
                    // }
                    g.drawRect((int)(c.frame_pos.x - c.size.x/2), (int)(c.frame_pos.y - c.size.y/2), (int)(c.size.x), (int)(c.size.y));

                }
            }
            g.setColor(Color.WHITE);
        }
    }

    public void update_frame_pos(){
        for (CollisionBox c : Game.collisionManager.collidable){
            c.frame_pos = c.pos.to_frame(Game.camera);
        }
    }

    public void render_tilemap(){
        ArrayList<Image> tileset = Game.level.tileset;
        ArrayList<int[][]> tilemap = Game.level.tilemap;
        int dimension = Game.level.tile_dimension;
        double real_dimension = Game.level.true_dimension;

        for (int[][] layer : tilemap){
            for (int i = 0; i < layer.length; i++){
                for (int j = 0; j < layer[i].length; j++){
                    int tile_id = layer[i][j];
                    tile_id--;
                    if (tile_id != -1){
                        Vector pos = new Vector(j*real_dimension, i*real_dimension).to_frame(Game.camera);
                        int x_dimension = dimension;
                        int y_dimension = dimension;
                        if ((int) Math.round(pos.x + 1) - (int) Math.round(pos.x) > 0){
                            g.drawImage(tileset.get(tile_id), (int) Math.round(pos.x + 1), (int) Math.round(pos.y), x_dimension, y_dimension, null);
                        }
                        if ((int) Math.round(pos.y + 1) - (int) Math.round(pos.y) > 0){
                            g.drawImage(tileset.get(tile_id), (int) Math.round(pos.x), (int) Math.round(pos.y + 1), x_dimension, y_dimension, null);
                        }
                        g.drawImage(tileset.get(tile_id), (int) Math.round(pos.x), (int) Math.round(pos.y), x_dimension, y_dimension, null);
                    }
                }
            }
        }
    }

    //A Single Frame picture for player is 96x80 pixels, with 1 grid = 8x8 pixels
    //The player frame took either 4x4 or 3x5 grids, depending on the state
    //If we scale the image to 2x, the player will be 48*80 pixels, which is 6x10 grids, and 48 = 1 grid for the game
    public void render_player(String state){
        Vector frame_pos = Game.player.pos.to_frame(Game.camera);
        Vector img_center;
        Image player_image;
        if (Game.player.facing == -1){
            img_center = Game.player_img_center_reverse.get(state);
            player_image = Game.player_img_reverse.get(state).get(Game.player.frame_ind);
        }
        else{
            img_center = Game.player_img_center.get(state);
            player_image = Game.player_img.get(state).get(Game.player.frame_ind);
        }
        int image_height = player_image.getHeight(null);
        int image_width = player_image.getWidth(null);
        g.drawImage(player_image, (int) (frame_pos.x -  img_center.x), (int) (frame_pos.y - img_center.y), (int)image_width, (int)image_height, null);
    }

    public BufferedImage rotate_image(BufferedImage img, int angle){
        double radians = Math.toRadians(angle);
        /*
        Unlike reflection, rotation will change the image dimensions
        For some weird cases that I can't really explain, the image will be cropped if the angle is > 180 degrees
        So we need to fit the rotated image into a new bounding box with rotated dimensions

        Rotation Matrix:
          [cos(theta) -sin(theta)]
          [sin(theta)  cos(theta)]
         */
        int newWidth = (int) Math.round(Math.abs(img.getWidth() * Math.cos(radians)) + Math.abs(img.getHeight() * Math.sin(radians)));
        int newHeight = (int) Math.round(Math.abs(img.getWidth() * Math.sin(radians)) + Math.abs(img.getHeight() * Math.cos(radians)));
        BufferedImage rotated_img = new BufferedImage(newWidth, newHeight, img.getType());

        
        AffineTransform at = new AffineTransform();
        /*Translate the orginal top left corner (0,0) to the top left corner after transformation
        (i.e move the origional center to the new center)
        Orgional Center: (img.getWidth()/2, img.getHeight()/2)
        New Center: (newWidth/2, newHeight/2)
        Distance between the two cetners: [newWidth/2 - img.getWidth()/2, newHeight/2 - img.getHeight()/2]
         = [(newWidth - img.getWidth()) / 2, (newHeight - img.getHeight()) / 2]
        */
        at.translate((newWidth - img.getWidth()) / 2, (newHeight - img.getHeight()) / 2);
        //Rotate the image around the center of the image
        at.rotate(radians, img.getWidth() / 2, img.getHeight() / 2);
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        op.filter(img, rotated_img);
        return rotated_img;
    }

    public void render_player_attck(boolean is_attack){
        double offset = 20/Game.SCALE;
        if (is_attack){
            Vector frame_pos = Game.player.pos.to_frame(Game.camera);
            BufferedImage attack = Game.player_attack.get(Game.player.attack_frame);
            BufferedImage rotated_attack = rotate_image(attack, 360 - Game.player.attack_dir);
            double height = rotated_attack.getHeight(null);
            double width = rotated_attack.getWidth(null);
            height = height * 2 / Game.SCALE;
            width = width * 2 / Game.SCALE;
            Image final_attack = scale(rotated_attack, 2);
            Vector image_center = new Vector(frame_pos.x + offset * Math.cos(Math.toRadians(Game.player.attack_dir)), frame_pos.y + -1 * offset * Math.sin(Math.toRadians(Game.player.attack_dir)));
            g.drawImage(final_attack, (int)Math.round(image_center.x - width/2), (int)Math.round(image_center.y - height/2), null);
            
        }
    }

    //Absolutly the same logic as render_player
    public void render_enemy_1(Enemy e){
        String state = e.state;
        Vector frame_pos = e.pos.to_frame(Game.camera);
        Vector img_center;
        Image enemy_image;
        if (e.facing == -1){
            img_center = Game.enemy_1_img_center_reverse.get(state);
            enemy_image = Game.enemy_1_img_reverse.get(state).get(e.frame);
        }
        else{
            img_center = Game.enemy_1_img_center.get(state);
            enemy_image = Game.enemy_1_img.get(state).get(e.frame);
        }

        int image_height = enemy_image.getHeight(null);
        int image_width = enemy_image.getWidth(null);
        g.drawImage(enemy_image, (int) (frame_pos.x -  img_center.x), (int) (frame_pos.y - img_center.y), (int)image_width, (int)image_height, null);
    }

    public BufferedImage horizantal_flip(BufferedImage image){
        AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
        tx.translate(-image.getWidth(null), 0);
        AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage new_img = op.filter(image, null);
        return new_img;
    }

    public void render_enemy_2(Enemy e){
        BufferedImage enemy = Game.enemy_2_img.get("Idle").get(0);
        if (Game.player.pos.x < e.pos.x){
            enemy = horizantal_flip(enemy);
        }
        BufferedImage gun = Game.enemy_2_img.get("Gun").get(0);
        int angle = e.player_dir;
        if (angle > 90 && angle < 270){
            int reference_angle = Math.abs(180 - angle);
            if (angle <= 180){
                angle = reference_angle;
            }
            else{
                angle = 360 - reference_angle;
            }
        }
        angle += 45;
        angle %= 360;
        if (angle > 180){
            angle -= 360;
        }
        gun = rotate_image(gun, 360 - angle);
        if (Game.player.pos.x < e.pos.x){
            gun = horizantal_flip(gun);
        }

        Image enemy_img = scale(enemy, 3);
        Vector frame_pos = e.pos.to_frame(Game.camera);
        Vector enemy_center = new Vector(2*8*3/Game.SCALE, 4*8*3/Game.SCALE);
        Vector gun_center = new Vector(2*8*3/Game.SCALE, 2*8*3/Game.SCALE);
        if (Game.player.pos.x < e.pos.x){
            enemy_center.x = 4*8*3/Game.SCALE;
            gun_center.x = 2*8*3/Game.SCALE;
        }
        Image gun_img = scale(gun, 2);
        g.drawImage(enemy_img, (int)(frame_pos.x - enemy_center.x), (int)(frame_pos.y - enemy_center.y), enemy_img.getWidth(null), enemy_img.getHeight(null), null);
        g.drawImage(gun_img, (int)(frame_pos.x - gun_center.x), (int)(frame_pos.y - gun_center.y), gun_img.getWidth(null), gun_img.getHeight(null), null);
    }

    public void render_bullet(Bullet b){
        int angle = b.dir;
        BufferedImage bullet = Game.bullet_img;
        bullet = rotate_image(bullet, 360 - angle);
        Image bullet_img = scale(bullet, 3);
        Vector frame_pos = b.pos.to_frame(Game.camera);
        g.drawImage(bullet_img, (int)Math.round(frame_pos.x - bullet_img.getWidth(null)/2), (int)Math.round(frame_pos.y - bullet_img.getHeight(null)/2), bullet_img.getWidth(null), bullet_img.getHeight(null), null);
    }

    public void render_background(){
        String type = Game.level_background.get(Game.current_level);
        if (type.equals("moveable")){
            DrawBackground(Game.moveable_background);
        }
        else if (type.equals("fixed")){
            DrawFixedBG(Game.static_background);
        }
    }

    public void render_pause_screen(){
        g.setColor(new Color(0, 0, 255, 125));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.ITALIC, 100));
        g.drawString("PAUSE", (int)(width/2 - (200/Game.SCALE)), (int)(height/2 - (200/Game.SCALE)));
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("Resume", (int)(width/2 - (125/Game.SCALE)), (int)(height/2 + 25/Game.SCALE));
        g.drawString("Exit the game", (int)(width/2 - (200/Game.SCALE)), (int)(height/2 + (175/Game.SCALE)));
        g.setFont(new Font("Arial", Font.PLAIN, 12));
    }

    public void update(){
        if (!Game.DEBUG){
            DrawBackground(Game.background.get(Game.current_level));
        }
        update_frame_pos();
        render_background();
        render_tilemap();
        if (Game.DEBUG){
            render_collision_box();
            g.drawString("Pos x: ", 300, 100);
            g.drawString(Double.toString(Game.player.pos.x), 300, 120);
            g.drawString("Pos y: ", 300, 140);
            g.drawString(Double.toString(Game.player.pos.y), 300, 160);
        }
        render_mouse();
        render_player(Game.player.state);
        render_player_attck(Game.player.attacking);
        for (Enemy e : Game.enemies){
            if (e.type.equals("enemy_2")){
                render_enemy_2(e);
            }
            else{
                render_enemy_1(e);
            }
        }

        for (Bullet b : Game.bullets){
            render_bullet(b);
        }
    }
}
