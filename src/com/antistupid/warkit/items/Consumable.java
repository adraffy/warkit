package com.antistupid.warkit.items;

import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.utils.WarBaseFmt;
import com.antistupid.warkit.player.Player;

public class Consumable {
    
    public final int itemId;
    public final String name;
    public final String icon;
    public final int type;
    public final boolean battle;
    public final boolean guardian;
    public final int reqLevel;
    public final ConsumableSpell spell;
    
    public Consumable(int itemId, String name, String icon, int type, int flags, int reqLevel, ConsumableSpell spell) {
        this.itemId = itemId;
        this.name = name;
        this.icon = icon;       
        this.type = type;
        this.reqLevel = reqLevel;
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
    
    public void renderDesc(StringBuilder sb, int playerLevel, StatMap statBuf) {        
        statBuf = StatMap.recycle(statBuf);
        collectStats(statBuf, playerLevel);        
        sb.append(name);
        sb.append(": ");
        statBuf.appendTo(sb, false, false);
        sb.append(" <");
        WarBaseFmt.msDur(sb, spell.duration);
        sb.append(">");
    }
    
    public String getDesc() { return getDesc(Player.MAX_PLAYER_LEVEL); }
    public String getDesc(int playerLevel) {
        StringBuilder sb = new StringBuilder();
        renderDesc(sb, playerLevel, null);
        return sb.toString();
    }
        
    @Override
    public String toString() {
        return String.format("%s<%d:%d>(%s)[%c%c]", getClass().getSimpleName(), itemId, spell.spellId, getDesc(), battle ? 'B' : '_', guardian ? 'G' : '_');
    }
    
}
