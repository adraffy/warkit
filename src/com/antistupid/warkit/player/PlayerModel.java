package com.antistupid.warkit.player;

import com.antistupid.warbase.data.HealthCurve;
import com.antistupid.warbase.data.ManaCurve;
import com.antistupid.warbase.stats.CompactBaseStats;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.RatingT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.StatT;
import com.antistupid.warbase.types.WeaponT;
import com.antistupid.warkit.items.Weapon;
import com.antistupid.warkit.items.Wearable;
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
    
    private double baseVersa;
    private double baseCrit;
    private double baseHaste;
    private double baseMastery;
    private double baseMulti;
    private double baseCritMod;
    
    private double staminaCoeff;
    
    final Player player;
    
    public PlayerModel(Player player) {
        this.player = player;
    }
    
    public int getStat(StatT stat) {
        if (stat == null) {
            return 0;
        }
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
    
    public double getMovementSpeed() {
        return 1 + getRatingPerc(RatingT.SPEED) + getStat(StatT.SPEED_PERC) * 0.01;
    }
    
    public double getVersaDamageDonePerc() {
        return baseVersa + getRatingPerc(RatingT.VERSA_DAMAGE_DONE);
    }
    
    public double getVersaDamageTakenPerc() {
        return baseVersa + getRatingPerc(RatingT.VERSA_DAMAGE_TAKEN);
    }
    
    public double getCritChance() {
        return baseCrit + getRatingPerc(RatingT.CRIT);
    }
    
    public double getHasteMod() {
        return baseHaste * (1 + getRatingPerc(RatingT.HASTE));
    }
    
    public double getMultiChance() {
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
    
    public int getMaximumHealth() {
        return (int)(getStat(StatT.HP) + staminaCoeff * getStat(StatT.STA));
    }
    
    public int getMaximumMana() {
        return getStat(StatT.MP) + playerManaMax;
    }
    
    public int getAP() {        
        return getStat(StatT.AP) + getStat(apStat);
    }
    
    public int getSP() {        
        return getStat(StatT.SP) + getStat(StatT.INT) + getStat(spStat);
    }
    
    public double getAttackSpeed_MH() {
        return getAttackSpeed(player.MH._item);
    }
    public double getAttackSpeed_OH() {
        return getAttackSpeed(player.OH._item);
    }
    
    private double getAttackSpeed(Wearable item) {
        return (item instanceof Weapon ? ((Weapon)item).speed : WeaponT.FIST_SPEED) / (1000D * getHasteMod());
    }
    
    
    
    // ---- 
    
    public boolean nightTime = true;
    
    //private SpecT spec;
    
    private StatT apStat;
    private StatT spStat;
    
    public void update() {
        //spec = p.spec;
        Arrays.fill(statMod, 1);
        Arrays.fill(ratingMod, 1);
        for (RatingT x: RatingT.db.types) {
            ratingCoeff[x.index] = x.getCoeff(player.playerLevel);
        }
        baseStats.clear();
        gearStats.clear();
        baseCritMod = 2;
        baseCrit = 0.05;
        baseHaste = 1;
        baseMulti = 0;
        baseVersa = 0;
        boolean hasMastery = player.playerLevel >= SpecT.PLAYER_LEVEL_MASTERY;        
        baseMastery = hasMastery ? 0.08 : 0; // base mastery           
        apStat = StatT.STR;
        spStat = null;
        if (player.spec != null) {
            CompactBaseStats.collectStats(baseStats, player.race.compactBaseStats);
            CompactBaseStats.collectStats(baseStats, player.spec.classType.getCompactBaseStats(player.playerLevel));
            if (player.spec.primaryStat != StatT.INT) {
                apStat = player.spec.primaryStat;
                if (player.spec.manaHybrid) {
                    // this is wrong for feral/guardian
                    spStat = player.spec.primaryStat;
                }  
            }          
            if (player.hasArmorSpecialization()) {
                statMod[player.spec.primaryStat.index] *= SpecT.ARMOR_SPECIALIZATION_COEFF;
            }
            if (player.playerLevel >= SpecT.PLAYER_LEVEL_ATTUNE_RATING) {
                ratingMod[player.spec.attuneRating.index] *= SpecT.ATTUNE_RATING_COEFF;
            }            
            if (player.spec.hasCritialStrikes()) {
                baseCrit += 0.1;
            }            
        }
        if (raidBuff_versa) {
            baseVersa += 0.03;
        }
        if (raidBuff_crit) {
            baseCrit += 0.05;
        }
        if (raidBuff_haste) {
            baseHaste *= 1.05;
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

        if (player.race == RaceT.DRAENEI) {
            int value = RaceT.getDraenei_heroicPresence_agiIntStr(player.playerLevel);
            baseStats.add(StatT.AGI, value);
            baseStats.add(StatT.INT, value);
            baseStats.add(StatT.STR, value);                
        } else if (player.race == RaceT.DWARF) {
            baseCritMod *= 1.02;
        } else if (player.race == RaceT.GNOME) {
            baseHaste *= 1.01;
        } else if (player.race == RaceT.HUMAN) {
            baseStats.add(StatT.AGI, RaceT.getHuman_theHumanSpirit_versa(player.playerLevel));
        } else if (player.race == RaceT.NE) {
            if (nightTime) {
                baseHaste *= 1.01;
            } else {
                baseCrit += 0.01;
            }
            baseStats.add(StatT.SPEED_PERC, 2);
        } else if (RaceT.isPandaren(player.race)) {
            // 2x well fed
        } else if (player.race == RaceT.WORGEN) {
            baseCrit += 0.01;
        } else if (player.race == RaceT.BE) {
            baseCrit += 0.01;
        } else if (player.race == RaceT.TAUREN) {
            baseCritMod *= 1.02;
            baseStats.add(StatT.STA, RaceT.getTauren_endurance_sta(player.playerLevel));
        }

        
        staminaCoeff = HealthCurve.get(player.playerLevel);
        playerManaMax = ManaCurve.get(player.playerLevel, player.spec);

        player.collectStats(gearStats);
        
        
        if (player.spec != null) {
            if (player.spec.hasAttackPowerMasteryBonus()) {
                statMod[StatT.AP.index] *= 1 + getMasteryPerc();
            }
            if (!player.spec.role.bonusArmor) {
                gearStats.clear(StatT.ARMOR);
            }
            if (!player.spec.role.spirit) {
                gearStats.clear(StatT.SPI);
            }            
        }
        
        /*
        System.out.println(baseStats);
        System.out.println(gearStats);
        
        System.out.println("Health: " + getMaximumHealth());
        System.out.println("Mana: " + getMaximumHealth());
        System.out.println("Crit: " + getCritChance() + " [" + getRating(RatingT.CRIT) + "] (" + ratingCoeff[RatingT.CRIT.index] + ")");
        System.out.println("Multi: " + getMultiChance() + " [" + getRating(RatingT.MULTI) + "] (" + ratingCoeff[RatingT.MULTI.index] + ")");
        System.out.println("Haste: " + getHasteMod() + " [" + getRating(RatingT.HASTE) + "] (" + ratingCoeff[RatingT.HASTE.index] + ")");
        System.out.println("Mastery: " + getMasteryPerc() * p.spec.masteryCoeff + " [" + getRating(RatingT.MASTERY) + "] (" + ratingCoeff[RatingT.MASTERY.index] + ")");
                */
    }
    
}
