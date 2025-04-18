public class Vector {
    public double x = 0;
    public double y = 0;
    public Vector(){
        x = 0;
        y = 0;
    }
    public Vector(double x, double y){
        this.x = x;
        this.y = y;
    }
    public Vector(Vector v){
        this.x = v.x;
        this.y = v.y;
    }

    public double magnitude(){
        return (double) Math.sqrt(x * x + y * y);
    }

    public Vector normalize(){
        double length = magnitude();
        return new Vector(x/length, y/length);
    }

    public Vector to_tile(){
        return new Vector((int) x/Game.GRIDSIZE, (int) y/Game.GRIDSIZE);
    }

    public Vector to_frame(Camera c){
        //Camera 的 (l, u) 为 (0, 0)
        return new Vector(x - c.l, y - c.u);
    }

    public Vector to_pixle(){
        return new Vector(x * Game.GRIDSIZE, y * Game.GRIDSIZE);
    }

    public Vector find_center(Vector size){
        return new Vector(x - size.x/2, y - size.y/2);
    }

    public double dot(Vector a){
        return x * a.x + y * a.y;
    }

    public Vector minus(Vector a){
        return new Vector(x - a.x, y - a.y);
    }

}
