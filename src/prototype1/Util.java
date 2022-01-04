package prototype1;

import battlecode.common.Direction;

import java.util.Random;

/**
 * Assorted utilities.
 */
public class Util {
    public static final Direction[] DIRECTIONS = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    private final static Random rng = new Random(63125);

    /**
     * The Fisher-Yates shuffle
     */
    public static <T> void shuffle(T[] array) {
        for (int i = 0; i < array.length; i++) {
            int j = rng.nextInt(array.length);
            T temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
    }
}
