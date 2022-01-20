package prototype2.build;

import battlecode.common.RobotType;

public enum LeadBuild {
    MINER,
    SOLDIER,
    BUILDER,
    WATCHTOWER,
    WATCHTOWER_L2,
    LABORATORY;

    public RobotType getType() {
        switch (this) {
            case MINER:
                return RobotType.MINER;
            case SOLDIER:
                return RobotType.SOLDIER;
            case BUILDER:
                return RobotType.BUILDER;
            case WATCHTOWER:
                return RobotType.WATCHTOWER;
            case LABORATORY:
                return RobotType.LABORATORY;
            case WATCHTOWER_L2:
                default:
                return null;
        }
    }
}
