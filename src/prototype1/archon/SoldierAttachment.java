package prototype1.archon;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

public class SoldierAttachment extends Attachment {
    private final Navigator nav;
    public SoldierAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if(lookForEnemy()){
            return;
        }
        moveRandom();

    }
    public boolean lookForEnemy() throws GameActionException{
        if(!rc.isActionReady()){
            return false;
        }
        MapLocation[] locations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared);
        //looks for archons first
        //canbeoptimized find locations on first loop through
        for(MapLocation location : locations){
            if(rc.canSenseRobotAtLocation(location)){
                if(rc.senseRobotAtLocation(location).getType() == RobotType.ARCHON && rc.senseRobotAtLocation(location).getTeam() != rc.getTeam()){
                    if(rc.canAttack(location)){
                        rc.attack(location);
                        return true;
                    }
                }
            }
        }
        for(MapLocation location : locations){
            if(rc.canSenseRobotAtLocation(location)){
                if(rc.senseRobotAtLocation(location).getTeam() != rc.getTeam()){
                    if(rc.canAttack(location)){
                        rc.attack(location);
                        return true;
                    }
                }
            }
        }
        return false;
    }
    public void moveRandom() throws GameActionException{
        for(Direction dir : Util.DIRECTIONS) {
            if(rc.canMove(dir)){
                rc.move(dir);
                break;
            }

        }
    }
}