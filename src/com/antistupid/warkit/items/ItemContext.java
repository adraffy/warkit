package com.antistupid.warkit.items;

public class ItemContext {
    
    public final int index;
    public final String context;
    public final ItemBonusCluster defaultBonus;
    public final ItemBonusCluster[] optionalBonuses;
    public final ItemBonus[] components;
    
    public ItemContext(int index, String context, ItemBonusCluster defaultBonus, ItemBonusCluster[] auxBonuses, ItemBonus[] components) {
        this.index = index;
        this.context = context;
        this.defaultBonus = defaultBonus;
        this.optionalBonuses = auxBonuses;
        this.components = components;
    }
    
    public String getName() {
        return defaultBonus.name;
    } 
    
    public int getOptionCount() {
        return optionalBonuses != null ? optionalBonuses.length : 0;
    }
    

}
