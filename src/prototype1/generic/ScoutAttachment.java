package prototype1.generic;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.comms.ScoutingMask;
import prototype1.nav.Navigator;

import java.util.List;

public class ScoutAttachment extends Attachment {
    private final Navigator nav;

    public ScoutAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);

    }

    @Override
    public void doTurn() throws GameActionException {
        if (!rc.isMovementReady()) return;
        if (rc.getLocation().distanceSquaredTo(Util.getCenterLocation(rc)) <= 16) {
            robot.moveRandom();
        } else {
            nav.advanceToward(Util.getCenterLocation(rc));
        }
    }

    /*private MapLocation chosen;
    private List<MapLocation> ruledOut;
    boolean movingRandom = false;

    public ScoutAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!rc.isMovementReady()) return;

        if (movingRandom) {
            robot.moveRandom();
            return;
        }

        ScoutingMask mask = robot.getComms().getScoutingMask();
        List<MapLocation> spottedLocs = mask.getEnemySpottedLocations(robot);
        MapLocation nearest = Util.getClosest(rc.getLocation(), spottedLocs);
        if (nearest != null) {
            if (rc.getLocation().distanceSquaredTo(nearest) <= 36) {
                movingRandom = true;
            }

            nav.advanceToward(nearest);
            rc.setIndicatorString("Scout-Attacking to " + nearest);
        } else {
            if (chosen != null && rc.getLocation().distanceSquaredTo(chosen) <= 36) {
                ruledOut.add(chosen);
                chosen = null;
            }
            if (chosen == null) {
                List<MapLocation> viable = ScoutingMask.getAllLocations(robot);
                do {
                    chosen = viable.get(robot.getRng().nextInt(viable.size()));
                } while (ruledOut.contains(chosen));
            }
            nav.advanceToward(chosen);
            rc.setIndicatorString("Scout-Scouting to " + chosen);
        }

        int numEnemies = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent()).length;
        if (numEnemies > 0) {
            for (int archonIndex = 0; archonIndex < rc.getArchonCount(); archonIndex++) {
                for (SymmetryType symm : SymmetryType.values()) {
                    MapLocation loc = symm.getSymmetryLocation(robot.getFriendlyArchons().get(archonIndex), rc);
                    if (Math.sqrt(loc.distanceSquaredTo(rc.getLocation())) <= rc.getMapWidth() / 3) {
                        mask.markEnemySpotted(archonIndex, symm);
                        robot.getComms().setScoutingMask(mask);
                        rc.setIndicatorString("Spotted at " + loc);
                        return;
                    }
                }
            }
        }
    }*/
}
