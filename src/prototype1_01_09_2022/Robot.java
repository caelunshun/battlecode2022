package prototype1_01_09_2022;

import battlecode.common.*;
import prototype1_01_09_2022.comms.Communications;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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
    private List<Boolean> archonsInDanger = new ArrayList<>();

    private MapLocation homeArchon;

    private Random random;

    private boolean endTurn = false;

    public Robot(RobotController rc) throws GameActionException {
        this.rc = rc;
        this.random = new Random(rc.getID());
        comms = new Communications(rc);

        for(Direction dir : Util.DIRECTIONS){
            if(rc.canSenseRobotAtLocation(rc.getLocation().add(dir))){
                RobotInfo info = rc.senseRobotAtLocation(rc.getLocation().add(dir));
                if(info.getType() == RobotType.ARCHON && info.getTeam() == rc.getTeam()){
                    homeArchon = info.location;
                }
            }
        }
    }

    public void endTurn() {
        endTurn = true;
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

    public void update() throws GameActionException {
        friendlyArchons = comms.readFriendlyArchons();
        enemyArchons = comms.readEnemyArchons();
        archonsInDanger.clear();
        for (int i = 0; i < friendlyArchons.size(); i++) {
            archonsInDanger.add(comms.isArchonInDanger(i));
        }
    }

    private void doTurn() throws GameActionException {
        for (Attachment attachment : attachments) {
            attachment.doTurn();
            if (endTurn) {
                endTurn = false;
                return;
            }
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

    public boolean isArchonInDanger(MapLocation archon) {
        int i = friendlyArchons.indexOf(archon);
        return archonsInDanger.get(i);
    }

    public boolean isAnyArchonInDanger() {
        for (MapLocation arch : friendlyArchons) {
            if (isArchonInDanger(arch)) return true;
        }
        return false;
    }

    public MapLocation getHomeArchon(){
        return homeArchon;
    }

    public void setHomeArchon(MapLocation loc) {
        homeArchon = loc;
    }

    public Communications getComms() {
        return comms;
    }

    private Direction moveRandomDirection;

    public void moveRandom() throws GameActionException {
        if (!rc.isMovementReady()) return;
        while (moveRandomDirection == null || !rc.canMove(moveRandomDirection)) {
            moveRandomDirection = Util.DIRECTIONS[random.nextInt(Util.DIRECTIONS.length)];
        }

        if (rc.canMove(moveRandomDirection)) {
            rc.move(moveRandomDirection);
        }
    }

    public Random getRng() {
        return random;
    }
}
