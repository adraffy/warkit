package com.antistupid.warkit.items;

import com.antistupid.warbase.data.ItemStatCurve;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.utils.CompareHelp;
import java.util.Arrays;
import java.util.Comparator;

public class RandomSuffix {

    public final int id;
    public final String name; 
    public final StatAlloc[] statAllocs; 
    public final ItemBonus bonus;
    
    public RandomSuffix(int id, String name, StatAlloc[] statAllocs, ItemBonus bonus) {
        this.id = id;
        this.name = name;
        this.statAllocs = statAllocs;
        this.bonus = bonus;
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>(\"%s\")", getClass().getSimpleName(), id, name);
    }
    
    
    public void collectStats(StatMap stats, Wearable item, int itemLevel, QualityT quality) {
        if (itemLevel == 0) {
            itemLevel = item.itemLevel;
        }
        if (quality == null) {
            quality = item.quality;
        }     
        StatAlloc.collectStats(stats, statAllocs, ItemStatCurve.get(itemLevel, quality, item.getRandPropIndex()));
    }
    
    private void appendSockets(StringBuilder sb) {
        sb.append(Arrays.toString(bonus.sockets)); // fix me
    }
    
    public void appendDescTo(StringBuilder sb, StatMap stats) {
        boolean hasSockets = bonus != null && bonus.sockets != null;
        if (stats.hasAny()) {
            stats.appendTo(sb, false, false);
            if (hasSockets) {                
                sb.append(", ");
                appendSockets(sb);
            }            
        } else if (hasSockets) {
            appendSockets(sb);
        }        
    }
    
    static public final Comparator<RandomSuffix> CMP_POWER = (a, b) -> {
        int c = a.name.compareTo(b.name);
        return c == 0 ? compareAllocs(a, b) : c;
    };
    
    
    static public int compareAllocs(RandomSuffix a, RandomSuffix b) {
        int c = StatAlloc.compare(a.statAllocs, b.statAllocs);
        if (c != 0) return c;
        c = CompareHelp.compareForNullAtBottom(a.bonus, b.bonus);
        if (c != 0 || a.bonus == null) return c;
        return StatAlloc.compare(a.bonus.statAllocs, b.bonus.statAllocs);
    }
    
    
}
