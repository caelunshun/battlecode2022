package prototype2.attachment.generic;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotType;
import prototype2.Attachment;
import prototype2.Robot;
import prototype2.nav.Navigator;

public class SacrificeWhenLowAttachment extends Attachment {
    private final Navigator nav;
    private boolean sack;
    public SacrificeWhenLowAttachment(Robot robot) {
        super(robot);
        nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (rc.getHealth() >= 40) {
            sack = false;
        }
        if (rc.getHealth() < 20 || sack) {
            sack = true;
            if (rc.getLocation().distanceSquaredTo(robot.getHomeArchon()) >= 25) {
                nav.advanceToward(robot.getHomeArchon());
            } else {
                MapLocation sacrificeLoc = getSacrificeLocation();
                if (rc.getLocation().equals(sacrificeLoc)) {
                    if (rc.getType().buildCostGold > 0) return;

                    if (rc.getType() != RobotType.SAGE && rc.getType() != RobotType.MINER) {
                        rc.disintegrate(); // Que Descanse En Paz
                    }
                } else {
                    nav.advanceToward(sacrificeLoc);
                }
            }
            rc.setIndicatorString("Sacrificing For Greater Good");
        }
    }

    private MapLocation getSacrificeLocation() throws GameActionException {
        MapLocation best = null;
        double bestScore = 0;
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            if (rc.senseRobotAtLocation(loc) == null) continue;
            if (rc.senseLead(loc) > 0) continue;
            double score = 0;
            score += Math.sqrt(loc.distanceSquaredTo(robot.getHomeArchon()));
            score += 0.2 * rc.senseRubble(loc);
            score += 2 * Math.sqrt(loc.distanceSquaredTo(rc.getLocation()));

            if (best == null || score < bestScore) {
                best = loc;
                bestScore = score;
            }
        }
        return best;
    }
}
