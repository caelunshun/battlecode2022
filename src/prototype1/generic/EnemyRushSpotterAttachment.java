package prototype1.generic;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import prototype1.Attachment;
import prototype1.Robot;

public class EnemyRushSpotterAttachment extends Attachment {
    public EnemyRushSpotterAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        int enemyUnits = 0;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (info.type.canAttack()) {
                ++enemyUnits;
            }
        }

        for (MapLocation enemyArchon : robot.getEnemyArchons()) {
            if (rc.getLocation().distanceSquaredTo(enemyArchon) <= 36) {
                return;
            }
        }

        if (enemyUnits > 6) {
            robot.getComms().addSpottedDanger(rc.getLocation());
        }
    }
}
