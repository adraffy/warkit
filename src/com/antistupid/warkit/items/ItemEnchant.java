package com.antistupid.warkit.items;

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
    
    abstract public boolean canApply(Item item);
    
    @Override
    public String toString() {
        return String.format("%s<%s>(%s)", getClass().getSimpleName(), spellId, name);
    }
    
    
    
}
