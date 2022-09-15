package byog.Core;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
///
/// The idea is port from a procedural dungeon generator.
/// @source http://journal.stuffwithstuff.com/2014/12/21/rooms-and-mazes/
public class MapGenerator extends StageBuilder {
    private static final int numRoomTries = 50;

    /// The inverse chance of adding a connector between two regions that have
    /// already been joined. Increasing this leads to more loosely connected
    /// dungeons.
    private static final int extraConnectorChance = 40;

    /// Increasing this allows rooms to be larger.
    private static final int roomExtraSize = 0;

    private static final int windingPercent = 60;

    private final int width;
    private final int height;
    private final Random r;
    
    private TETile[][] _map;
    private Position _exit;
    private Player _player;
    private List<Room> _rooms;

    /// For each open position in the dungeon, the index of the connected region
    /// that that position is a part of.
    private Map<Position, Integer> _regions;

    /// The index of the current region being carved.
    private int _currentRegion;
    
    public MapGenerator(int width, int height, long seed) {
        this.width = width;
        this.height = height;
        this.r = new Random(seed);
        generate();
    }

    public TETile[][] getMap() {
        return _map;
    }

    public TETile getTile(Position pos) {
        return getTile(_map, pos);
    }

    public int checkStatus() {
        if (_player.getPos().equals(_exit)) return 1;
        return 0;
    }

    public void move(char c) {
        switch (c) {
            case 'W':
                _player.moveUp(_map);
                break;
            case 'A':
                _player.moveLeft(_map);
                break;
            case 'S':
                _player.moveDown(_map);
                break;
            case 'D':
                _player.moveRight(_map);
                break;
            default:
        }
    }

    private void generate() {
        if (width % 2 == 0 || height % 2 == 0) {
            throw new IllegalArgumentException("The map must be odd-sized.");
        }

        _map = new TETile[width][height];

        fill(_map, Tileset.WALL);
        _rooms = new ArrayList<Room>();
        _regions = new HashMap<>();
        _currentRegion = -1;

        addRooms();

        // Fill in the rest of the stage with mazes.
        for (int y = 1; y < height; y += 2) {
            for (int x = 1; x < width; x += 2) {
                Position pos = new Position(x, y);
                if (!getTile(_map, pos).equals(Tileset.WALL)) continue;
                growMaze(pos);
            }
        }

        connectRegions();
        removeDeadEnds();
        removeInnerWalls();
        
        // TODO: _rooms.forEach(onDecorateRoom);

        addExits();
        addPlayer();
    }

    /// Places rooms ignoring the existing maze corridors.
    private void addRooms() {
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

            Room room = new Room(new Position(x, y), w, h).fixOutOfBounds(_map);

            boolean overlaps = false;
            for (Room other : _rooms) {
                if (room.isOverlapTo(other)) {
                    overlaps = true;
                    break;
                }
            }

            if (overlaps) continue;

            _rooms.add(room);

            startRegion();
            for (int rx = room.getPos().getX(); rx < room.getPos().getX() + room.getW(); rx++) {
                for (int ry = room.getPos().getY(); ry < room.getPos().getY() + room.getH(); ry++) {
                    carve(new Position(rx, ry));
                }
            }
        }
    }

    /// Implementation of the "growing tree" algorithm from here:
    /// http://www.astrolog.org/labyrnth/algrithm.htm.
    private void growMaze(Position start) {
        Deque<Position> cells = new LinkedList<>();
        Position lastDir = null;

        startRegion();
        carve(start);

        cells.add(start);
        while (!cells.isEmpty()) {
            Position cell = cells.getLast();

            // See which adjacent cells are open.
            List<Position> unmadeCells = new ArrayList<>();

            for (Position dir : Position.CARDINALS) {
                if (canCarve(cell, dir)) unmadeCells.add(dir);
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

                carve(cell.turn(dir));
                carve(cell.turn(dir).turn(dir));

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

    private void connectRegions() {
        // Find all of the tiles that can connect two (or more) regions.
        Map<Position, Set<Integer>> connectorRegions = new HashMap<>();
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                Position pos = new Position(x, y);

                // Can't already be part of a region.
                if (!getTile(_map, pos).equals(Tileset.WALL)) continue;

                Set<Integer> regions = new HashSet<>();
                for (Position dir : Position.CARDINALS) {
                    if (_regions.containsKey(pos.turn(dir))) {
                        int region = _regions.get(pos.turn(dir));
                        regions.add(region);
                    }
                }

                if (regions.size() < 2) continue;

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
            addJunction(connector);

            // Merge the connected regions. We'll pick one region (arbitrarily) and
            // map all of the other regions to its index.
            Set<Integer> regions = connectorRegions.get(connector).stream()
                    .map((region) -> merged.get(region)).collect(Collectors.toSet());
            int dest = regions.iterator().next();
            Set<Integer> sources = regions.stream()
                    .filter((region) -> region != dest).collect(Collectors.toSet());

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
                    if (connector.turn(dir).equals(pos)) return true;
                }
                
                // If the connector no long spans different regions, we don't need it.
                Set<Integer> spansRegions = connectorRegions.get(pos).stream()
                        .map((region) -> merged.get(region)).collect(Collectors.toSet());

                if (spansRegions.size() > 1) return false;

                // This connecter isn't needed, but connect it occasionally so that the
                // dungeon isn't singly-connected.
                if (RandomUtils.uniform(r, extraConnectorChance) < 1) addJunction(pos);

                return true;
            });
        }
    }

    private void addJunction(Position pos) {
        if (RandomUtils.uniform(r, 4) < 1) {
            TETile tile = (RandomUtils.uniform(r, 3) < 1) ? Tileset.FLOOR : Tileset.FLOOR;
            setTile(_map, pos, tile);
        } else {
            setTile(_map, pos, Tileset.FLOOR);
        }
    }

    private void removeDeadEnds() {
        boolean done = false;

        while (!done) {
            done = true;

            for (int x = 1; x < width - 1; x++) {
                for (int y = 1; y < height - 1; y++) {
                    Position pos = new Position(x, y);

                    if (getTile(_map, pos).equals(Tileset.WALL)) continue;

                    // If it only has one exit, it's a dead end.
                    int exits = 0;
                    for (Position dir : Position.CARDINALS) {
                        if (!getTile(_map, pos.turn(dir)).equals(Tileset.WALL)) exits++;
                    }

                    if (exits != 1) continue;

                    done = false;
                    setTile(_map, pos, Tileset.WALL);
                }
            }
        }
    }

    /// Gets whether or not an opening can be carved from the given starting
    /// [Cell] at [pos] to the adjacent Cell facing [direction]. Returns `true`
    /// if the starting Cell is in bounds and the destination Cell is filled
    /// (or out of bounds).</returns>
    private boolean canCarve(Position pos, Position dir) {
        // Must end in bounds.
        if (!(pos.turn(dir).turn(dir).turn(dir)).isInBounds(_map)) return false;

        // Destination must not be open.
        return getTile(_map, pos.turn(dir).turn(dir)).equals(Tileset.WALL);
    }

    private void startRegion() {
        _currentRegion++;
    }

    private void carve(Position pos) {
        TETile tile = Tileset.FLOOR;
        setTile(_map, pos, tile);
        _regions.put(pos, _currentRegion);
    }

    private void removeInnerWalls() {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position pos = new Position(x, y);
                if (pos.isInnerWall(_map)) setTile(_map, pos, Tileset.NOTHING);
            }
        }
    }

    private void addExits() {
        for (int y = height - 1; y >= 0; y--) {
            for (int x = width - 1; x >= 0; x--) {
                Position pos = new Position(x, y);
                for (Position dir : Position.CARDINALS) {
                    if (getTile(_map, pos.turn(dir).fixOutOfBounds(_map)).equals(Tileset.FLOOR)) {
                        setTile(_map, pos, Tileset.LOCKED_DOOR);
                        _exit = pos;
                        return;
                    }
                }
            }
        }
    }

    private void addPlayer() {
        for (int y = height / 4; y < height / 2; y++) {
            for (int x = width / 4; x < width / 2; x++) {
                Position pos = new Position(x, y);
                if (getTile(_map, pos).equals(Tileset.FLOOR)) {
                    setTile(_map, pos, Tileset.PLAYER);
                    _player = new Player(pos);
                    return;
                }
            }
        }
    }
}
