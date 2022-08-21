package byog.lab5;

public class Position {
 
    public int x;
    public int y;
 
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Given a hex size, computes and returns the position of
     * the bottom right neighbor.
     * @param s the size of the square
     * @return Tthe position of the bottom right neighbor.
     */
    public Position bottomRightNeighbor(int s) {
        return new Position(this.x + 2 * s - 1, this.y - s);
    }

    /**
     * Given a hex size, computes and returns the position of
     * the top right neighbor.
     * @param s the size of the square
     * @return Tthe position of the top right neighbor.
     */
    public Position topRightNeighbor(int s) {
        return new Position(this.x + 2 * s - 1, this.y + s);
    }
    
}
