package prototype1.archon;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;

public class ArchonAttachment extends Attachment {
    private int lastBuiltIndex = -1;

    public static int SOLDIER_BUILDING_OFFSET = 3;

    public ArchonAttachment(Robot robot) throws GameActionException {
        super(robot);
        robot.getComms().addFriendlyArchon(rc.getLocation());
    }

    @Override
    public void doTurn() throws GameActionException {
        build();
        repair();
    }

    private void build() throws GameActionException {
        int currentBuildIndex = robot.getComms().getBuildIndex();
        if (currentBuildIndex - lastBuiltIndex < robot.getFriendlyArchons().size() - 1) {
            return;
        }

        RobotType type;
        if (currentBuildIndex < SOLDIER_BUILDING_OFFSET - 1) {
            type = RobotType.MINER;
        } else if (currentBuildIndex < SOLDIER_BUILDING_OFFSET + 2) {
            type = RobotType.SOLDIER;
        } else if (currentBuildIndex < 6) {
            type = RobotType.MINER;
        } else if (rc.getTeamGoldAmount(rc.getTeam()) >= RobotType.SAGE.buildCostGold) {
            type = RobotType.SAGE;
        } else if (currentBuildIndex % 6 < 2) {
            type = RobotType.BUILDER;
        } else if (currentBuildIndex % 6 < 4) {
            type = RobotType.MINER;
        } else {
            type = RobotType.SOLDIER;
        }

        rc.setIndicatorString("Build #" + currentBuildIndex);

        if (rc.getTeamLeadAmount(rc.getTeam()) < 200
            && rc.getRoundNum() > 60) {
            return;
        }

        if (tryBuild(type)) {
            ++currentBuildIndex;
            robot.getComms().setBuildIndex(currentBuildIndex);
            lastBuiltIndex = currentBuildIndex;
        }
    }

    private boolean tryBuild(RobotType type) throws GameActionException {
        Direction dir = getAvailableBuildDirection();
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            rc.setIndicatorString("Built a " + type);
            return true;
        }
        return false;
    }

    private Direction getAvailableBuildDirection() throws GameActionException {
        for (Direction dir : Util.DIRECTIONS) {
            if (rc.senseRobotAtLocation(rc.getLocation().add(dir)) == null) {
                return dir;
            }
        }
        return Direction.CENTER;
    }

    private void repair() throws GameActionException{
        if(!rc.isActionReady()){
            return;
        }
        RobotInfo[] nearbyRobots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam());
        for(RobotInfo robs :nearbyRobots){
            if(robs.getHealth() < robs.getType().health){
                if(rc.canRepair(robs.getLocation())){
                    rc.repair(robs.getLocation());
                    return;
                }
            }
        }
    }
}
