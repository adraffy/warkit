package warkit.items;

import java.util.Arrays;
import java.util.Comparator;
import warbase.types.QualityT;

public class NamedItemBonus {
        
    public final ItemBonus[] components; // never null
    public final String name;
    //public final String fancyName;
    public final int itemLevelDelta;
    public final int reqLevelDelta;
    public final QualityT quality;

    public NamedItemBonus(ItemBonus[] components, String name, int itemLevelDelta, int reqLevelDelta, QualityT quality) {
        this.components = components;
        this.name = name;
        this.itemLevelDelta = itemLevelDelta;
        this.reqLevelDelta = reqLevelDelta;     
        this.quality = quality;
        //this.fancyName = itemLevelDelta != 0 ? String.format("%s (%+d)", name, itemLevelDelta) : name;
    }
    
    static public final NamedItemBonus NONE = new NamedItemBonus(new ItemBonus[0], "None", 0, 0, null);
      
    
    @Override
    public String toString() {
        return String.format("%s(%s,%d,%d,%s)%s", getClass().getSimpleName(), name, itemLevelDelta, reqLevelDelta, quality, Arrays.toString(components));
    }
    
    static public final Comparator<NamedItemBonus> CMP_ITEM_LEVEL = (a, b) -> a.itemLevelDelta - b.itemLevelDelta;
    
    
}
