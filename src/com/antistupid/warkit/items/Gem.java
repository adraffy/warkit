package com.antistupid.warkit.items;

import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.StatMap;
import com.antistupid.warbase.data.PlayerScaling;
import com.antistupid.warbase.types.BindT;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.GemT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.QualityT;

public class Gem extends Item<GemT> {

    static public boolean areEffectivelyEqualWithoutScaling(Gem a, Gem b) {
        return a == null || b == null ? a == b : a.statAllocs == b.statAllocs && a.spellId == b.spellId;        
    }
    
    public final int gemIndex;
    public final int scalingLevelMin;
    public final int scalingLevelMax;
    public final int reqItemLevel;  
    public final int scalingId;
    public final int scalingPerLevel;
    public final int spellId;
    public final StatAlloc[] statAllocs;

    public Gem(
            int itemId, int itemLevel, GemT type, QualityT quality, EquipT equip, 
            BindT bind, Unique unique, String name, String text, String fileName, 
            ProfT reqSkill, int reqSkillRank, int reqRepId, int reqRepRank,
            long reqRace, long reqClass,
            int gemIndex, int reqItemLevel, int scalingLevelMin, int scalingLevelMax, 
            int scalingId, int scalingPerLevel,
            int spellId, StatAlloc[] statAllocs) {        
        super(
                itemId, itemLevel, type, quality, equip, 
                bind, unique, name, text, fileName,
                reqSkill, reqSkillRank, reqRepId, reqRepRank,
                reqRace, reqClass                
        );
        this.gemIndex = gemIndex;
        this.reqItemLevel = reqItemLevel;
        this.scalingLevelMin = scalingLevelMin;
        this.scalingLevelMax = scalingLevelMax;
        this.scalingId = scalingId;
        this.scalingPerLevel = scalingPerLevel;
        this.spellId = spellId;
        this.statAllocs = statAllocs;        
    }
    

    @Override
    public void appendTo(StringBuilder sb) {
        super.appendTo(sb);
        sb.append("ReqItemLevel: ").append(reqItemLevel).append("\n");
        sb.append("ScalingLevelRange: ").append(scalingLevelMin).append(" - ").append(scalingLevelMax).append("\n");
        sb.append("ScalingId: ").append(scalingId).append(" / ScalingPerLevel: ").append(scalingPerLevel).append("\n");
        sb.append("SpellId: ").append(spellId).append("\n");
        if (statAllocs != null) {
            for (int i = 0; i < statAllocs.length; i++) {
                sb.append("StatAlloc").append(1 + i).append(": ").append(statAllocs[i]).append("\n");
            }
        }
    }
    
    public void renderStats(int playerLevel, StatMap stats) {
        stats.clear();
        if (statAllocs != null) {           
            if (scalingLevelMax == 0) {
                for (StatAlloc x: statAllocs) {
                    stats.add(x.stat, x.alloc);
                }                
            } else {
                int lvl = Math.min(playerLevel, scalingLevelMax);  
                int min = scalingLevelMin;
                float scaling = PlayerScaling.get(Math.max(min, lvl), scalingId);
                /*if (perLevel > 0 && lvl > min) {                
                    scaling *= (min + perLevel * (lvl - min)) / lvl;                
                } */              
                //System.out.println("SCALING: " + scaling + ":" + _gem.scalingId  + perLevel);
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
    }
    
}
