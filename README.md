#WarKit
**A high-level World of Warcraft toolkit for Java**

WarKit is a single-jar library + external database file for Warlords of Draenor (WoD) theorycrafting and World of Warcraft (WoW) related applications.  WarKit provides a very friendly interface for anything item or spell related.  

####Instructions
1. download single-jar library: WarKit.jar
2. download latest database: WKDB.dat
3. include WarKit in your Java project
4. make sure WKDB.dat is accessible from your application

####Random Code Examples
```java
// load warkit
WarKit wk = WarKit.load();

// create an armory 
Armory a = new Armory(wk, new HttpCache());

// get realm list for US
a.getRealmList(RegionT.US).forEach(System.out::println);

// find all Druids named "Edgy" on US
a.findPlayers("Fdgy", RegionT.US, 0, (name, realmName, realmSlug, level, race, cls) -> cls == ClassT.DRUID);

// import "Edgy/Suramar" from US Armory
Player p = a.getPlayer("Edgy", "Suramar", RegionT.US, false, System.out::println);

// and then change the gear a bit:
p.HEAD.clear();
p.FEET.clearGems(); 
p.NECK.setUpgradeLevel(4); 
p.MH.getSocket(0).setGem(wk.gemMap.get(12345)); 
p.F1.setBonuses(566, 42); 
p.WAIST.setCustomItemLevel(900);
p.T2.setUpgradeLevelMax();
p.T1.setExtraSocket(true);
p.HANDS.setItem(wk.wearableMap.get(23456));
p.getProf(0).setProf(ProfT.BS, 699);
p.SHOULDERS.setNamedItemBonusIndex(2);
p.SHOULDERS.setRandomSuffixIndex(3);
p.SHOULDERS.setAuxItemBonusIndex(7);

// most changes are dynamic, but some require manual validation
p.playerLevel = 90; 
p.spec = SpecT.GUARDIAN; // change spec
p.scaledItemLevel = -463; // global scaling (challenge mode)
p.validate();

// query some item information
Wearable w = wk.wearableMap.get(61931); 
w.extraSocket; // boolean, true if supports extra socket (eg. eye of black prince)
w.namedBonusGroup; // universe of bonus ids that alter the name desc
w.auxBonusGroup; // universe of bonus ids that alter other stuff
w.suffixGroup; // unvierse of suffixes
w.itemGroup; // universe of similar items
w.set; // set bonuses, etc.
w.unique; // unique group
w.statAllocs; // stat allocation distribution
w.socketBonus; // socket bonus enchantment
w.upgradeChain; // sequence of upgrades

// load to characters and diff them
Player p1 = ...
Player p2 = ...
PlayerDiff diff = PlayerDiff.compare(p1, p2);
System.out.println(diff.title);
System.out.println(diff.text);

// import from compact gear string
Player p = CompactGear.fromString(wk, "12345\n56789 :12345 $4125");

// export to compact gear string
String code = CompactGear.toString(p);

// import from simc profile string
Player p = SimcProfile.fromString(wk, "hands=bigswordofthebear,id=12345");

// export to simc profile string
String code = SimcProfile.toString(p);

// launch armory website
a.visitArmory("Fdgy", "Suramar", RegionT.US);

// launch wow progress website
a.visitWowProgress("Fdgy", "Suramar", RegionT.US);

// launch wow database site
Player p = ...
ExternalWebsite www = ExternalWebsite.WH; // wowhead
www.show(p.FEET.getItem()); // show item
www.show(p.HANDS.getEnchant()); // show spell
www.show(p.CHEST.getItemSet()); // show item set

// tally stats
Player p = ...
StatMap stats = new StatMap();
CompactBaseStats.collectStats(stats, p.race.compactBaseStats); // race stats
CompactBaseStats.collectStats(stats, p.spec.classType.getCompactBaseStats(p.playerLevel)); // class stats for level
p.collectStats(stats);
System.out.println(stats); // in-game stats (before buffs)
```
####Application Example
I'm using WarKit as the backend for my WoD universal paper doll application **Apparatus**. 
![Appartus Screenshot](https://github.com/adraffy/warkit/raw/master/apparatus-ss.png)

I'm also using WarKit as the backend for my WoD Feral simulator **Catus**. 

You can find out more about WarKit, WarBase, WarDBC, Apparatus, Catus, and others at the [Fluid Druid forums](http://fluiddruid.net/forum/viewtopic.php?f=3&t=4574).


