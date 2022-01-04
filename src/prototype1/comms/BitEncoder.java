package prototype1.comms;

import battlecode.common.MapLocation;

/**
 * Used to encode data into bits for the shared array.
 */
public final class BitEncoder {
    private int result = 0;
    private int cursor = 0;

    public void writeMapLocation(MapLocation loc) {
        write(loc.x, 6);
        write(loc.y, 6);
    }

    public void writeBoolean(boolean b) {
        write(b ? 1 : 0, 1);
    }

    public void write(int x, int bits) {
        if (cursor + bits > 32) {
            throw new RuntimeException("too many bits in message");
        }
        result |= x << cursor;
        cursor += bits;
    }

    public int finish() {
        return result;
    }
}
