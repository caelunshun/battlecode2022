package prototype1.soldier;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
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

        if (distanceToNearestEnemyArchon == -1 || Math.sqrt(distanceToNearestEnemyArchon) >= rc.getMapWidth() / 5) {
            advanceTowardEnemyArchon();
        } else {
            robot.moveRandom();
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
        nav.advanceToward(loc);
    }
}
