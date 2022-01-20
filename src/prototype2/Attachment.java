package prototype2;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import prototype2.Robot;

/**
 * A behavior attached to a robot.
 */
public abstract class Attachment {
    protected Robot robot;
    protected RobotController rc;

    public Attachment(Robot robot) {
        this.robot = robot;
        this.rc = robot.getRc();
    }

    /**
     * Invoked each turn the robot runs.
     */
    public abstract void doTurn() throws GameActionException;
}
