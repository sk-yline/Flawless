// Vector class represents a 2D vector with x and y coordinates
// Used for positions, sizes, and mathematical calculations throughout the game
public class Vector {
    public double x = 0;
    public double y = 0;
    
    // Default constructor creates a vector at origin (0,0)
    public Vector(){
        x = 0;
        y = 0;
    }
    
    // Create a vector with specific x,y coordinates
    public Vector(double x, double y){
        this.x = x;
        this.y = y;
    }
    
    // Copy constructor - creates a new vector with the same values as v
    public Vector(Vector v){
        this.x = v.x;
        this.y = v.y;
    }

    // Calculate the length (magnitude) of the vector
    public double magnitude(){
        return (double) Math.sqrt(x * x + y * y);
    }

    // Returns a unit vector (length 1) in the same direction as this vector
    public Vector normalize(){
        double length = magnitude();
        return new Vector(x/length, y/length);
    }

    // Converts world coordinates to tile coordinates
    public Vector to_tile(){
        return new Vector((int) x/Game.GRIDSIZE, (int) y/Game.GRIDSIZE);
    }

    // Converts world coordinates to screen (frame) coordinates based on camera position
    // Camera's (l, u) is used as the (0, 0) reference point for the screen
    public Vector to_frame(Camera c){
        //Camera 的 (l, u) 为 (0, 0)
        return new Vector(x - c.l, y - c.u);
    }

    // Converts tile coordinates to world coordinates (pixels)
    public Vector to_pixle(){
        return new Vector(x * Game.GRIDSIZE, y * Game.GRIDSIZE);
    }

    // Calculates the top-left corner position when given a center position and size
    public Vector find_center(Vector size){
        return new Vector(x - size.x/2, y - size.y/2);
    }

    // Calculates the dot product of this vector with another vector
    public double dot(Vector a){
        return x * a.x + y * a.y;
    }

    // Returns a new vector that is the result of subtracting vector a from this vector
    public Vector minus(Vector a){
        return new Vector(x - a.x, y - a.y);
    }

}
