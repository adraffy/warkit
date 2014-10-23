package com.antistupid.warkit.items;

import java.util.Arrays;
import com.antistupid.warbase.types.SpecT;

public class ItemSet {
    
    public final int id;
    public final String name;
    public final int size;
    public final ProfValue reqProf;
    public final SpecT[] specs;
    public final SetBonus[][] bonuses;
    
    public ItemSet(int id, String name, int size, ProfValue reqProf, int specCount) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.reqProf = reqProf;
        specs = new SpecT[specCount];
        bonuses = new SetBonus[specCount][];
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>(%s,%d,%s)%s", getClass().getSimpleName(), id, name, size, reqProf, Arrays.toString(specs));
    }

}
