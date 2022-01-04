package prototype1.archon;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.Navigator;

public class BuilderAttachment extends Attachment {
    private final Navigator nav;

    public BuilderAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
        if(wantsToBuildLab()){
            //find a better way to choose location of this.
           nav.advanceToward(chooseLocation());
        }
    }
    public boolean wantsToBuildLab(){
        if(rc.getTeamLeadAmount(rc.getTeam()) > 2400){
            return true;
        }
        return false;
    }
    public MapLocation chooseLocation(){
        return rc.getLocation();
    }
}