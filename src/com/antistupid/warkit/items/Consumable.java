package com.antistupid.warkit.items;

import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.structs.StatAlloc;

public class Consumable {
    
    public final int itemId;
    public final String name;
    public final String icon;
    public final int type;
    public final boolean battle;
    public final boolean guardian;
    public final ConsumableSpell spell;
    
    public Consumable(int itemId, String name, String icon, int type, int flags, ConsumableSpell spell) {
        this.itemId = itemId;
        this.name = name;
        this.icon = icon;       
        this.type = type;
        this.spell = spell;
        this.battle = (flags & 0x1) != 0;
        this.guardian = (flags & 0x02) != 0;
    }
    
    public StatMap getStats(int playerLevel) {
        StatMap stats = new StatMap();
        collectStats(stats, playerLevel);
        return stats;
    }
    
    public void collectStats(StatMap stats, int playerLevel) {
        StatAlloc.collectSpellStats(stats, spell.statAllocs, playerLevel, 0, spell.scalingLevelMax, spell.scalingId);
    }
    
    @Override
    public String toString() {
        return String.format("%s<%s>(%s)", getClass().getSimpleName(), itemId, name);
    }
        
}
