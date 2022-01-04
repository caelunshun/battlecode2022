package prototype1;

import battlecode.common.RobotController;

/**
 * A behavior attached to a robot.
 */
public class Attachment {
    protected Robot robot;
    protected RobotController rc;

    public Attachment(Robot robot) {
        this.robot = robot;
        this.rc = robot.getRc();
    }
}
