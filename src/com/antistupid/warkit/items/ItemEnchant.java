package com.antistupid.warkit.items;

import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warkit.player.Player;

abstract public class ItemEnchant {

    public final int spellId;
    public final int itemId;
    public final String name;
    public final String spellDesc;
    public final String icon;
    public final int maxItemLevel;
    public final boolean isTinker;
    public final boolean isRetired;
    public final Enchantment enchantment;
    
    ItemEnchant(int spellId, int itemId, String name, String spellDesc, String icon, int maxItemLevel, boolean isTinker, boolean isRetired, Enchantment enchantment) {
        this.spellId = spellId;
        this.itemId = itemId;
        this.name = name;
        this.spellDesc = spellDesc;
        this.icon = icon;
        this.maxItemLevel = maxItemLevel;
        this.isTinker = isTinker;
        this.isRetired = isRetired;
        this.enchantment = enchantment;        
    }
    
    public boolean checkItemLevel(int itemLevel) {
        return maxItemLevel == 0 || itemLevel <= maxItemLevel;
    }
    
    // does not check item level
    // does not check tinker
    abstract public boolean canApply(Item item);
    
    public void renderDesc(StringBuilder sb, int playerLevel, StatMap statBuf) {
        sb.append(name);
        if (enchantment.hasDesc) {
            sb.append(": ");
            enchantment.renderDesc(sb, playerLevel, statBuf);
        }
    }
    
    public String getDesc() { return getDesc(Player.MAX_PLAYER_LEVEL); }
    public String getDesc(int playerLevel) {
        StringBuilder sb = new StringBuilder();
        renderDesc(sb, playerLevel, null);
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("%s<%s>(%s)", getClass().getSimpleName(), spellId, name);
    }
    
}
