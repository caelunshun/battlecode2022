package prototype1_01_09_2022.comms;

import battlecode.common.MapLocation;

/**
 * Used to decode data from bits for the shared array.
 */
public class BitDecoder {
    private final int value;
    private int cursor = 0;

    public BitDecoder(int value) {
        this.value = value;
    }

    public boolean readBool() {
        return read(1) != 0;
    }

    public MapLocation readMapLocation() {
        int x = read(6);
        int y = read(6);
        return new MapLocation(x, y);
    }

    public int read(int bits) {
        int res = (value >>> cursor) & ((1 << bits) - 1);
        cursor += bits;
        return res;
    }
}
