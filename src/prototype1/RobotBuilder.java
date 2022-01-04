package prototype1;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype1.archon.*;

public class RobotBuilder {
    private final RobotType type;

    public RobotBuilder(RobotType type) {
        this.type = type;
    }

    public Robot build(RobotController rc) throws GameActionException {
        Robot robot = new Robot(rc);
        addAttachments(robot);
        return robot;
    }

    private void addAttachments(Robot robot) throws GameActionException  {
        robot.addAttachment(new ArchonSpotterAttachment(robot));
        switch (type) {
            case ARCHON:
                robot.addAttachment(new ArchonAttachment(robot));
                break;
            case LABORATORY:
                robot.addAttachment(new LaboratoryAttachment(robot));
                break;
            case WATCHTOWER:
                robot.addAttachment(new WatchtowerAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new RandomMovementAttachment(robot));
                break;
            case MINER:
                robot.addAttachment(new MinerAttachment(robot));
                break;
            case BUILDER:
                break;
            case SOLDIER:
                robot.addAttachment(new SoldierAttachment(robot));
                robot.addAttachment(new AttackAttachment(robot));
                robot.addAttachment(new RandomMovementAttachment(robot));
                break;
            case SAGE:
                break;
        }
    }
}
