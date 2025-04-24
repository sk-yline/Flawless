import java.awt.MouseInfo;
import java.awt.Point;

// Mouse class tracks the mouse cursor position and provides a visual representation in-game
// Used for aiming player attacks and determining attack direction
public class Mouse {
    // Position of the mouse cursor in screen coordinates
    Vector pos;
    
    // Size of the mouse cursor visual representation
    Vector size;

    // Constructor initializes the mouse with default position and size
    public Mouse(){
        pos = new Vector(0,0);
        size = new Vector(60/Game.SCALE,10/Game.SCALE);
    }

    // Updates the mouse position based on current mouse cursor location
    // Called every frame to track mouse movement
    public void update(){
        Point mouse = MouseInfo.getPointerInfo().getLocation();
        pos = new Vector(mouse.x, mouse.y);
    }
}
