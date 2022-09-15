package byog.Core;

import java.io.Serializable;

import byog.TileEngine.TETile;

abstract class StageBuilder implements Serializable {
    private static final long serialVersionUID = 1234567890L;

    public TETile getTile(TETile[][] map, Position pos) {
        return map[pos.x][pos.y];
    }

    public void setTile(TETile[][] map, Position pos, TETile tile) {
        map[pos.x][pos.y] = tile;
    }

    public void fill(TETile[][] map, TETile tile) {
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length; x++) {
                setTile(map, new Position(x, y), tile);
            }
        }
    }
}
