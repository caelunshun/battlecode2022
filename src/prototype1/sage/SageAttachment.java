package prototype1.sage;

import battlecode.common.*;
import prototype1.Attachment;
import prototype1.Robot;
import prototype1.Util;
import prototype1.generic.AttackAttachment;
import prototype1.nav.Navigator;

public class SageAttachment extends Attachment {
    AttackAttachment attack;
    public SageAttachment(Robot robot) {
        super(robot);
        attack = new AttackAttachment(robot);
    }
    @Override
    public void doTurn() throws GameActionException {
    }
    public void decideAttack() throws GameActionException{
        int furyDamage = 0;
        int chargeDamage = 0;
        RobotInfo[] nearbyEnemyRobots = rc.senseNearbyRobots(rc.getType().actionRadiusSquared, rc.getTeam().opponent());
        if(nearbyEnemyRobots.length == 0){
            return;
        }
        for(int i = 0; i < nearbyEnemyRobots.length; i++){
            RobotInfo rob = nearbyEnemyRobots[i];
            if(rob.type.isBuilding() && rob.mode == RobotMode.TURRET){
                furyDamage += (rob.type.getMaxHealth(rob.level)/10);
            } else{
                chargeDamage+=(rob.type.getMaxHealth(rob.level)/10);
            }
        }
        if(furyDamage > 45){
            if(chargeDamage > furyDamage){
                //charge
                if(rc.canEnvision(AnomalyType.CHARGE)){
                    rc.envision(AnomalyType.CHARGE);
                }
            } else{
                //fury
                if(rc.canEnvision(AnomalyType.CHARGE)){
                    rc.envision(AnomalyType.CHARGE);
                }
            }
        } else if(chargeDamage > 45){
            //charge
            if(rc.canEnvision(AnomalyType.CHARGE)){
                rc.envision(AnomalyType.CHARGE);
            }
        } else{
            //normal
            attack.doTurn();
        }
    }
}
