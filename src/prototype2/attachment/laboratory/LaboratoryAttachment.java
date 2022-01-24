package prototype2.attachment.laboratory;

import battlecode.common.GameActionException;
import prototype2.Attachment;
import prototype2.Robot;

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

    public boolean moreGold() throws GameActionException {
        if(robot.getComms().canMakeGold()){
            return true;
        }
        rc.setIndicatorString("I was told not to");
        return false;
    }
}