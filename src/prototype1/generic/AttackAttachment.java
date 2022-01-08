package prototype1.generic;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.nav.Navigator;
import java.util.*;

public class AttackAttachment extends Attachment {
    private Navigator nav;

    public AttackAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
       // advanceTowardEnemyArchon();
        lookForEnemy();
    }

    private void advanceTowardEnemyArchon() throws GameActionException {
        MapLocation closestArchon = null;
        for (MapLocation loc : robot.getEnemyArchons()) {
            if (closestArchon == null
                || closestArchon.distanceSquaredTo(rc.getLocation()) > loc.distanceSquaredTo(rc.getLocation())) {
                closestArchon = loc;
            }
        }

        if (closestArchon != null && closestArchon.distanceSquaredTo(rc.getLocation()) > 8) {
            nav.advanceToward(closestArchon);
        }
    }

    public boolean lookForEnemy() throws GameActionException {
        if (!rc.isActionReady()) {
            return false;
        }
        int bestLocation = 0;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        ArrayList<RobotType> robotTypes = new ArrayList<RobotType>();
        for(int i = 0; i < robots.length; i++){
            if(isFirstBetter(robots[i].getType(), robots[bestLocation].getType())){
                bestLocation = i;
            }
        }
        if(robots.length > 0 && rc.canAttack(robots[bestLocation].getLocation())){
            rc.attack(robots[bestLocation].getLocation());
            return true;
        }
        return false;

    }
    public static boolean isFirstBetter(RobotType first, RobotType second){
        //look at this later order is changing
        if(first == RobotType.SOLDIER){
            return true;
        }
        if(second == RobotType.SOLDIER){
            return false;
        }
        if(first == RobotType.ARCHON){
            return true;
        }
        if(second == RobotType.ARCHON){
            return false;
        }
        if(first == RobotType.WATCHTOWER){
            return true;
        }
        if(second == RobotType.WATCHTOWER){
            return false;
        }
        if(first == RobotType.SAGE){
            return true;
        }
        if(second == RobotType.SAGE){
            return false;
        }
        if(first == RobotType.MINER){
            return true;
        }
        if(second == RobotType.MINER){
            return false;
        }
        if(first == RobotType.LABORATORY){
            return true;
        }
        if(second == RobotType.LABORATORY){
            return false;
        }
        return true;

    }
}