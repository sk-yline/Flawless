import java.awt.Image;
import java.util.ArrayList;

// Level class represents a game level with its tilemap, collision objects, and properties
// Each level is loaded from a TMX file and contains visual tiles and gameplay elements
public class Level{
    // Unique identifier for the level
    public int level_id;
    
    // Width and height of the level in tiles
    public int width = 0;
    public int height = 0;
    
    // Collection of tile images used in this level
    public ArrayList<Image> tileset = new ArrayList<>();
    
    // Multi-layered tilemap data (each layer is a 2D array of tile IDs)
    public ArrayList<int[][]> tilemap = new ArrayList<>();
    
    // Collection of collision objects in the level (walls, platforms, spawn points, etc.)
    public ArrayList<CollisionBox> collider = new ArrayList<>();

    // Size of a tile in pixels
    public int tile_dimension = 0;
    
    // Precise tile dimension (may be a floating point value for scaling)
    public double true_dimension = 0;
    
    // Starting position for the player in this level
    public Vector start_pos = new Vector(0, 0);

    // Constructor creates a level with the specified level ID
    public Level(int lv){
        this.level_id = lv;
    }

    // Initializes the level by loading and processing the corresponding TMX file
    // Sets up tiles, collision objects, enemies, and player position
    public void initialize(){
        // Create a tilemap reader to parse the TMX file
        TileMapReader tmr = new TileMapReader();
        String path = "\\data\\level" + level_id + ".tmx";
        tmr.read_tilemap(path);
        
        // Store the tile data from the reader
        this.tileset = tmr.tileset;
        this.tilemap = tmr.tilemap;
        this.collider = tmr.collider;
        this.height = tmr.h;
        this.width = tmr.w;
        
        // Process collision objects to create game entities
        for (CollisionBox c : collider){
            // Player spawn point
            if (c.type.equals("player")){
                Game.player = new Player(new Vector(c.pos.x, c.pos.y), new Vector(c.size.x, c.size.y));
                start_pos = new Vector(c.pos.x, c.pos.y);
            }
            // Enemy spawn point
            else if (c.type.contains("enemy")){
                Game.enemies.add(new Enemy(c.pos, c.size, c.type, c));
            }
            // Other collision objects (walls, platforms, etc.)
            else{
                Game.collisionManager.collidable.add(c);
            }
        }
        
        // Calculate tile dimensions for rendering
        this.tile_dimension = (int) Math.round(tmr.grid_scale * 16 / Game.SCALE);
        this.true_dimension = tmr.grid_scale * 16 / Game.SCALE;
    }
}