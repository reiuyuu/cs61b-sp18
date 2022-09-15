package byog.Core;

import byog.TileEngine.TETile;

public class Room extends StageBuilder {
    private Position pos;
    private int w;
    private int h;
    
    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public Room(Position pos, int h, int w) {
        this.pos = pos;
        this.h = h;
        this.w = w;
    }

    public boolean isOverlapTo(Room other) {
        return (Math.min(this.pos.getX() + this.w, other.pos.getX() + other.w)
                >= Math.max(this.pos.getX(), other.pos.getX()))
                && (Math.min(this.pos.getY() + this.h, other.pos.getY() + other.h)
                >= Math.max(this.pos.getY(), other.pos.getY()));
    }

    public Room fixOutOfBounds(TETile[][] map) {
        if (this.pos.getX() + this.w > map.length - 1)
            this.pos.setX(map.length - 1 - this.pos.getX());
        if (this.pos.getY() + this.h > map[0].length - 1)
            this.pos.setY(map[0].length - 1 - this.pos.getY());

        return this;
    }
}
