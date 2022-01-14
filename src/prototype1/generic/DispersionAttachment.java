package prototype1.generic;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.nav.BugNavigator;
import prototype1.nav.Navigator;

public class DispersionAttachment extends Attachment {
    private double theta;
    private BugNavigator bugnav;

    public DispersionAttachment(Robot robot) {
        super(robot);
        this.bugnav = new BugNavigator(robot);
        theta = getRandomAngle();
    }

    @Override
    public void doTurn() throws GameActionException {
        disperse();
    }

    private void disperse() throws GameActionException {
        if (!rc.isMovementReady()) return;
        int tries = 0;
        while (!checkCurrentDisperseTheta() && tries++ < 5) {
            resetDisperseTheta();
        }

        int dx = (int) (10 * Math.cos(theta));
        int dy = (int) (10 * Math.sin(theta));
        bugnav.advanceToward(rc.getLocation().translate(dx, dy));

        rc.setIndicatorString("Dispersing: Theta = " + Math.toDegrees(theta));
    }

    private boolean checkCurrentDisperseTheta() throws GameActionException {
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
        return robot.getRng().nextFloat() * Math.PI * 2;
    }

    /*private BugNavigator bugnav;
    private Navigator nav;
    private double theta;

    public DispersionAttachment(Robot robot) {
        super(robot);
        this.bugnav = new BugNavigator(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        disperse();
        rc.setIndicatorString("Dispersing: Target = " + target);
    }

    private void disperse() throws GameActionException {
        if (!rc.isMovementReady()) return;
        int tries = 0;
        while (!checkCurrentTarget() && tries++ < 6) {
            resetTarget();
        }

            nav.advanceToward(target);
            ++targetTurns;
            if (targetTurns >= 30) {
                target = null;
            }
    }

    private boolean checkCurrentTarget() throws GameActionException {
        Direction dir = rc.getLocation().directionTo(target);
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (info.type.canAttack()) {
                double angle = Math.atan2(info.location.y - rc.getLocation().y, info.location.x - rc.getLocation().x);
                if (Util.cmpAngles(Math.atan2(dir.dy, dir.dx), angle) <= Math.PI / 3) {
                    return false;
                }
            }
        }

        return true;
    }

    private void resetTarget() {
        target = getRandomLocation();
        targetTurns = 0;
    }

    private MapLocation getRandomLocation() {
        int radiusX = rc.getMapWidth() / 3;
        int radiusY = rc.getMapHeight() / 3;
        double theta = robot.getRng().nextFloat() * Math.PI * 2;
        int x = (int) (radiusX * Math.cos(theta));
        int y = (int) (radiusY * Math.sin(theta));
        return rc.getLocation().translate(x, y);
    }*/
}
