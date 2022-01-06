package prototype1;

import battlecode.common.Direction;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.Collection;
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


    public static MapLocation getClosest(MapLocation center, Collection<MapLocation> locs) {
        MapLocation closest = null;
        for (MapLocation loc : locs) {
            if (closest == null || loc.distanceSquaredTo(center) < closest.distanceSquaredTo(center)) {
                closest = loc;
            }
        }
        return closest;
    }

    public static boolean isClockwise(Direction dir) {
        if (dir == Direction.NORTH || dir == Direction.NORTHEAST || dir == Direction.EAST) {
            return true;
        }
        return false;
    }

    public static boolean isOnTheMap(MapLocation loc, RobotController rc) {
        return loc.x >= 0 && loc.y >= 0
                && loc.x < rc.getMapWidth()
                && loc.y < rc.getMapHeight();
    }
    public static Direction bestPossibleDirection(Direction dir, RobotController rc){
        double min = 9.0;
        Direction best = Direction.CENTER;
        for(int i = 0; i < DIRECTIONS.length; i++){
            double a = getAngle(dir, DIRECTIONS[i]);
                if(a < min && rc.canMove(DIRECTIONS[i])){
                    min = a;
                    best = DIRECTIONS[i];
                }
            }
        return best;
    }
    public static double getAngle(Direction first, Direction second){
       return cmpAngles(Math.atan2(first.dy, first.dx), Math.atan2(second.dy, second.dx));
    }
    private static double cmpAngles(double a, double b) {
        a = normalizeAngle(a);
        b = normalizeAngle(b);

        if (b - a > Math.PI) {
            return (b - a) - Math.PI*2;
        } else if (b - a < -Math.PI) {
            return (b - a) + Math.PI*2;
        } else {
            return b - a;
        }
    }
    private static double normalizeAngle(double theta) {
        if (theta < 0) {
            theta = Math.PI * 2 + theta;
        }
        theta %= Math.PI * 2;
        return theta;
    }

}
