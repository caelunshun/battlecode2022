package prototype1_01_12_2022;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import prototype1_01_12_2022.generic.SymmetryType;

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

    public static Direction oppositeDirection(Direction dir) {
        switch (dir) {
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

    public static Direction bestPossibleDirection(Direction dir, RobotController rc) {
        double min = 9.0;
        Direction best = Direction.CENTER;
        for (int i = 0; i < DIRECTIONS.length; i++) {
            double a = getAngle(dir, DIRECTIONS[i]);
            if (a < min && rc.canMove(DIRECTIONS[i])) {
                min = a;
                best = DIRECTIONS[i];
            }
        }
        return best;
    }

    public static double getAngle(Direction first, Direction second) {
        return Math.abs(cmpAngles(Math.atan2(first.dy, first.dx), Math.atan2(second.dy, second.dx)));
    }

    public static double cmpAngles(double a, double b) {
        a = normalizeAngle(a);
        b = normalizeAngle(b);

        if (b - a > Math.PI) {
            return (b - a) - Math.PI * 2;
        } else if (b - a < -Math.PI) {
            return (b - a) + Math.PI * 2;
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

    public static MapLocation getCenterLocation(RobotController rc) {
        return new MapLocation(rc.getMapHeight() / 2, rc.getMapWidth() / 2);
    }

    public static MapLocation getReflectedLocation(RobotController rc, Robot robot) throws GameActionException {
        robot.getHomeArchon();
        SymmetryType symm = robot.getComms().getSymmetryType();
        if (symm == null) {
            return SymmetryType.ROTATIONAL.getSymmetryLocation(robot.getHomeArchon(), rc);
        }
        return symm.getSymmetryLocation(robot.getHomeArchon(), rc);
    }

    public static double getAngleFromVec(MapLocation vec) {
        return Math.atan2(vec.y, vec.x);
    }

    public static Direction getDirFromAngle(double angle) {
        angle = normalizeAngle(angle);
        if (angle > Math.PI / 8 && angle <= (3 * Math.PI / 8)) {
            return Direction.NORTHEAST;
        }
        if (angle > (3 * Math.PI / 8) && angle <= (5 * Math.PI / 8)) {
            return Direction.NORTH;
        }
        if (angle > (5 * Math.PI / 8) && angle <= (7 * Math.PI / 8)) {
            return Direction.NORTHWEST;
        }
        if (angle > (7 * Math.PI / 8) && angle <= (9 * Math.PI / 8)) {
            return Direction.WEST;
        }
        if (angle > (9 * Math.PI / 8) && angle <= (11 * Math.PI / 8)) {
            return Direction.SOUTHWEST;
        }
        if (angle > (11 * Math.PI / 8) && angle <= (13 * Math.PI / 8)) {
            return Direction.SOUTH;
        }
        if (angle > (13 * Math.PI / 8) && angle <= (15 * Math.PI / 8)) {
            return Direction.SOUTHEAST;
        }
        return Direction.EAST;
    }

    public static Direction getDirectionFromAngle(double theta) {
        Direction best = null;
        for (Direction dir : DIRECTIONS) {
            if (best == null || cmpAngles(getAngle(dir, Direction.EAST), theta) < cmpAngles(getAngle(best, Direction.EAST), theta)) {
                best = dir;
            }
        }
        return best;
    }
}
