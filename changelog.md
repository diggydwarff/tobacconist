## Update 4.0.0 - The Tobacco Industry Update

### Major Changes
- Added extensive **Create 6 integration** for industrial-scale tobacco production while keeping Create fully optional
- Reworked tobacco flavoring into a complete **Aqua Vitae → Flavoring Essence → Aromatic Tobacco / Flavored Molasses → Shisha** production chain
- Expanded automation so Create factories preserve tobacco **variety, quality, cure, cut, aging, fermentation, flavor, blend identity, and other metadata** instead of producing generic replacement stacks
- Added broad **tag-driven flavor compatibility** so matching ingredients from other food and farming mods can participate without becoming hard dependencies

### Create Automation
- Added **Mechanical Harvester** support for mature two-block tobacco crops
  - Preserves normal harvest quality and seed behavior
  - Handles tall crops without duplicating upper/lower drops

- Added **Deployer + Chaveta** tobacco cutting
  - Cured Leaf → Rough Cut → Ribbon Cut → Shag Cut
  - Supports normal Chaveta processing while preserving tobacco metadata

- Added **Mechanical Press** processing
  - Rough Cut tobacco can be pressed into **Flake**
  - Cigarette and Cigar assembly can be mechanically finished without losing product metadata

- Added **Mechanical Mixer** processing
  - Blend two or three compatible loose tobaccos into metadata-aware blends
  - Preserve blend composition and secret blend identity
  - Automate Aqua Vitae, Flavoring Essence, flavored molasses, aromatic casing, and Shisha production

- Added automated **Cigarette and Cigar assembly** using Deployers and the Mechanical Press
- Added automated **Tobacco Box packing** and reusable **Brass Label** application

### Create Curing & Smoke
- Encased Fan airflow can accelerate **Air Curing** on Drying Racks
- Fan-blown **Lava heat** can accelerate **Flue Curing**
- Fan-blown **Campfire smoke and heat** can accelerate **Fire Curing**
- Sun curing remains dependent on the normal sunlight/environment rules
- Added Create-aware tobacco smoke ventilation
  - Encased Fans can push and pull nearby tobacco smoke
  - Indoor smoke can travel along ceilings toward extraction airflow
  - Smoke is consumed at fan intakes and resumes normal upward behavior after being exhausted outdoors

### Create Logistics
- Added native **Mechanical Arm** interaction support for Tobacconist processing blocks
  - Drying Racks accept raw leaves and expose only completed cured output
  - Tobacco Barrels respect batch compatibility and processing rules
  - Flue Fireboxes accept valid fuel
  - Hookahs route valid fuel, Shisha, and water-slot inputs

- Added Create logistics support for **Stock Links, Packagers, Funnels, Chutes, Belts, Depots, and filtered automation**
- Added package import routing for Drying Racks, Tobacco Barrels, Flue Fireboxes, and Hookahs
- Added **Factory Gauge quality-tier matching**
  - Different numeric quality values can satisfy a request when they belong to the same Tobacconist quality tier
  - Meaningful tobacco identity and metadata still remain part of matching
- Expanded **Create Attribute Filter** support for tobacco variety, cure, cut, quality, flavor, blends, labels, and downstream products
- Added **Display Link** information for Drying Racks, Tobacco Barrels, Flue Fireboxes, and Hookahs

### Aqua Vitae, Essences & Shisha
- Added **Aqua Vitae** as a dedicated extraction/crafting spirit
  - Brewing Stand: Mundane Potion + Wheat → Aqua Vitae
  - Create: Water + Sugar + Wheat in a heated Mechanical Mixer → Aqua Vitae

- Added **Flavoring Essences** for all supported flavor profiles
  - Aqua Vitae + a matching flavor ingredient → Flavoring Essence
  - Essences are full **single-use** bottles
  - Create fluid containers represent **1000 mB** per Essence bottle

- Added **Aromatic Tobacco**
  - Apply one Flavoring Essence directly to suitable loose tobacco for a light casing
  - Create Spouts can apply the same treatment using 1000 mB Essence
  - Original tobacco cut and processing metadata are preserved

- Reworked **Flavored Molasses**
  - Plain Molasses + one full Flavoring Essence → one full flavored Molasses bottle
  - Create equivalent uses 1000 mB Plain Molasses + 1000 mB Essence
  - Used processing bottles return normal vanilla Glass Bottles

- Reworked **Shisha** production
  - Shisha is no longer restricted to Rough Cut tobacco
  - Any suitable loose cut or blend can be used, with cut choice still affecting quality
  - Full flavored Molasses acts as the heavy wet treatment
  - Multi-flavor Shisha remains supported

### Flavor Compatibility
- Flavor ingredients now use shared **`tobacconistmod:flavorings/<flavor>` tags** for both Brewing Stand and Create Mixer recipes
- Added support for common `c:` ingredient tags and compatible legacy `forge:` tags where useful
- Existing flavors such as Peach, Lemon, Mango, Orange, Pineapple, Kiwi, Blueberry, Cranberry, Fig, Cocoa, Honey, Melon, and others can now accept equivalent ingredients from multiple mods instead of being tied to one provider
- Added compatibility inputs for **Farmer's Delight, Fruits Delight, Croptopia, Neapolitan, HerbalBrews, Expanded Delight, Rustic Delight, Coffee Delight, Create Confectionery**, and other correctly tagged mods
- All food-mod integrations remain optional

- Added 24 new flavor profiles:
  - **Coffee, Vanilla, Strawberry, Banana, Mint, Lime, Grapefruit, Cherry, Grape, Coconut, Blackberry, Raspberry**
  - **Cinnamon, Caramel, Apricot, Plum, Dragonfruit, Marshmallow, Tea, Hibiscus, Lavender, Peanut, Brownie, Custard**

### Create Ponder
- Added Tobacconist **Ponder scenes** for Create-enabled factories
- Added curing demonstrations for:
  - Air curing and fan-assisted Air curing
  - Sun curing
  - Campfire Fire curing and fan-blown smoke curing
  - Flue Firebox curing and fan-blown Lava heat curing
- Added Ponders for tobacco cutting/pressing, blending/barrel processing, Aqua Vitae/Essence/Shisha production, Cigarette/Cigar assembly, and factory logistics
- Raw and cured leaves link directly to the relevant curing/processing tutorials

### Vanilla Integration & Tobacconist Polish
- Reworked the **Tobacconist villager** into a simple five-level profession while keeping seed acquisition as its core role
  - Normal Tobacconists no longer sell **Wild Tobacco Seeds**
  - New Tobacconists reliably sell two cultivated seed varieties suited to their local region
  - Later levels add a small set of useful tobacco supplies, equipment, and storage trades
- Added restrained **Wandering Trader** access to Wild Tobacco Seeds and Wild Flowering Tobacco as a backup to natural discovery
- Added the reusable **Tobacco Pouch**, holding up to 16 units of one exact loose-tobacco batch and packing pipes directly from the offhand
- Added a simple **Clay Smoking Pipe** with the normal reusable pipe-smoking workflow
- Added vanilla **Composter** support for tobacco seeds, raw leaves, Wild Flowering Tobacco, and Spoiled Tobacco
- Added **Potted Wild Flowering Tobacco** and vanilla flower/bee tag integration
- Added **Seeds of the World** and **Well Equipped** advancements
- Expanded tobacco-family item tags and added common `c:seeds/tobacco` and `c:crops/tobacco` interoperability tags
- Updated The Tobacconist's Manual for regional seed trading, new items, composting, Hookah effects, current automation, tobacco tags, and the full Create 4.0 integration
- Removed two obsolete duplicate Manual categories left beside the newer Cutting & Crafting and Fermenting & Aging chapters

### Visuals & Quality of Life
- Added subtle per-flavor **Flavoring Essence bottle colors** while retaining the vanilla-style bottle appearance
- Added distinct **pressed Flake tobacco item models/textures**
- Cleaned partial transparency artifacts from several tobacco item textures
- Reorganized the creative tab so **all Flavoring Essences appear together before all Molasses bottles**
- Updated The Tobacconist's Manual with the new industrial processing, flavor compatibility, and Create workflows

### Fixes & Compatibility
- Hookah hose draws now use the same nicotine, configured-effect, quality, and aging pipeline as other smoking methods, based on the loaded Shisha
- Packed pipes now evaluate smoking quality/aging effects from the tobacco actually stored inside the pipe instead of the pipe item itself
- Fixed Double Apple, Double Golden Apple, and Double Royal Apple Create recipe selection/production
- Hardened Create classloading and resource conditions so **Tobacconist still launches and functions without Create installed**
- Kept Curios, JEI, Patchouli, and supported food-mod integrations optional
- Standardized Spectacles compatibility around the **Curios Head slot** with vanilla Head-slot fallback
- Removed obsolete Forge/1.20 resource leftovers, stale generated resources, and orphan item models from the 1.21.1 source tree

-----------------

## Update 3.1.0 - The Refinement Update

### Major Changes
- Updated Tobacconist to **Minecraft 1.21.1**
- Migrated from **Forge to NeoForge**
- Preserved existing item and block registry IDs where possible for world compatibility

### New Features
- Added **Tobacconist's Spectacles**
  - Inspect tobacco crops and processing blocks in-world
  - View crop growth, growing conditions, potential quality, curing progress, barrel status, aging/fermentation information, and more
  - Can be worn in the **Curios Eyes slot**
  - Falls back to the vanilla **Head slot** when Curios is not installed

- Added **Mouth Slot Smoking Hotkey**
  - Smoke pipes, cigars, and cigarettes directly while equipped in the Curios Mouth slot
  - Added action-bar feedback for empty, invalid, or exhausted smoking items

- Added a new **Tobacconist advancement tree**
  - A New Leaf
  - Homegrown
  - Cured to Perfection
  - A Cut Above
  - Tobacconist's Eye
  - Roll Your Own
  - Fine Taste
  - Cloud Culture
  - Master Tobacconist

- Completely expanded **The Tobacconist's Manual**
  - Updated to accurately document the current mod
  - Covers growing, curing, cutting, fermentation, aging, quality, smoking, hookahs, automation, Spectacles, compatibility, and more
  - Added normal crafting recipes and JEI visibility for the Manual

### Configuration
- Added option to **disable the Tobacco Quality System**
  - Processing mechanics remain available when quality is disabled
  - Existing quality data is preserved if the system is re-enabled

- Added option to **disable Nicotine effects**

### Quality of Life
- Improved Curios integration while keeping **Curios fully optional**
- Improved Spectacles rendering and positioning
- Spectacles now use the same forehead appearance whether equipped through Curios or the vanilla Head slot
- Improved crop inspection so **environmental conditions are evaluated separately from crop maturity**
- Added improved processing information for Drying Racks and Tobacco Barrels
- Improved hopper and automation support
- Removed misleading/outdated tooltips and debug instructions
- Cleaned up the Tobacconist creative tab
- Restored the Moroccan Hookah painting to normal painting selection

### Gameplay & Balance
- Cigarettes now correctly use the intended tobacco cut-quality system
  - **Shag** is best for cigarettes
  - **Flake** is best for cigars
  - **Rough** is best for shisha

- Added a short server-side cooldown to Mouth-slot smoking to prevent packet/effect spam
- Improved smoke particle behavior when manually smoking

### Fixes
- Fixed several tobacco crop loot tables incorrectly checking Burley crop states
- Fixed duplicate seed drops from tall tobacco crops
- Fixed several incorrect block hardness and tool requirements
- Fixed Drying Racks allowing hoppers to extract an entire batch when only one item was requested
- Fixed Hookah slots accepting invalid items
- Fixed incorrect Hookah processing logic involving the water slot
- Fixed Hookah smoke-position calculations
- Fixed Tobacco Barrel client synchronization
- Fixed Tobacco Box quality grades using outdated quality thresholds
- Fixed wooden pipe colors rendering incorrectly after the 1.21 rendering changes
- Fixed Tobacconist's Spectacles rendering twice when equipped in the vanilla Head slot
- Fixed Moroccan Hookah painting data and placement
- Fixed and cleaned up several recipes, models, loot tables, tags, and obsolete resources

### Compatibility
- Improved **JEI 1.21.1 compatibility**
- Improved **Patchouli 1.21.1 integration**
- Improved **Curios 1.21.1 integration**
- Hardened optional Curios support so Tobacconist can run without Curios installed
- Maintained optional Farmer's Delight and Fruits Delight integration

-----------------

## Update 3.0.0 - The Processing Update

### Major Additions
- **Complete Tobacco Processing System**
  - Tobacco now follows a full pipeline: **grow → cure → ferment/age → cut → craft**
  - Final product quality now depends on the entire process

- **Growth Quality System**
  - Each tobacco variety now has unique growth preferences
  - Inspect crops with **Shift + Right Click** to view growth quality

- **Curing System**
  - Added **Drying Rack**
  - Tobacco leaves can now be **Air, Sun, Fire, or Flue Cured**
  - Curing quality is tracked and carried forward
  - Mixed leaves average their quality when crafted together

- **Fermentation & Aging**
  - Added **Tobacco Barrel**
  - Manage **temperature, humidity, and light** to ferment tobacco
  - Aging improves quality over time (up to a limit)

- **Tobacco Cutting**
  - Added **Chaveta cutting tools** (Stone → Netherite)
  - Tobacco can now be cut into multiple styles:
    - Ribbon
    - Shag
    - Rough
    - Flake

### New Content
- **Drying Rack** block
- **Tobacco Barrel** block
- **Chaveta tools**
- **Loose tobacco variants**
- **Japanese Kiseru pipes**
- New **tobacco-themed paintings**
- New **advancements**

### Integration & Quality of Life
- **JEI integration** for custom recipes
- **Curios mouth slot support**
  - Smoking items equipped in the mouth slot can be used with empty-hand right click
- **Optional Patchouli guide book support**
- Molasses flavorings now have **4 uses instead of 1**

### Balance
- **Nicotine effect** now grants small **mining speed and movement boosts**

### Fixes
- Improved **hookah textures**
- Fixed **JEI compatibility for custom recipes**

----------------------------------------------------------------------------

## Update 2.3.1
##### Fixes:
- Ornate hookah recipes work
- Fixed iron/copper pipe recipe bug
- Fixed hookah stacking bug

## Update 2.3.0
##### New Features:
- NEW! Added additional types of ornate double-high hookahs
    - Ornate Copper hookah
    - Ornate Iron Hookah
    - Ornate Gold Hookah
    - Ornate Diamond Hookah
    - Ornate Amethyst Hookah
#### Changes:
- Base hookah is now colorless and can be dyed with any dye (including glow ink sacs)

----------------------------------------------------------------------------

## Update 2.2.0
##### New Features:
- NEW! Curious API Integration!
    - Wear your pipes, cigars, and cigarettes in your mouth when using the Curious API mod!
- NEW! Added bamboo charcoals and support for charcoal in hookah
    - Smelt bamboo in a furnace/blast furnace to produce bamboo charcoals
    - From best to worst hookah fuel: bamboo charcoals > normal charcoals > coal
- NEW! Added new types of smoking pipes (visual differences only, just for fun)
    - Gold smoking pipe
    - Netherite smoking pipe
    - Diamond-encrusted smoking pipe
    - Emerald-encrusted smoking pipe
    - Lapis-encrusted smoking pipe
    - Gem-encrusted smoking pipe
    - Emerald Aztec smoking pipe
##### Changes:
- Updated some item textures
- Upped pipe uses from 20 to 40 per tobacco
- Changed duration of normal coal in hookah to be a bit worse
##### Fixes:
- Fixed pipes stacking bug
- Hookahs only except water now
- Hookah fuel is properly consumed
- Hookah burn duration is stable
- Hookah fuel texture indicator now works