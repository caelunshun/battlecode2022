package OLDCODE.nav;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import OLDCODE.Robot;

/**
 * Implements the infamous bugnav algorithm.
 */
public class Bugger {
    Robot robot;
    RobotController rc;

    Direction facing;

    int passabilityThreshold = 24;

    public Bugger(Robot robot) {
        this.robot = robot;
        this.rc = robot.getRc();
    }

    public void advance(MapLocation target) throws GameActionException {
        if (facing == null) {
            facing = rc.getLocation().directionTo(target);
        }
        Direction dir = bug();
        if (rc.canMove(dir)) {
            rc.move(dir);
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

    public void reset() {
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
