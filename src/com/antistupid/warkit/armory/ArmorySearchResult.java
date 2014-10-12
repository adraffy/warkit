package com.antistupid.warkit.armory;

import com.antistupid.warbase.types.ClassT;
import com.antistupid.warbase.types.RaceT;
import java.util.Comparator;

public class ArmorySearchResult {

    public String name;
    public String realmName;
    public String realmSlug;
    public int level;
    public RaceT race;
    public ClassT cls;
    
    ArmorySearchResult(String name, String realm, String realmSlug, int level, RaceT race, ClassT cls) {
        this.name = name;
        this.realmName = realm;
        this.realmSlug = realmSlug;
        this.level = level;
        this.race = race;
        this.cls = cls;
    }
    
    @Override
    public String toString() {
        return name + ":" + realmName + ":" + realmSlug + ":" + level + ":" + race + ":" + cls;
    }
    
    static public final Comparator<ArmorySearchResult> CMP_LEVEL_CLASS = (a, b) -> {
        int c = Integer.compare(b.level, a.level);
        return c == 0 ? Integer.compare(a.cls.index, b.cls.index) : c;
    };
    
    static public final Comparator<ArmorySearchResult> CMP_CLASS_LEVEL = (a, b) -> {
        int c = Integer.compare(a.cls.index, b.cls.index);
        return c == 0 ? Integer.compare(b.level, a.level) : c;
    };
    
}
