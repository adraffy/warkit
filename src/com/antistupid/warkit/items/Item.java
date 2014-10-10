package com.antistupid.warkit.items;

import java.util.Comparator;
import com.antistupid.warbase.types.BindT;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.types.TypeT;

abstract public class Item<T extends TypeT> {
    
    static public final Comparator<Item> CMP_ITEM_LEVEL = (a, b) -> a.itemLevel - b.itemLevel;
    
    static public final Comparator<Item> CMP_ITEM_POWER = (a, b) -> {
        int c = b.itemLevel - a.itemLevel;
        return c != 0 ? c : b.quality.id - a.quality.id;
    };

    public final int itemId;
    public final int itemLevel; 
    public final T type;
    public final QualityT quality;
    public final EquipT equip;
    
    public final BindT bind;
    public final Unique unique;
    
    public final String name;   
    public final String text;
    public final String icon; 
    
    public final ProfT reqProf;
    public final int reqProfLevel;        
    
    public final int reqRepId;
    public final int reqRepRank;
    
    public final long reqRace;   
    public final long reqClass;

    public Item(
            int itemId, int itemLevel, T type, QualityT quality, EquipT equip, 
            BindT bind, Unique unique, String name, String text, String fileName,
            ProfT reqProf, int reqProfLevel, int reqRepId, int reqRepRank,
            long reqRace, long reqClass) {
        this.itemId = itemId;
        this.itemLevel = itemLevel;
        this.type = type;
        this.quality = quality;
        this.equip = equip;
        this.bind = bind;
        this.unique = unique;
        this.name = name;
        this.text = text;
        this.icon = fileName;
        //this.reqLevel = reqLevel;
        //this.reqLevelMax = reqLevelMax;
        this.reqProf = reqProf;
        this.reqProfLevel = reqProfLevel;
        this.reqRepId = reqRepId;
        this.reqRepRank = reqRepRank;
        this.reqRace = reqRace;
        this.reqClass = reqClass;
    }
    
    public String getNameAndId() {
        return String.format("%s<%d>", name, itemId);
    }
        
    public int getRandPropIndex() {
        return equip.randPropIndex;
    }
    
    public void dump() {
        StringBuilder sb = new StringBuilder();
        appendTo(sb);
        System.out.println(sb);
    }
        
    public void appendTo(StringBuilder sb) {
        sb.append("ItemId: ").append(itemId).append("\n");
        sb.append("ItemLevel: ").append(itemLevel).append("\n");  
        sb.append("Type: ").append(type).append("\n");
        sb.append("Quality: ").append(quality).append("\n");
        sb.append("Equip: ").append(bind).append("\n");
        sb.append("Unique: ").append(unique).append("\n");   
        sb.append("Name: ").append(name).append("\n");             
        sb.append("Text: ").append(text).append("\n"); 
        sb.append("File: ").append(icon).append("\n"); 
        sb.append("ReqProf: ").append(reqProf).append(" - ").append(reqProfLevel).append("\n");    
        sb.append("ReqRep: ").append(reqRepId).append(" - ").append(reqRepRank).append("\n");    
        sb.append("ReqRace: ").append(reqRace).append("\n");    
        sb.append("ReqClass: ").append(reqClass).append("\n"); 
    }
    
    @Override
    public String toString() {
        return String.format("%s<%d>", name, itemId);
    }
    
}
