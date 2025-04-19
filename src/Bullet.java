public class Bullet {
    public Vector pos;
    public int dir;
    public double speed = 1125/Game.SCALE;
    public CollisionBox box;
    public String owner;

    public Bullet(Vector pos, int dir, String owner) {
        this.owner = owner;
        this.pos = pos;
        this.dir = dir;
        this.box = new CollisionBox(pos, new Vector(20, 10), dir, "bullet");
        Game.collisionManager.collidable.add(box);
    }

    public void destroy(){
        Game.removeables.add(box);
    }

    public int find_ind(){
        for (int i = 0; i<Game.collisionManager.collidable.size(); i++){
            if (Game.collisionManager.collidable.get(i).equals(this.box)){
                return i;
            }
        }
        return -1;
    }

    public void update_position(double delta){
        //System.out.println("bullet pos: " + pos.x + ", " + pos.y);
        double x = Math.cos(Math.toRadians(dir)) * speed * delta;
        double y = Math.sin(Math.toRadians(dir)) * speed * delta;
        pos.x += x;
        pos.y -= y;
        int box_ind = find_ind();
        if (box_ind == -1){
            System.out.println("bullet not found in collision box list!");
            return;
        }
        box.pos = pos;
        Game.collisionManager.collidable.get(box_ind).pos = pos;
        Game.collisionManager.collidable.get(box_ind).frame_pos = pos.to_frame(Game.camera);
    }

    public void update(double delta) {
        update_position(delta);

        for (CollisionBox c : Game.collisionManager.collidable){
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

            if (owner.equals("enemy") && c.type.equals("player")){
                if (Game.collisionManager.collide(box, c)){
                    //System.out.println("bullet hit player!");
                    Game.player.hp -= 1;
                    destroy();
                }
            }

            if (c.type.equals("player_atk") && c.enable && owner.equals("enemy")){
                if (Game.collisionManager.collide(box, c)){
                    dir += 180;
                    dir %= 360;
                    owner = "player";
                    Game.sound.get("clash").play();
                }
            }
        }

        if (pos.x < Game.camera.l || pos.x > Game.camera.r || pos.y < Game.camera.u || pos.y > Game.camera.b){
            destroy();
        }

        for (CollisionBox c : Game.collisionManager.collidable){
            if (c.type.equals("wall")){
                if (Game.collisionManager.collide(box, c)){
                    destroy();
                    break;
                }
            }
        }
    }
}
