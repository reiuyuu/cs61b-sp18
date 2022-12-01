package byog.Core;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import edu.princeton.cs.introcs.StdDraw;

import byog.TileEngine.TERenderer;
import byog.TileEngine.TETile;

public class Game {
    public static final int WIDTH = 75;
    public static final int HEIGHT = 31;
    public static final int XOFFSET = 0;
    public static final int YOFFSET = 4;
    
    /**
     * Method used for playing a fresh game. The game should start from the main menu.
     */
    public void playWithKeyboard() {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH + XOFFSET, HEIGHT + YOFFSET, 0, 0);
        MapGenerator mg = null;
        
        drawMenuFrame("", "");
        switch (solicitNLQ()) {
            case 'N':
                mg = new MapGenerator(WIDTH, HEIGHT, getSeed());
                break;
            case 'L':
                mg = loadMap();
                break;
            case 'Q':
                System.exit(0);
                break;
            default:
        }

        while (true) {
            if (mg.checkStatus() == 1) drawWin();

            while (!StdDraw.hasNextKeyTyped()) {
                ter.renderFrame(mg.getMap());
                drawDescription(mg.getMap());
            }
            
            char d = solicitInput();
            if (d == ':' && solicitInput() == 'Q') {
                saveMap(mg);
                System.exit(0);
            }
            mg.move(d);
        }
    }

    /**
     * Method used for autograding and testing the game code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The game should
     * behave exactly as if the user typed these characters into the game after playing
     * playWithKeyboard. If the string ends in ":q", the same world should be returned as if the
     * string did not end with q. For example "n123sss" and "n123sss:q" should return the same
     * world. However, the behavior is slightly different. After playing with "n123sss:q", the game
     * should save, and thus if we then called playWithInputString with the string "l", we'd expect
     * to get the exact same world back again, since this corresponds to loading the saved game.
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] playWithInputString(String input) {
        // Run the game using the input passed in,
        // and return a 2D tile representation of the world that would have been
        // drawn if the same inputs had been given to playWithKeyboard().
        MapGenerator mg = null;
        String s = null;
        
        input = input.toUpperCase();
        switch (input.charAt(0)) {
            case 'N':
                mg = new MapGenerator(WIDTH, HEIGHT, getSeed(input));
                s = input.substring(input.indexOf('S') + 1);
                break;
            case 'L':
                mg = loadMap();
                s = input.substring(input.indexOf('L') + 1);
                break;
            case 'Q':
                System.exit(0);
                break;
            default:
        }
        
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == ':' && (s.charAt(i + 1) == 'Q')) {
                saveMap(mg);
                break;
            }
            mg.move(s.charAt(i));
        }

        return mg.getMap();
    }

    private void saveMap(MapGenerator mg) {
        File f = new File("./world.ser");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream fs = new FileOutputStream(f);
            ObjectOutputStream os = new ObjectOutputStream(fs);
            os.writeObject(mg);
            os.close();
        } catch (FileNotFoundException e) {
            System.out.println("file not found");
            System.exit(0);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(0);
        }
    }

    private MapGenerator loadMap() {
        MapGenerator mg = null;
        File f = new File("./world.ser");
        if (f.exists()) {
            try {
                FileInputStream fs = new FileInputStream(f);
                ObjectInputStream os = new ObjectInputStream(fs);
                mg = (MapGenerator) os.readObject();
                os.close();
            } catch (FileNotFoundException e) {
                System.out.println("file not found");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e);
                System.exit(0);
            } catch (ClassNotFoundException e) {
                System.out.println("class not found");
                System.exit(0);
            }
        }

        return mg;
    }

    private char solicitInput() {
        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                return Character.toUpperCase(StdDraw.nextKeyTyped());
            }
        }
    }
    
    private char solicitNLQ() {
        while (true) {
            char c = solicitInput();
            if ((c  == 'N' || c == 'L' || c == 'Q')) return c;
        }
    }
    
    private char solicitSeed() {
        while (true) {
            char c = solicitInput();
            if (Character.isDigit(c) || c == 'S') return c;
        }
    }

    private long getSeed() {
        String seed = "";
        drawMenuFrame("Enter Seed", seed);
        
        while (true) {
            char c = solicitSeed();
            if (c == 'S') break;
            seed += String.valueOf(c);
            drawMenuFrame("Enter Seed", seed);
        }
        StdDraw.pause(500);
        
        return Long.parseLong(seed);
    }

    private long getSeed(String input) {
        return Long.parseLong(input.substring(1, input.indexOf('S')));
    }

    private void drawMenuFrame(String prompt, String seed) {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.clear(Color.BLACK);
        
        Font mediumFont = new Font("Monaco", Font.BOLD, 48);
        StdDraw.setFont(mediumFont);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(midWidth, midHeight / 8 * 24, "CS61BYoG");
        
        Font smallFont = new Font("Monaco", Font.BOLD, 24);
        StdDraw.setFont(smallFont);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(midWidth, midHeight + 4, "New Game (N)");
        StdDraw.text(midWidth, midHeight + 2, "Load Game (L)");
        StdDraw.text(midWidth, midHeight, "Quit (Q)");
        
        StdDraw.text(midWidth, midHeight - 4, prompt);
        StdDraw.text(midWidth, midHeight - 6, seed);

        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }

    private void drawDescription(TETile[][] map) {
        int x = (int) StdDraw.mouseX();
        int y = (int) StdDraw.mouseY();

        if (!new Position(x, y).isInBounds(map)) return;
        
        Font smallFont = new Font("Monaco", Font.BOLD, 24);
        StdDraw.setFont(smallFont);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(6, HEIGHT + 2, map[x][y].description());

        StdDraw.enableDoubleBuffering();
        StdDraw.show();
    }

    private void drawWin() {
        int midWidth = WIDTH / 2;
        int midHeight = HEIGHT / 2;

        StdDraw.pause(500);

        Font largeFont = new Font("Monaco", Font.BOLD, 64);
        StdDraw.setFont(largeFont);
        StdDraw.setPenColor(Color.WHITE);
        StdDraw.text(midWidth, midHeight, "YOU WIN");
        
        StdDraw.enableDoubleBuffering();
        StdDraw.show();

        solicitInput();
        System.exit(0);
    }
}
