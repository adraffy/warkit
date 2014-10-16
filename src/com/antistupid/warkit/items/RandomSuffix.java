package com.antistupid.warkit.items;

import com.antistupid.warbase.structs.StatAlloc;

public class RandomSuffix {

    public final int id;
    public final String name; 
    public final StatAlloc[] statAllocs; 
    public final ItemBonus bonus;
    
    public RandomSuffix(int id, String name, StatAlloc[] statAllocs, ItemBonus bonus) {
        this.id = id;
        this.name = name;
        this.statAllocs = statAllocs;
        this.bonus = bonus;
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>(\"%s\")", getClass().getSimpleName(), id, name);
    }
    
}
