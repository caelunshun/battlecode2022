package prototype1;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class RobotPlayer {
    public static void run(RobotController rc) {
        RobotBuilder builder = new RobotBuilder(rc.getType());
        Robot robot = builder.build(rc);
        robot.run();
    }
}
