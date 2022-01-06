package prototype1_pre_defense;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
       RobotBuilder builder = new RobotBuilder(rc.getType());
       Robot robot = builder.build(rc);
       robot.run();
    }
}
