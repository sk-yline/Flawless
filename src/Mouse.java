
import java.awt.MouseInfo;
import java.awt.Point;

public class Mouse {
    Vector pos;
    Vector size;

    public Mouse(){
        pos = new Vector(0,0);
        size = new Vector(60/Game.SCALE,10/Game.SCALE);
    }

    public void update(){
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        pos = new Vector(mouse.x, mouse.y);
    }
}
