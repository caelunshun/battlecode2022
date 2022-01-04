package prototype1;

import battlecode.common.RobotController;
import battlecode.common.RobotType;
import prototype1.archon.ArchonAttachment;
import prototype1.archon.LaboratoryAttachment;
import prototype1.archon.MinerAttachment;
import prototype1.archon.SoldierAttachment;

public class RobotBuilder {
    private RobotType type;

    public RobotBuilder(RobotType type) {
        this.type = type;
    }

    public Robot build(RobotController rc) {
        Robot robot = new Robot(rc);
        addAttachments(robot);
        return robot;
    }

    private void addAttachments(Robot robot) {
        switch (type) {
            case ARCHON:
                robot.addAttachment(new ArchonAttachment(robot));
                break;
            case LABORATORY:
                robot.addAttachment(new LaboratoryAttachment(robot));
                break;
            case WATCHTOWER:
                break;
            case MINER:
                robot.addAttachment(new MinerAttachment(robot));
                break;
            case BUILDER:
                break;
            case SOLDIER:
                robot.addAttachment(new SoldierAttachment(robot));
                break;
            case SAGE:
                break;
        }
    }
}
