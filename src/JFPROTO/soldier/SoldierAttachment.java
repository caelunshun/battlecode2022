package JFPROTO.soldier;

import JFPROTO.Attachment;
import JFPROTO.Robot;
import JFPROTO.Util;
import JFPROTO.nav.Navigator;
import battlecode.common.*;

import java.util.Random;

public class SoldierAttachment extends Attachment {
    private final Navigator nav;
    private int waitingTime = 0;
    private MapLocation latticeLocation;
    private final boolean willRush;
    private final boolean willDefendOtherArchon;
    private MapLocation rushingArchon;
    private MapLocation helpingArchon;

    public SoldierAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);

        Random random = new Random(rc.getID());
        willRush = random.nextFloat() < 0.7;
        willDefendOtherArchon = random.nextFloat() < 0.6;
    }

    @Override
    public void doTurn() throws GameActionException {
        if (helpingArchon != null && !robot.getFriendlyArchons().contains(helpingArchon)) {
            helpingArchon = null;
        }

        if (helpingArchon != null && rc.getLocation().distanceSquaredTo(helpingArchon) > 40) {
            nav.advanceToward(helpingArchon);
        }

        aggravate();
        if (rushingArchon == null) {
            rushingArchon = robot.getComms().getRushingArchon();
        } else if (!robot.getEnemyArchons().contains(rushingArchon)) {
            rushingArchon = null;
        }
        if (willRush && rushingArchon != null ) {
            attackMicro();
        } else if (willDefendOtherArchon && robot.isAnyArchonInDanger()) {
            defendOtherArchon();
        } else if (rc.getRoundNum() > 400) {
            moveToLatticePosition();
            robot.endTurn();
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
        if (rc.getLocation().distanceSquaredTo(robot.getHomeArchon()) > 15 * 15) {
            nav.advanceToward(robot.getHomeArchon());
            return;
        }

        if (rc.getLocation().equals(latticeLocation)) {
            rc.setIndicatorString("AT POSITION");
            return;

        }
        if (latticeLocation != null && !rc.canSenseRobotAtLocation(latticeLocation)) {
            nav.advanceToward(latticeLocation);
            waitingTime = 0;
            rc.setIndicatorString("MOVING TO LATTICE LOCATION" + latticeLocation);
            return;
        } else if (latticeLocation != null && rc.canSenseRobotAtLocation(latticeLocation)) {
            if (rc.senseRobotAtLocation(latticeLocation).getType() == RobotType.MINER) {
                nav.advanceToward(latticeLocation);
                rc.setIndicatorString("MOVING TO LL EVEN WITH UNIT");
            } else {
                latticeLocation = findLatticePosition(rc.getType().visionRadiusSquared);
                rc.setIndicatorString("FOUND GOOD SPOT");
                if (latticeLocation == null) {
                    nav.advanceToward(Util.getReflectedLocation(rc, robot));
                }
            }
        } else {
            latticeLocation = findLatticePosition(rc.getType().visionRadiusSquared);
            rc.setIndicatorString("FOUND GOOD SPOT");
            if (latticeLocation == null) {
                nav.advanceToward(Util.getReflectedLocation(rc, robot));
            }

        }
    }

    public MapLocation findLatticePosition(int sizeOfSearch) throws GameActionException {
        MapLocation[] locs = rc.getAllLocationsWithinRadiusSquared(robot.getRc().getLocation(), sizeOfSearch);
        for (MapLocation loc : locs) {
            if ((loc.x % 2) != (loc.y % 2)) {
                if (!rc.canSenseRobotAtLocation(loc) && robot.getHomeArchon().distanceSquaredTo(loc) >= 5) {
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
            helpingArchon = target;
        }
    }

    public void attackMicro() throws GameActionException {
        int enemyStrength = 0;
        int teamStrength = 0;
        RobotInfo[] robs = rc.senseNearbyRobots(rc.getType().visionRadiusSquared);
        for(int i = 0; i < robs.length; i++){
            RobotInfo thisRob = robs[i];
            if(thisRob.getTeam() == rc.getTeam() && thisRob.getType() == RobotType.SOLDIER){
                teamStrength += thisRob.getHealth();
            } else if(thisRob.getTeam() != rc.getTeam() && thisRob.getType().canAttack() ){
                enemyStrength += thisRob.getHealth();
            }
        }
        if ((teamStrength + rc.getHealth() - enemyStrength) < 0){
            retreat(robs);
            rc.setIndicatorString("RETREAT");
        } else {
            rush();
            rc.setIndicatorString("RUSH");
        }
    }


    public void retreat(RobotInfo[] robots) throws GameActionException{
        int xVec = 0;
        int yVec = 0;
        for(int i = 0; i < robots.length; i++){
           RobotInfo rob = robots[i];
           if(rob.getTeam() != rc.getTeam()){
               MapLocation vec = getVec(rob);
               xVec += vec.x;
               yVec += vec.y;
           }
        }
        Direction dir  = Util.bestPossibleDirection(Util.getDirFromAngle(Util.getAngleFromVec(new MapLocation(xVec, yVec))), rc);
        if(rc.canMove(dir)){
            rc.move(dir);
        }
    }
    public MapLocation getVec(RobotInfo enemy){
        MapLocation enemyLocation = enemy.getLocation();
        MapLocation myLocation = rc.getLocation();
        return new MapLocation(enemyLocation.x - myLocation.x, enemyLocation.y - myLocation.y);
    }

}