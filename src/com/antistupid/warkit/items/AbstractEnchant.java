package com.antistupid.warkit.items;

import com.antistupid.warbase.stats.StatMap;

abstract public class AbstractEnchant {

    public final int spellId;
    public final String spellName;
    public final String spellDesc;
    public final String spellIcon;
    public final int maxItemLevel;
    public final boolean isTinker;
    public final Enchantment enchantment;
    
    AbstractEnchant(int spellId, String spellName, String spellDesc, String spellIcon, int maxItemLevel, boolean isTinker, Enchantment enchantment) {
        this.spellName = spellName;
        this.spellDesc = spellDesc;
        this.spellIcon = spellIcon;
        this.spellId = spellId;
        this.maxItemLevel = maxItemLevel;
        this.isTinker = isTinker;
        this.enchantment = enchantment;        
    }
    
    public boolean checkItemLevel(int itemLevel) {
        return maxItemLevel == 0 || itemLevel <= maxItemLevel;
    }
    
    abstract public boolean canApply(Item item);
    
    @Override
    public String toString() {
        return String.format("%s<%s>(%s)", getClass().getSimpleName(), spellId, spellName);
    }
    
    
    
}
