package byog.Core;

import java.util.List;
import java.util.Deque;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.Random;

import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

/// The random dungeon generator.
///
/// Starting with a stage of solid walls, it works like so:
///
/// 1. Place a number of randomly sized and positioned rooms. If a room
///    overlaps an existing room, it is discarded. Any remaining rooms are
///    carved out.
/// 2. Any remaining solid areas are filled in with mazes. The maze generator
///    will grow and fill in even odd-shaped areas, but will not touch any
///    rooms.
/// 3. The result of the previous two steps is a series of unconnected rooms
///    and mazes. We walk the stage and find every tile that can be a
///    "connector". This is a solid tile that is adjacent to two unconnected
///    regions.
/// 4. We randomly choose connectors and open them or place a door there until
///    all of the unconnected regions have been joined. There is also a slight
///    chance to carve a connector between two already-joined regions, so that
///    the dungeon isn't single connected.
/// 5. The mazes will have a lot of dead ends. Finally, we remove those by
///    repeatedly filling in any open tile that's closed on three sides. When
///    this is done, every corridor in a maze actually leads somewhere.
///
/// The end result of this is a multiply-connected dungeon with rooms and lots
/// of winding corridors.
public class MapGenerator extends StageBuilder {
    private final int width;
    private final int height;

    public final long seed = 174114077;
    public final Random r = new Random(seed);

    private int numRoomTries = 50;

    /// The inverse chance of adding a connector between two regions that have
    /// already been joined. Increasing this leads to more loosely connected
    /// dungeons.
    private int extraConnectorChance = 25;

    /// Increasing this allows rooms to be larger.
    private int roomExtraSize = -1;

    private int windingPercent = 75;

    private List<Room> _rooms = new ArrayList<Room>();
    
    /// For each open position in the dungeon, the index of the connected region
    /// that that position is a part of.
    private static Map<Position, Integer> _regions;
    
    /// The index of the current region being carved.
    private static int _currentRegion = -1;
    
    public MapGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public TETile[][] generate() {
        
        if (width % 2 == 0 || height % 2 == 0) {
            throw new IllegalArgumentException("Width and height must be odd.");
        }
        
        TETile[][] map = new TETile[width][height];
        
        fill(map, Tileset.WALL);
        _regions = new HashMap<>();
        
        addRoom(map);

        // Fill in the rest of the stage with mazes.
        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                Position pos = new Position(x, y);
                if (getTile(map, pos) != Tileset.WALL) {
                    continue;
                }
                growMaze(map, pos);
            }
        }

        connectRegions(map);
        removeDeadEnds(map);
        removeOuterWalls(map);
        addExits(map);
        addPlayer(map);

        // TODO: onDecorateRoom
        // _rooms.forEach(onDecorateRoom);

        return map;
    }

    // TODO: onDecorateRoom
    // void onDecorateRoom(Rect room) {}

    /// Implementation of the "growing tree" algorithm from here:
    /// http://www.astrolog.org/labyrnth/algrithm.htm.
    private void growMaze(TETile[][] map, Position start) {
        Deque<Position> cells = new LinkedList<>();
        Position lastDir = null;

        startRegion();
        carve(map, start);

        cells.add(start);
        while (!cells.isEmpty()) {
            Position cell = cells.getLast();

            // See which adjacent cells are open.
            List<Position> unmadeCells = new ArrayList<>();

            for (Position dir : Position.CARDINALS) {
                if (canCarve(map, cell, dir)) {
                    unmadeCells.add(dir);
                }
            }

            if (!unmadeCells.isEmpty()) {
                // Based on how "windy" passages are, try to prefer carving in the
                // same direction.
                Position dir;
                if (unmadeCells.contains(lastDir) && RandomUtils.uniform(r, 100) > windingPercent) {
                    dir = lastDir;
                } else {
                    dir = unmadeCells.get(RandomUtils.uniform(r, unmadeCells.size()));
                }

                carve(map, cell.turn(dir));
                carve(map, cell.turn(dir).turn(dir));

                cells.add(cell.turn(dir).turn(dir));
                lastDir = dir;
            } else {
                // No adjacent unmade cells.
                cells.removeLast();

                // This path has ended.
                lastDir = null;
            }
        }
    }

    /// Places rooms ignoring the existing maze corridors.
    private void addRoom(TETile[][] map) {
        for (int i = 0; i < numRoomTries; i++) {
            // Pick a random room size. The funny math here does two things:
            // - It makes sure rooms are odd-sized to line up with maze.
            // - It avoids creating rooms that are too rectangular: too tall and
            // narrow or too wide and flat.
            // TODO: This isn't very flexible or tunable. Do something better here.
            int size = RandomUtils.uniform(r, 1, 3 + roomExtraSize) * 2 + 1;
            int rectangularity = RandomUtils.uniform(r, 0, 1 + size / 2) * 2;
            int w = size;
            int h = size;

            if (RandomUtils.uniform(r, 2) < 1) {
                w += rectangularity;
            } else {
                h += rectangularity;
            }

            int x = RandomUtils.uniform(r, (width - w) / 2) * 2 + 1;
            int y = RandomUtils.uniform(r, (height - h) / 2) * 2 + 1;

            Room room = new Room(new Position(x, y), w, h);

            // prevent rooms from overlapping.
            boolean overlaps = false;
            for (Room other : _rooms) {
                if (room.isOverlapTo(other)) {
                    overlaps = true;
                    break;
                }
            }

            if (overlaps) {
                continue;
            }

            // TODO: optimize
            // prevent rooms from out of bounds.
            if (room.pos.x + room.w > width - 1) {
                room.w = width - 1 - room.pos.x;
            }

            if (room.pos.y + room.h > height - 1) {
                room.h = height - 1 - room.pos.y;
            }

            _rooms.add(room);

            startRegion();
            for (int rx = room.pos.x; rx < room.pos.x + room.w; rx++) {
                for (int ry = room.pos.y; ry < room.pos.y + room.h; ry++) {
                    Position pos = new Position(rx, ry);
                    carve(map, pos);
                }
            }
        }
    }

    private void connectRegions(TETile[][] map) {
        // Find all of the tiles that can connect two (or more) regions.
        Map<Position, Set<Integer>> connectorRegions = new HashMap<>();
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                Position pos = new Position(x, y);

                // Can't already be part of a region.
                if (getTile(map, pos) != Tileset.WALL) {
                    continue;
                }

                Set<Integer> regions = new HashSet<>();
                for (Position dir : Position.CARDINALS) {
                    if (_regions.containsKey(pos.turn(dir))) {
                        int region = _regions.get(pos.turn(dir));
                        regions.add(region);
                    }
                }

                if (regions.size() < 2) {
                    continue;
                }

                connectorRegions.put(pos, regions);
            }
        }

        List<Position> connectors = new ArrayList<>(connectorRegions.keySet());

        // Keep track of which regions have been merged. This maps an original
        // region index to the one it has been merged to.
        Map<Integer, Integer> merged = new HashMap<>();
        Set<Integer> openRegions = new HashSet<>();
        for (int i = 0; i <= _currentRegion; i++) {
            merged.put(i, i);
            openRegions.add(i);
        }

        // Keep connecting regions until we're down to one.
        while (openRegions.size() > 1) {
            Position connector = connectors.get(RandomUtils.uniform(r, connectors.size()));

            // Carve the connection.
            addJunction(map, connector);

            // Merge the connected regions. We'll pick one region (arbitrarily) and
            // map all of the other regions to its index.
            Set<Integer> regions = connectorRegions.get(connector).stream().map((region) -> merged.get(region)).collect(Collectors.toSet());
            int dest = regions.iterator().next();
            Set<Integer> sources = regions.stream().filter((region) -> region != dest).collect(Collectors.toSet());

            // Merge all of the affected regions. We have to look at *all* of the
            // regions because other regions may have previously been merged with
            // some of the ones we're merging now.
            for (int i = 0; i <= _currentRegion; i++) {
                if (sources.contains(merged.get(i))) {
                    merged.put(i, dest);
                }
            }
            
            // The sources are no longer in use.
            openRegions.removeAll(sources);

            // Remove any connectors that aren't needed anymore.
            connectors.removeIf((pos) -> {
                // Don't allow connectors right next to each other.
                for (Position dir : Position.CARDINALS) {
                    if (connector.turn(dir).x == pos.x && connector.turn(dir).y == pos.y) {
                        return true;
                    }
                }
                
                // If the connector no long spans different regions, we don't need it.
                Set<Integer> SpansRegions = connectorRegions.get(pos).stream().map((region) -> merged.get(region)).collect(Collectors.toSet());

                if (SpansRegions.size() > 1) {
                    return false;
                }

                // This connecter isn't needed, but connect it occasionally so that the
                // dungeon isn't singly-connected.
                if (RandomUtils.uniform(r, extraConnectorChance) < 1) {
                    addJunction(map, pos);
                }

                return true;
            });
        }
    }

    private void addJunction(TETile[][] map, Position pos) {
        if (RandomUtils.uniform(r, 4) < 1) {
            TETile tile = (RandomUtils.uniform(r, 3) < 1) ? Tileset.FLOOR : Tileset.FLOOR;
            setTile(map, pos, tile);
        } else {
            setTile(map, pos, Tileset.FLOOR);
        }
    }

    private void removeDeadEnds(TETile[][] map) {
        boolean done = false;

        while (!done) {
            done = true;

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    Position pos = new Position(x, y);

                    if (getTile(map, pos) == Tileset.WALL) {
                        continue;
                    }

                    // If it only has one exit, it's a dead end.
                    int exits = 0;
                    for (Position dir : Position.CARDINALS) {
                        if (getTile(map, pos.turn(dir)) != Tileset.WALL) {
                            exits++;
                        }
                    }

                    if (exits != 1) {
                        continue;
                    }

                    done = false;

                    setTile(map, pos, Tileset.WALL);
                }
            }
        }
    }

    private void removeOuterWalls(TETile[][] map) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position pos = new Position(x, y);
                if (pos.isInnerWall(map)) {
                    setTile(map, pos, Tileset.NOTHING);
                }
            }
        }
    }

    private void addExits(TETile[][] map) {
        for (int y = height - 1; y >= 0 ; y--) {
            for (int x = width - 1; x >= 0; x--) {
                Position pos = new Position(x, y);
                for (Position dir : Position.CARDINALS) {
                    if (getTile(map, pos.turn(dir).fixOutOfBounds(map)) == Tileset.FLOOR) {
                        setTile(map, pos, Tileset.LOCKED_DOOR);
                        return;
                    }
                }
            }
        }
    }

    private void addPlayer(TETile[][] map) {
        for (int y = height / 4; y < height / 2; y++) {
            for (int x = width / 4; x < width / 2; x++) {
                Position pos = new Position(x, y);
                if (getTile(map, pos) == Tileset.FLOOR) {
                    setTile(map, pos, Tileset.PLAYER);
                    return;
                }
            }
        }
    }

    /// Gets whether or not an opening can be carved from the given starting
    /// [Cell] at [pos] to the adjacent Cell facing [direction]. Returns `true`
    /// if the starting Cell is in bounds and the destination Cell is filled
    /// (or out of bounds).</returns>
    boolean canCarve(TETile[][] map, Position pos, Position dir) {
        // Must end in bounds.
        if (!(pos.turn(dir).turn(dir).turn(dir)).isInBounds(map)) {
            return false;
        }

        // Destination must not be open.
        return getTile(map, pos.turn(dir).turn(dir)) == Tileset.WALL;
    }

    private void startRegion() {
        _currentRegion++;
    }

    public void carve(TETile[][] map, Position pos) {
        TETile tile = Tileset.FLOOR;
        setTile(map, pos, tile);
        _regions.put(pos, _currentRegion);
    }
}
