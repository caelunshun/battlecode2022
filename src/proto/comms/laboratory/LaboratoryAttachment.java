package proto.comms.laboratory;

import battlecode.common.GameActionException;
import proto.comms.Attachment;
import proto.comms.Robot;

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