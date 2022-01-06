package prototype1.soldier;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

import java.util.Random;

public class SoldierAttachment extends Attachment {
    private final Navigator nav;
    private int waitingTime = 0;
    private MapLocation latticeLocation;
    private final boolean willRush;

    public SoldierAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);

      //  willRush = new Random(rc.getID()).nextFloat() < 0.7;
        willRush = false;
    }

    @Override
    public void doTurn() throws GameActionException {
        aggravate();
        if (willRush && robot.getComms().getRushingArchon() != null) {
            rush();
        } else {
            moveToLatticePosition();
        }
    }

    private void aggravate() throws GameActionException {
        for (RobotInfo enemy : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (enemy.location.distanceSquaredTo(rc.getLocation())
                > rc.getType().actionRadiusSquared) {
                nav.advanceToward(enemy.location);
                break;
            }
        }
    }

    public void moveToLatticePosition() throws GameActionException {
        if (rc.getLocation().equals(latticeLocation)) {
            rc.setIndicatorString("AT POSITION");
            return;

        }
        if (latticeLocation != null && !rc.canSenseRobotAtLocation(latticeLocation)) {
            nav.advanceToward(latticeLocation);
            waitingTime = 0;
            rc.setIndicatorString("MOVING TO LATTICE LOCATION" + latticeLocation);
            return;
        } else if(latticeLocation != null && rc.canSenseRobotAtLocation(latticeLocation)) {
            if(rc.senseRobotAtLocation(latticeLocation).getType() == RobotType.MINER){
                nav.advanceToward(latticeLocation);
                rc.setIndicatorString("MOVING TO LL EVEN WITH UNIT");
            } else{
                latticeLocation = findLatticePosition(rc.getType().visionRadiusSquared);
                rc.setIndicatorString("FOUND GOOD SPOT");
                if(latticeLocation == null){
                    nav.advanceToward(Util.getCenterLocation(rc));
                    rc.setIndicatorString("GOING TO CENTER");
                }
            }
        }
        else {
                latticeLocation = findLatticePosition(rc.getType().visionRadiusSquared);
            rc.setIndicatorString("FOUND GOOD SPOT");
                if(latticeLocation == null){
                    nav.advanceToward(Util.getCenterLocation(rc));
                    rc.setIndicatorString("GOING TO CENTER");
                }

            }
        }



    public MapLocation findLatticePosition(int sizeOfSearch) throws GameActionException {
        MapLocation[] locs = rc.getAllLocationsWithinRadiusSquared(robot.getRc().getLocation(), sizeOfSearch);
        for (MapLocation loc : locs) {
            if ((loc.x % 2) != (loc.y % 2)) {
                if (!rc.canSenseRobotAtLocation(loc) && robot.getHomeArchon().getLocation().distanceSquaredTo(loc) >= 5) {
                    return loc;
                }
            }
        }
        return null;
    }

    private void rush() throws GameActionException {
        MapLocation target = robot.getComms().getRushingArchon();
        nav.advanceToward(target);
    }
}