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
    private final boolean willDefendOtherArchon;
    private MapLocation rushingArchon;

    public SoldierAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);

        Random random = new Random(rc.getID());
        willRush = random.nextFloat() < 0.7;
        willDefendOtherArchon = random.nextFloat() < 0.5;
    }

    @Override
    public void doTurn() throws GameActionException {
        aggravate();
        if (rushingArchon == null) {
            rushingArchon = robot.getComms().getRushingArchon();
        } else if (!robot.getEnemyArchons().contains(rushingArchon)) {
            rushingArchon = null;
        }
        if (willRush && rushingArchon != null) {
            rush();
        } else if (willDefendOtherArchon && robot.isAnyArchonInDanger()) {
            defendOtherArchon();
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
                    nav.advanceToward(Util.getReflectedLocation(rc, robot));
                }
            }
        }
        else {
                latticeLocation = findLatticePosition(rc.getType().visionRadiusSquared);
            rc.setIndicatorString("FOUND GOOD SPOT");
                if(latticeLocation == null){
                    nav.advanceToward(Util.getReflectedLocation(rc, robot));
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
        nav.advanceToward(rushingArchon);
    }

    private void defendOtherArchon() throws GameActionException {
        MapLocation target = null;
        for (MapLocation loc : robot.getFriendlyArchons()) {
            if (robot.isArchonInDanger(loc)) {
                if (target == null || rc.getLocation().distanceSquaredTo(loc)
                    < rc.getLocation().distanceSquaredTo(target)) {
                    target = loc;
                }
            }
        }

        if (target != null) {
            nav.advanceToward(target);
        }
    }
}