import java.applet.AudioClip;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;


import tapplet.TApplet;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.Renderer;
import javax.swing.RepaintManager;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class Game extends TApplet implements MouseMotionListener, MouseListener
{
    
    @Override
    public void mouseDragged(MouseEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
        
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mousedown = true;
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousedown = false;
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void main(String[] args) throws Exception {
        Game curGame = new Game();
        // while(true){
        //     while(curGame.isVisible()){
        //         try{
        //             Thread.sleep(500);
        //         }
        //         catch (InterruptedException e){
        //             e.printStackTrace();
        //         }
        //     }
        // }
    }

final public static double SCALE = 1.25; //Corresponds to 125% in display setting
final public static boolean DEBUG = true;
final public static int WIDTH = Math.round((float)(1920 / SCALE));
final public static int HEIGHT = Math.round((float)(1200 / SCALE));
final public static int GRIDSIZE = Math.round((float)(16*3 / SCALE));

public static Render rend;
public static ArrayList<Image> background; 
public static ArrayList<Level> levels = new ArrayList<>();
public static int current_level = 0;
public static CollisionManager collisionManager = new CollisionManager();
public static Camera camera;
public static Mouse mouse;
public static Player player;

public static boolean keydown[] = new boolean[256];
public static boolean keyhold[] = new boolean[256];
public static boolean mousedown = false;

long previous_time = System.currentTimeMillis();
long current_time = System.currentTimeMillis();

public static double hit_pause = 0;
final public static double hit_pause_time = 0.1;

public static HashMap<String, AudioClip> sound = new HashMap();

public static ArrayList<Bullet> bullets = new ArrayList<>();
public static ArrayList<Enemy> enemies = new ArrayList<>();
public static ArrayList<CollisionBox> removeables = new ArrayList<>();

public static HashMap<String, ArrayList<Image>> player_img = new HashMap();
public static HashMap<String, ArrayList<Image>> player_img_reverse = new HashMap();
public static HashMap<String, Vector> player_img_center = new HashMap();
public static HashMap<String, Vector> player_img_center_reverse = new HashMap();
public static HashMap<String, Integer> player_frame = new HashMap();
public static ArrayList<BufferedImage> player_attack = new ArrayList<>();

public static HashMap<String, ArrayList<Image>> enemy_1_img = new HashMap();
public static HashMap<String, ArrayList<Image>> enemy_1_img_reverse = new HashMap();
public static HashMap<String, Vector> enemy_1_img_center = new HashMap();
public static HashMap<String, Vector> enemy_1_img_center_reverse = new HashMap();
public static HashMap<String, Integer> enemy_1_frame = new HashMap();
public static HashMap<String, ArrayList<BufferedImage>> enemy_2_img = new HashMap();

public static BufferedImage bullet_img;
public static HashMap<String, ArrayList<Image>> effects_img = new HashMap();

public void intialize_sound(){
    sound.put("jump", getAudioClip(getCodeBase(), "\\audio\\jump.wav"));
    sound.put("clash", getAudioClip(getCodeBase(), "\\audio\\clash.wav"));
    sound.put("swosh", getAudioClip(getCodeBase(), "\\audio\\swosh.wav"));
    sound.put("impact", getAudioClip(getCodeBase(), "\\audio\\impact.wav"));
    sound.put("sword", getAudioClip(getCodeBase(), "\\audio\\sword.wav"));
    sound.put("walk", getAudioClip(getCodeBase(), "\\audio\\walk.wav"));
}

public BufferedImage loadimg(String path){
    BufferedImage img = null;
    String basepath = System.getProperty("user.dir");
    try {
        img = ImageIO.read(new File(basepath + path));
    } catch (Exception e) {
        e.printStackTrace();
    }

    return img;
}

public Image scale(BufferedImage img, double s){
    int img_width = (int)Math.round(img.getWidth() * s / SCALE);
    int img_height = (int)Math.round(img.getHeight() * s / SCALE);
    return img.getScaledInstance(img_width, img_height, Image.SCALE_DEFAULT);
}

/*
Affine Tranformation is a specific type of linear transformation that allows orgion to move.
It is stored in the homogeneous coordinates [x, y, 1] and the transformation matrix is a 3x3 matrix:
[x'] =  [A00, A01, b0][x]
[y'] =  [A10, A11, b1][y]
[1]  =  [0,    0,   1][1]

It can be also written in the expansion form as:
[x'] = [A00*x + A01*y + b0]
[y'] = [A10*x + A11*y + b1]

or in foundamental theorm of vector composition:
[x', y'] = x*i' + y*j' + b
Where i' and j' are the unit vectors after sheering/scaling/rotation and b is the translation vector (moves origion to (bx, by)).

In the class AffineTransform, .getScaleInstance(sx, sy) changes a unit matrix to:
[sx, 0, 0]
[0, sy, 0]
[0, 0, 1]
if sx is negative, it means flipping the coordiante axis horizontaly.

.translate(tx, ty) changes a unit matrix to:
[1, 0, tx]
[0, 1, ty]
[0, 0, 1]
Where the orgion is moved to (tx, ty).

All of such transformations can be applied to BufferedImage to perform translation/rotation/sheering/etc.

The AffineTransformOp class is used to apply the transformation matrix to the image.
*/

/*
 I can totally use for loop to for loop over every pixel in the image and reverse the pixel position using
 BufferedImage.getRGB(x, y) and BufferedImage.setRGB(x, y, rgb), but it is much slower than using AffineTransform,
 and I don't want the game to intialize for that long. 
 (I said all those stuff just to prove that I know what I'm doing, and it isn't gpt generated)
 */
public Image horizantal_flip(BufferedImage image, int scale){
    AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
    tx.translate(-image.getWidth(null), 0);
    AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
    BufferedImage new_img = op.filter(image, null);
    return scale(new_img, scale);
}

public void initialize_pictures(){
    String[] player_states = {"Dash", "Jump", "Death", "Idle", "Run", "Wall", "Land", "Hit","DoubleJump"};
    HashMap<String, Vector> player_center = new HashMap<String, Vector>() {{
        //Player image center relative to the grid
        put("Dash", new Vector(6, 6));
        put("Jump", new Vector(6, 6));
        put("Death", new Vector(6, 6));
        put("Idle", new Vector(6, 6));
        put("Run", new Vector(6, 6));
        put("Land", new Vector(6, 6.5));
        put("DoubleJump", new Vector(6, 6));
        put("Hit", new Vector(6, 6));
        put("Wall", new Vector(6, 6.5));
    }};

    for (String s : player_states){
        player_img.put(s, new ArrayList<>());
        player_img_reverse.put(s, new ArrayList<>());
    }

    for (String s : player_states){
        BufferedImage img = loadimg("\\resource\\player\\" + s + ".png");
        int frame_cnt = img.getWidth(null) / 96;
        player_frame.put(s, frame_cnt);
        for (int i = 0; i<frame_cnt; i++){
            BufferedImage croped_frame = img.getSubimage(i*96, 0, 96, 80);
            Image img_scaled = scale(croped_frame, 3);
            player_img.get(s).add(img_scaled);
            player_img_reverse.get(s).add(horizantal_flip(croped_frame, 3));
            //For a sprite with 12 grid x 10 grid, the image's center of mass is at (6, 6.5);
            Vector center = player_center.get(s);
            player_img_center.put(s, new Vector(center.x*8*3/SCALE, center.y*8*3/SCALE));
            player_img_center_reverse.put(s, new Vector((12 - center.x)*8*3/SCALE, center.y*8*3/SCALE));
        }
    }

    player_frame.put("Attack", 5);
    for (int i = 0; i<5; i++){
        BufferedImage img = loadimg("\\resource\\player\\Slash" + i + ".png");
        player_attack.add(img);
    }

    String[] enemy_1_states = {"Idle", "Run", "Death", "Attack"};
    HashMap<String, Vector> enemy_1_center = new HashMap<String, Vector>() {{
        put("Idle", new Vector(6, 4));
        put("Run", new Vector(7, 4));
        put("Death", new Vector(6, 4));
        put("Attack", new Vector(6, 4));
    }};

    for (String s : enemy_1_states){
        enemy_1_img.put(s, new ArrayList<>());
        enemy_1_img_reverse.put(s, new ArrayList<>());
    }

    for (String s : enemy_1_states){
        BufferedImage img = loadimg("\\resource\\enemy_1\\" + s + ".png");
        int frame_cnt = img.getWidth(null) / 96;
        enemy_1_frame.put(s, frame_cnt);
        for (int i = 0; i<frame_cnt; i++){
            BufferedImage croped_frame = img.getSubimage(i*96, 0, 96, 64);
            Image img_scaled = scale(croped_frame, 3);
            enemy_1_img.get(s).add(img_scaled);
            enemy_1_img_reverse.get(s).add(horizantal_flip(croped_frame, 3));
            //For a sprite with 12 grid x 10 grid, the image's center of mass is at (6, 6.5);
            Vector center = enemy_1_center.get(s);
            enemy_1_img_center.put(s, new Vector(center.x*8*3/SCALE, center.y*8*3/SCALE));
            enemy_1_img_center_reverse.put(s, new Vector((12 - center.x)*8*3/SCALE, center.y*8*3/SCALE));
        }
    }

    enemy_2_img.put("Idle", new ArrayList<>());
    enemy_2_img.put("Gun", new ArrayList<>());
    enemy_2_img.get("Idle").add(loadimg("\\resource\\enemy_2\\Idle.png").getSubimage(0, 0, 48, 48));
    enemy_2_img.get("Gun").add(loadimg("\\resource\\enemy_2\\Gun.png"));

    bullet_img = loadimg("\\resource\\bullet\\bullet.png");
}

public void init(){ 
        this.dispose();
        this.setUndecorated(true);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setSize(WIDTH, HEIGHT);
        
        rend = new Render(getScreenBuffer(), WIDTH, HEIGHT);
        background = new ArrayList<>();
        background.add(rend.scale(rend.loadimg("\\resource\\background\\10.png"), SCALE));
        mouse = new Mouse();

        intialize_sound();

        initialize_pictures();
        // Hide System cursor
        BufferedImage cursorImg = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        this.getContentPane().setCursor(blankCursor);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);

        levels.add(new Level(0));
        levels.get(0).initialize();
        camera = new Camera(new Vector(0, player.pos.y), WIDTH, HEIGHT);
    }

    public void update_input(){
        for (int i = 0; i<256; i++){
            boolean input = this.keyHeld(i);
            keydown[i] = (input && !keyhold[i]);
            keyhold[i] = input;
        }
    }

    public void reset(){
        collisionManager.collidable.clear();
        bullets.clear();
        enemies.clear();

        levels.get(current_level).initialize();
    }

    public void remove_all(){
        for (CollisionBox c : removeables){
            if (c.type.contains("enemy")){
                for (Enemy e : enemies){
                    if (e.box.equals(c)){
                        enemies.remove(e);
                        break;
                    }
                }
            }

            else if (c.type.equals("bullet")){
                for (Bullet b : bullets){
                    if (b.box.equals(c)){
                        bullets.remove(b);
                        break;
                    }
                }
            }

            collisionManager.collidable.remove(c);
        }
    }

    public void movie(Graphics g){
        // System.out.println(mousedown);

        update_input();
        rend.g = g;
        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.white);

        previous_time = current_time;
        current_time = System.currentTimeMillis();
        double delta = (current_time - previous_time) / 1000.0;

        if (hit_pause == 0){
            collisionManager.update(delta);
            camera.update(delta);
            mouse.update();
            for (Enemy e : enemies){
                e.update(delta);
            }
            for (Bullet b : bullets){
                b.update(delta);
            }
            player.update(delta);
            remove_all();
            rend.update();
            rend.g.drawString(Integer.toString(enemies.size()), 1100, 100);
            rend.g.drawString(Integer.toString(collisionManager.collidable.size()), 1100, 120);
            rend.g.drawString(Integer.toString(bullets.size()), 1100, 140);
            rend.g.drawString(Integer.toString(player.hp), 100, 160);
            repaint();
        }
        hit_pause -= delta;
        if (hit_pause < 0){
            hit_pause = 0;
        }
    }
}
