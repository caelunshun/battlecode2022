package prototype2.attachment.soldier;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype2.*;
import prototype2.comms.Archon;
import prototype2.comms.CryForHelp;
import prototype2.comms.EnemySpottedLocation;
import prototype2.nav.Navigator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SoldierMacroAttachment extends Attachment {
    private final Navigator nav;

    public SoldierMacroAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!rc.isMovementReady()) return;

        Strategy strat = robot.getComms().getStrategy();
        if (strat == null) strat = Strategy.RUSH;

        if (!followCallsForHelp()) {
            if (strat != Strategy.RUSH && robot.getRc().getRoundNum() < 500) {
                if (rc.getLocation().distanceSquaredTo(robot.getHomeArchon()) > 4) {
                    nav.advanceToward(robot.getHomeArchon());
                } else {
                    nav.advanceToward(Util.getCenterLocation(rc));
                }
            } else if (!followEnemyLocations()) {
                advanceTowardEnemyArchon();
            }
        }
    }

    private boolean followEnemyLocations() throws GameActionException {
        EnemySpottedLocation[] enemies = robot.getComms().getEnemySpottedLocations();
        MapLocation closest = null;
        for (EnemySpottedLocation enemy : enemies) {
            if (enemy != null
                && (closest == null || closest.distanceSquaredTo(rc.getLocation()) > enemy.loc.distanceSquaredTo(rc.getLocation()))) {
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

    private List<MapLocation> ruledOutSymmetryLocations = new ArrayList<>();

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

        if (ruledOutSymmetryLocations.contains(loc)) {
            robot.moveRandom();
            rc.setIndicatorString("[Macro] Random Movement");
            return;
        }

        nav.advanceToward(loc);
        rc.setIndicatorString("[Macro] Advancing To Enemy Archon: " + loc);

        if (rc.canSenseLocation(loc)) {
            RobotInfo info = rc.senseRobotAtLocation(loc);
            if (info == null || !(info.type == RobotType.ARCHON && info.team == rc.getTeam().opponent())) {
                ruledOutSymmetryLocations.add(loc);
            }
        }
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
