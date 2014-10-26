package com.antistupid.warkit;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import com.antistupid.warbase.IntSet;
import com.antistupid.warkit.items.RandomSuffix;
import com.antistupid.warkit.items.RandomSuffixGroup;
import com.antistupid.warbase.structs.StatAlloc;
import com.antistupid.warbase.utils.CompareHelp;
import com.antistupid.warbase.data.AsiaUpgradeChain;
import com.antistupid.warbase.ids.ItemBonusType;
import com.antistupid.warbase.ids.ItemClass;
import com.antistupid.warbase.types.ArmorT;
import com.antistupid.warbase.types.BindT;
import com.antistupid.warbase.types.ClassT;
import com.antistupid.warbase.types.ConsumeT;
import com.antistupid.warbase.types.EquipT;
import com.antistupid.warbase.types.GemT;
import com.antistupid.warbase.types.ProfT;
import com.antistupid.warbase.types.QualityT;
import com.antistupid.warbase.types.RaceT;
import com.antistupid.warbase.types.SlotT;
import com.antistupid.warbase.types.SocketT;
import com.antistupid.warbase.types.SpecT;
import com.antistupid.warbase.types.StatT;
import com.antistupid.warbase.types.WeaponT;
import com.antistupid.warbase.utils.SystemHelp;
import com.antistupid.warkit.items.ItemEnchant;
import com.antistupid.warkit.items.Armor;
import com.antistupid.warkit.items.ArmorEnchant;
import com.antistupid.warkit.items.Consumable;
import com.antistupid.warkit.items.ConsumableSpell;
import com.antistupid.warkit.items.Enchantment;
import com.antistupid.warkit.items.Gem;
import com.antistupid.warkit.items.Item;
import com.antistupid.warkit.items.ItemBonus;
import com.antistupid.warkit.items.ItemSet;
import com.antistupid.warkit.items.ItemContext;
import com.antistupid.warkit.items.ItemBonusCluster;
import com.antistupid.warkit.items.ProfValue;
import com.antistupid.warkit.items.SetBonus;
import com.antistupid.warkit.items.Unique;
import com.antistupid.warkit.items.Upgrade;
import com.antistupid.warkit.items.UpgradeChain;
import com.antistupid.warkit.items.Weapon;
import com.antistupid.warkit.items.WeaponEnchant;
import com.antistupid.warkit.items.Wearable;
import java.nio.file.Paths;

public class WarKit {
   
    public final int version;
    public final long createdTime;

    public final SortedMap<Integer,Wearable> wearableMap;
    public final SortedMap<Integer,Gem> gemMap;
    public final SortedMap<Integer,ItemSet> itemSetMap;    
    public final SortedMap<Integer,ItemBonus> itemBonusMap;
    public final SortedMap<Integer,ItemEnchant> enchantMap;
    public final SortedMap<Integer,Consumable> consumeMap;

    public final SortedMap<Integer,Wearable>[] slotItems;
    public final SortedMap<Integer,WeaponEnchant>[] weaponEnchants;
    public final SortedMap<Integer,ArmorEnchant>[] armorEnchants;
            
    private WarKit(int version, long createdTime, 
            TreeMap<Integer,Wearable> wearableMap,
            TreeMap<Integer,Gem> gemMap, 
            TreeMap<Integer,ItemSet> itemSetMap,
            TreeMap<Integer,ItemBonus> itemBonusMap,
            TreeMap<Integer,ItemEnchant> enchantMap,
            TreeMap<Integer,Consumable> consumeMap) {
        this.version = version;
        this.createdTime = createdTime;
        this.wearableMap = Collections.unmodifiableSortedMap(wearableMap);
        this.gemMap = Collections.unmodifiableSortedMap(gemMap);
        this.itemSetMap = Collections.unmodifiableSortedMap(itemSetMap);
        this.itemBonusMap = Collections.unmodifiableSortedMap(itemBonusMap);
        this.enchantMap = Collections.unmodifiableSortedMap(enchantMap);
        this.consumeMap = Collections.unmodifiableSortedMap(consumeMap);
        //
        armorEnchants = allocate(EquipT.db.size());
        weaponEnchants = allocate(WeaponT.db.size());
        for (ItemEnchant x: enchantMap.values()) {
            if (x instanceof ArmorEnchant) {
                ArmorEnchant e = (ArmorEnchant)x;
                EquipT.db.forEach(e.allowedEquip, t -> {                        
                    armorEnchants[t.index].put(e.enchantment.id, e);
                });
            } else if (x instanceof WeaponEnchant) {
                WeaponEnchant e = (WeaponEnchant)x;
                WeaponT.db.forEach(e.allowedWeapons, t -> {                        
                    weaponEnchants[t.index].put(e.enchantment.id, e);
                });
            }
        }
        protect(armorEnchants);
        protect(weaponEnchants);
        slotItems = allocate(SlotT.db.size());
        for (Wearable x: wearableMap.values()) {
            for (SlotT s: SlotT.db.types) {
                if (s.canContain(x.equip)) {
                    slotItems[s.index].put(x.itemId, x);
                }
            }
        }
        protect(slotItems);
    }
    
    static private SortedMap[] allocate(int num) {
        SortedMap[] v = new SortedMap[num];
        for (int i = 0; i < num; i++) {
            v[i] = new TreeMap<>();
        }
        return v;
    }
    
    static private void protect(SortedMap[] v) {
        for (int i = 0; i < v.length; i++) {
            v[i] = Collections.unmodifiableSortedMap(v[i]);    
        }
    }
    
    static final SortedMap<Integer,? extends ItemEnchant> emptyEnchantMap = Collections.unmodifiableSortedMap(new TreeMap<>());    
    public SortedMap<Integer,? extends ItemEnchant> getEnchantUniverse(Item item) {
        if (item instanceof Armor) {
            return armorEnchants[item.equip.index];
        } else if (item instanceof Weapon) {
            return weaponEnchants[item.type.index];
        } else {
            return emptyEnchantMap;
        }
    }
    
    public ItemEnchant findEnchant(Item item, int enchantId) {
        return getEnchantUniverse(item).get(enchantId);
    }
    
    public IntSet repairItemBonuses(Wearable item, IntSet bonuses) {
        if (bonuses.isEmpty()) {
            return bonuses;
        }
        HashMap<Integer,ItemBonus> map = new HashMap<>();
        if (item.contexts != null) {
            for (ItemContext ctx: item.contexts) {
                for (ItemBonus x: ctx.components) {
                    map.put(x.id, x);                    
                }                
            }
        }
        // fix me: suffixes?
        IntSet fixed = new IntSet();
        bonuses.forEach(id -> {
            ItemBonus b = map.get(id);
            if (b == null) {         
                b = itemBonusMap.get(id);
                if (b != null) {
                    for (ItemBonus x: map.values()) {             
                        if (x.isEffectivelyEqual(b)) {
                            id = x.id;
                            break;
                        }
                    }
                }
            } 
            fixed.add(id);
        });
        return fixed;
    }
    
    /*
    public int wearableCount() { return wearableMap.size(); }    
    public int gemCount() { return gemMap.size(); }
    public int itemCount() { return gemCount() + wearableCount(); }
        
    public Wearable getWearable(int itemId) { return wearableMap.get(itemId); }
    public Gem getGem(int itemId) { return gemMap.get(itemId); }
    public Item getItem(int itemId) {
        Item item = getWearable(itemId);
        if (item == null) {
            item = getGem(itemId);
        }
        return item;
    }
    
    public ItemBonus getItemBonus(int bonusId) {
        return itemBonusMap.get(bonusId);
    }
    
    */
    @FunctionalInterface
    static private interface IndexedString {
        String read() throws IOException;
    }
    
    static public final String FILE = "WKDB.dat";
    static public WarKit load() { // try to find a local WKDB
        Path path = Paths.get(FILE);
        if (Files.isReadable(path)) {
            return load(path);
        }
        // local debug only (remove me later)
        path = Paths.get("../WarExport/", FILE);
        if (Files.isReadable(path)) {
            return load(path);
        }        
        path = SystemHelp.DATA_DIR.resolve(FILE);
        if (Files.isReadable(path)) {
            return load(path);
        }
        path = SystemHelp.HOME_DIR.resolve(FILE);
        if (Files.isReadable(path)) {
            return load(path);
        }
        throw new RuntimeException("Unable to locate WKDB.dat");
    }
    static public WarKit load(Path file) {
        final long startTime = System.nanoTime();

        // expose
        TreeMap<Integer,Wearable> wearableMap = new TreeMap<>(); 
        TreeMap<Integer,Gem> gemMap = new TreeMap<>();
        TreeMap<Integer,ItemSet> itemSetMap = new TreeMap<>();
        TreeMap<Integer,ItemBonus> itemBonusMap = new TreeMap<>();
        TreeMap<Integer,ItemEnchant> enchantMap = new TreeMap<>();
        TreeMap<Integer,Consumable> consumeMap = new TreeMap<>();
        
        // helper struct
        class ItemGroup {
            final int[] ids;
            final Wearable[] items;   
            ItemGroup(int n) {
                ids = new int[n];
                items = new Wearable[n];
            }
        }
        // internal
        TreeMap<Integer,Enchantment> enchantmentMap = new TreeMap<>();
        TreeMap<Integer,Unique> uniqueMap = new TreeMap<>();
        TreeMap<Integer,UpgradeChain> upgradeChainMap = new TreeMap<>();
        TreeMap<Integer,Upgrade> upgradeMap = new TreeMap<>();
        TreeMap<Integer,RandomSuffix> suffixMap = new TreeMap<>();
        TreeMap<Integer,RandomSuffixGroup> suffixGroupMap = new TreeMap<>();
        TreeMap<Integer,ItemGroup> itemGroupMap = new TreeMap<>();
        TreeMap<Integer,ItemContext[]> ctxMap = new TreeMap<>();
        
        // memo
        TreeMap<SocketT[],SocketT[]> socketsMemo = new TreeMap<>(CompareHelp.arrayComparator((a, b) -> Integer.compare(a.id, b.id)));     
        TreeMap<StatAlloc,StatAlloc> statAllocMemo = new TreeMap<>(StatAlloc::compare);     
        TreeMap<StatAlloc[],StatAlloc[]> statAllocsMemo = new TreeMap<>(CompareHelp.arrayComparator(StatAlloc::compare));     
        //TreeMap<NamedItemBonus[],ItemBonusCluster[]> namedBonusesMemo = new TreeMap<>(CompareHelp.arrayComparator((a, b) -> ItemBonus.CMP_ARRAY.compare(a.components, b.components))); 
            
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            int version = in.readInt();
            long createdTime = in.readLong();  
            int strCount = in.readInt();
            int upgradeCount = in.readInt();
            int uniqueCount = in.readInt();
            int enchantmentCount = in.readInt();
            //int fileNameCount = in.readInt(); 
            //int nameDescCount = in.readInt(); 
            int itemBonusCount = in.readInt();
            //int namedGroupCount = in.readInt();
            //int auxGroupCount = in.readInt();
            int contextGroupCount = in.readInt();
            int suffixCount = in.readInt();
            int suffixGroupCount = in.readInt();
            int itemGroupCount = in.readInt();
            int itemSetCount = in.readInt();
            int enchantCount = in.readInt();
            int itemCount = in.readInt();
            
            int consumeSpellCount = in.readInt();
            int consumeItemCount = in.readInt();
            /*
            o.writeInt(enchantmentMap.size());
            o.writeInt(itemLimitMap.size());
            o.writeInt(fileDataMap.size());
            o.writeInt(nameDescMap.size());
            o.writeInt(flatItemBonusMap.size()); 
            o.writeInt(encodedItems.size());
            */
            
            String[] strings = new String[1 + strCount]; // 0 = null
            for (int i = 1; i <= strCount; i++) {
                strings[i] = in.readUTF(); 
            }            
            
            IndexedString indexedString = () -> strings[in.readInt()];
            
            for (int i = 0; i < upgradeCount; i++) {
                int id = in.readUnsignedShort();
                int num = in.readUnsignedByte(); // must be >0
                int[] upgradeId = new int[num];
                int[] itemLevelDelta = new int[num];
                for (int j = 0; j < num; j++) {
                    upgradeId[j] = in.readUnsignedShort();
                    itemLevelDelta[j] = in.readUnsignedByte();                    
                }                
                upgradeChainMap.put(id, new UpgradeChain(id, upgradeId, itemLevelDelta));
            }
            for (UpgradeChain x: upgradeChainMap.values()) {                
                UpgradeChain y = upgradeChainMap.get(AsiaUpgradeChain.convert(x.chainId));
                upgradeMap.put(x.chainId, new Upgrade(x, y));
            }            
            for (int i = 0; i < uniqueCount; i++) {
                int id = in.readUnsignedShort();
                String name = in.readUTF();
                int max = 1 + in.readUnsignedByte(); // >0                
                uniqueMap.put(id, new Unique(id, name, max));                
            }
            for (int i = 0; i < enchantmentCount; i++) {
                int id = in.readUnsignedShort();
                int minScalingLevel = in.readUnsignedShort();
                int maxScalingLevel = in.readUnsignedShort();
                int scalingId = in.readUnsignedByte();
                int scalingPerLevel = in.readUnsignedShort();
                ProfT reqProf = ProfT.db.by_id.get(in.readUnsignedShort());
                int reqProfSkill = in.readUnsignedShort();

                int statCount = in.readUnsignedByte();
                StatAlloc[] statAllocs = null;
                if (statCount > 0) {
                    statAllocs = new StatAlloc[statCount];
                    for (int j = 0; j < statCount; j++) {
                        StatT stat = StatT.db.by_id.require(in.readUnsignedByte());
                        int value = in.readUnsignedShort();
                        float coeff = in.readFloat();
                        statAllocs[j] = memo(statAllocMemo, new StatAlloc(stat, value, coeff));
                    }          
                    statAllocs = memo(statAllocsMemo, statAllocs);
                }        

                int profCount = in.readUnsignedByte();
                ProfValue[] profs = null;
                if (profCount > 0) {
                    profs = new ProfValue[profCount];
                    for (int j = 0; j < profCount; j++) {
                        ProfT prof = ProfT.db.by_id.require(in.readShort());
                        int delta = in.readShort();
                        profs[j] = new ProfValue(prof, delta);
                    }
                }

                int spellCount = in.readUnsignedByte();
                int[] spells = null;
                if (spellCount > 0 ) {
                    spells = new int[spellCount];
                    for (int j = 0; j < spellCount; j++) {
                        spells[j] = in.readInt();
                    }
                }
                String desc = in.readUTF();                
                enchantmentMap.put(id, new Enchantment(id, desc, minScalingLevel, maxScalingLevel, scalingId, scalingPerLevel, reqProf, reqProfSkill, statAllocs, profs, spells));
            }
            /*for (int i = 0; i < fileNameCount; i++) {
                int id = in.readInt();
                String name = in.readUTF();                
                int pos = name.indexOf('.');
                if (pos >= 0) {
                    name = name.substring(0, pos);
                }                
                name = name.toLowerCase(); // convert to icon
                fileNameMap.put(id, name);
            }*/   
            /*
            for (int i = 0; i < nameDescCount; i++) {
                int id = in.readUnsignedShort();
                String name = in.readUTF();
                nameDescMap.put(id, name);
            } 
            */
            
            ArrayList<SocketT> socketBuf = new ArrayList<>();
            ArrayList<StatAlloc> statAllocBuf = new ArrayList<>();
            for (int i = 0; i < itemBonusCount; i++) {
                int id = in.readUnsignedShort();
                int num = in.readUnsignedByte();
                int itemLevelDelta = 0;
                int reqLevelDelta = 0;
                String nameDesc = null;
                QualityT quality = null;
                String suffixName = null;
                socketBuf.clear();
                statAllocBuf.clear();
                for (int j = 0; j < num; j++) {
                    int type = in.readUnsignedByte();
                    int val1 = in.readInt();
                    int val2 = in.readInt();                    
                    switch (type) {
                        case ItemBonusType.ITEM_LEVEL_DELTA: {
                            itemLevelDelta = val1;
                            break;
                        }
                        case ItemBonusType.REQ_LEVEL_DELTA: {
                            reqLevelDelta = val1;
                            break;
                        }
                        case ItemBonusType.NAME_DESC: {
                            nameDesc = strings[val1];
                            break;
                        }
                        case ItemBonusType.NAME_SUFFIX: {
                            suffixName = strings[val1]; //DescId = val1;
                            break;
                        }
                        case ItemBonusType.SOCKET: {
                            SocketT socket = SocketT.db.by_id.require(val2);
                            for (int k = 0; k < val1; k++) {
                                socketBuf.add(socket);
                            }
                            break;
                        }
                        case ItemBonusType.STAT_ALLOC: {
                            statAllocBuf.add(memo(statAllocMemo, new StatAlloc(StatT.db.by_id.require(val1), val2, 0)));
                            break;
                        }
                        case ItemBonusType.QUALITY: {
                            quality = QualityT.db.by_id.require(val1);
                            break;
                        }
                        //default: System.err.println("Unused Item Bonus: " + type + "/" + val1 + "/" + val2);                            
                    }
                }
                SocketT[] sockets = socketBuf.isEmpty() ? null : memo(socketsMemo, socketBuf.toArray(new SocketT[socketBuf.size()]));                
                statAllocBuf.sort(StatAlloc::compare);
                StatAlloc[] statAllocs = statAllocBuf.isEmpty() ? null : memo(statAllocsMemo, statAllocBuf.toArray(new StatAlloc[statAllocBuf.size()]));
                ItemBonus b = new ItemBonus(id, itemLevelDelta, reqLevelDelta, quality, nameDesc, suffixName, sockets, statAllocs);
                itemBonusMap.put(id, b);
                //FlatItemBonus f = new ItemBonusCluster(null, itemLevelDelta, reqLevelDelta, nameDescMap.get(nameDescId));                
                //flatItemBonusMap.put(id, f);
            }        
            // itemBonusMap.put(ItemBonus.IDENTITY.id, ItemBonus.IDENTITY); // hax?
             
            for (int ctx = 0; ctx < contextGroupCount; ctx++) {
                int id = in.readUnsignedByte();
                int size = in.readUnsignedByte();
                ItemContext[] ctxs = new ItemContext[size];                 
                for (int i = 0; i < size; i++) {
                    String name = indexedString.read();
                    String context = indexedString.read();
                    ItemBonus[] defaultBonuses;
                    int num = in.readUnsignedByte();
                    ArrayList<ItemBonus> comps = new ArrayList<>();
                    defaultBonuses = new ItemBonus[num];
                    for (int j = 0; j < num; j++) {
                        ItemBonus b = itemBonusMap.get(in.readUnsignedShort());
                        defaultBonuses[j] = b;
                        comps.add(b);
                    }  
                    num = in.readUnsignedByte();    
                    ItemBonusCluster[] opts = null;
                    if (num > 0) {
                        ItemBonus[][] m = new ItemBonus[num][];
                        int p = 1;
                        for (int j = 0; j < num; j++) {
                            int n = in.readUnsignedByte();
                            ItemBonus[] v = new ItemBonus[1 + n];
                            for (int k = 0; k < n; k++) {
                                ItemBonus b = itemBonusMap.get(in.readUnsignedShort());
                                v[1 + k] = b;
                                comps.add(b);
                            }
                            m[j] = v;
                            p *= v.length;
                        }
                        ArrayList<ItemBonusCluster> uni = new ArrayList<>(p);
                        uni.add(ItemBonusCluster.NONE);
                        ItemBonus[] v = new ItemBonus[num];
                        for (int j = 0; j < p; j++) {
                            int d = j;
                            int n = 0;
                            for (int k = 0; k < num; k++) {
                                ItemBonus[] u = m[k];
                                ItemBonus b = u[d % u.length];
                                d /= u.length;
                                if (b != null) {
                                    v[n++] = b;
                                }                   
                            }
                            if (n > 0) {
                                ItemBonusCluster b = mergeItemBonuses(null, Arrays.copyOf(v, n), statAllocsMemo, socketsMemo);                            
                                uni.add(renameItemBonuses(b));
                            }
                        }            
                        uni.sort((a, b) -> ItemBonus.CMP_ARRAY.compare(a.components, b.components));    
                        opts = uni.toArray(new ItemBonusCluster[uni.size()]);
                    }                    
                    comps.sort(ItemBonus.CMP_ID);
                    ctxs[i] = new ItemContext(i, context, 
                            mergeItemBonuses(name, defaultBonuses, statAllocsMemo, socketsMemo), 
                            opts, comps.toArray(new ItemBonus[comps.size()])
                    );                      
                 }
                 ctxMap.put(id, ctxs);
            }
            /*
            for (int i = 0; i < namedGroupCount; i++) {
                int id = in.readUnsignedByte();
                String name0 = in.readUTF();
                int num = in.readUnsignedByte();
                ItemBonusCluster[] universe = new ItemBonusCluster[num];
                TreeMap<Integer,ItemBonus> map = new TreeMap<>();
                int defaultIndex = 0;
                for (int j = 0; j < num; j++) {
                    int n = in.readUnsignedByte();
                    ItemBonus[] bonuses = new ItemBonus[n];
                    for (int k = 0; k < n; k++) {
                        ItemBonus b = itemBonusMap.get(in.readUnsignedShort());
                        bonuses[k] = b;
                        map.put(b.id, b);
                    }
                    universe[j] = mergeItemBonuses(name0, bonuses);
                    if (bonuses.length == 0) {
                        defaultIndex = j;
                    }
                }                
                namedGroupMap.put(id, new BonusGroup(id, map.values().toArray(new ItemBonus[map.size()]), universe, defaultIndex));                
            }           
            for (int i = 0; i < auxGroupCount; i++) {
                int id = in.readUnsignedShort();
                int num = in.readUnsignedByte();
                ItemBonus[][] m = new ItemBonus[num][];
                int p = 1;
                ArrayList<ItemBonus> itemSet = new ArrayList<>();
                for (int j = 0; j < num; j++) {
                    int n = in.readUnsignedByte();
                    ItemBonus[] v = new ItemBonus[1 + n];
                    for (int k = 0; k < n; k++) {
                        ItemBonus bonus = itemBonusMap.get(in.readUnsignedShort());
                        v[1 + k] = bonus;
                        itemSet.add(bonus);
                    }
                    m[j] = v;
                    p *= v.length;
                }
                ArrayList<NamedItemBonus> uni = new ArrayList<>(p);
                uni.add(ItemBonusCluster.NONE);
                ItemBonus[] v = new ItemBonus[num];
                for (int j = 0; j < p; j++) {
                    int d = j;
                    int n = 0;
                    for (int k = 0; k < num; k++) {
                        ItemBonus[] u = m[k];
                        ItemBonus b = u[d % u.length];
                        d /= u.length;
                        if (b != null) {
                            v[n++] = b;
                        }                   
                    }
                    if (n > 0) {
                        uni.add(mergeItemBonuses(Arrays.copyOf(v, n)));
                    }
                }              
                itemSet.sort(ItemBonus.CMP_ID);
                uni.sort((a, b) -> ItemBonus.CMP_ARRAY.compare(a.components, b.components));
                auxGroupMap.put(id, new BonusGroup(id, 
                        itemSet.toArray(new ItemBonus[itemSet.size()]),
                        uni.toArray(new ItemBonusCluster[uni.size()]), 0));                
            }
            */
            for (int i = 0; i < suffixCount; i++) {
                int id = in.readShort();
                String name = in.readUTF();
                int num = in.readUnsignedByte();
                StatAlloc[] statAllocs = new StatAlloc[num];
                for (int j = 0; j < num; j++) {
                    StatT stat = StatT.db.by_id.require(in.readUnsignedByte());
                    int alloc = in.readInt();
                    statAllocs[j] = memo(statAllocMemo, new StatAlloc(stat, alloc, 0));
                }            
                statAllocs = memo(statAllocsMemo, statAllocs);
                if (name.isEmpty()) {
                    if (num == 1) {
                        name = "of the " + statAllocs[0].stat.name;
                    } else {
                        name = null;
                    }
                }       
                ItemBonus bonus = itemBonusMap.get(-id);
                if (bonus != null) {
                    statAllocs = bonus.statAllocs; // make it easier
                }
                suffixMap.put(id, new RandomSuffix(id, name, statAllocs, itemBonusMap.get(-id)));
            }
            for (int i = 0; i < suffixGroupCount; i++) {
                int id = in.readShort();
                int num = in.readUnsignedShort();
                RandomSuffix[] v = new RandomSuffix[num];
                for (int j = 0; j < num; j++) {
                    v[j] = suffixMap.get((int)in.readShort()); // must exist
                }
                //System.out.println(id  + ":"+ Arrays.toString(v));
                Arrays.sort(v, RandomSuffix.CMP_POWER);
                suffixGroupMap.put(id, new RandomSuffixGroup(id, v));
            }            
            for (int i = 0; i < itemGroupCount; i++) {
                int id = in.readUnsignedShort();
                int num = in.readUnsignedByte();
                ItemGroup g = new ItemGroup(num);
                for (int j = 0; j < num; j++) {
                    g.ids[j] = in.readInt();
                }                
                itemGroupMap.put(id, g);
            }
            for (int i = 0; i < itemSetCount; i++) {
                int id = in.readUnsignedShort();
                String name = in.readUTF();
                int size = in.readUnsignedByte();
                ProfValue reqProf = readProfValue(in);
                int specCount = in.readUnsignedByte();
                ItemSet set = new ItemSet(id, name, size, reqProf, specCount);
                for (int j = 0; j < specCount; j++) {
                    SpecT spec = SpecT.db.by_id.get(in.readUnsignedShort());
                    int num = in.readUnsignedByte();
                    set.specs[j] = spec;
                    SetBonus[] v = set.bonuses[j] = new SetBonus[num];
                    for (int k = 0; k < num; k++) {                        
                        int thres = in.readUnsignedByte();
                        int spellId = in.readInt();
                        String bonusName = in.readUTF();
                        String bonusDesc = in.readUTF();
                        v[k] = new SetBonus(set, spec, k, thres, spellId, bonusName, bonusDesc);
                    }                    
                }      
                itemSetMap.put(id, set);
            }            
            
            for (int index = 0; index < enchantCount; index++) {
                int spellId = in.readInt();
                int itemId = in.readInt();
                String name = indexedString.read();
                String spellDesc = indexedString.read();
                String icon = indexedString.read();
                int maxItemLevel = in.readUnsignedShort();                
                Enchantment enchantment = enchantmentMap.get(in.readUnsignedShort());
                int flags = in.readUnsignedByte();
                boolean isWeapon = (flags & 0x01) != 0;
                boolean isTinker = (flags & 0x02) != 0; //? 1 : 0; // tinker or not
                boolean isRetired = (flags & 0x04) != 0; //? 1 : 0; // tinker or not
                int mask1 = in.readInt();
                int mask2 = in.readInt();                
                ItemEnchant enchant;
                if (isWeapon) {
                    long allowedWeapons = WeaponT.blizzBits.decode(mask2);
                    enchant = new WeaponEnchant(spellId, itemId, name, spellDesc, icon, maxItemLevel, isTinker, isRetired, enchantment, allowedWeapons);
                } else {
                    long allowedEquip = EquipT.blizzBits.decode(mask1);
                    long allowedArmor = ArmorT.blizzBits.decode(mask2);
                    enchant = new ArmorEnchant(spellId, itemId, name, spellDesc, icon, maxItemLevel, isTinker, isRetired, enchantment, allowedEquip, allowedArmor);                    
                }
                enchantMap.put(enchant.spellId, enchant);
            }
            
            int gemCount = 0;
            for (int index = 0; index < itemCount; index++) {
                
                int itemId = in.readInt();
                int itemClass = in.readUnsignedByte();
                int subClass = in.readUnsignedByte();
                int itemLevel = in.readUnsignedShort();
                
                //System.out.println(itemId);
                EquipT equip = EquipT.db.by_id.require(in.readUnsignedByte());
                QualityT quality = QualityT.db.by_id.require(in.readUnsignedByte());
                BindT bind = BindT.ids.require(in.readUnsignedByte());
                         
                String name = in.readUTF();
                String text = indexedString.read(); //strings[in.readInt()];                
                
                ProfT reqProf = ProfT.db.by_id.get(in.readUnsignedShort());
                int reqProfLevel = in.readUnsignedShort();                
                
                int reqRepId = in.readUnsignedShort();
                int reqRepRank = in.readUnsignedShort();                    
                
                long reqRace = RaceT.blizzBits.decode(in.readInt());
                long reqClass = ClassT.blizzBits.decode(in.readInt());                
                
                String iconName = indexedString.read();
                
                int uniqueId = in.readShort();
                Unique unique;
                if (uniqueId == -1) {
                    unique = new Unique(-itemId, name, 1);
                } else {
                    unique = uniqueMap.get(uniqueId);
                }               
                
                Item item;
                switch (itemClass) {
                    case ItemClass.ARMOR:
                    case ItemClass.WEAPON: {
                        int reqLevel = in.readUnsignedShort();
                        int reqLevelMax = in.readUnsignedShort();
                        int reqLevelCurveId = in.readUnsignedShort();  
                        
                        StatAlloc[] statAllocs = null;
                        int statAllocNum = in.readUnsignedByte();
                        if (statAllocNum > 0) {
                            statAllocs = new StatAlloc[statAllocNum];
                            for (int j = 0; j < statAllocNum; j++) {
                                StatT stat = StatT.db.by_id.require(in.readUnsignedByte());
                                int alloc = in.readInt();
                                float socketMod = in.readFloat();
                                statAllocs[j] = memo(statAllocMemo, new StatAlloc(stat, alloc, socketMod));
                            }
                            statAllocs = memo(statAllocsMemo, statAllocs);
                        }
                        
                        Upgrade upgrade = upgradeMap.get(in.readUnsignedShort());
                            
                        int pvpItemLevel = in.readUnsignedShort();
                        
                        int socketCount = in.readUnsignedByte();
                        Enchantment socketBonus = null;
                        SocketT[] sockets = null;
                        if (socketCount > 0) {
                            sockets = new SocketT[socketCount];
                            for (int i = 0; i < socketCount; i++) {
                                sockets[i] = SocketT.db.by_id.require(in.readUnsignedByte());
                            }
                            sockets = memo(socketsMemo, sockets);
                            socketBonus = enchantmentMap.get(in.readUnsignedShort());
                        }
                        
                        RandomSuffixGroup suffixGroup = suffixGroupMap.get((int)in.readShort());
                 
                        ItemGroup g = itemGroupMap.get(in.readUnsignedShort());
                        Wearable[] group = null;
                        int groupIndex = -1;
                        if (g != null) {
                            group = g.items;
                            for (int i = 0; i < g.ids.length; i++) {
                                if (g.ids[i] == itemId) {
                                    groupIndex = i; // this must exist
                                    break;
                                }
                            }
                        }   
                        
                        ItemContext[] contexts = null;
                        String nameDesc = null;
                        int contextsId = in.readUnsignedByte();
                        if (contextsId > 0) {                            
                            contexts = ctxMap.get(contextsId); // must exist                         
                        } else {
                            nameDesc = indexedString.read();
                        }

                        ItemSet itemSet = itemSetMap.get(in.readUnsignedShort());
                        
                        int[] spellIds = null;
                        int spellCount = in.readUnsignedByte();
                        if (spellCount > 0) {
                            spellIds = new int[spellCount];
                            for (int i = 0; i < spellCount; i++) {
                                spellIds[i] = in.readInt();
                            }
                        }
                        
                        int flags = in.readUnsignedByte();
                        boolean extraSocket = (flags & 0x01) != 0;
                        
                        if (itemClass == ItemClass.WEAPON) {
                            int speed = in.readUnsignedShort();
                            float range = in.readFloat();
                            boolean caster = in.readBoolean();
                            WeaponT type = WeaponT.db.by_id.require(subClass);
                            int damageType = 0;
                            if (type == WeaponT.WAND) {
                                damageType = in.readUnsignedByte();
                            }
                            
                            item = new Weapon(
                                    itemId, itemLevel, type, quality, equip, 
                                    bind, unique, name, text, iconName, 
                                    reqProf, reqProfLevel, reqRepId, reqRepRank,
                                    reqRace, reqClass,
                                    nameDesc, reqLevel, reqLevelMax, reqLevelCurveId, 
                                    statAllocs, sockets, socketBonus, 
                                    upgrade, pvpItemLevel,
                                    suffixGroup, contexts,
                                    itemSet, group, groupIndex, spellIds, extraSocket,
                                    speed, range, caster, damageType);    
                        } else {
                            ArmorT type = ArmorT.db.by_id.require(subClass);
                            item = new Armor(
                                    itemId, itemLevel, type, quality, equip, 
                                    bind, unique, name, text, iconName,                                    
                                    reqProf, reqProfLevel, reqRepId, reqRepRank,
                                    reqRace, reqClass, 
                                    nameDesc, reqLevel, reqLevelMax, reqLevelCurveId, 
                                    statAllocs, sockets, socketBonus,
                                    upgrade, pvpItemLevel,
                                    suffixGroup, contexts,
                                    itemSet, group, groupIndex, spellIds, extraSocket);
                        }
                        break;
                    }
                    case ItemClass.GEM: {                   
                        //int enchantId = in.readUnsignedShort();
                        int reqItemLevel = in.readUnsignedShort();                        
                        int scalingLevelMin = in.readUnsignedShort();
                        int scalingLevelMax = in.readUnsignedShort();
                        int scalingId = in.readUnsignedByte();
                        int scalingPerLevel = in.readUnsignedShort();
                        int spellId = in.readInt();
                        int num = in.readUnsignedByte();
                        StatAlloc[] statAllocs = null;
                        if (num > 0) {
                            statAllocs = new StatAlloc[num];
                            for (int i = 0; i < num; i++) {
                                int statId = in.readUnsignedByte();
                                int value = in.readUnsignedShort();
                                float coeff = in.readFloat();
                                StatT stat = StatT.db.by_id.require(statId);
                                statAllocs[i] = memo(statAllocMemo, new StatAlloc(stat, value, coeff));
                            }           
                            statAllocs = memo(statAllocsMemo, statAllocs);
                        }
                        GemT type = GemT.db.by_id.require(subClass);
                        item = new Gem(
                                itemId, itemLevel, type, quality, equip, 
                                bind, unique, name, text, iconName, 
                                reqProf, reqProfLevel, reqRepId, reqRepRank,
                                reqRace, reqClass,
                                gemCount++, reqItemLevel, scalingLevelMin, scalingLevelMax,
                                scalingId, scalingPerLevel,
                                spellId, statAllocs
                        ); 
                        break;
                    }
                    default: throw new IOException("Unsupported ItemClass: " + itemClass);
                }
                if (item instanceof Wearable) {
                    Wearable w = (Wearable)item;
                    if (w.itemGroup != null) {
                        w.itemGroup[w.groupIndex] = w; 
                    }
                    wearableMap.put(itemId, w);                
                } else if (item instanceof Gem) {
                    Gem g = (Gem)item;
                    gemMap.put(itemId, g);          
                }            
            }
            
            HashMap<Integer,ConsumableSpell> consumeSpellMap = new HashMap<>();
            for (int index = 0; index < consumeSpellCount; index++) {
                int spellId = in.readInt();
                int duration = in.readInt();
                int scalingLevelMax = in.readUnsignedByte();
                int scalingId = in.readUnsignedByte();
                int num = in.readUnsignedByte();
                StatAlloc[] statAllocs = new StatAlloc[num];
                for (int i = 0; i < num; i++) {
                    StatT stat = StatT.db.by_id.require(in.readUnsignedByte());
                    int value = in.readUnsignedShort();
                    float coeff = in.readFloat();
                    statAllocs[i] = memo(statAllocMemo, new StatAlloc(stat, value, coeff));
                }
                statAllocs = memo(statAllocsMemo, statAllocs);                
                consumeSpellMap.put(spellId, new ConsumableSpell(spellId, duration, scalingLevelMax, scalingId, statAllocs));
            }
            for (int index = 0; index < consumeItemCount; index++) {
                int itemId = in.readInt();
                int reqLevel = in.readUnsignedByte();
                ConsumeT type = ConsumeT.db.by_id.require(in.readUnsignedByte());
                int flags = in.readUnsignedByte();
                String name = indexedString.read();
                String icon = indexedString.read();
                int spellId = in.readInt();
                ConsumableSpell spell = consumeSpellMap.get(spellId); // never null                
                consumeMap.put(itemId, new Consumable(itemId, name, icon, type, flags, reqLevel, spell));
            }            
            System.out.println(String.format("WarKit (v%d, %tc) <%dms>", version, createdTime, (System.nanoTime() - startTime) / 1000000));
            return new WarKit(version, createdTime, wearableMap, gemMap, itemSetMap, itemBonusMap, enchantMap, consumeMap);
        } catch (IOException err) {        
            throw new UncheckedIOException("WarKit Load Failed: " + file, err);
        } 
    }
        
    static ProfValue readProfValue(DataInputStream in) throws IOException {
        ProfT prof = ProfT.db.by_id.get(in.readUnsignedShort());
        int level = in.readUnsignedShort();   
        return prof != null ? new ProfValue(prof, level) : null;
    }
    
    static private <X> X memo(Map<X,X> map, X temp) {
        if (temp == null) {
            return null;
        }
        X other = map.get(temp);
        if (other != null) {
            return other;
        }
        map.put(temp, temp);
        return temp;
    }

    static private ItemBonusCluster mergeItemBonuses(String name, ItemBonus[] v, TreeMap<StatAlloc[],StatAlloc[]> statAllocsMemo, TreeMap<SocketT[],SocketT[]> socketsMemo) {
        QualityT qualityMax = null;
        int itemLevelDelta = 0;
        int reqLevelDelta = 0;
        //ArrayList<String> names = new ArrayList<>();    
        ArrayList<SocketT> socketBuf = new ArrayList();
        ArrayList<StatAlloc> statAllocBuf = new ArrayList<>();
        for (ItemBonus b: v) {
            itemLevelDelta += b.itemLevelDelta;
            reqLevelDelta += b.reqLevelDelta;
            qualityMax = QualityT.max(qualityMax, b.quality);            
            if (b.sockets != null) {
                for (SocketT x: b.sockets) {
                    socketBuf.add(x);
                }
            }
            if (b.statAllocs != null) {
                for (StatAlloc x: b.statAllocs) {
                    statAllocBuf.add(x);
                }
            }
        }
        SocketT[] sockets = socketBuf.isEmpty() ? null : memo(socketsMemo, socketBuf.toArray(new SocketT[socketBuf.size()]));                
        statAllocBuf.sort(StatAlloc::compare);
        StatAlloc[] statAllocs = statAllocBuf.isEmpty() ? null : memo(statAllocsMemo, statAllocBuf.toArray(new StatAlloc[statAllocBuf.size()]));
        Arrays.sort(v, ItemBonus.CMP_ID);
        return new ItemBonusCluster(v, name, itemLevelDelta, reqLevelDelta, qualityMax, statAllocs, sockets);           
    }
    
    static private ItemBonusCluster renameItemBonuses(ItemBonusCluster b) {
        ArrayList<String> names = new ArrayList<>();    
        if (b.sockets != null) {
            if (b.sockets.length == 1) {
                names.add(b.sockets[0].name);
            } else {
                SocketT sameColor = b.sockets[0];
                for (int i = 1; i < b.sockets.length; i++) {
                    if (b.sockets[i] != sameColor) {
                        sameColor = null;
                        break;
                    }                                    
                }
                if (sameColor != null) {
                    names.add(b.sockets.length + "x " + sameColor);                                    
                } else {
                    names.add(Arrays.toString(b.sockets));
                }
            }
        }
        if (b.statAllocs != null) {
            if (b.statAllocs.length == 1) {
                names.add(b.statAllocs[0].stat.name);
            } else {                  
                for (StatAlloc x: b.statAllocs) {
                    names.add(x.stat.shortName);
                }
            } 
        }     
        String name = names.stream().collect(Collectors.joining(" + "));
        return new ItemBonusCluster(b.components, name, b.itemLevelDelta, b.reqLevelDelta, b.quality, b.statAllocs, b.sockets);         
    }
    
    
    
    
}
