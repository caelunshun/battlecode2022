package JFPROTO.comms.watchtower;

import JFPROTO.comms.Attachment;
import JFPROTO.comms.Robot;
import JFPROTO.comms.Util;
import JFPROTO.comms.nav.Navigator;
import battlecode.common.*;

public class WatchtowerAttachment extends Attachment {
    private boolean isRushTower;
    private MapLocation rushArchon;
    private Navigator nav;

    public WatchtowerAttachment(Robot robot) throws GameActionException {
        super(robot);
        for (Direction dir : Util.DIRECTIONS) {
            MapLocation loc = rc.getLocation().add(dir);
            if (isEnemyArchon(loc)) {
                isRushTower = true;
                rushArchon = loc;
            }
        }
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (isRushTower && !isEnemyArchon(rushArchon) && !robot.getEnemyArchons().isEmpty()) {
            rushArchon = robot.getEnemyArchons().get(0);
        }

        if (rushArchon != null) {
            if (!rc.getLocation().isAdjacentTo(rushArchon)) {
                if (rc.getMode() != RobotMode.PORTABLE && rc.canTransform()) {
                    rc.transform();
                }
                nav.advanceToward(rushArchon);
            } else if (rc.getMode() == RobotMode.PORTABLE && rc.canTransform()) {
                rc.transform();
            }
        }
    }

    private boolean isEnemyArchon(MapLocation loc) throws GameActionException {
        if (loc == null) return false;
        if (!rc.canSenseLocation(loc)) return false;
        RobotInfo info = rc.senseRobotAtLocation(loc);
        if (info == null) return false;
        return info.type == RobotType.ARCHON && info.team == rc.getTeam().opponent();
    }

}
