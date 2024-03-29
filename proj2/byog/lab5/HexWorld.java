package byog.lab5;
import org.junit.Test;
import static org.junit.Assert.*;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;
import byog.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {

    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 6472329;
    private static final Random RANDOM = new Random(SEED);
    
    /**
     * Computes the width of row i for a size s hexagon.
     * @param s the size of the hex
     * @param i the row number where i = 0 is the bottom row
     * @return
     */
    public static int hexRowWidth(int s, int i) {
        int effectiveI = i;

        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }

        return s + 2 * effectiveI;
    }

    /**
     * Computesrelative x coordinate of the leftmost tile in the ith
     * row of a hexagon, assuming that the bottom row has an x-coordinate
     * of zero. For example, if s = 3, and i = 2, this function
     * returns -2, because the row 2 up from the bottom starts 2 to the left
     * of the start position, e.g.
     *   xxxx
     *  xxxxxx
     * xxxxxxxx
     * xxxxxxxx <-- i = 2, starts 2 spots to the left of the bottom of the hex
     *  xxxxxx
     *   xxxx
     *
     * @param s size of the hexagon
     * @param i row num of the hexagon, where i = 0 is the bottom
     * @return
     */
    public static int hexRowOffset(int s, int i) {
        int effectiveI = i;

        if (i >= s) {
            effectiveI = 2 * s - 1 - effectiveI;
        }

        return -effectiveI;
    }

    /** Adds a row of the same tile.
     * @param world the world to draw on
     * @param p the leftmost position of the row
     * @param width the number of tiles wide to draw
     * @param t the tile to draw
     */
    public static void addRow(TETile[][] world, Position p, int width, TETile t) {
        for (int xi = 0; xi < width; xi += 1) {
            int xCoord = p.x + xi;
            int yCoord = p.y;
            world[xCoord][yCoord] = TETile.colorVariant(t, 32, 32, 32, RANDOM);
        }
    }

    /**
     * Adds a hexagon to the world.
     * @param world the world to draw on
     * @param p the bottom left coordinate of the hexagon
     * @param s The size of the hex
     * @param t the tile to draw
     */
    public static void addHexagon(TETile[][] world, Position p, int s, TETile t) {

        if (s < 2) {
            throw new IllegalArgumentException("Size must be at least 2");
        }
        
        for (int yi = 0; yi < 2 * s; yi += 1) {     
            int thisRowY = p.y + yi;

            int xRowStart = p.x + hexRowOffset(s, yi);
            Position rowStartP = new Position(xRowStart, thisRowY);
            
            int rowWidth = hexRowWidth(s, yi);

            addRow(world, rowStartP, rowWidth, t);
        }
    }

    /**
     * Draws n hexagons of size s, vertically upward, starting from the position p.
     * @param world the world to draw on
     * @param p the bottom left coordinate of the first hexagon
     * @param s the size of the hex
     * @param n number of hexagons
     */
    public static void drawNVerticalHexes(TETile[][] world, Position p, int s, int n) {
        Position thisP = p;
        for (int i = 0; i < n; i++) {
            addHexagon(world, thisP, s, randomTile());
            thisP = new Position(thisP.x, thisP.y + 2 * s);
        }
    }

    /**
     * Drawing A Tesselation of Hexagons.
     * @param world the world to draw on
     * @param p the bottom left coordinate of the bottom left corner hexagon.
     * @param s the size of the hex
     */
    public static void drawTesselationOfHexagons(TETile[][] world, Position p, int s) {
        drawNVerticalHexes(world, p, s, 3);
        
        p = p.bottomRightNeighbor(s);
        drawNVerticalHexes(world, p, s, 4);
        
        p = p.bottomRightNeighbor(s);
        drawNVerticalHexes(world, p, s, 5);
        
        p = p.topRightNeighbor(s);
        drawNVerticalHexes(world, p, s, 4);
        
        p = p.topRightNeighbor(s);
        drawNVerticalHexes(world, p, s, 3);
    }

    /**
     * Fills the given 2D array of tiles with RANDOM tiles.
     * @param tiles
     */
    public static void fillWithNothing(TETile[][] world) {
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }
    }

    /** Picks a RANDOM tile with a 33% change of being
     *  a wall, 33% chance of being a flower, and 33%
     *  chance of being empty space.
     */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(6);
        switch (tileNum) {
            case 0: return Tileset.PLAYER;
            case 1: return Tileset.WALL;
            case 2: return Tileset.FLOOR;
            case 3: return Tileset.GRASS;
            case 4: return Tileset.WATER;
            case 5: return Tileset.FLOWER;
            default: return Tileset.WALL;
        }
    }

    public static void main(String[] args) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        
        Position p = new Position(15, 15);
        int s = 3;

        fillWithNothing(world);
        drawTesselationOfHexagons(world, p, s);

        ter.renderFrame(world);
    }

    @Test
    public void testHexRowWidth() {
        assertEquals(3, hexRowWidth(3, 5));
        assertEquals(5, hexRowWidth(3, 4));
        assertEquals(7, hexRowWidth(3, 3));
        assertEquals(7, hexRowWidth(3, 2));
        assertEquals(5, hexRowWidth(3, 1));
        assertEquals(3, hexRowWidth(3, 0));
        assertEquals(2, hexRowWidth(2, 0));
        assertEquals(4, hexRowWidth(2, 1));
        assertEquals(4, hexRowWidth(2, 2));
        assertEquals(2, hexRowWidth(2, 3));
    }
    
    @Test
    public void testHexRowOffset() {
        assertEquals(0, hexRowOffset(3, 5));
        assertEquals(-1, hexRowOffset(3, 4));
        assertEquals(-2, hexRowOffset(3, 3));
        assertEquals(-2, hexRowOffset(3, 2));
        assertEquals(-1, hexRowOffset(3, 1));
        assertEquals(0, hexRowOffset(3, 0));
        assertEquals(0, hexRowOffset(2, 0));
        assertEquals(-1, hexRowOffset(2, 1));
        assertEquals(-1, hexRowOffset(2, 2));
        assertEquals(0, hexRowOffset(2, 3));
    }
}
