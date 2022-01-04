package prototype1.archon;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.comms.LeadCluster;
import prototype1.nav.Navigator;

public class MinerAttachment extends Attachment {
    private final Navigator nav;

    private MapLocation targetCluster;

    public MinerAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (!mine()) {
            moveTowardLead();
        }
        rc.setIndicatorString("LC: " + robot.getLeadClusters().toString());
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
            if (rc.senseLead(loc) > 1) {
                if (nearestLead == null || rc.getLocation().distanceSquaredTo(nearestLead) < nearestLead.distanceSquaredTo(rc.getLocation())) {
                    nearestLead = loc;
                }
            }
        }

        if (nearestLead != null) {
            nav.advanceToward(nearestLead);
            return;
        }

        if (targetCluster != null && rc.getLocation().distanceSquaredTo(targetCluster) <= 5) {
            targetCluster = null;
        }

        // Move to a different lead cluster
        if (targetCluster == null && !robot.getLeadClusters().isEmpty()) {
            LeadCluster cluster = robot.getLeadClusters().get(Util.getRng().nextInt(robot.getLeadClusters().size()));
            targetCluster = cluster.loc;
        }

        if (targetCluster != null) {
            nav.advanceToward(targetCluster);
        }
    }
}
