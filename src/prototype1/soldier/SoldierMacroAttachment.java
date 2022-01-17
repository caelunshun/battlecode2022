package prototype1.soldier;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.comms.CryForHelp;
import prototype1.comms.EnemySpottedLocation;
import prototype1.generic.SymmetryType;
import prototype1.nav.Navigator;

public class SoldierMacroAttachment extends Attachment {
    private final Navigator nav;

    // states reset each round
    int distanceToNearestFriendlyArchon;
    // -1 if no enemy archons are known
    int distanceToNearestEnemyArchon;

    public SoldierMacroAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!rc.isMovementReady()) return;
        updateStates();

        if (!followCallsForHelp()) {
            if (!followEnemyLocations()) {
                advanceTowardEnemyArchon();
            }
        }
    }

    private void updateStates() throws GameActionException {
        distanceToNearestFriendlyArchon = Integer.MAX_VALUE;
        distanceToNearestEnemyArchon = Integer.MAX_VALUE;
        for (MapLocation friendly : robot.getFriendlyArchons()) {
            if (rc.getLocation().distanceSquaredTo(friendly) < distanceToNearestFriendlyArchon) {
                distanceToNearestFriendlyArchon = rc.getLocation().distanceSquaredTo(friendly);
            }
        }
        for (MapLocation enemy : robot.getEnemyArchons()) {
            if (rc.getLocation().distanceSquaredTo(enemy) < distanceToNearestEnemyArchon) {
                distanceToNearestEnemyArchon = rc.getLocation().distanceSquaredTo(enemy);
            }
        }

        if (robot.getEnemyArchons().isEmpty()) distanceToNearestEnemyArchon = -1;
    }

    private boolean followEnemyLocations() throws GameActionException {
        EnemySpottedLocation[] enemies = robot.getComms().getEnemySpottedLocations();
        MapLocation closest = null;
        for (EnemySpottedLocation enemy : enemies) {
            if (enemy != null
                && (closest == null || closest.distanceSquaredTo(rc.getLocation()) > enemy.loc.distanceSquaredTo(rc.getLocation()))
                && rc.getRoundNum() - enemy.roundNumber <= 3) {
                closest = enemy.loc;
            }
        }
        if (closest != null) {
            rc.setIndicatorString("[Macro] Following " + closest);
            nav.advanceToward(closest);
            return true;
        }
        return false;
    }

    private void advanceTowardEnemyArchon() throws GameActionException {
        SymmetryType predictedSymmetry;
        if (rc.getMapWidth() == rc.getMapHeight()) {
            predictedSymmetry = SymmetryType.ROTATIONAL;
        } else if (rc.getMapWidth() > rc.getMapHeight()) {
            predictedSymmetry = SymmetryType.VERTICAL;
        } else {
            predictedSymmetry = SymmetryType.HORIZONTAL;
        }

        MapLocation loc = predictedSymmetry.getSymmetryLocation(robot.getHomeArchon(), rc);

        if (!robot.getEnemyArchons().isEmpty()) {
            loc = Util.getClosest(rc.getLocation(), robot.getEnemyArchons());
        }

        nav.advanceToward(loc);
        rc.setIndicatorString("[Macro] Advancing To Enemy Archon: " + loc);
    }


    private boolean followCallsForHelp() throws GameActionException {
        CryForHelp closest = null;
        for (CryForHelp cry : robot.getComms().getCriesForHelp()) {
            if (cry == null) continue;
            if (rc.getRoundNum() - cry.roundNumber > 3) continue;
            if (closest == null
                    || rc.getLocation().distanceSquaredTo(cry.enemyLoc) < rc.getLocation().distanceSquaredTo(closest.enemyLoc)) {
                closest = cry;
            }
        }

        if (closest == null) return false;

        rc.setIndicatorString("[Macro] Following Cry for Help " + closest.enemyLoc);

        nav.advanceToward(closest.enemyLoc);
        return true;
    }
}
