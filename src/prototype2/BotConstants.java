package prototype2;

import battlecode.common.RobotController;

public class BotConstants {
    public static int getPregameMinersPerArchon(RobotController rc) {
        int totalMiners;
        if (rc.getMapHeight() * rc.getMapWidth() <= 1800) {
            totalMiners = 8;
        } else {
            totalMiners = 12;
        }
        return totalMiners / rc.getArchonCount();
    }
}
