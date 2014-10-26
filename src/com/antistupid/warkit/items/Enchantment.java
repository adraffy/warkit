package com.antistupid.warkit.items;

import java.util.Arrays;
import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warkit.player.Player;

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
    public final boolean hasDesc;
    
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
        hasDesc = spells == null && (statAllocs != null || profBoosts != null); // wut: ((statAllocs == null) != (profBoosts == null));
    }
    
    public StatMap getStats(int playerLevel) {
        StatMap stats = new StatMap();
        collectStats(stats, playerLevel);
        return stats;
    }
    
    public void collectStats(StatMap stats, int playerLevel) {
        /*
        if (statAllocs != null) {
            int lvl = PlayerScaling.max(scalingLevelMax, playerLevel);
            float scaling = PlayerScaling.get(Math.max(scalingLevelMin, lvl), scalingId);
            //if (perLevel > 0 && lvl > min) {                
            //  scaling *= (min + perLevel * (lvl - min)) / lvl;                
            //}              
            for (StatAlloc x: statAllocs) {
                if (x.mod == 0) {
                    stats.add(x.stat, x.alloc);
                } else {
                    int value = (int)(0.5 + scaling * x.mod);
                    stats.add(x.stat, value);
                }
            }
        }
        */
        StatAlloc.collectSpellStats(stats, statAllocs, playerLevel, scalingLevelMin, scalingLevelMax, scalingId);
    }
    
    public int renderDesc(StringBuilder sb, int playerLevel, StatMap statBuf) {
        int num = 0;
        if (statAllocs != null) {      
            statBuf = StatMap.recycle(statBuf);
            collectStats(statBuf, playerLevel);
            num += statBuf.appendTo(sb, false, false);
        }
        if (profBoosts != null) {
            for (ProfValue x : profBoosts) {
                if (num++ > 0) {
                    sb.append(", ");
                }
                sb.append("+");
                sb.append(x.value);
                sb.append(" ");
                sb.append(x.prof.name);
            }
        }    
        return num;
    }
    
    public String getDesc() { return getDesc(Player.MAX_PLAYER_LEVEL); }
    public String getDesc(int playerLevel) {
        StringBuilder sb = new StringBuilder();
        renderDesc(sb, playerLevel, null);
        return sb.toString();
    }    
    
    @Override
    public String toString() {
        return String.format("%s<%d>(%s)", getClass().getSimpleName(), id, getDesc());
    }
    
    // for debugging
    public void dump() {
        System.out.println("Desc[100]: " + getDesc(100));
        System.out.println("ScalingLevelMin: " + scalingLevelMin);
        System.out.println("ScalingLevelMax: " + scalingLevelMax);
        System.out.println("ScalingId: " + scalingId);
        if (statAllocs != null) {
            System.out.println(Arrays.toString(statAllocs));
        }
    }
    
}
