package prototype1_01_14_2022.nav;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import prototype1_01_14_2022.Robot;

/**
 * Macro-level navigator with bugnav.
 */
public final class BugNavigator {
    Robot robot;
    RobotController rc;

    MapLocation currentTarget;
    boolean bugging;
    Line sourceToTarget;
    Direction facing;

    int passabilityThreshold = 24;

    public BugNavigator(Robot robot) {
        this.robot = robot;
        this.rc = robot.getRc();
    }

    public void advanceToward(MapLocation target) throws GameActionException {
        if (currentTarget == null) {
            reset(target);
        }

        currentTarget = target;
        Direction dir = pathfind();
        if (robot.getRc().canMove(dir)) {
            robot.getRc().move(dir);
        }
    }

    private Direction pathfind() throws GameActionException {
        Direction naive = rc.getLocation().directionTo(currentTarget);
        if (!canMove(naive))  {
            if (!bugging) facing = naive;
            bugging = true;
        }
        if (!bugging) {
            return naive;
        } else {
            if (!sourceToTarget.isEndpoint(rc.getLocation())
                    && sourceToTarget.intersectsTile(rc.getLocation())
                    && canMove(naive)) {
                bugging = false;
                return naive;
            } else {
                return bug();
            }
        }
    }

    private Direction bug() throws GameActionException {
        fixDiagonals();

        // first, try to turn left...
        Direction temp = facing.rotateLeft().rotateLeft();
        Direction move;
        if (canMove(temp) && !canMove(temp.rotateLeft())) {
            facing = temp;
            move = facing;
        } else if (canMove(facing.rotateLeft()) && !canMove(temp)) {
            move = facing.rotateLeft();
            facing = temp;
        } else if (!canMove(facing)) {
            // turn right to keep left hand on wall...
            Direction dir = facing.rotateRight();
            while (!canMove(dir)) dir = dir.rotateRight();
            facing = dir;
            move = dir;
        } else {
            move = facing;
        }

        return move;
    }

    private void fixDiagonals() {
        switch (facing) {
            case NORTHEAST:
                facing = Direction.NORTH;
                break;
            case NORTHWEST:
                facing = Direction.WEST;
                break;
            case SOUTHWEST:
                facing = Direction.SOUTH;
                break;
            case SOUTHEAST:
                facing = Direction.EAST;
                break;
        }
    }

    public void reset(MapLocation target) {
        currentTarget = target;
        bugging = false;
        sourceToTarget = new Line(rc.getLocation(), target);
        facing = null;
    }

    private boolean canMove(Direction dir) throws GameActionException {
        MapLocation loc = rc.getLocation().add(dir);
        return
                rc.onTheMap(loc)
                        && rc.senseRobotAtLocation(loc) == null
                        && rc.senseRubble(loc) <= passabilityThreshold;
    }
}