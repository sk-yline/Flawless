public class Camera {
    public Vector campos;
    public double l, r, u, b, width, height;
    public double camera_offest = 4 * Game.GRIDSIZE;
    public double dead_zone = 0.5 * Game.GRIDSIZE;
    public double damping = 2;

    public Camera(Vector pos, double width, double height){
        this.campos = pos;
        this.width = width;
        this.height = height;
    }

    public void clamp_boundary(){
        int current_lv = Game.current_level;
        int level_height = Game.levels.get(current_lv).height * Game.GRIDSIZE;
        int level_width = Game.levels.get(current_lv).width * Game.GRIDSIZE;
        if (campos.x - width/2 < 0) campos.x = width/2;
        if (campos.x + width/2 > level_width) campos.x = level_width - width/2;
        if (campos.y - height/2 < 0) campos.y = height/2;
        if (campos.y + height/2 > level_height) campos.y = level_height - height/2;
    }

    public void move_camera(double delta){
        Vector target_pos = new Vector(Game.player.pos.x, Game.player.pos.y);

        if (Math.abs(target_pos.x - campos.x) > camera_offest){
            campos.x = target_pos.x - camera_offest * Math.signum(target_pos.x - campos.x);
            campos.y = target_pos.y;
        }
        else if (Math.abs(target_pos.x - campos.x) < dead_zone) {
            campos.y = target_pos.y;
        }
        else{
            if (Game.DEBUG){
                Game.rend.g.drawString(Double.toString(target_pos.x - campos.x), 400, 100);
                Game.rend.g.drawString(Double.toString(camera_offest), 400, 200);
            }
            // Interpolate the camera's position toward the target position
            campos.x += (target_pos.x - campos.x) * damping * delta;
            campos.y += (target_pos.y - campos.y);
        }

        clamp_boundary();
    }

    public void update(double delta){
        move_camera(delta);
        l = campos.x - width/2;
        r = campos.x + width/2;
        u = campos.y - height/2;
        b = campos.y + height/2;
    }

}
