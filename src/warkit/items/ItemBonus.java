package warkit.items;

import java.util.Arrays;
import java.util.Comparator;
import warbase.IntSet;
import warbase.StatAlloc;
import warbase.Misc;
import warbase.types.QualityT;
import warbase.types.SocketT;

public class ItemBonus {
    
    //static public final ItemBonus IDENTITY = new ItemBonus(0, 0, 0, null, "Normal", null, null, null);
    
    public final int id;
    public final int itemLevelDelta;
    public final int reqLevelDelta;
    public final QualityT quality; // can be null
    public final String nameDesc; // can be null
    public final String suffixName; // can be null
    public final SocketT[] sockets; // can be null
    public final StatAlloc[] statAllocs; // can be null
    //public final boolean something;
    
    public ItemBonus(int id, int itemLevelDelta, int reqLevelDelta, QualityT quality, String nameDesc, String suffixName, SocketT[] sockets, StatAlloc[] statAllocs) {
        this.id = id;
        this.itemLevelDelta = itemLevelDelta;
        this.reqLevelDelta = reqLevelDelta;
        this.quality = quality;
        this.nameDesc = nameDesc;
        this.suffixName = suffixName;
        this.sockets = sockets;
        this.statAllocs = statAllocs;
        /*something = itemLevelDelta != 0 
                || reqLevelDelta != 0
                || quality != null
                || nameDesc != null
                || suffixName != null
                || sockets != null
                || statAllocs != null;*/
    }
    
    @Override
    public String toString() {
        return String.format("%s<%s>(%+di,%+dr,%s,%s,%s,%s)", getClass().getSimpleName(), id, 
                itemLevelDelta, reqLevelDelta, quality, nameDesc, suffixName, 
                Arrays.toString(sockets), Arrays.toString(statAllocs));
    }
    
    // we can use equality because we memo everything
    public boolean isEffectivelyEqual(ItemBonus other) {
        return other != null 
                && other.itemLevelDelta == itemLevelDelta
                && other.nameDesc == nameDesc
                && other.quality == quality
                && other.reqLevelDelta == reqLevelDelta
                && other.suffixName == suffixName
                && other.sockets == sockets
                && other.statAllocs == statAllocs;
    }
    
    static public final Comparator<ItemBonus> CMP_ID = (a, b) -> a.id - b.id;
    static public final Comparator<ItemBonus[]> CMP_ARRAY = Misc.makeArrayComparator(CMP_ID);

    static public double score(ItemBonus[] components, IntSet ids) {
        int match = match(components, ids);        
        return match - 0.001 * (components.length - match);
    }
    
    static public int match(ItemBonus[] components, IntSet ids) {
        int num = 0;
        for (ItemBonus x: components) {
            if (ids.contains(x.id)) {
                num++;
            }
        }        
        return num;        
    }
    
    static public void remove(ItemBonus[] components, IntSet from) {
        for (ItemBonus x: components) {
            from.remove(x.id);
        }
    }
   
    
}
