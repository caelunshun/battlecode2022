package JFPROTO.comms;

public class BecomeSwarmLeader {
    public final int robotID;
    public final int swarmIndex;

    public BecomeSwarmLeader(int robotID, int swarmIndex) {
        this.robotID = robotID;
        this.swarmIndex = swarmIndex;
    }
}
