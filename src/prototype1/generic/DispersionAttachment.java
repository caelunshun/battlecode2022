package prototype1.generic;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;

public class DispersionAttachment extends Attachment {
    private double theta;

    public DispersionAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        disperse();
    }

    private void disperse() throws GameActionException {
        if (!rc.isMovementReady()) return;
        rc.setIndicatorString("Dispersing");
        int tries = 0;
        while (!checkCurrentDisperseTheta() && tries++ < 5) {
            resetDisperseTheta();
        }

        Direction dir = Util.getDirFromAngle(theta);
        if (rc.canMove(dir)) {
            rc.move(dir);
        }

        rc.setIndicatorString("Dispersing: Theta = " + Math.toDegrees(theta));
    }

    private boolean checkCurrentDisperseTheta() throws GameActionException {
        Direction dir = Util.getDirFromAngle(theta);
        if (!rc.canMove(dir)) {
            return false;
        }

        MapLocation loc = rc.getLocation().add(dir);
        if (rc.senseRubble(loc) >= 40) {
            return false;
        }

        // Ray-trace along the current disperse theta.
        // If we will hit the edge of the map, then stop.
        double dy = Math.sin(theta);
        double dx = Math.cos(theta);
        MapLocation edgeLoc = rc.getLocation().translate((int) (dx * 5), (int) (dy * 5));
        if (!Util.isOnTheMap(edgeLoc, rc)) {
            return false;
        }

        return true;
    }

    private void resetDisperseTheta() {
        theta = getRandomAngle();
    }

    private double getRandomAngle() {
        MapLocation diff = rc.getLocation().translate(-robot.getHomeArchon().x, -robot.getHomeArchon().y);
        double angleToArchon = Math.atan2(diff.y, diff.x);
        return angleToArchon + (robot.getRng().nextDouble() * 2 - 1) * Math.PI * 2 / 3;
    }
}
