public class Camera {
    // Position of the camera in world coordinates
    public Vector campos;
    
    // Boundaries of the camera view (left, right, up, bottom)
    public double l, r, u, b;
    
    // Dimensions of the camera view (viewport)
    public double width, height;
    
    // Distance from player at which camera starts moving
    public double camera_offest = 4 * Game.GRIDSIZE;
    
    // Area where camera doesn't move when player is inside it
    public double dead_zone = 0.5 * Game.GRIDSIZE;
    
    // Smoothing factor for camera movement (higher = faster follow)
    public double damping = 2;

    // Constructor initializes a camera at a specific position with given viewport dimensions
    public Camera(Vector pos, double width, double height){
        this.campos = pos;
        this.width = width;
        this.height = height;
    }

    // Prevents camera from showing areas outside the level boundaries
    public void clamp_boundary(){
        int current_lv = Game.current_level;
        int level_height = Game.level.height * Game.GRIDSIZE;
        int level_width = Game.level.width * Game.GRIDSIZE;
        
        // Clamp to left boundary
        if (campos.x - width/2 < 0) campos.x = width/2;
        // Clamp to right boundary
        if (campos.x + width/2 > level_width) campos.x = level_width - width/2;
        // Clamp to top boundary
        if (campos.y - height/2 < 0) campos.y = height/2;
        // Clamp to bottom boundary
        if (campos.y + height/2 > level_height) campos.y = level_height - height/2;
    }

    // Moves the camera to follow the player with smoothing
    public void move_camera(double delta){
        // Target position is the player's position
        Vector target_pos = new Vector(Game.player.pos.x, Game.player.pos.y);

        // Camera moves instantly when player gets too far from center
        if (Math.abs(target_pos.x - campos.x) > camera_offest){
            campos.x = target_pos.x - camera_offest * Math.signum(target_pos.x - campos.x);
            campos.y = target_pos.y;
        }
        // Camera doesn't move horizontally when player is in the dead zone
        else if (Math.abs(target_pos.x - campos.x) < dead_zone) {
            campos.y = target_pos.y;
        }
        // Smooth camera movement with damping
        else{
            if (Game.DEBUG){
                Game.rend.g.drawString(Double.toString(target_pos.x - campos.x), 400, 100);
                Game.rend.g.drawString(Double.toString(camera_offest), 400, 200);
            }
            // Interpolate the camera's position toward the target position
            campos.x += (target_pos.x - campos.x) * damping * delta;
            campos.y += (target_pos.y - campos.y);
        }

        // Ensure camera stays within level boundaries
        clamp_boundary();
    }

    // Main update method called every frame
    // Updates camera position and calculates viewport boundaries
    public void update(double delta){
        move_camera(delta);
        
        // Update camera view boundaries
        l = campos.x - width/2;  // left boundary
        r = campos.x + width/2;  // right boundary
        u = campos.y - height/2; // upper boundary
        b = campos.y + height/2; // bottom boundary
    }

}
