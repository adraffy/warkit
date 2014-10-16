package com.antistupid.warkit.player;

import com.antistupid.warbase.data.HealthCurve;
import com.antistupid.warbase.data.ManaCurve;
import com.antistupid.warbase.stats.CompactBaseStats;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.RatingT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.StatT;
import java.util.Arrays;
import java.util.function.Consumer;

public class PlayerModel {
    
    public boolean raidBuff_versa; // +3% versa
    public boolean raidBuff_multi; // +5% multi
    public boolean raidBuff_stats; // +5% stats
    public boolean raidBuff_sta; // +10% Stamina
    public boolean raidBuff_crit; // +5% crit
    public boolean raidBuff_haste; // +5% haste
    public boolean raidBuff_mastery; // +550 mastery (110 per)
    public boolean raidBuff_ap; // +10% ap
    public boolean raidBuff_sp; // +10% sp
    
    // --------------
    
    int playerManaMax;
    
    private final double[] statMod = new double[StatT.db.size()];
    private final StatMap baseStats = new StatMap();
    private final StatMap gearStats = new StatMap();    
    
    private final double[] ratingMod = new double[RatingT.db.size()];
    private final float[] ratingCoeff = new float[RatingT.db.size()];
    private double baseCrit;
    private double baseHaste;
    private double baseMastery;
    private double baseMulti;
    private double baseCritMod;
    
    private double staminaCoeff;
    
    
    public PlayerModel() {
    }
    
    public int getStat(StatT stat) {
        double mod = statMod[stat.index];
        return (int)(mod * baseStats.getEffective(stat)) + (int)(mod * gearStats.getEffective(stat));
    }
    
    public int getRating(RatingT rating) {
        return (int)(getStat(rating.stat) * ratingMod[rating.index]);
    }
    
    public double getRatingPerc(RatingT rating) {
        return getRating(rating) / ratingCoeff[rating.index] / 100D;
    }
    
    // -----
    
    public double getCritPerc() {
        return baseCrit + getRatingPerc(RatingT.CRIT);
    }
    
    public double getHastePerc() {
        return baseHaste + getRatingPerc(RatingT.HASTE);
    }
    
    public double getMultiPerc() {
        return baseMulti + getRatingPerc(RatingT.MULTI);
    }
    
    public double getMasteryPerc() {
        return baseMastery + getRatingPerc(RatingT.MASTERY);
    }
    
    /*
    public double getSpecializationMasteryPerc() {
        return getMasteryPerc() * SpecT.FERAL.masteryCoeff;
    }
    */
    
    public double getMaximumHealth() {
        return getStat(StatT.HP) + staminaCoeff * getStat(StatT.STA);
    }
    
    // ---- 
    
    public boolean nightTime;
    
    public void setup(Player p, Consumer<String> errorSink) {
        Arrays.fill(statMod, 1);
        Arrays.fill(ratingMod, 1);
        for (RatingT x: RatingT.db.types) {
            ratingCoeff[x.index] = x.getCoeff(p.playerLevel);
        }
        baseStats.clear();
        CompactBaseStats.collectStats(baseStats, p.race.compactBaseStats);
        CompactBaseStats.collectStats(baseStats, p.spec.classType.getCompactBaseStats(p.playerLevel));
        
        if (p.hasArmorSpecialization()) {
            statMod[p.spec.primaryStat.index] *= SpecT.ARMOR_SPECIALIZATION_COEFF;
        }
        if (p.playerLevel >= SpecT.PLAYER_LEVEL_ATTUNE_RATING) {
            ratingMod[p.spec.attuneRating.index] *= SpecT.ATTUNE_RATING_COEFF;
        }
        
        baseCritMod = 2;
        baseCrit = 0.05;
        baseHaste = 1;
        baseMulti = 0;
        
        boolean hasMastery = p.playerLevel >= SpecT.PLAYER_LEVEL_MASTERY;        
        baseMastery = hasMastery ? 0.08 : 0; // base mastery        

        if (p.spec.hasCritialStrikes()) {
            baseCrit += 0.1;
        }
        if (raidBuff_crit) {
            baseCrit += 0.05;
        }
        if (raidBuff_haste) {
            baseHaste += 0.05;
        }        
        if (raidBuff_multi) {
            baseMulti += 0.05;
        }
        if (raidBuff_mastery && hasMastery) {
            baseStats.add(StatT.MASTERY, (int)(5 * ratingCoeff[RatingT.MASTERY.index])); // herp
        }
        if (raidBuff_sta) {
            statMod[StatT.STA.index] *= 1.1;
        }
        if (raidBuff_ap) {
            statMod[StatT.AP.index] *= 1.1;
        }
        if (raidBuff_sp) {
            statMod[StatT.SP.index] *= 1.1;
        }
        if (raidBuff_stats) {
            double coeff = 1.05;
            statMod[StatT.AGI.index] *= coeff;
            statMod[StatT.STR.index] *= coeff;
            statMod[StatT.INT.index] *= coeff;
        }

        if (p.race == RaceT.DRAENEI) {
            int value = RaceT.getDraenei_heroicPresence_agiIntStr(p.playerLevel);
            baseStats.add(StatT.AGI, value);
            baseStats.add(StatT.INT, value);
            baseStats.add(StatT.STR, value);                
        } else if (p.race == RaceT.DWARF) {
            baseCritMod *= 1.02;
        } else if (p.race == RaceT.GNOME) {
            baseHaste += 0.01;
        } else if (p.race == RaceT.HUMAN) {
            baseStats.add(StatT.AGI, RaceT.getHuman_theHumanSpirit_versa(p.playerLevel));
        } else if (p.race == RaceT.NE) {
            if (nightTime) {
                baseHaste += 0.01;
            } else {
                baseCrit += 0.01;
            }
        } else if (RaceT.isPandaren(p.race)) {
            // 2x well fed
        } else if (p.race == RaceT.WORGEN) {
            baseCrit += 0.01;
        } else if (p.race == RaceT.BE) {
            baseCrit += 0.01;
        } else if (p.race == RaceT.TAUREN) {
            baseCritMod *= 1.02;
            baseStats.add(StatT.STA, RaceT.getTauren_endurance_sta(p.playerLevel));
        }

        
        staminaCoeff = HealthCurve.get(p.playerLevel);
        playerManaMax = ManaCurve.getHybrid(p.playerLevel);

        p.collectStats(gearStats);
        
        System.out.println(baseStats);
        System.out.println(gearStats);
        
        System.out.println("Health: " + getMaximumHealth());
        System.out.println("Mana: " + getMaximumHealth());
        System.out.println("Crit: " + getCritPerc() + " [" + getRating(RatingT.CRIT) + "] (" + ratingCoeff[RatingT.CRIT.index] + ")");
        System.out.println("Multi: " + getMultiPerc() + " [" + getRating(RatingT.MULTI) + "] (" + ratingCoeff[RatingT.MULTI.index] + ")");
        System.out.println("Haste: " + getHastePerc() + " [" + getRating(RatingT.HASTE) + "] (" + ratingCoeff[RatingT.HASTE.index] + ")");
        System.out.println("Mastery: " + getMasteryPerc() * p.spec.masteryCoeff + " [" + getRating(RatingT.MASTERY) + "] (" + ratingCoeff[RatingT.MASTERY.index] + ")");
    }
    
}
