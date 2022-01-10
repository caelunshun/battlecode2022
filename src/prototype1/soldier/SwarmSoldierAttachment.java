package prototype1.soldier;

import battlecode.common.*;
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
            robot.getComms().setSwarmLocation(swarmIndex, rc.getLocation());
            int swarmSize = rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam()).length;
            rc.setIndicatorString("Swarm Leader - " + swarmIndex + " - size = " + swarmSize);

            if (isGoingToDie()) {
                appointNewLeader();
            }

            if (!doMicroMovements()) {
                if (swarmSize >= 4) {
                    doLeaderMacroMovement();
                }
            }
        } else {
            rc.setIndicatorString("Swarm " + swarmIndex);
            if (!doMicroMovements()) {
                nav.advanceToward(swarmLocation);
            }
        }

        robot.endTurn();
    }

    private boolean isGoingToDie() {
        return rc.getHealth() < rc.getType().health * 0.5;
    }

    private void appointNewLeader() throws GameActionException {
        RobotInfo bestCandidate = null;
        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam())) {
            if (info.health < 25) continue;
            if (info.type != RobotType.SOLDIER) continue;
            if (bestCandidate == null || info.health > bestCandidate.health) {
                bestCandidate = info;
            }
        }

        if (bestCandidate != null) {
            robot.getComms().commandBecomeSwarmLeader(new BecomeSwarmLeader(bestCandidate.ID, swarmIndex));
            isLeader = false;
            rc.setIndicatorString("Delegating Swarm Leadership");
        } else {
            // Swarm is gone... RIP.
            robot.getComms().clearSwarm(swarmIndex);
            rc.setIndicatorString("Swarm has died. RIP");
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

    int retreatTurns = 0;
    double retreatX;
    double retreatY;

    private boolean doMicroMovements() throws GameActionException {
        int enemyDamage = 0;
        MapLocation closestPrey = null;

        double vx = 0;
        double vy = 0;

        for (RobotInfo info : rc.senseNearbyRobots(rc.getType().visionRadiusSquared, rc.getTeam().opponent())) {
            if (info.type.canAttack() || info.type == RobotType.ARCHON) {
                if (info.type.damage > 0) {
                    enemyDamage += info.type.damage;
                }

                double dx = rc.getLocation().x - info.location.x;
                double dy = rc.getLocation().y - info.location.y;
                double len = Math.hypot(dy, dx);
                dx /= len;
                dy /= len;
                vx += dx;
                vy += dy;
            } else if (closestPrey == null
                || rc.getLocation().distanceSquaredTo(closestPrey) > rc.getLocation().distanceSquaredTo(info.location)){
                closestPrey = info.location;
            }
        }

        int ourDamage = 0;
        for (RobotInfo info : rc.senseNearbyRobots(8, rc.getTeam())) {
            if (info.type.canAttack()) {
                ourDamage += info.type.damage;
            }
        }

        --retreatTurns;
        if (ourDamage >= enemyDamage * 2 && retreatTurns <= 0) {
            if (closestPrey != null) {
                nav.advanceToward(closestPrey);
                return true;
            }
        } else {
            // Retreat.
            rc.setIndicatorString("Retreating - " + vy + ", " + vx);
            if (vx == 0 && vy == 0) {
                vx = retreatX;
                vy = retreatY;
            }
            Direction dir = Util.bestPossibleDirection(Util.getDirFromAngle(Math.atan2(vy, vx)), rc);

            if (rushingArchon != null) {
                dangerArchons.add(rushingArchon);
                rushingArchon = null;
            }

            if (dir != null && rc.canMove(dir)) {
                rc.move(dir);
                return true;
            }

            retreatTurns = 10;
            retreatX = vx;
            retreatY = vy;
        }
        return false;
    }

    private MapLocation rushingArchon;
    private List<MapLocation> dangerArchons = new ArrayList<>();

    private void doLeaderMacroMovement() throws GameActionException {
        for (MapLocation enemy : robot.getEnemyArchons()) {
            if (enemy.distanceSquaredTo(rc.getLocation()) < 12 * 12 && !dangerArchons.contains(enemy)) {
                rushingArchon = enemy;
                break;
            }
        }

        if (rushingArchon != null) {
            nav.advanceToward(rushingArchon);
        } else if (rc.getRoundNum() % 2 == 0) {
            robot.moveRandom();
        }
    }
}
