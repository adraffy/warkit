package com.antistupid.warkit.armory;

import com.antistupid.warbase.types.ClassT;
import com.antistupid.warbase.types.RaceT;
import java.util.Comparator;

public class ArmorySearchResult {

    public final String name;
    public final String realmName;
    public final String realmSlug;
    public final int level;
    public final RaceT race;
    public final boolean male;
    public final ClassT cls;
    public final String guild; // null for no guild
    
    ArmorySearchResult(String name, String realmName, String realmSlug, int level, RaceT race, boolean male, ClassT cls, String guild) {
        this.name = name;
        this.realmName = realmName;
        this.realmSlug = realmSlug;
        this.level = level;
        this.race = race;
        this.male = male;
        this.cls = cls;
        this.guild = guild;
    }
    
    @Override
    public String toString() { // fix me
        return name + ":" + realmName + ":" + realmSlug + ":" + level + ":" + race + ":" + cls + ":" + guild;
    }
    
    static public final Comparator<ArmorySearchResult> CMP_levelZA_classAZ = (a, b) -> {
        int c = Integer.compare(b.level, a.level);
        return c == 0 ? Integer.compare(a.cls.index, b.cls.index) : c;
    };
    
    static public final Comparator<ArmorySearchResult> CMP_classAZ_levelZA = (a, b) -> {
        int c = Integer.compare(a.cls.index, b.cls.index);
        return c == 0 ? Integer.compare(b.level, a.level) : c;
    };
    
}
