package warkit.armory;

public class ArmoryRealm {

    public final String realmName;
    public final String realmSlug;
    public final String battlegroup;
    
    public ArmoryRealm(String realmName, String realmSlug, String battlegroup) {
        this.realmName = realmName;
        this.realmSlug = realmSlug;
        this.battlegroup = battlegroup;
    }
    
    @Override
    public String toString() {
        return String.format("%s(%s,%s)", realmSlug, realmName, battlegroup);
    }
    
    
}
