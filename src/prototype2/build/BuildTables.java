package prototype2.build;

public class BuildTables {
    private final BuildWeightTable leadTable;
    private final BuildWeightTable goldTable;

    public BuildTables() {
        this.leadTable = new BuildWeightTable(LeadBuild.values().length);
        this.goldTable = new BuildWeightTable(GoldBuild.values().length);
    }

    public void addWeight(LeadBuild build, int weight) {
        leadTable.addWeight(build.ordinal(), weight);
    }

    public void addWeight(GoldBuild build, int weight) {
        goldTable.addWeight(build.ordinal(), weight);
    }

    public void clearWeight(LeadBuild build) {
        leadTable.clearWeight(build.ordinal());
    }

    public void clearWeight(GoldBuild build) {
        goldTable.clearWeight(build.ordinal());
    }

    public LeadBuild getHighestLeadWeight() {
        return LeadBuild.values()[leadTable.getHighestWeight()];
    }

    public GoldBuild getHighestGoldWeight() {
        return GoldBuild.values()[goldTable.getHighestWeight()];
    }
}
