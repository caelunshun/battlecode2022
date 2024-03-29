package prototype1_01_09_2022.builder;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype1_01_09_2022.Attachment;
import prototype1_01_09_2022.Robot;
import prototype1_01_09_2022.nav.Navigator;

public class SacrificeBuilderAttachment extends Attachment {
    private Navigator nav;

    public SacrificeBuilderAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        heal();

        if (rc.senseLead(rc.getLocation()) == 0) {
            sacrificeForTheGreaterGood();
        } else {
            MapLocation closest = null;
            for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
                if (rc.senseLead(loc) == 0 && !rc.isLocationOccupied(loc)) {
                    if (closest == null
                        || rc.getLocation().distanceSquaredTo(loc)
                            < rc.getLocation().distanceSquaredTo(closest)) {
                        closest = loc;
                    }
                }
            }
            if (closest != null) {
                nav.advanceToward(closest);
            } else {
                nav.advanceToward(new MapLocation(rc.getMapWidth() / 2, rc.getMapHeight() / 2));
            }
        }
    }

    private void heal() throws GameActionException {
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam())) {
            if (info.health != info.type.health) {
                if (rc.canRepair(info.location)) {
                    rc.repair(info.location);
                    rc.setIndicatorString("Healed at " + info.location);
                }
            }
        }
    }

    private void sacrificeForTheGreaterGood() throws GameActionException {
        if (rc.senseLead(rc.getLocation()) == 0) {
            rc.disintegrate(); // RIP
        }
    }
}
