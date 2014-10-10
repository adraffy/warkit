package com.antistupid.warkit.player;

import com.antistupid.warkit.items.Item;

public class PlayerError extends RuntimeException {

    PlayerError(String msg) {
        super(msg);
    }
    
    
    static public class Slot extends PlayerError {
        public final PlayerSlot slot;
        public final String slotError;
        Slot(PlayerSlot slot, String msg) {
            super(slot.slotType.name + ": " + msg);
            this.slot = slot;
            this.slotError = msg;
        }
    }
      
    static public class EquipSlot extends Slot {
        public final Item item;
        public final String equipError;
        EquipSlot(PlayerSlot slot, Item item, String msg) {
            super(slot, String.format("Unable to equip \"%s\": %s", item.name, msg));
            this.item = item;
            this.equipError = msg;
        }
    }
    
    static public class EquipSocket extends EquipSlot {
        public final PlayerSocket socket;
        public final Item gem;
        EquipSocket(PlayerSocket socket, Item gem, String msg) {
            super(socket.slot, socket.slot._item, String.format("Unable to gem[%d] \"%s\": %s", 1 + socket.index, gem.name, msg));
            this.socket = socket;
            this.gem = gem;
        }
        
    }
    
    
    
    
    
}
