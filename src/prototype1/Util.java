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

    public static Random getRng() {
        return rng;
    }

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

    public static Direction oppositeDirection(Direction dir){
        switch(dir){
            case NORTH:
                return Direction.SOUTH;
            case NORTHEAST:
                return Direction.SOUTHWEST;
            case EAST:
                return Direction.WEST;
            case SOUTHEAST:
                return Direction.NORTHWEST;
            case SOUTH:
                return Direction.NORTH;
            case SOUTHWEST:
                return Direction.NORTHEAST;
            case WEST:
                return Direction.EAST;
            case NORTHWEST:
                return Direction.SOUTHEAST;
            case CENTER:
                return Direction.CENTER;
        }
        return null;
    }
}
