package com.antistupid.warkit.items;

public class BonusGroup {

    public final int id;
    public final ItemBonus[] components;
    public final NamedItemBonus[] universe;
    public final int defaultIndex;
    
    public BonusGroup(int id, ItemBonus[] components, NamedItemBonus[] universe, int defaultIndex) {
        this.id = id;
        this.components = components;
        this.universe = universe;
        this.defaultIndex = defaultIndex;
    }
    
}
