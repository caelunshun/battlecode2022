package prototype2.build;

import battlecode.common.RobotType;

public enum GoldBuild {
    SAGE,
    WATCHTOWER_L3;

    public RobotType getType() {
        switch (this) {
            case SAGE:
                return RobotType.SAGE;
            case WATCHTOWER_L3:
            default:
                return null;
        }
    }
}
