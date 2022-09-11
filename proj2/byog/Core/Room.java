package byog.Core;

public class Room extends StageBuilder {
    public Position pos;
    public int w;
    public int h;

    public Room(Position pos, int h, int w) {
        this.pos = pos;
        this.h = h;
        this.w = w;
    }

    public boolean isOverlapTo(Room other) {
        return (Math.min(this.pos.x + this.w, other.pos.x + other.w) >= Math.max(this.pos.x, other.pos.x))
                && (Math.min(this.pos.y + this.h, other.pos.y + other.h) >= Math.max(this.pos.y, other.pos.y));
    }
}
