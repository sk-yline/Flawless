import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import javax.imageio.ImageIO;

public class TileMapReader{
    private final String basepath = System.getProperty("user.dir");
    //Tile set in regular size: i.e 96 x 96 pixels
    public ArrayList<Image> tileset = new ArrayList<>();
    public ArrayList<int[][]> tilemap = new ArrayList<>();
    public ArrayList<CollisionBox> collider = new ArrayList<>();
    public double sc;
    public int grid_scale;
    public String type;
    public int h = 0;
    public int w = 0;
    public TileMapReader(){
        this.sc = Game.SCALE;
        this.grid_scale = Math.round((float)(Game.GRIDSIZE / (16/Game.SCALE)));
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
        int img_width = (int)Math.round(img.getWidth() * s / sc);
        int img_height = (int)Math.round(img.getHeight() * s / sc);
        return img.getScaledInstance(img_width, img_height, Image.SCALE_DEFAULT);
    }
    
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

    public void read_tilemap(String path){
        try {
            BufferedReader br = new BufferedReader(new FileReader(basepath + path));
            String line;
            br.readLine();
            br.readLine();
            line = br.readLine().trim();
            HashMap<String, String> info = decode_line(line, true);
            if (type.equals("tileset")){
                read_tileset(basepath + "\\data\\"+ info.get("source"));
                line = br.readLine().trim();
                info = decode_line(line, true);
            }

            while(!line.equals("</map>")){
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
