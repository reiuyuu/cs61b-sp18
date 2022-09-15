package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

public class Player extends StageBuilder {
    public Position pos;
    
    public Player(Position pos) {
        this.pos = pos;
    }

    public void moveUp(TETile[][] map) {
        move(map, Position.CARDINALS[1]);
    }

    public void moveLeft(TETile[][] map) {
        move(map, Position.CARDINALS[2]);
    }

    public void moveDown(TETile[][] map) {
        move(map, Position.CARDINALS[3]);
    }

    public void moveRight(TETile[][] map) {
        move(map, Position.CARDINALS[0]);
    }

    private void move(TETile[][] map, Position dir) {
        Position destPos = pos.turn(dir);
        TETile destTile = getTile(map, destPos);

        if (destTile.equals(Tileset.WALL)) {
            return;
        } else if (destTile.equals(Tileset.LOCKED_DOOR)) {
            setTile(map, this.pos, Tileset.PLAYER);
            setTile(map, destPos, Tileset.UNLOCKED_DOOR);
        } else {
            setTile(map, this.pos, Tileset.FLOOR);
            setTile(map, destPos, Tileset.PLAYER);
            this.pos = destPos;
        }
    }
}
