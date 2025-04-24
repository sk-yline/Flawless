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
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.Renderer;
import javax.swing.RepaintManager;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

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
final public static boolean DEBUG = false;
final public static int WIDTH = Math.round((float)(1920 / SCALE));
final public static int HEIGHT = Math.round((float)(1200 / SCALE));
final public static int GRIDSIZE = Math.round((float)(16*3 / SCALE));

public static Render rend;
public static ArrayList<Image> background; 
public static Level level;
public static int current_level = 1;
public static boolean next_level = false;
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
final public static double hit_pause_time = 0.15;

public static HashMap<String, AudioClip> sound = new HashMap();
public static ArrayList<String> level_background = new ArrayList<>();
public static ArrayList<Image> moveable_background = new ArrayList<>();
public static Image static_background = null;

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
public static BufferedImage killing_strip;
public static HashMap<String, ArrayList<Image>> effects_img = new HashMap();

public static Image mainscreen = null;
public static Clip mainmusic = null;

//Intialize all the sound effects
public void intialize_sound(){
    sound.put("jump", getAudioClip(getCodeBase(), "\\audio\\jump.wav"));
    sound.put("clash", getAudioClip(getCodeBase(), "\\audio\\clash.wav"));
    sound.put("swosh", getAudioClip(getCodeBase(), "\\audio\\swosh.wav"));
    sound.put("impact", getAudioClip(getCodeBase(), "\\audio\\impact.wav"));
    sound.put("sword", getAudioClip(getCodeBase(), "\\audio\\sword.wav"));
    sound.put("walk", getAudioClip(getCodeBase(), "\\audio\\walk.wav"));
    sound.put("break", getAudioClip(getCodeBase(), "\\audio\\break.wav"));
    sound.put("gun", getAudioClip(getCodeBase(), "\\audio\\gun.wav"));
    sound.put("pause", getAudioClip(getCodeBase(), "\\audio\\pause.wav"));
    sound.put("unpause", getAudioClip(getCodeBase(), "\\audio\\unpause.wav"));
    sound.put("death", getAudioClip(getCodeBase(), "\\audio\\death.wav"));

    try {
        AudioInputStream music = AudioSystem.getAudioInputStream(new File(System.getProperty("user.dir") + "\\audio\\bgm.wav"));
        mainmusic = AudioSystem.getClip();
        mainmusic.open(music);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

//Load the image
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

//Initialize all the images
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

    //Frame pictures
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

    //Attack Image
    player_frame.put("Attack", 5);
    for (int i = 0; i<5; i++){
        BufferedImage img = loadimg("\\resource\\player\\Slash" + i + ".png");
        player_attack.add(img);
    }


    //Enemy Frames
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

    //Bullet Images
    bullet_img = loadimg("\\resource\\bullet\\bullet.png");
    killing_strip = loadimg("\\resource\\bullet\\killing_strip.png");

    //Backgrounds
    for (int i = 1; i<=5; i++){
        moveable_background.add(scale(loadimg("\\resource\\background\\Moveable_background\\" + i + ".png"), 4));
    }
    static_background = loadimg("\\resource\\background\\Static_background.png");

    level_background = new ArrayList<String>() {{
        add("moveable");
        add("moveable");
        add("moveable");
        add("moveable");
        add("moveable");
    }};

    mainscreen = scale(loadimg("\\resource\\background\\Mainscreen.png"), 1);
}

public void init(){ 
        this.dispose();
        this.setUndecorated(true);
        this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.setSize(WIDTH, HEIGHT);
        
        rend = new Render(getScreenBuffer(), WIDTH, HEIGHT);
        mouse = new Mouse();

        intialize_sound();

        initialize_pictures();
        // Hide System cursor
        BufferedImage cursorImg = new BufferedImage(16,16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        this.getContentPane().setCursor(blankCursor);
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
    }

    public void update_input(){
        for (int i = 0; i<256; i++){
            boolean input = this.keyHeld(i);
            keydown[i] = (input && !keyhold[i]);
            keyhold[i] = input;
        }
    }

    //Fade out
    public void fade(double opacity){
        rend.g.setColor(new Color(0, 0, 0, (int)(opacity*255)));
        rend.g.fillRect(0, 0, WIDTH, HEIGHT);
        rend.g.setColor(Color.white);
    }

    //Reset the level and empty all the reference ArrayLists
    public void reset(){ 
        collisionManager.collidable.clear();
        bullets.clear();
        enemies.clear();
        removeables.clear();
        level = new Level(current_level);
        level.initialize();
        camera = new Camera(level.start_pos, WIDTH, HEIGHT);
        hit_pause = 0;
    }

    //Remove all the objects that needs to be removed after this frame (i.e if died)
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

    boolean fade_out = false;
    boolean fade_in = false;
    double opacity = 0;
    boolean can_reset = false;
    boolean pause = false;
    int pause_select = 0;
    String state = "mainscreen";
    boolean thank_page = false;
    boolean quote = false;
    int screen_select = 1;
    int ending_select = 1;

    public void movie(Graphics g){
        rend.g = g;

        update_input();
        g.setColor(Color.black);
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.white);

        previous_time = current_time;
        current_time = System.currentTimeMillis();
        double delta = (current_time - previous_time) / 1000.0;
        
        if (state.equals("mainscreen")){
            //UI
            rend.g.drawImage(mainscreen, 0, 0, WIDTH, HEIGHT, null);
            rend.g.setFont(new Font("Arial", Font.ITALIC, 100));
            rend.g.drawString("Flawless", (int) (100/SCALE), HEIGHT/2 - (int) (100 / SCALE));
            rend.g.setFont(new Font("Arial", Font.PLAIN, 50));
            rend.g.drawString("New Game", (int) (100/SCALE), (int) (HEIGHT/2 + 250/SCALE));
            rend.g.drawString("Exit", (int) (100/SCALE), (int) (HEIGHT/2 + 350/SCALE));

            //Selection on mainscreen
            if (keydown[KeyEvent.VK_W] || keydown[KeyEvent.VK_UP]){
                screen_select--;
                if (screen_select < 1){
                    screen_select = 2;
                }
            }
            else if (keydown[KeyEvent.VK_S] || keydown[KeyEvent.VK_DOWN]){
                screen_select++;
                if (screen_select > 2){
                    screen_select = 1;
                }
            }

            if (screen_select != 0){
                rend.g.setColor(new Color(255, 255, 255, 100));
                rend.g.fillRoundRect((int)(25/Game.SCALE), (int)(HEIGHT/2 - (50/Game.SCALE) + 225/Game.SCALE + 100/Game.SCALE * (screen_select - 1)), (int)(600/Game.SCALE), (int)(100/Game.SCALE), 10, 10);
                rend.g.setColor(Color.white);
                if (keydown[KeyEvent.VK_SPACE] || keydown[KeyEvent.VK_ENTER]){
                    if (screen_select == 1){
                        //Start the game 
                        fade_out = true;
                        opacity = 0;
                    }
                    else if (screen_select == 2){
                        //Exit the game
                        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    }
                }
            }

            if (fade_out){
                //UI
                rend.g.drawImage(mainscreen, 0, 0, WIDTH, HEIGHT, null);
                rend.g.setFont(new Font("Arial", Font.ITALIC, 100));
                rend.g.drawString("Flawless", (int) (100/SCALE), HEIGHT/2 - (int) (100 / SCALE));
                rend.g.setFont(new Font("Arial", Font.PLAIN, 50));
                rend.g.drawString("New Game", (int) (100/SCALE), (int) (HEIGHT/2 + 250/SCALE));
                rend.g.drawString("Exit", (int) (100/SCALE), (int) (HEIGHT/2 + 350/SCALE));
                rend.g.setColor(new Color(255, 255, 255, 100));
                rend.g.fillRoundRect((int)(25/Game.SCALE), (int)(HEIGHT/2 - (50/Game.SCALE) + 225/Game.SCALE), (int)(600/Game.SCALE), (int)(100/Game.SCALE), 10, 10);
                rend.g.setColor(Color.white);

                //Gradually increase opacity
                opacity += 0.01;
                fade(opacity);
                if (opacity > 0.96){
                    state = "running";
                    reset();
                    fade_out = false;
                    mainmusic.loop(Clip.LOOP_CONTINUOUSLY);
                    mainmusic.start();
                }
            }
            repaint();
        }

        if (state.equals("running")){
            if (player.died){
                if (!fade_out){
                    fade_out = true;
                    opacity = 0;
                }
                if (can_reset){
                    reset();
                    can_reset = false;
                    fade_out = false;
                }
            }

            if (next_level){
                if (!fade_out){
                    fade_out = true;
                    opacity = 0;
                    current_level++;
                }
                if (can_reset){
                    if (current_level == 5){
                        //Don't reset on the "level_5", else it will be subscript out of range
                        state = "ending";
                    }
                    else{
                        reset();
                    }
                    can_reset = false;
                    fade_out = false;
                    next_level = false;
                }
            }  

            if (fade_out){

                //fading after death or screen transition
                opacity += 0.02;
                rend.update();
                fade(opacity);
                if (opacity > 0.9){
                    can_reset = true;
                }
                repaint();
            }
            else{
                if (keydown[KeyEvent.VK_ESCAPE]){
                    //pause manuel
                    pause = !pause;
                    pause_select = 1;
                    if (pause == true){
                        sound.get("pause").play();
                    }
                    else{
                        sound.get("unpause").play();
                    }
                }

                if (pause){
                    rend.update();
                    rend.render_pause_screen();

                    //use arrow key to select option
                    if (keydown[KeyEvent.VK_W] || keydown[KeyEvent.VK_UP]){
                        pause_select--;
                        if (pause_select < 1){
                            pause_select = 3;
                        }
                    }
                    else if (keydown[KeyEvent.VK_S] || keydown[KeyEvent.VK_DOWN]){
                        pause_select++;
                        if (pause_select > 3){
                            pause_select = 1;
                        }
                    }
                    // highlight selected option
                    if (pause_select != 0){
                        rend.g.setColor(new Color(255, 255, 255, 100));
                        rend.g.fillRoundRect((int) (WIDTH/2 - 300/Game.SCALE), (int)(HEIGHT/2 - (50/Game.SCALE) + 150/Game.SCALE * (pause_select - 1)), (int)(600/Game.SCALE), (int)(100/Game.SCALE), 10, 10);
                        rend.g.setColor(Color.white);
                        if (keydown[KeyEvent.VK_SPACE] || keydown[KeyEvent.VK_ENTER]){
                            if (pause_select == 1){
                                pause = false;
                            }
                            else if (pause_select == 2){
                                reset();
                            }
                            else if (pause_select == 3){
                                this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                            }
                        }
                    }
                    repaint();
                }
                else{
                    if (hit_pause == 0){

                        int previous_hp = player.hp;
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
                        if (player.hp != previous_hp){
                            player.hp = previous_hp - 1;
                        }
        
                        rend.update();
                        repaint();
        
                    }
                    else{
                        if (player.hit){
                            System.out.println("hit!");
                            player.change_state("Hit");
                            rend.update();
                            repaint();
                        }
                        else if (player.hp == 0){
                            player.update(delta);
                            rend.update();
                            repaint();
        
                        }
                    }

                    //pause game for a short bit when hit 
                    player.hit_time -= delta;
                    if (player.hit_time <= 0){
                        player.hit_time = 0;
                        player.hit = false;
                    }
                    hit_pause -= delta;
                    if (hit_pause < 0){
                        hit_pause = 0;
                    }
                }
            }
        }

        if (state.equals("ending")){
            mainmusic.stop();
            rend.g.setColor(Color.black);
            rend.g.fillRect(0, 0, WIDTH, HEIGHT);
            rend.g.setColor(Color.white);
            rend.g.setFont(new Font("Arial", Font.BOLD, 80));
            rend.g.drawString("Thank you for playing the demo!", (int) (WIDTH/2 - 600/SCALE), (int) (HEIGHT/2));
            rend.g.setFont(new Font("Arial", Font.PLAIN, 40));
            rend.g.drawString("Back to Mainscreen", (int) (WIDTH/2 - 150/SCALE), (int) (HEIGHT/2 + 100/SCALE));
            rend.g.drawString("Exit the game", (int) (WIDTH/2 - 150/SCALE), (int) (HEIGHT/2 + 200/SCALE));
            if (keydown[KeyEvent.VK_W] || keydown[KeyEvent.VK_UP]){
                ending_select--;
                if (ending_select < 1){
                    ending_select = 2;
                }
            }
            else if (keydown[KeyEvent.VK_S] || keydown[KeyEvent.VK_DOWN]){
                ending_select++;
                if (ending_select > 2){
                    ending_select = 1;
                }
            }

            if (ending_select != 0){
                rend.g.setColor(new Color(255, 255, 255, 100));
                rend.g.fillRoundRect((int)(WIDTH/2 - 200/Game.SCALE), (int)(HEIGHT/2 - (50/Game.SCALE) + 100/Game.SCALE + 100/Game.SCALE * (ending_select - 1)), (int)(400/Game.SCALE), (int)(100/Game.SCALE), 10, 10);
                rend.g.setColor(Color.white);
                if (keydown[KeyEvent.VK_SPACE] || keydown[KeyEvent.VK_ENTER]){
                    if (ending_select == 1){
                        state = "mainscreen";
                    }
                    else if (ending_select == 2){
                        this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
                    }
                }
            }

            repaint();
        }
    }
}
