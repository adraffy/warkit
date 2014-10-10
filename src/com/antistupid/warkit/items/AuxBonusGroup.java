package com.antistupid.warkit.items;

public class AuxBonusGroup {

    public final int id;
    public final ItemBonus[] components;
    public final NamedItemBonus[] universe;
    
    public AuxBonusGroup(int id, ItemBonus[] components, NamedItemBonus[] universe) {
        this.id = id;
        this.components = components;
        this.universe = universe;
    }
    
}
