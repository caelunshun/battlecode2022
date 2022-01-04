package prototype1.archon;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;

public class SoldierAttachment extends Attachment {
    public SoldierAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        lookForEnemy();
    }
    public void lookForEnemy() throws GameActionException{
        if(!rc.isActionReady()){
            return;
        }
        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
        //looks for archons first
        //canbeoptimized find locations on first loop through
        for(MapLocation location : locations){
            if(rc.canSenseRobotAtLocation(location)){
                if(rc.senseRobotAtLocation(location).getType() == RobotType.ARCHON && rc.senseRobotAtLocation(location).getTeam() != rc.getTeam()){
                    if(rc.canAttack(location)){
                        rc.attack(location);
                    }
                }
            }
        }
        for(MapLocation location : locations){
            if(rc.canSenseRobotAtLocation(location)){
                if(rc.senseRobotAtLocation(location).getTeam() != rc.getTeam()){
                    if(rc.canAttack(location)){
                        rc.attack(location);
                        break;
                    }
                }
            }
        }
    }
}