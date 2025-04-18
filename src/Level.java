import java.awt.Image;
import java.util.ArrayList;

public class Level{
    public int level_id;
    public int width = 0;
    public int height = 0;
    public ArrayList<Image> tileset = new ArrayList<>();
    public ArrayList<int[][]> tilemap = new ArrayList<>();
    public ArrayList<CollisionBox> collider = new ArrayList<>();
    public double player_x_scale = 1.0;
    public double player_y_scale = 1.0;
    public int tile_dimension = 0;
    public double true_dimension = 0;
    public Vector start_pos = new Vector(0, 0);

    public Level(int lv){
        this.level_id = lv;
    }

    public void initialize(){
        TileMapReader tmr = new TileMapReader();
        String path = "\\data\\level" + level_id + ".tmx";
        tmr.read_tilemap(path);
        this.tileset = tmr.tileset;
        this.tilemap = tmr.tilemap;
        this.collider = tmr.collider;
        this.height = tmr.h;
        this.width = tmr.w;
        for (CollisionBox c : collider){
            if (c.type.equals("player")){
                Game.player = new Player(new Vector(c.pos.x, c.pos.y), new Vector(c.size.x * player_x_scale, c.size.y * player_y_scale));
                start_pos = new Vector(c.pos.x, c.pos.y);
            }
            else if (c.type.contains("enemy")){
                Game.enemies.add(new Enemy(c.pos, c.size, c.type, c));
            }
            else{
                Game.collisionManager.collidable.add(c);
            }
        }
        this.tile_dimension = (int) Math.round(tmr.grid_scale * 16 / Game.SCALE);
        this.true_dimension = tmr.grid_scale * 16 / Game.SCALE;
    }
}