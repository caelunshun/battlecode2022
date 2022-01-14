package JFPROTO.comms.generic;

import JFPROTO.comms.Attachment;
import JFPROTO.comms.Robot;
import JFPROTO.comms.nav.Navigator;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;

import java.util.ArrayList;

public class AttackAttachment extends Attachment {
    private Navigator nav;

    public AttackAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!rc.isActionReady()) return;
        lookForEnemy();
    }

    public boolean lookForEnemy() throws GameActionException {
        if (!rc.isActionReady()) {
            return false;
        }
        int bestLocation = 0;
        RobotInfo[] robots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        ArrayList<RobotType> robotTypes = new ArrayList<RobotType>();
        for(int i = 0; i < robots.length; i++){
            if(isFirstBetter(robots[i], robots[bestLocation])){
                bestLocation = i;
            }
        }
        if(robots.length > 0 && rc.canAttack(robots[bestLocation].getLocation())){
            rc.attack(robots[bestLocation].getLocation());
            return true;
        }
        return false;

    }
    public static boolean isFirstBetter(RobotInfo first, RobotInfo second){
        if (first.type == second.type) {
            return first.health < second.health;
        }
        //look at this later order is changing
        if(first.type == RobotType.SOLDIER){
            return true;
        }
        if(second.type == RobotType.SOLDIER){
            return false;
        }
        if(first.type == RobotType.ARCHON){
            return true;
        }
        if(second.type == RobotType.ARCHON){
            return false;
        }
        if(first.type == RobotType.WATCHTOWER){
            return true;
        }
        if(second.type == RobotType.WATCHTOWER){
            return false;
        }
        if(first.type == RobotType.SAGE){
            return true;
        }
        if(second.type == RobotType.SAGE){
            return false;
        }
        if(first.type == RobotType.MINER){
            return true;
        }
        if(second.type == RobotType.MINER){
            return false;
        }
        if(first.type == RobotType.LABORATORY){
            return true;
        }
        if(second.type == RobotType.LABORATORY){
            return false;
        }

        return true;
    }
}