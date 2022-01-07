package prototype1_01_06_2022.miner;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1_01_06_2022.Attachment;
import prototype1_01_06_2022.Robot;
import prototype1_01_06_2022.nav.Navigator;

import java.util.ArrayList;
import java.util.List;

public class MinerAttachment extends Attachment {
    private final Navigator nav;
    private final List<MapLocation> leadTileQueue = new ArrayList<>();
    private final LeadGrid leadGrid;
    private int leadTileQueueCursor = 0;

    private MapLocation targetTile;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
        this.leadGrid = new LeadGrid(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        spotLead();
        mine();
        if (!moveTowardCloseLead()) {
            moveTowardDistantLead();
        }
    }

    private void spotLead() throws GameActionException {
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(),
                rc.getType().visionRadiusSquared)) {
            if (rc.senseLead(loc) > 0) {
                LeadTile tile = leadGrid.getTile(loc);
                if (!tile.known) {
                    tile.known = true;
                    leadTileQueue.add(loc);
                }
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

            if (rc.senseLead(loc) == 1) {
                leadGrid.getTile(loc).lastExhaustedRound = rc.getRoundNum();
            }
        }
        return false;
    }

    private boolean moveTowardCloseLead() throws GameActionException {
        MapLocation bestLead = null;
        int bestScore = 0;
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().visionRadiusSquared)) {
            int lead = rc.senseLead(loc);
            int gold = rc.senseGold(loc);
            if (lead > 1 || gold > 0) {
                int score = rc.getLocation().distanceSquaredTo(loc);
                score -= lead * 5;
                score -= gold * 50;

                if (bestLead == null || score < bestScore) {
                    bestLead = loc;
                    bestScore = score;
                }
            }

            if (Clock.getBytecodesLeft() < 1000) {
                break;
            }
        }

        if (bestLead != null) {
            nav.advanceToward(bestLead);
            return true;
        }
        return false;
    }

    private void moveTowardDistantLead() throws GameActionException {
        if (targetTile != null) {
            if (rc.getLocation().distanceSquaredTo(targetTile) <= 2) {
                targetTile = null;
            }
        }

        if (targetTile == null) {
            targetTile = findTargetLeadTile();
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
        int tries = 0;

        MapLocation bestLoc = null;
        int bestScore = 0;

        while (tries < leadTileQueue.size()
            && Clock.getBytecodesLeft() > 1000) {
            MapLocation loc = nextLeadTileInQueue();
            LeadTile tile = leadGrid.getTile(loc);

            ++tries;

            if (rc.getRoundNum() - tile.lastExhaustedRound < 100) {
                continue;
            }

            int locScore = (int) Math.sqrt(rc.getLocation().distanceSquaredTo(loc));

            if (locScore < bestScore) {
                bestLoc = loc;
                bestScore = locScore;
            }
        }

        return bestLoc;
    }

    private MapLocation nextLeadTileInQueue() {
        MapLocation result = leadTileQueue.get(leadTileQueueCursor++);
        if (leadTileQueueCursor >= leadTileQueue.size()) {
            leadTileQueueCursor = 0;
        }
        return result;
    }
}
