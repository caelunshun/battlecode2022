package prototype2.attachment.miner;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype2.Attachment;
import prototype2.Robot;
import prototype2.Util;
import prototype2.comms.CryForHelp;
import prototype2.nav.Bugger;
import prototype2.nav.Navigator;

public class MinerDispersionAttachment extends Attachment {
    private Double angle;
    private MapLocation origin;
    private Bugger bugger;
    private Navigator nav;
    private boolean isBugging = false;
    private MapLocation bugOrigin;
    private int bugTurns = 0;

    public MinerDispersionAttachment(Robot robot) {
        super(robot);
        origin = rc.getLocation();
        bugger = new Bugger(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (angle == null) {
            int index = robot.getHomeArchonIndex();
            angle = robot.getComms().getDispersionAngles()[index];
            robot.getComms().setDispersionAngle(index, null);
        }

        if (angle == null) {
            robot.moveRandom();
            //rc.setIndicatorString("Missing Angle");
            return;
        }

        if (isBugging && rc.getLocation().equals(bugOrigin) && bugTurns >= 3) {
            isBugging = false;
        }

        if (!isBugging) {
            while (!isAngleStillValid()) {
                origin = rc.getLocation();
                angle = robot.getRng().nextDouble() * Math.PI * 2;
                isBugging = false;
            }
        }

        //rc.setIndicatorString("Angle: " + Math.toDegrees(angle));

        if (!rc.isMovementReady()) return;

        double dist = Math.sqrt(rc.getLocation().distanceSquaredTo(origin));
        dist += 3;
        MapLocation target = origin.translate((int) (dist * Math.cos(angle)), (int) (dist * Math.sin(angle)));

        Direction dir = rc.getLocation().directionTo(target);
        if (!Util.isOnTheMap(rc.getLocation().add(dir), rc) || rc.senseRubble(rc.getLocation().add(dir)) >= 25) {
            origin = rc.getLocation();
            angle = robot.getRng().nextDouble() * Math.PI * 2;
            isBugging = false;
        }

        if (isBugging) {
            bugger.advance(target);
            //rc.setIndicatorString("Bugging Toward  " + target);
            ++bugTurns;
        } else {
            nav.advanceToward(target);
            //rc.setIndicatorString("Dispersing Toward " + target);
        }
    }

    private boolean isAngleStillValid() throws GameActionException {
        double dist = Math.sqrt(rc.getLocation().distanceSquaredTo(origin));
        double targetDist = dist + 3;
        MapLocation target = origin.translate((int) (targetDist * Math.cos(angle)), (int) (targetDist * Math.sin(angle)));

        for (CryForHelp cry : robot.getComms().getCriesForHelp()) {
            if (cry == null) continue;
            if (cry.enemyLoc.distanceSquaredTo(target) <= 8) {
                return false;
            }
        }

        return true;
    }
}
