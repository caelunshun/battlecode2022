package prototype1;

import battlecode.common.*;
import prototype1.comms.Communications;
import prototype1.comms.LeadCluster;

import java.util.ArrayList;
import java.util.List;

/**
 * The core robot class.
 *
 * We use composition to define robot behavior (rather than inheritance).
 * A robot contains a list of Attachments that can perform actions on each round.
 *
 * Multiple Attachments can be composed together to create compound behaviors. For
 * example, a robot can get one Attachment for movement and one for actions. The movement
 * attachment could be shared with other robot types, while the action attachment would
 * be specific to the robot type.
 */
public final class Robot {
    private RobotController rc;
    private List<Attachment> attachments = new ArrayList<>();
    private Communications comms;

    private List<MapLocation> friendlyArchons;
    private List<MapLocation> enemyArchons;
    private List<LeadCluster> leadClusters;

    public Robot(RobotController rc) {
        this.rc = rc;
        comms = new Communications(rc);
    }

    public RobotController getRc() {
        return rc;
    }

    public void run() {
        while(true) {
            try {
                rc.setIndicatorString("OK");
                update();
                doTurn();
            } catch (Exception e) {
                rc.setIndicatorString("ERROR - Exception");
                e.printStackTrace();;
            }
            Clock.yield();
        }
    }

    private void update() throws GameActionException {
        friendlyArchons = comms.readFriendlyArchons();
        enemyArchons = comms.readEnemyArchons();
        leadClusters = comms.readLeadClusters();
    }

    private void doTurn() throws GameActionException {
        for (Attachment attachment : attachments) {
            attachment.doTurn();
        }
    }

    public void addAttachment(Attachment attachment) {
        attachments.add(attachment);
    }

    public <T> T getAttachment(Class<T> type) {
        for (Attachment attachment : attachments) {
            if (attachment.getClass().equals(type)) {
                return (T) attachment;
            }
        }
        return null;
    }

    public List<MapLocation> getFriendlyArchons() {
        return friendlyArchons;
    }

    public List<MapLocation> getEnemyArchons() {
        return enemyArchons;
    }

    public List<LeadCluster> getLeadClusters() {
        return leadClusters;
    }

    public Communications getComms() {
        return comms;
    }

    public void moveRandom() throws GameActionException{
        Util.shuffle(Util.DIRECTIONS);
        for(Direction dir : Util.DIRECTIONS) {
            if(rc.canMove(dir)){
                rc.move(dir);
                break;
            }

        }
    }
}
