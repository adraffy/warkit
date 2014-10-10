package com.antistupid.warkit.items;

import com.antistupid.warbase.types.SpecT;

public class SetBonus {
    
    public final ItemSet set;
    public final SpecT spec;
    public final int index;
    public final int thres;
    public final int spellId;
    public final String name;
    public final String desc;
    
    public SetBonus(ItemSet set, SpecT spec, int index, int thres, int spellId, String name, String desc) {
        this.set = set;
        this.spec = spec;
        this.index = index;
        this.thres = thres;
        this.spellId = spellId;
        this.name = name;
        this.desc = desc; // has vars in it
    }
    
    @Override
    public String toString() {
        return String.format("[%d]<%d>(%s)", thres, spellId, name, desc);
    }

}
