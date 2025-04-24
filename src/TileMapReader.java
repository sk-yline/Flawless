import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;

// TileMapReader parses TMX files exported from Tiled Map Editor
// Loads and processes tileset images, tile layers, and collision objects
public class TileMapReader{
    // Base path for locating files
    private final String basepath = System.getProperty("user.dir");
    
    // Tileset images loaded from the TMX file
    public ArrayList<Image> tileset = new ArrayList<>();
    
    // Tile layers data (2D arrays of tile IDs)
    public ArrayList<int[][]> tilemap = new ArrayList<>();
    
    // Collision objects defined in the TMX file
    public ArrayList<CollisionBox> collider = new ArrayList<>();
    
    // Scaling factor for tiles
    public double sc;
    
    // Grid size for scaling tile dimensions
    public int grid_scale;
    
    // Type of the current TMX element being processed
    public String type;
    
    // Height and width of the tilemap in tiles
    public int h = 0;
    public int w = 0;
    
    // Constructor initializes scaling values based on game settings
    public TileMapReader(){
        this.sc = Game.SCALE;
        this.grid_scale = Math.round((float)(Game.GRIDSIZE / (16/Game.SCALE)));
    }

    // Loads an image from the specified path
    public BufferedImage loadimg(String path){
        BufferedImage img = null;
        
        try {
            img = ImageIO.read(new File(basepath + path));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return img;
    }

    // Scales an image based on the game's scaling factor
    public Image scale(BufferedImage img, double s){
        int img_width = (int)Math.round(img.getWidth() * s / sc);
        int img_height = (int)Math.round(img.getHeight() * s / sc);
        return img.getScaledInstance(img_width, img_height, Image.SCALE_DEFAULT);
    }
    
    // Parses a TMX file line into key-value pairs
    // Example: <tileset name="tiles" tilewidth="16" tileheight="16"> becomes {name="tiles", tilewidth="16", tileheight="16"}
    public HashMap<String, String> decode_line(String str, boolean replace_slah){
        str = str.replace("<", "").replace(">", "").replace("\"", "");
        if (replace_slah){
            str = str.replace("/", "");
        }
        str = str.trim();
        String[] decoded = str.split("[=\s]");
        type = decoded[0];
        HashMap<String, String> mp = new HashMap<>();
        for (int i = 1; i<decoded.length; i+=2){
            mp.put(decoded[i], decoded[i+1]);
        }

        return mp;
    }

    // Extracts the resource path from a full file path
    // Example: "C:/Projects/Game/resource/tiles.png" becomes "\resource\tiles.png"
    public String extract_sorce(String source){
        String[] path = source.split("/");
        int ind = 0;
        String new_source = "";
        while (!(path[ind].equals("resource"))){
            ind++;
        }

        for (int i = ind; i<path.length; i++){
            new_source += "\\" + path[i];
        }

        return new_source;
    }

    // Reads and processes a tileset file
    // Loads the tileset image and splits it into individual tiles
    public void read_tileset(String path){
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            br.readLine();
            line = br.readLine().trim();
            HashMap<String, String> tileset_info = decode_line(line, false);
            line = br.readLine().trim();
            HashMap<String, String> img_info = decode_line(line, false);
            int column = Integer.parseInt(tileset_info.get("columns"));
            int row = Integer.parseInt(tileset_info.get("tilecount"))/column;
            BufferedImage ts = loadimg(extract_sorce(img_info.get("source")));
            
            // Split the tileset image into individual tiles
            for (int i = 0; i<row; i++){
                for (int j = 0; j<column; j++){
                    BufferedImage croped_img = ts.getSubimage(j*16, i*16, 16, 16);
                    tileset.add(scale(croped_img, grid_scale));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Reads and processes a TMX tilemap file
    // Parses tileset references, tile layers, and object layers
    public void read_tilemap(String path){
        try {
            BufferedReader br = new BufferedReader(new FileReader(basepath + path));
            String line;
            br.readLine();
            br.readLine();
            line = br.readLine().trim();
            HashMap<String, String> info = decode_line(line, true);
            
            // Process tileset references
            while (type.equals("tileset")){
                read_tileset(basepath + "\\data\\"+ info.get("source"));
                line = br.readLine().trim();
                info = decode_line(line, true);
            }

            // Process map data until end of file
            while(!line.equals("</map>")){
                // Process tile layers (visual tiles)
                if (type.equals("layer")){
                    br.readLine();
                    h = Integer.parseInt(info.get("height"));
                    w = Integer.parseInt(info.get("width"));
                    int[][] map = new int[h][w];
                    for (int i = 0; i<h; i++){
                        int[] row = Arrays.stream(br.readLine().trim().split(",")).mapToInt(Integer::parseInt).toArray();
                        map[i] = row;
                    }

                    tilemap.add(map);

                    br.readLine();
                    br.readLine();
                }

                // Process object layers (collision boxes, spawn points, etc.)
                if (type.equals("objectgroup")){
                    String name = info.get("name");
                    line = br.readLine().trim();
                    while (!line.equals("</objectgroup>")){
                        info = decode_line(line, true);
                        double x = Double.parseDouble(info.get("x")) * grid_scale / sc;
                        double y = Double.parseDouble(info.get("y")) * grid_scale / sc;
                        double height = Double.parseDouble(info.get("height")) * grid_scale / sc;
                        double width = Double.parseDouble(info.get("width")) * grid_scale / sc;
                        collider.add(new CollisionBox(new Vector(x + width/2, y + height/2), new Vector(width, height), 0, name));
                        line = br.readLine().trim();
                    }
                }

                line = br.readLine().trim();
                info = decode_line(line, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}