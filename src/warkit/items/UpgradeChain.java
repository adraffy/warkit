package warkit.items;

public class UpgradeChain {

    public int chainId;    
    public final int[] upgradeId;
    public final int[] itemLevelDelta;
    
    public UpgradeChain(int chainId, int[] upgradeId, int[] itemLevelDelta) {
        this.chainId = chainId;
        this.upgradeId = upgradeId;
        this.itemLevelDelta = itemLevelDelta;
    }
    
    public int size() {
        return upgradeId.length;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append("<");
        sb.append(chainId);
        sb.append(">[");
        sb.append(upgradeId.length);
        sb.append("]{");
        for (int i = 0; i < upgradeId.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(upgradeId[i]);
            sb.append(":");
            sb.append(itemLevelDelta[i]);            
        }
        sb.append("}");
        return sb.toString();
    }
    
}
