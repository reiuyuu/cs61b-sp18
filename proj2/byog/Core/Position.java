package byog.Core;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

public class Position extends StageBuilder {
    public static final Position[] CARDINALS = new Position[] {
        new Position(1, 0),
        new Position(0, 1),
        new Position(-1, 0),
        new Position(0, -1)
    };
    
    public static final Position[] AROUNDS = new Position[] {
        new Position(1, 0),
        new Position(0, 1),
        new Position(-1, 0),
        new Position(0, -1),
        new Position(1, 1),
        new Position(-1, 1),
        new Position(1, -1),
        new Position(-1, -1)
    };

    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public Position turn(Position dir) {
        return new Position(this.x + dir.x, this.y + dir.y);
    }

    public Position fixOutOfBounds(TETile[][] map) {
        this.x = this.x < 0 ? 0 : this.x;
        this.x = this.x > map.length - 1 ? map.length - 1 : this.x;
        this.y = this.y < 0 ? 0 : this.y;
        this.y = this.y > map[0].length - 1 ? map[0].length - 1 : this.y;

        return this;
    }

    public boolean isInBounds(TETile[][] map) {
        return this.x >= 0 && this.x < map.length && this.y >= 0 && this.y < map[0].length;
    }

    public boolean isInnerWall(TETile[][] map) {
        if (!getTile(map, this).equals(Tileset.WALL)) return false;
        
        for (Position dir : Position.AROUNDS) {
            Position pos = this.turn(dir).fixOutOfBounds(map);
            TETile tile = getTile(map, pos);
            if (!tile.equals(Tileset.WALL) && !tile.equals(Tileset.NOTHING)) return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;

        Position other = (Position) obj;
        if (x != other.x) return false;
        if (y != other.y) return false;

        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;

        return result;
    }
}
