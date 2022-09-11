package byog.Core;

import byog.TileEngine.TETile;

abstract class StageBuilder {

    public TETile getTile(TETile[][] map, Position pos) {
        return map[pos.x][pos.y];
    }

    public void setTile(TETile[][] map, Position pos, TETile type) {
        map[pos.x][pos.y] = type;
    }

    public void fill(TETile[][] map, TETile tile) {
        for (int y = 0; y < map[0].length; y++) {
            for (int x = 0; x < map.length; x++) {
                setTile(map, new Position(x, y), tile);
            }
        }
    }
}
