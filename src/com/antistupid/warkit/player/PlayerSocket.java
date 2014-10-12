package com.antistupid.warkit.player;

import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.data.PlayerScaling;
import com.antistupid.warbase.types.GemT;
import com.antistupid.warbase.types.SocketT;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;

public class PlayerSocket {

    public final PlayerSlot slot;
    public final int index;
    
    SocketT _socket;
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
        if (_socket == null) {
            sb.append("<Invalid>");
        } else {
            sb.append(_socket.name);
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
        _gem.renderStats(slot.owner.playerLevel, _stats);
    }  
    
    public boolean isGemEffectivelyEqual(PlayerSocket other) {
        return other != null && _stats.isSame(other._stats) && Gem.areEffectivelyEqualWithoutScaling(_gem, other._gem);
    }
    
    public boolean isValid() {
        return _socket != null;
    }
    
    public boolean isEmpty() {
        return _gem == null;
    }
    
    public SocketT getSocketColor() {
        return _socket;
    }
    
    public GemT getGemColor() {
        return _socket != null && _gem != null ? _gem.type : null;
    }
    
    public boolean matches(boolean bonus) {
        return _socket != null && _gem != null && _socket.matches(_gem.type, bonus);
    }
    
    public String getGemName(boolean nullIfEmpty) {
        if (_socket == null) {
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
        if (_socket == null) {
            throw new PlayerError.EquipSocket(this, gem, "No socket available");
        }
        slot.checkItem(gem, index);
        if (!_socket.matches(gem.type, false)) {
            throw new PlayerError.EquipSocket(this, gem, String.format("%s socket does not accept %s", _socket.name, gem.type));
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
