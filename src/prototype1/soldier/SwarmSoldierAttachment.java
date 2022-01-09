package prototype1.soldier;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import prototype1.Attachment;
import prototype1.BotConstants;
import prototype1.Robot;
import prototype1.Util;
import prototype1.comms.BecomeSwarmLeader;
import prototype1.generic.SymmetryType;
import prototype1.nav.Navigator;

import java.util.ArrayList;
import java.util.List;

public class SwarmSoldierAttachment extends Attachment {
    /**
     * The swarm we're a part of
     */
    private int swarmIndex = -1;
    private boolean isLeader;
    private MapLocation swarmLocation;

    private final Navigator nav;


    public SwarmSoldierAttachment(Robot robot) {
        super(robot);
        this.nav = new Navigator(robot);
    }

    @Override
    public void doTurn() throws GameActionException {
        if (rc.getRoundNum() > BotConstants.DEFENSE_MODE_TURN) return;

        handleLeaderAppoint();
        if (!hasSwarmIndex()) {
            findSwarmIndex();

            if (!hasSwarmIndex()) {
                rc.setIndicatorString("Missing Swarm");
                return;
            }
        }

        swarmLocation = robot.getComms().getSwarms()[swarmIndex];

        if (isLeader) {
            if (isGoingToDie()) {
                appointNewLeader();
            }
            leaderMoveSwarm();
            MapLocation swarmLocation = rc.getLocation();
            robot.getComms().setSwarmLocation(swarmIndex, swarmLocation);
            rc.setIndicatorString("Swarm Leader - " + swarmIndex + " (Target: " + swarmLocation + " )");
        } else {
            doMicroMovements();
            nav.advanceToward(swarmLocation);
            rc.setIndicatorString("Swarm " + swarmIndex);
        }

        robot.endTurn();
    }

    private boolean isGoingToDie() {
        return rc.getHealth() < rc.getType().health * 0.5;
    }

    private void appointNewLeader() throws GameActionException {
        RobotInfo bestCandidate = null;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (bestCandidate == null || info.health > bestCandidate.health) {
                bestCandidate = info;
            }
        }

        if (bestCandidate != null) {
            robot.getComms().commandBecomeSwarmLeader(new BecomeSwarmLeader(bestCandidate.ID, swarmIndex));
            isLeader = false;
        } else {
            // Swarm is gone... RIP.
            robot.getComms().clearSwarm(swarmIndex);
        }
    }

    private void handleLeaderAppoint() throws GameActionException {
        for (BecomeSwarmLeader cmd : robot.getComms().getBecomeSwarmLeaderCommands()) {
            if (cmd == null) continue;
            if (cmd.robotID == rc.getID()) {
                swarmIndex = cmd.swarmIndex;
                isLeader = true;
            }
        }
    }

    private void findSwarmIndex() throws GameActionException {
        int closest = -1;
        MapLocation[] swarms = robot.getComms().getSwarms();
        for (int i = 0; i < swarms.length; i++) {
            MapLocation swarm = swarms[i];
            if (swarm == null) continue;
            if (closest == -1 || swarm.distanceSquaredTo(rc.getLocation()) < swarms[closest].distanceSquaredTo(rc.getLocation())) {
                closest = i;
            }
        }

        swarmIndex = closest;
    }

    private boolean hasSwarmIndex() {
        return swarmIndex != -1;
    }

    private MapLocation leaderTarget;
    private MapLocation attackingArchon;

    private void leaderMoveSwarm() throws GameActionException {
        if (robot.isAnyArchonInDanger()) {
            MapLocation danger = null;
            for (MapLocation loc : robot.getFriendlyArchons()) {
                if (robot.isArchonInDanger(loc)) {
                    if (danger == null || loc.distanceSquaredTo(rc.getLocation()) < danger.distanceSquaredTo(rc.getLocation())) {
                        danger = loc;
                    }
                    if (loc.equals(robot.getHomeArchon())) {
                        danger = loc;
                        break;
                    }
                }
            }

            if (danger != null) {
                nav.advanceToward(danger);
                return;
            }
        }

        if (leaderTarget == null
                || !isValidLeaderTarget(leaderTarget)) leaderTarget = findLeaderTarget();

        senseRushingArchon();
        doLeaderMicroMovements();

        if (!rc.isMovementReady()) return;

        if (attackingArchon != null) {
            nav.advanceToward(attackingArchon);
        } else if (leaderTarget != null) {
            nav.advanceToward(leaderTarget);
        }
    }

    private void senseRushingArchon() {
        if (!robot.getEnemyArchons().contains(attackingArchon)) attackingArchon = null;

        for (MapLocation enemy : robot.getEnemyArchons()) {
            if (enemy.distanceSquaredTo(rc.getLocation()) < 10) {
                attackingArchon = enemy;
                break;
            }
        }
    }

    private void doLeaderMicroMovements() throws GameActionException {
        if (!rc.isMovementReady()) return;

        int swarmSize = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam()).length;

        double vx = 0, vy = 0;
        int enemySize = 0;
        MapLocation enemyPos = new MapLocation(0, 0);
        for (RobotInfo enemy : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (enemy.type.canAttack()) {
                ++enemySize;
                double dx = rc.getLocation().x - enemy.location.x;
                double dy = rc.getLocation().y - enemy.location.y;
                double len = Math.hypot(dx, dy);
                dx /= len;
                dy /= len;
                vx += dx;
                vy += dy;
                enemyPos = enemyPos.translate(enemy.location.x, enemy.location.y);
            }
        }

        if (swarmSize * 0.7 < enemySize) {
            attackingArchon = null;
            double theta = Math.atan2(vy, vx);
            double x = Math.cos(theta) * 10;
            double y = Math.sin(theta) * 10;
            badTargets.add(leaderTarget);
            leaderTarget = rc.getLocation().translate((int) x, (int) y);
        } else if (enemySize > 0) {
            leaderTarget = new MapLocation(enemyPos.x / enemySize, enemyPos.y / enemySize);
        }
    }

    private List<MapLocation> badTargets = new ArrayList<>();

    private MapLocation findLeaderTarget() {
        for (MapLocation enemy : robot.getEnemyArchons()) {
            if (isValidLeaderTarget(enemy)) {
                return enemy;
            }
        }

        for (MapLocation friendly : robot.getFriendlyArchons()) {
            for (SymmetryType symmetry : SymmetryType.values()) {
                MapLocation loc = symmetry.getSymmetryLocation(friendly, rc);
                if (isValidLeaderTarget(loc)) {
                    return loc;
                }
            }
        }

        return null;
    }

    private boolean isValidLeaderTarget(MapLocation loc) {
        return loc.distanceSquaredTo(rc.getLocation()) > 15 && !badTargets.contains(loc);
    }

    private void doMicroMovements() throws GameActionException {
        int enemyDamage = 0;
        MapLocation closestPrey = null;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (info.type.canAttack()) {
                enemyDamage += info.type.damage;
            } else if (closestPrey == null
                || rc.getLocation().distanceSquaredTo(closestPrey) > rc.getLocation().distanceSquaredTo(info.location)){
                closestPrey = info.location;
            }
        }

        if (enemyDamage < 6 && closestPrey != null) {
            nav.advanceToward(closestPrey);
        }
    }
}
