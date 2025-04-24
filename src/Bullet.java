public class Bullet {
    // Position vector of the bullet
    public Vector pos;
    // Direction in degrees
    public int dir;
    // Bullet speed - scales based on game scale
    public double speed = 1125/Game.SCALE;
    // Collision box for bullet
    public CollisionBox box;
    // Owner of bullet ("player", "enemy", or "killing_strip")
    public String owner;

    /**
     * Creates a new bullet
     * @param pos Starting position
     * @param dir Direction in degrees
     * @param owner Entity that fired the bullet
     */
    public Bullet(Vector pos, int dir, String owner) {
        this.owner = owner;
        this.pos = pos;
        this.dir = dir;
        // Create collision box for the bullet
        this.box = new CollisionBox(pos, new Vector(18, 6), dir, "bullet");
        Game.collisionManager.collidable.add(box);
        // Special case for killing_strip - much faster speed
        if (owner.equals("killing_strip")){
            speed = 10000/Game.SCALE;
        }
    }

    /**
     * Marks bullet for removal from the game
     */
    public void destroy(){
        Game.removeables.add(box);
    }

    /**
     * Finds the index of this bullet's collision box in the collision manager
     * @return Index of the collision box or -1 if not found
     */
    public int find_ind(){
        for (int i = 0; i<Game.collisionManager.collidable.size(); i++){
            if (Game.collisionManager.collidable.get(i).equals(this.box)){
                return i;
            }
        }
        return -1;
    }

    /**
     * Updates the bullet's position based on direction and speed
     * @param delta Time elapsed since last update
     */
    public void update_position(double delta){
        //System.out.println("bullet pos: " + pos.x + ", " + pos.y);
        // Calculate movement based on direction angle
        double x = Math.cos(Math.toRadians(dir)) * speed * delta;
        double y = Math.sin(Math.toRadians(dir)) * speed * delta;
        // Update position
        pos.x += x;
        pos.y -= y;
        // Update collision box position
        int box_ind = find_ind();
        if (box_ind == -1){
            System.out.println("bullet not found in collision box list!");
            return;
        }
        box.pos = pos;
        Game.collisionManager.collidable.get(box_ind).pos = pos;
        Game.collisionManager.collidable.get(box_ind).frame_pos = pos.to_frame(Game.camera);
    }

    /**
     * Main update method for bullet logic
     * @param delta Time elapsed since last update
     */
    public void update(double delta) {
        // Update position first
        update_position(delta);

        // Skip collision checks for killing_strip bullets
        if (!owner.equals("killing_strip")){
            for (CollisionBox c : Game.collisionManager.collidable){
                // Check for player bullet hitting enemies
                if (owner.equals("player") && c.type.contains("enemy")){
                    if (Game.collisionManager.collide(box, c)){
                        for (Enemy e : Game.enemies){
                            if (e.box.equals(c)){
                                e.destroy();
                                break;
                            }
                        }
                    }
                }

                // Check for enemy bullet hitting player
                if (owner.equals("enemy") && c.type.equals("player")){
                    if (Game.collisionManager.collide(box, c) && c.enable){
                        //System.out.println("bullet hit player!");
                        // Reduce player health
                        Game.player.hp -= 1;
                        // Handle player taking damage
                        if (Game.player.hp == 1){
                            // Player at critical health
                            Game.player.hit = true;
                            Game.hit_pause = Game.hit_pause_time;
                            Game.player.hit_time = Game.player.hit_duration;
                            Game.sound.get("break").play();
                        }
                        else{
                            // Player defeated
                            Game.hit_pause = 1000;
                        }
                        destroy();
                    }
                }

                // Check for collision between enemy bullet and player's attack - reflects the bullet back
                if (c.type.equals("player_atk") && c.enable && owner.equals("enemy")){
                    if (Game.collisionManager.collide(box, c)){
                        // Reverse the bullet direction
                        dir += 180;
                        dir %= 360;
                        // Change ownership to player so it can damage enemies
                        owner = "player";
                        // Play clash sound effect
                        Game.sound.get("clash").play();
                    }
                }
            }

            // Destroy bullet if it goes outside of camera bounds
            if (pos.x < Game.camera.l || pos.x > Game.camera.r || pos.y < Game.camera.u || pos.y > Game.camera.b){
                destroy();
            }

            // Check for collision with walls
            for (CollisionBox c : Game.collisionManager.collidable){
                if (c.type.equals("wall")){
                    if (Game.collisionManager.collide(box, c)){
                        // Destroy bullet when it hits a wall
                        destroy();
                        break;
                    }
                }
            }
        }
    }
}
