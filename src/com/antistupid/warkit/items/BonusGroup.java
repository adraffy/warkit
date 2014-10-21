package com.antistupid.warkit.items;

public class BonusGroup {

    public final int id;
    public final ItemBonus[] components;
    public final ItemBonusCluster[] universe;
    public final int defaultIndex;
    
    public BonusGroup(int id, ItemBonus[] components, ItemBonusCluster[] universe, int defaultIndex) {
        this.id = id;
        this.components = components;
        this.universe = universe;
        this.defaultIndex = defaultIndex;
    }
    
}
