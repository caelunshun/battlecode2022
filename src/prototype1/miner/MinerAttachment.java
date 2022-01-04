package prototype1.miner;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.nav.Navigator;

public class MinerAttachment extends Attachment {
    private final Navigator nav;
    private final LeadGrid leadGrid;

    private MapLocation targetTile;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
        this.leadGrid = new LeadGrid(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        spotLead();
        if (!mine()) {
            leadGrid.getTile(rc.getLocation()).roundLastVisited = rc.getRoundNum();
            moveTowardLead();
        }
    }

    private void spotLead() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),
                rc.getType().visionRadiusSquared)) {
            if (rc.senseLead(loc) > 0) {
                LeadTile tile = leadGrid.getTile(loc);
                tile.addLead(loc);
            }

            if (Clock.getBytecodesLeft() < 1000) {
                return;
            }
        }
    }

    private boolean mine() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().actionRadiusSquared)) {
            if (!rc.isActionReady()) {
                return true;
            }
            while (rc.canMineGold(loc)) {
                rc.mineGold(loc);
                rc.setIndicatorString("Mined gold");
            }
            while (rc.senseLead(loc) > 1 && rc.canMineLead(loc)) {
                rc.mineLead(loc);
                rc.setIndicatorString("Mined lead");
            }
        }
        return false;
    }

    private void moveTowardLead() throws GameActionException {
        MapLocation nearestLead = null;
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            if (rc.senseLead(loc) > 1 || rc.senseGold(loc) > 0) {
                if (nearestLead == null || rc.getLocation().distanceSquaredTo(nearestLead)
                        < nearestLead.distanceSquaredTo(rc.getLocation())) {
                    nearestLead = loc;
                }
            }
        }

        if (nearestLead != null) {
            nav.advanceToward(nearestLead);
            rc.setIndicatorString("Spotted lead " + nearestLead);
            return;
        }

        if (targetTile != null) {
            if (rc.getLocation().distanceSquaredTo(targetTile) <= 5) {
                targetTile = null;
            }
        }

        if (targetTile == null) {
            targetTile = findTargetLeadTile();
            if (targetTile != null) {
                targetTile = targetTile.translate(3, 3);
            }
        }

        if (targetTile != null) {
            nav.advanceToward(targetTile);
            rc.setIndicatorString("Advancing toward " + targetTile);
        } else {
            robot.moveRandom();
            rc.setIndicatorString("Moving randomly");
        }
    }

    private MapLocation findTargetLeadTile() throws GameActionException {
        LeadTile[] tiles = leadGrid.getTiles();

        int bestScore = 0;
        MapLocation bestTarget = null;
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].lead == null) {
                continue;
            }

            MapLocation origin = leadGrid.getLocationFromTileIndex(i);

            int score = origin.distanceSquaredTo(rc.getLocation())
                    - (int) Math.pow(tiles[i].lead.size(), 3);
            if (rc.getRoundNum() - tiles[i].roundLastVisited < 80) {
                continue;
            }

            if (bestTarget == null || score < bestScore) {
                bestTarget = origin;
                bestScore = score;
            }
        }

        return bestTarget;
    }
}
