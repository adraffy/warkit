package com.antistupid.warkit.player;

import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.data.PlayerScaling;
import com.antistupid.warbase.types.GemT;
import com.antistupid.warbase.types.SocketT;
import com.antistupid.warbase.types.StatT;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;

public class PlayerSocket {

    public final PlayerSlot slot;
    public final int index;
    
    SocketT _socketType;
    Gem _gem;    
    final StatMap _stats = new StatMap();
    
    PlayerSocket(PlayerSlot slot, int index) {
        this.slot = slot;
        this.index = index;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();        
        sb.append("[");
        sb.append(index);
        sb.append("] ");
        if (_socketType == null) {
            sb.append("<Invalid>");
        } else {
            sb.append(_socketType.name);
            sb.append(": ");
            if (_gem == null) {
                sb.append("Empty");                
            } else {
                sb.append("[");
                _stats.appendTo(sb, true, false);
                sb.append("]");
                if (_gem.spellId != 0) {
                    sb.append(" (Spell:");
                    sb.append(_gem.spellId);
                    sb.append(")");
                }
            }            
        }    
        return sb.toString();
    }
    
    public void clear() {
        if (_gem == null) {
            return;
        }           
        _gem = null;
        _stats.clear();        
        if (slot._socketBonusSatisfied) {
            slot.updateSocketBonus();
        }
    }
    
    void copy(PlayerSocket socket) {
        setGem(socket._gem);
        if (_gem == null) return;              
    }
    
    void update() {
        if (_gem == null) {
            return;
        }
        _stats.clear();
        _gem.collectStats(_stats, slot.owner.playerLevel);
    }  
    
    public boolean isGemEffectivelyEqual(PlayerSocket other) {
        return other != null && _stats.isSame(other._stats) && Gem.areEffectivelyEqualWithoutScaling(_gem, other._gem);
    }
    
    public boolean isValid() {
        return _socketType != null;
    }
    
    public boolean isEmpty() {
        return _gem == null;
    }
    
    public SocketT getSocketColor() {
        return _socketType;
    }
    
    public GemT getGemColor() {
        return _socketType != null && _gem != null ? _gem.type : null;
    }
    
    public boolean matches(boolean bonus) {
        return _socketType != null && _gem != null && _socketType.matches(_gem.type, bonus);
    }
    
    public int getStat(StatT stat, boolean effective) {
        return _socketType != null && _gem != null ? _stats.get(stat, effective) : 0;
    }
    
    public String getGemName(boolean nullIfEmpty) {
        if (_socketType == null) {
            return nullIfEmpty ? null : "No Socket";
        } else if (_gem == null) {
            return nullIfEmpty ? null : "No Gem";
        } else {
            return _gem.name;
        }
    }
    
    public Gem getGem() {
        return _gem;
    }
    
    public void setGem(Item item) {
        if (item == null) {            
            clear();
            return;
        }
        if (!(item instanceof Gem)) {
            throw new PlayerError.EquipSocket(this, item, item.name + " is not a gem");
        }
        Gem gem = (Gem)item;
        if (_socketType == null) {
            throw new PlayerError.EquipSocket(this, gem, "No socket available");
        }
        slot.checkItem(gem, index);
        if (!_socketType.matches(gem.type, false)) {
            throw new PlayerError.EquipSocket(this, gem, String.format("%s socket does not accept %s", _socketType.name, gem.type));
        }
        if (slot._item.itemLevel < gem.reqItemLevel) {
            throw new PlayerError.EquipSocket(this, gem, String.format("%s item level too low (%d required)", slot._item, gem.reqItemLevel));
        }
        _gem = gem;
        update();
        slot.updateSocketBonus();
    }
    
    public void appendGemStatsTo(StringBuilder sb, boolean tiny) {
        if (_gem == null) {
            return;
        }
        _stats.appendTo(sb, tiny, false);  
    }
    
}
