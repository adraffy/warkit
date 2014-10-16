package com.antistupid.warkit.player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import com.antistupid.warbase.IntSet;
import com.antistupid.warkit.items.RandomSuffix;
import com.antistupid.warkit.items.RandomSuffixGroup;
import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.stats.StatMap;
import com.antistupid.warbase.data.ArmorCurve;
import com.antistupid.warbase.data.DamageCurve;
import com.antistupid.warbase.data.ItemLevelCurve;
import com.antistupid.warbase.data.ItemStatCurve;
import com.antistupid.warbase.data.PlayerScaling;
import com.antistupid.warbase.data.SocketCostCurve;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.types.SlotT;
import com.antistupid.warbase.types.SocketT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.StatT;
import com.antistupid.warbase.types.WeaponT;
import com.antistupid.warkit.items.AbstractEnchant;
import com.antistupid.warkit.items.Armor;
import com.antistupid.warkit.items.Enchantment;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.ItemBonus;
import com.antistupid.warkit.items.BonusGroup;
import com.antistupid.warkit.items.NamedItemBonus;
import com.antistupid.warkit.items.Unique;
import com.antistupid.warkit.items.UpgradeChain;
import com.antistupid.warkit.items.Weapon;
import com.antistupid.warkit.items.Wearable;

public class PlayerSlot {

    public final SlotT slotType;
    public final Player owner;
    
    PlayerSlot(Player owner, SlotT type) {
        this.owner = owner;
        this.slotType = type;
        for (int i = 0; i < _socket.length; i++) {
            _socket[i] = new PlayerSocket(this, i);
        }
    } 
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(36);
        sb.append(slotType.name);
        sb.append(": ");
        if (_item == null) {
            sb.append("Empty");
        } else {
            appendItemName(sb, true, true);
        }
        return sb.toString();
    }
    

    Wearable _item; // item or weapon
    int _itemLevelCustom;    
    final PlayerSocket[] _socket = new PlayerSocket[Player.MAX_SOCKETS];
    int _namedBonusIndex;
    int _auxBonusIndex;
    int _suffixIndex;
    int _upgradeIndex;
    boolean _extraSocket;
    AbstractEnchant _enchant;
    
    // computed
    int _socketCount;
    boolean _socketBonusSatisfied;
    int _itemLevelBase;
    int _itemLevelActual;
    int _itemLevelScaled;
    String _nameDesc;
    int _reqLevel;
    QualityT _quality;
    int _baseArmor;
    final StatMap _gearStats = new StatMap();
    final StatMap _socketBonusStats = new StatMap();
    //final ArrayList<ItemBonus> _customBonuses = new ArrayList<>();    
    float _weaponDamage;
    
    public int getGemCount() {
        int n = getSocketCount();
        int c = 0;
        for (int i = 0; i < n; i++) {
            if (_socket[i]._gem != null) {
                c++;
            }            
        }
        return c;        
    }
    
    public int getSocketCount() {
        return _item != null ? _socketCount : 0;
    }
    
    public Gem[] copyGems() {
        Gem[] gems = new Gem[_socketCount];
        for (int i = 0; i < _socketCount; i++) {
            gems[i] = _socket[i]._gem;
        }   
        return gems;
    }
    
    public PlayerSocket getSocket(int i) {
        return i >= 0 && i < _socket.length ? _socket[i] : null;
    }
    
    int uniqueGemCount(Unique unique, int ignoreIndex) {
        int c = 0;
        for (int i = 0; i < _socketCount; i++) {
            if (i == ignoreIndex) continue;
            Gem gem = _socket[i]._gem;
            if (gem == null) continue;
            if (gem.unique == unique) {
                c++;
            }            
        }    
        return c;
    }
    
    public boolean isEmpty() {
        return _item == null; 
    }
    public boolean isArmor() {
        return _item instanceof Armor;
    }
    public boolean isWeapon() {
        return _item instanceof Weapon;
    }
    public Wearable getItem() {
        return _item;
    }
    public void swapItem(Item item) {
        if (_item == null || item == null) {
            setItem(item);
            return;
        }
        // fix me: remember more stuff
        Wearable oldItem = _item;
        int upgradeIndex = _upgradeIndex;
        AbstractEnchant enchant = _enchant;
        boolean extraSocket = _extraSocket;
        Gem[] gems = copyGems();
        setItem(item); // warning: can fail
        setExtraSocket(extraSocket && canExtraSocket()); // cant fail
        try {
            setEnchant(enchant);
        } catch (PlayerError err) {
            // ignore
        }
        if (oldItem.upgrade == _item.upgrade) {
            setUpgradeLevel(upgradeIndex);
        }        
        for (int i = 0; i < _socketCount; i++) {
            try {
                _socket[i].setGem(gems[i]);
            } catch (PlayerError err) {
                // ignore
            }
        }        
    }
    public void setItem(Item item) {
        if (item == null) {            
            clear();          
            return;
        }
        if (!(item instanceof Wearable)) {
            throw new PlayerError.EquipSlot(this, item, String.format("%s is not wearable", item.name));
        }        
        if (!slotType.canContain(item.equip)) {
            throw new PlayerError.EquipSlot(this, item, String.format("%s does not accept %s", slotType.name, item.equip.name));
        }            
        checkWearable((Wearable)item);
        boolean bothHands = false; // save this
        if (item instanceof Weapon) {
            Weapon w = (Weapon)item;
            if (owner.spec != null) {
                if (!owner.spec.classType.canWield(w.type)) {
                    throw new PlayerError.EquipSlot(this, item, String.format("%s cannot use %s", owner.spec.classType, w.type));
                } else if (slotType == SlotT.OFF_HAND && !owner.spec.canDualWield) {
                    throw new PlayerError.EquipSlot(this, item, String.format("%s cannot dual-wield", owner.spec));
                } 
            }
            boolean twoHand = w.isTwoHand();
            if (twoHand) {
                if (!isTitanGrippable(w)) {
                    if (slotType == SlotT.OFF_HAND) {
                        owner.MH.setItem(item);
                        return;
                    } else if (slotType == SlotT.MAIN_HAND) {
                        owner.OH.clear();
                        bothHands = true;
                    }
                }
            } else if (slotType == SlotT.OFF_HAND && owner._bothHandsForMH && !isTitanGrippable(w)) {
                // we're trying to set our offhand
                // but we have a 2hander active
                // so kill the 2h, so we can set the offhand
                owner.MH.clear();
            }   
        } else {
            Armor a = (Armor)item;
            if (owner.spec != null) {
                if (!owner.spec.classType.canWear(owner.playerLevel, a.type)) {
                    throw new PlayerError.EquipSlot(this, item, String.format("%s cannot use %s", owner.spec.classType, a.type));
                }
            }
        }     
        clear();
        _item = (Wearable)item;       
        if (_item.namedBonusGroup != null) {
            _namedBonusIndex = _item.namedBonusGroup.defaultIndex; // fuckign default to raid finder my ass!
        }
        if (slotType == SlotT.MAIN_HAND) {
            owner._bothHandsForMH = bothHands;
        }        
        update();
    }
    
    boolean isTitanGrippable(Weapon w) {
        return owner.spec == SpecT.FURY && owner.playerLevel >= 38 && w.type.isMemberOf(WeaponT.TITANS_GRIP);
    }
    
    void checkWearable(Wearable item) {
        if (owner.playerLevel < item.reqLevel) {
            throw new PlayerError.EquipSlot(this, item, String.format("Requires Level %d", item.reqLevel));
        }        
        checkItem(item, -1);
    }
    
    void checkItem(Item item, int ignoreGemIndex) {        
        if (owner.spec != null && !owner.spec.classType.isMemberOf_passZero(item.reqClass)) {    
            throw new PlayerError.EquipSlot(this, item, String.format("Cannot be used by %s", owner.spec.classType.name));
        }            
        if (owner.race != null && !owner.race.isMemberOf_passZero(item.reqRace)) {
            throw new PlayerError.EquipSlot(this, item, String.format("Cannot be used by %s", owner.race.name));
        }            
        if (item.reqProf != null) {
            int index = owner.findProf(item.reqProf);
            if (index == -1) {
                throw new PlayerError.EquipSlot(this, item, String.format("%s requires %s", item.name, item.reqProf));
            }
            if (owner.PROF[index]._level < item.reqProfLevel) {
                throw new PlayerError.EquipSlot(this, item, String.format("%s requires %s (%d)", item.name, item.reqProf, item.reqProfLevel));
            }            
        }  
        
        Unique u = item.unique;
        if (u != null) {
            int count = owner.uniqueCount(u, slotType.index, ignoreGemIndex);
            if (count >= u.max) {
                throw new PlayerError.EquipSlot(this, item, String.format("The maximum number (%d) of %s have been equipped", u.max, u.name));
            }
        }
    }
    
    
    public void setCustomItemLevel(int level) {
        if (level < 0 || level > Player.MAX_ITEM_LEVEL) { // allow 0
            throw new PlayerError.Slot(this, "Invalid Item Level: " + level);
        }
        _itemLevelCustom = level;
        update();
    }  
    public UpgradeChain getUpgradeChain() {
        if (_item == null || _item.upgrade == null) {
            return null;
        } 
        return _item.upgrade.getChain(owner.asiaMode);    
    }
    public int getUpgradeLevel() {
        return _upgradeIndex; //_item == null ? 0 : _upgradeIndex
    }
    public int getUpgradeItemLevelDelta() {
        UpgradeChain chain = getUpgradeChain();
        if (chain == null) {
            return 0;
        }
        return chain.itemLevelDelta[_upgradeIndex];
    }    
    public void setUpgradeLevelOrDelta(int thing) {
        if (_item == null) {            
            throw new PlayerError.Slot(this, "Cannot Upgrade: Empty Slot");
        }
        UpgradeChain chain = getUpgradeChain();
        if (chain != null) {            
            int up = Arrays.binarySearch(chain.itemLevelDelta, thing);
            if (up >= 0) {
                setUpgradeLevel(up);
                return;
            }            
        } 
        if (_item != null) {
            setCustomItemLevel(getActualItemLevel() + thing);
        }
    }
    public void setUpgradeLevel(int index) {
        if (_item == null) {
            return;
        }
        if (_item.upgrade == null) {
            if (index != 0) {      
                throw new PlayerError.EquipSlot(this, _item, "Not upgradable");
            }            
        } else {
            UpgradeChain chain = _item.upgrade.getChain(owner.asiaMode);     
            if (index < 0 || index >= chain.itemLevelDelta.length) {
                throw new PlayerError.EquipSlot(this, _item, "Invalid upgrade level: " + index + " -> " + chain);
            }
        }
        _itemLevelCustom = 0;
        _upgradeIndex = index;
        update();
    }    
    public void setUpgradeLevelMax() {
        if (_item == null) {
            return;
        }
        _itemLevelCustom = 0;
        if (_item.upgrade != null) {
            UpgradeChain chain = _item.upgrade.getChain(owner.asiaMode);        
            _upgradeIndex = chain.itemLevelDelta.length - 1;
        }
        update();
    }
    public int getScaledItemLevel() {
        return _itemLevelScaled;
    }
    public int getActualItemLevel() {
        return _itemLevelActual;
    } 
    public int getBaseItemLevel() {
        return _itemLevelBase;
    }
    public boolean isItemLevelCustom() {
        return _item != null && _itemLevelCustom != 0;
    }
    public boolean isItemLevelScaled() {
        return _item != null && _itemLevelScaled != _itemLevelActual;
    }
    public RandomSuffixGroup getSuffixGroup() {
        return _item != null ? _item.suffixGroup : null;
    }
    public int getSuffixIndex() {
        return _suffixIndex;
    }
    public void setSuffixIndex(int index) {
        if (_item == null) {
            return;
        } 
        if (_item.suffixGroup == null) {
            throw new PlayerError.EquipSlot(this, _item, "No Suffixes");
        }
        if (index < 0 || index >= _item.suffixGroup.suffixes.length) {
            throw new PlayerError.EquipSlot(this, _item, "Invalid Suffix Index: " + index);
        }
        _suffixIndex = index;
        update();     
    }
    public RandomSuffix getSuffix() {
        if (_item == null || _item.suffixGroup == null) {
            return null;
        } 
        return _item.suffixGroup.suffixes[_suffixIndex];
    }
    /*
    public void setCustomItemBonus(ItemBonus bonus, boolean enable) {
        if (enable) {
            
        }        
    }    
    */
    public void setNamedBonusIndex(int index) {
        if (_item == null) {
            return;
        }
        if (_item.namedBonusGroup == null) {
            throw new PlayerError.EquipSlot(this, _item, "No Named Item Bonuses");
        }
        if (index < 0 || index >= _item.namedBonusGroup.universe.length) {
            throw new PlayerError.EquipSlot(this, _item, "Invalid Named Item Bonus Index: " + index);
        }
        _namedBonusIndex = index;
        update();       
        setExtraSocket(_extraSocket && canExtraSocket());    
    }
    public int getNamedBonusIndex() {
        return _namedBonusIndex;
    }
    public BonusGroup getNamedBonuses() {
        return _item == null ? null : _item.namedBonusGroup;
    }    
    
    public void setAuxBonusIndex(int index) {        
        if (_item == null) {
            return;
        }
        if (_item.auxBonusGroup == null) {
            throw new PlayerError.EquipSlot(this, _item, "No Aux Item Bonuses");
        }
        if (index < 0 || index >= _item.auxBonusGroup.universe.length) {
            throw new PlayerError.EquipSlot(this, _item, "Invalid Aux Item Bonus Index: " + index);
        }
        _auxBonusIndex = index;
        update();        
    }
    public int getAuxBonusIndex() {
        return _auxBonusIndex;
    }
    public BonusGroup getAuxBonuses() {
        return _item == null ? null : _item.auxBonusGroup;
    }
    public IntSet getItemBonuses() {
        IntSet set = new IntSet();
        collectItemBonuses(set);
        return set;
    }
    public void collectItemBonuses(IntSet set) {
        if (_item == null) {
            return;
        }
        if (_item.namedBonusGroup != null) {     
            for (ItemBonus x: _item.namedBonusGroup.universe[_namedBonusIndex].components) {
                set.add(x.id);
            }
        }
        if (_item.auxBonusGroup != null) {
            for (ItemBonus x: _item.auxBonusGroup.universe[_auxBonusIndex].components) {
                set.add(x.id);
            }            
        }
    }    
    // warning: mutates set, leaves unused bonuses
    public void setItemBonuses(IntSet set) {
        if (_item == null) {
            if (set != null && !set.isEmpty()) {
                throw new PlayerError.Slot(this, "Cannot set bonuses: Empty Slot");
            }
            return;
        } 
        if (_item.namedBonusGroup != null) {                        
            int bestIndex = 0;
            NamedItemBonus[] v = _item.namedBonusGroup.universe;
            double bestScore = ItemBonus.score(v[0].components, set);
            for (int i = 1; i < v.length; i++) {
                double score = ItemBonus.score(v[i].components, set);
                if (score > bestScore) {
                    bestScore = score;
                    bestIndex = i;
                }
            }     
            _namedBonusIndex = bestIndex;            
            ItemBonus.remove(v[bestIndex].components, set);
        }
        if (_item.auxBonusGroup != null) {
            int bestIndex = 0;
            NamedItemBonus[] v = _item.auxBonusGroup.universe;
            double bestScore = ItemBonus.score(v[0].components, set);
            for (int i = 1; i < v.length; i++) {
                double score =  ItemBonus.score(v[i].components, set);
                if (score > bestScore) {
                    bestScore = score;
                    bestIndex = i;
                }
            }                
            _auxBonusIndex = bestIndex;
            ItemBonus.remove(v[bestIndex].components, set);
        }    
        update();    
    }
    
    public int getRequiredLevel() {
        return _reqLevel;
    }
    
    public AbstractEnchant getEnchant() {
        return _enchant;
    }
    //public boolean canEnchant(AbstractEnchant enchant)
    
    public void setEnchant(AbstractEnchant enchant) {
        if (enchant == null) {
            _enchant = null;
            return;
        }
        if (_item == null) {
            return; // could error here
        }
        int ilvl = getBaseItemLevel();
        if (!enchant.checkItemLevel(ilvl)) {
            throw new PlayerError.EquipSlot(this, _item, String.format("%s cannot be applied to items higher than level %d", enchant.name, enchant.maxItemLevel));
        } else if (!enchant.canApply(_item)) {
            throw new PlayerError.EquipSlot(this, _item, String.format("%s cannot be applied", enchant.name));
        }
        _enchant = enchant;        
    }
    
    void update() {
        if (_item == null) return;
        _nameDesc = null; //_item.;
        _quality = _item.quality;
        int ilvl;
        if (_item.reqLevelMax > 0) {
            _reqLevel = Math.min(owner.playerLevel, _item.reqLevelMax);   
            ilvl = ItemLevelCurve.getItemLevel(_item.reqLevelCurveId, _reqLevel);
        } else {
            _reqLevel = _item.reqLevel;
            ilvl = _item.itemLevel;
        } 
        if (_item.namedBonusGroup != null) {
            NamedItemBonus bonus = _item.namedBonusGroup.universe[_namedBonusIndex];
            ilvl += bonus.itemLevelDelta; 
            _reqLevel += bonus.reqLevelDelta;
            _nameDesc = bonus.name;
            _quality = QualityT.max(_quality, bonus.quality); // necessary?
        } else {
            _nameDesc = _item.nameDesc;
        }
        _itemLevelBase = ilvl;
        if (_itemLevelCustom > 0) {
            ilvl = _itemLevelCustom;            
        } else {
            ilvl += getUpgradeItemLevelDelta();            
            if (owner.pvpMode && _item.pvpItemLevelDelta > 0) {
                ilvl += _item.pvpItemLevelDelta;
            }            
        }
        _itemLevelActual = ilvl;
        int scaled = owner.scaledItemLevel;
        if (scaled < 0) {
            ilvl = Math.min(ilvl, -scaled);
        } else if (scaled > 0) {
            ilvl = scaled;
        }
        _itemLevelScaled = ilvl;
        
        int randPropIndex = _item.getRandPropIndex();        
        double statBudget = ItemStatCurve.get(ilvl, _quality, randPropIndex) / 10000D;
        _gearStats.clear();        
        if (_item.statAllocs != null) {    
            int socketCost = SocketCostCurve.get(ilvl);
            for (StatAlloc x: _item.statAllocs) {                
                double l = statBudget * x.alloc;
                double r = x.mod * socketCost;                
                _gearStats.add(x.stat, (int)(0.5 + l) - (int)(0.5 + r));
            }
        }
        RandomSuffix suffix = getSuffix();
        if (suffix != null) {            
            if (suffix.statAllocs != null) {
                for (StatAlloc x: suffix.statAllocs) {             
                    _gearStats.add(x.stat, (int)(statBudget * x.alloc));
                }            
            }
            if (suffix.bonus != null && suffix.bonus.statAllocs != null) {
                for (StatAlloc x: suffix.bonus.statAllocs) {             
                    _gearStats.add(x.stat, (int)(statBudget * x.alloc));
                }
            }            
        }
        if (_item instanceof Armor) {
            Armor a = (Armor)_item;
            _baseArmor = ArmorCurve.get(ilvl, _quality, a.type, _item.equip);  
            _weaponDamage = 0;
        } else { //if (_socket instanceof Weapon) {
            Weapon w = (Weapon)_item;
            _weaponDamage = DamageCurve.get(ilvl, _quality, w.equip, w.type, w.caster);            
            _baseArmor = 0;            
        }        
        int oldSockets = _socketCount;
        int newSockets = 0;
        if (_item.sockets != null) {
            for (SocketT x: _item.sockets) {
                _socket[newSockets++]._socket = x;
            }
        }
        if (_item.auxBonusGroup != null) {            
            NamedItemBonus bonus = _item.auxBonusGroup.universe[_auxBonusIndex];            
            for (ItemBonus b: bonus.components) {
                if (b.sockets != null) {
                    for (SocketT x: b.sockets) {
                        _socket[newSockets++]._socket = x;
                    }
                }
                if (b.statAllocs != null) {
                    for (StatAlloc x: b.statAllocs) {
                        _gearStats.add(x.stat, (int)(0.5 + statBudget * x.alloc));
                    }
                }
            }            
        }       
        if (suffix != null && suffix.bonus != null && suffix.bonus.sockets != null) {
            for (SocketT x: suffix.bonus.sockets) {
                _socket[newSockets++]._socket = x;
            }
        }    
        /*if (_extraSocket > 1) {
            _extraSocket = canExtraSocket() ? 1 : 0;                
        }*/
        // i dont know what comes first socket wise
        // gear > bonuses? > extra 
        // it probably doesn't happen
        if (_extraSocket) {
            _socket[newSockets++]._socket = SocketT.PRISMATIC;
        }        
        // the socket types could of changed
        // we may need to check that gems can still fit  :|        
        for (int i = newSockets; i < oldSockets; i++) {
            _socket[i].clear();
            _socket[i]._socket = null;
        }  
        for (int i = 0; i < newSockets; i++) {
            _socket[i].update();
        }
        _socketCount = newSockets;
        updateSocketBonus();
    }
    
    void updateSocketBonus() {
        _socketBonusStats.clear();
        Enchantment bonus = _item.socketBonus;
        if (bonus != null) {
            // _socket namedBonuses are only stats
            // never _prof requirement
            _socketBonusSatisfied = true;
            for (int i = 0; i < _item.sockets.length; i++) {
                if (!_socket[i].matches(true)) {
                    _socketBonusSatisfied = false;
                    break;
                }
            }         
            int lvl = Math.min(owner.playerLevel, bonus.scalingLevelMax);  
            int min = bonus.scalingLevelMin;
            float scaling = PlayerScaling.get(Math.max(min, lvl), bonus.scalingId);
            if (bonus.scalingPerLevel > 0 && lvl > min) {                
                scaling *= (min + bonus.scalingPerLevel * (lvl - min)) / lvl;                
            }            
            for (StatAlloc x: bonus.statAllocs) {
                if (x.mod == 0) {
                    _socketBonusStats.add(x.stat, x.alloc);
                    continue;
                }
                int value = (int)(0.5 + scaling * x.mod);
                _socketBonusStats.add(x.stat, value);
            }           
        } else {            
            _socketBonusSatisfied = false;
        }
        
    }
    
    public StatMap getGearStats() {
        return _gearStats;
    }
    
    public StatMap getSocketBonusStats() {
        return _socketBonusStats; // don't mutate me bro!
    }
    public boolean isSocketBonusEnabled() {
        return _socketBonusSatisfied;
    }
    
    
    public boolean canExtraSocket() {
        return _item != null && (_item.extraSocket || (slotType == SlotT.WAIST && _itemLevelBase <= 600));
    }
    public boolean getExtraSocket() {
        return _item != null && _extraSocket;
    }    
    public void setExtraSocket(boolean enabled) {
        if (_extraSocket == enabled) {
            return;
        }
        _extraSocket = enabled;
        update();
    }
    
    public int getGearStat(StatT x, boolean effective) {
        return _item == null ? 0 : _gearStats.getEffective(x);
    }
    
    public void collectStats(StatMap stats) {        
        stats.add(_gearStats);
        for (int i = 0; i < _socketCount; i++) {
            stats.add(_socket[i]._stats);
        }
        if (_socketBonusSatisfied) {
            stats.add(_socketBonusStats);
        }
        if (_enchant != null) {            
            _enchant.enchantment.collectStats(stats, owner.playerLevel);
        }
    }
    
    public float getWeaponDamage() {
        return _weaponDamage;
    }   
    /*
    public int getWeaponMillis() {
        return _item instanceof Weapon ? ((Weapon)_item).speed : 0;
    }
    */
    
    public int getBonusArmor() {
        return _item == null || owner.spec == null || !owner.spec.role.bonusArmor ? 0 : _gearStats.get(StatT.ARMOR) + _socketBonusStats.get(StatT.ARMOR);
    }
    public int getBaseArmor() {
        return _baseArmor;
    }    
    public int getTotalArmor() {
        return getBaseArmor() + getBonusArmor();
    }
    
    public boolean isSameItem(PlayerSlot other) {
        if (other == null || _item != other._item) {
            return false;
        } else if (_item == null) {
            return true;
        }
        return _namedBonusIndex == other._namedBonusIndex 
            && _suffixIndex == other._suffixIndex;
    }
    public void copy(Player other, Consumer<PlayerError> errors) {
        copy(other.SLOT[slotType.index], errors);
    }
    public void copy(PlayerSlot other, Consumer<PlayerError> errors) {
        setItem(other._item);
        if (_item == null) return;
        _itemLevelCustom = other._itemLevelCustom;
        _namedBonusIndex = other._namedBonusIndex;
        _auxBonusIndex = other._auxBonusIndex;
        _suffixIndex = other._suffixIndex;
        _upgradeIndex = other._upgradeIndex;
        _extraSocket = other._extraSocket;
        _enchant = other._enchant;
        update(); // make sockets and stuff available again        
        for (int i = 0; i < _socketCount; i++) {
            try {
                _socket[i].copy(other._socket[i]);
            } catch (PlayerError err) { 
                if (errors != null) {
                    errors.accept(err);
                }
            }
        }
        updateSocketBonus(); // recompute socket bonus :\
    }

    public void clear() {
        if (_item == null) {
            return;
        }
        _item = null;          
        _gearStats.clear();
        _enchant = null;
        _nameDesc = null; 
        _quality = null;
        _itemLevelCustom = 0;
        _itemLevelBase = 0;
        _itemLevelActual = 0;
        _namedBonusIndex = 0;
        _extraSocket = false;
        _upgradeIndex = 0;
        _auxBonusIndex = 0;
        _reqLevel = 0;
        _weaponDamage = 0;
        _baseArmor = 0;
        _socketCount = 0;
        _socketBonusSatisfied = false;
        _socketBonusStats.clear();
        clearGems();
        if (slotType == SlotT.MAIN_HAND) {
            owner._bothHandsForMH = false;
        }        
    }

    public void clearGems() {
        for (PlayerSocket x : _socket) {
            x.clear();
        }
    }        
    
    public void dump() {
        _item.dump();
        System.out.println("ItemLevel: " + getActualItemLevel());
        System.out.println("Name: " + getItemName(true, true, false));
        System.out.println("Armor: " + getTotalArmor());
        System.out.println("GearStats: " + _gearStats);
        System.out.println("Sockets: " + getSocketDescription());
        System.out.println("SocketBonus: " + _socketBonusStats);
       
        if (_item instanceof Weapon) {
            Weapon w = (Weapon)_item;

            float dps = getWeaponDamage();
            float dmg = dps * w.speed / 1000f;
            float min = dmg * (1 - w.range / 2);
            float max = dmg * (1 + w.range / 2);
            int roundedMin = (int)min;
            int roundedMax = (int)(0.5 + max); // was ceiling...
            float roundedDps = (roundedMin + roundedMax) * 500f / w.speed; 
            System.out.println(String.format("Damage: %d - %d (%.2f)", roundedMin, roundedMax, roundedDps));
        }
        for (int i = 0; i < _socketCount; i++) {            
            System.out.println("Socket" + i + ": " + _socket[i]);
        }
        
        /*
                    Wearable _socket;
    int _itemLevelCustom;
    final PlayerSocket[] _socket = new PlayerSocket[Player.MAX_SOCKETS];
    int _namedBonusIndex;
    int _suffixIndex;
    
    // computed
    int _itemLevelActual;
    String _nameDesc;
    int _reqLevel;
    QualityT _quality;
    int _baseArmor;
    final StatMap _gearStats = new StatMap();
    
                */
    } 
    
    
    public QualityT getQuality() {
        return _quality;
    }
    public int getItemId() {
        return _item == null ? 0 : _item.itemId;
    }
    public String getItemIcon() {
        return _item == null ? null : _item.icon;
    }

    void appendItemName(StringBuilder sb, boolean suffix, boolean nameDesc) {
        if (isItemLevelScaled()) {
            sb.append("(");
            sb.append(getScaledItemLevel());
            sb.append(") ");
        }        
        boolean custom = isItemLevelCustom();
        sb.append("[");
        sb.append(getActualItemLevel());
        if (custom) {
            sb.append("!");
        }
        sb.append("] ");
        sb.append(_item.name);
        if (suffix && _item.suffixGroup != null) {
            sb.append(" ");
            sb.append(_item.suffixGroup.suffixes[_suffixIndex].name);
        }        
        if (nameDesc) {
            if (_nameDesc != null) {
                sb.append(" (");
                sb.append(_nameDesc);
                sb.append(")");
            }        
        }
    }
    
    public String getItemBaseName() {
        return _item == null ? null : _item.name;
    }    
    public String getItemName(boolean suffix, boolean nameDesc, boolean nullForEmpty) {
        if (_item == null) {
            return nullForEmpty ? null : "Empty " + slotType.name;
        }
        StringBuilder sb = new StringBuilder();
        appendItemName(sb, suffix, nameDesc);
        return sb.toString();
    }
    
    public String getSocketDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < _socketCount; i++) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            sb.append(_socket[i]._socket);
        }        
        sb.append("]");
        return sb.toString();
    }

    
}
