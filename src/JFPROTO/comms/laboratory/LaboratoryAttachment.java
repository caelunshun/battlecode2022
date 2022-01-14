package JFPROTO.comms.laboratory;

import JFPROTO.comms.Attachment;
import JFPROTO.comms.Robot;
import battlecode.common.GameActionException;

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