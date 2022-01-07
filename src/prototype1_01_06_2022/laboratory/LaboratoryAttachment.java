package prototype1_01_06_2022.laboratory;

import battlecode.common.GameActionException;
import prototype1_01_06_2022.Attachment;
import prototype1_01_06_2022.Robot;

public class LaboratoryAttachment extends Attachment {
    public LaboratoryAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        createGold();
    }
    public void createGold() throws GameActionException{
        if(rc.canTransmute() && moreGold()){
            rc.transmute();
        }
    }
    public boolean moreGold() throws GameActionException{
        //Probably change this eventually once we understand more
        if(rc.getTeamLeadAmount(rc.getTeam()) < 100){
            return false;
        }
        return true;
    }
}