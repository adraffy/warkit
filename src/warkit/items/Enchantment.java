package warkit.items;

import java.util.ArrayList;
import java.util.Arrays;
import warbase.StatAlloc;
import warbase.StatMap;
import warbase.data.PlayerScaling;
import warbase.types.ProfT;

public class Enchantment {

    public final int id;
    public final String desc;
    public final int scalingLevelMin;
    public final int scalingLevelMax;
    public final int scalingId;
    public final int scalingPerLevel;
    public final ProfT reqProf;
    public final int reqProfLevel;
    public final StatAlloc[] statAllocs; // can be null
    public final ProfValue[] profBoosts;
    public final int[] spells;
    
   /*
    int scalingLevelMin = in.readUnsignedShort();
        int scalingLevelMax = in.readUnsignedShort();
        int scalingId = in.readUnsignedByte();
        int scalingPerLevel = in.readUnsignedShort();
        ProfT reqProf = ProfT.db.getById(in.readUnsignedShort());
        int reqProfSkill = in.readUnsignedShort();
    */
    
    public final boolean hasDescription;
    
    public Enchantment(int id, String desc, int minScalingLevel, int maxScalingLevel, int scalingId, int scalingPerLevel, ProfT reqProf, int reqProfLevel, 
            StatAlloc[] statAllocs, ProfValue[] profBoosts, int[] spells) {
        this.id = id;
        this.desc = desc;
        this.scalingLevelMin = minScalingLevel;
        this.scalingLevelMax = maxScalingLevel;
        this.scalingId = scalingId;
        this.scalingPerLevel = scalingPerLevel;
        this.reqProf = reqProf;
        this.reqProfLevel = reqProfLevel;
        this.statAllocs = statAllocs;
        this.profBoosts = profBoosts;
        this.spells = spells;
        hasDescription = spells == null && ((statAllocs == null) != (profBoosts == null));
    }
    
    // relatively expensive
    public String toDesc(int playerLevel) {
        StringBuilder sb = new StringBuilder();
        renderDesc(sb, playerLevel, new StatMap());
        return sb.toString();
    }    
    
    public void collectStats(StatMap stats, int playerLevel) {
        if (statAllocs != null) {
            int lvl = scalingLevelMax > 0 ? Math.min(playerLevel, scalingLevelMax) : playerLevel;
            int min = scalingLevelMin;
            float scaling = PlayerScaling.get(Math.max(min, lvl), scalingId);
            /*if (perLevel > 0 && lvl > min) {                
                scaling *= (min + perLevel * (lvl - min)) / lvl;                
            } */              
            for (StatAlloc x: statAllocs) {
                if (x.mod == 0) {
                    stats.add(x.stat, x.alloc);
                } else {
                    int value = (int)(0.5 + scaling * x.mod);
                    stats.add(x.stat, value);
                }
            }
        }
    }
    
    public void renderDesc(StringBuilder sb, int playerLevel, StatMap stats) {
        if (statAllocs != null) {           
            stats.clear(); 
            collectStats(stats, playerLevel);
            stats.appendTo(sb, false);
        }
        if (profBoosts != null) {
            for (int i = 0; i < profBoosts.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                ProfValue x = profBoosts[i];
                sb.append("+");
                sb.append(x.value);
                sb.append(" ");
                sb.append(x.prof.name);
            }
        }
        
    }
    
    /*
    @Override
    public String toString() {
        
    }
    */
    
    public void dump() {
        System.out.println("Desc[100]: " + toDesc(100));
        System.out.println("ScalingLevelMin: " + scalingLevelMin);
        System.out.println("ScalingLevelMax: " + scalingLevelMax);
        System.out.println("ScalingId: " + scalingId);
        if (statAllocs != null) {
            System.out.println(Arrays.toString(statAllocs));
        }
    }
    
}
