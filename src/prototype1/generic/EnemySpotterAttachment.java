package prototype1.generic;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.comms.EnemySpottedLocation;

public class EnemySpotterAttachment extends Attachment {
    public EnemySpotterAttachment(Robot robot) {
        super(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        RobotInfo[] enemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent());
        if (enemies.length > 0) {
            robot.getComms().addEnemySpottedLocation(new EnemySpottedLocation(enemies[0].location, rc.getRoundNum()));
        }
    }
}
