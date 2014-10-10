package com.antistupid.warkit.items;

import java.util.Arrays;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.SpecT;

public class ItemSet {
    
    public final int id;
    public final String name;
    public final ProfT reqProf;
    public final int reqProfLevel;
    public final SpecT[] specs;
    public final SetBonus[][] bonuses;
    
    public ItemSet(int id, String name, ProfT reqProf, int reqProfLevel, int specCount) {
        this.id = id;
        this.name = name;
        this.reqProf = reqProf;
        this.reqProfLevel = reqProfLevel;
        specs = new SpecT[specCount];
        bonuses = new SetBonus[specCount][];
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>(%s,%s,%d)%s", getClass().getSimpleName(), id, name, reqProf, reqProfLevel, Arrays.toString(specs));
    }

}
