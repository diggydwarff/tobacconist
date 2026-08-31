## Update 4.0.0 - The Tobacco Industry Update

Tobacconist 4.0.0 is released for **Minecraft 1.20.1 Forge** and **Minecraft 1.21.1 NeoForge** with the same intended gameplay and feature set. Loader/API differences are implementation details and are not separate feature versions.

### Major Changes
- Greatly expanded optional **Create integration** for factory-scale tobacco production while keeping the normal hand-crafted progression fully usable.
- Reworked flavoring around the full **Aqua Vitae → Flavoring Essence → Aromatic Tobacco / Flavored Molasses → Shisha** chain.
- Expanded metadata-aware automation so variety, quality, cure, cut, age, fermentation, flavor, blend identity, and other tobacco data survive processing.
- Added broad tag-driven flavor compatibility so supported ingredients from other food/farming mods can participate without hard dependencies.

### Create Processing & Automation
- Added Mechanical Harvester support for mature two-block tobacco crops while preserving normal quality and seed behavior.
- Added Deployer + Chaveta cutting: **Cured Leaf → Rough → Ribbon → Shag**.
- Added Mechanical Press support for **Flake** and for finishing the dedicated Cigarette/Cigar production lines.
- Added Mechanical Mixer support for metadata-aware blending, Aqua Vitae, Flavoring Essence, flavored molasses, Shisha, and bulk leaf-quality homogenization.
- Added dedicated automated Cigarette and Cigar assembly using **Deployer → Incomplete Product → Mechanical Press** while preserving full product quality.
- Added automated Tobacco Box packing and reusable Brass Label application.

### Leaf Quality Homogenization
- Added Basin-local bulk homogenization for compatible raw or cured tobacco leaves.
- Homogenization requires multiple quality values and uses a count-weighted average that **rounds down**, preventing processing from creating quality points.
- No redstone signal targets 64 leaves; signals **1–14** select progressively larger continuous batch sizes up to 576 leaves.
- Signal **15** performs a one-shot finish of the compatible tobacco currently in the Basin, with a 16-leaf minimum.
- Pending homogenizer inputs are protected from generic extraction, and full uniform batches can pass through safely to prevent factory deadlocks.
- Homogenizers can be chained into larger downstream batches for more consistent bulk grading.

### Curing & Storage
- Rebuilt the wooden Drying Rack as a one-block, 16-leaf rack with live load/progress visuals.
- Wooden Drying Racks accept valid input from the **top or horizontal sides**; bottom insertion is blocked and bottom extraction is available only after curing finishes.
- Added traditional **Hanging Tobacco Bunches** for curing, decoration, and storage.
- Added **Tobacco Crates** that losslessly store nine matching tobacco units while preserving each unit's metadata.
- Added the **Industrial Drying Rack**, a two-block, 32-leaf Create factory rack with shared inventory and validated top/side automation.
- Industrial curing requires matching airflow across both rack levels from **two distinct Encased Fans**; Air, Sun, Fire, and Flue curing remain environment-dependent.
- Added Create-assisted curing and tobacco-smoke ventilation using Encased Fan airflow.
- Added redstone pause control to the **Flue Firebox**; powering it pauses heat/fuel consumption and preserves the remaining burn time.

### Factory Logistics & Monitoring
- Added validated Mechanical Arm support for Drying Racks, Tobacco Barrels, Flue Fireboxes, and Hookahs.
- Expanded support for Funnels, Chutes, Belts, Depots, Packagers, Stock Links, filtered automation, and package routing.
- Expanded Create Attribute Filter support for tobacco identity, cure, cut, flavor, blend data, exact quality, quality tiers/bands, and aging data.
- Added tier-aware Factory Gauge/Frogport matching while preserving meaningful tobacco identity.
- Expanded Display Link information for Tobacconist processing blocks and homogenization.
- Added the **Production Monitor** for non-invasive factory throughput tracking:
  - Counts **Items**, **64-item Stack equivalents**, or successful **Transfers**.
  - Supports normal item filters plus Create Filters/Attribute Filters.
  - Configurable target behavior: Keep Counting, Stop Counting, or Reset Count with overflow preservation.
  - Output modes: None, Pulse, or Hold; also supports comparator output and optional rising-edge external reset.
  - Exposes live count/rate/status through its UI, Tobacconist's Spectacles, Create Goggles, and Display Links.

### Automation Quality
- Hand crafting and the dedicated Deployer → Incomplete Product → Mechanical Press Cigarette/Cigar route retain full quality.
- Generic crafting automation while Create is installed applies a configurable quality penalty to Cigarettes/Cigars; the default is **10 raw quality points** on the underlying 0–120 scale.
- The automation penalty can be configured down to 0.

### Aqua Vitae, Essences & Shisha
- Added **Aqua Vitae** as the extraction/crafting spirit used for Flavoring Essences.
- Added Flavoring Essences for all supported flavor profiles.
- Standardized Tobacconist processing bottles to **250 mB per bottle-equivalent**.
- Create Spouts use **250 mB Flavoring Essence** for aromatic casing.
- Flavored Molasses uses **250 mB Plain Molasses + 250 mB Flavoring Essence** per bottle-equivalent.
- Shisha can use suitable loose tobacco cuts or blends, retains cut-dependent quality, and supports multiple flavors.

### Flavor Compatibility
- Flavor ingredients use shared Tobacconist flavor tags, common `c:` tags, and compatible legacy `forge:` tags where useful.
- Added broad optional ingredient mappings for Farmer's Delight, Farmer's Respite, Fruits Delight, Croptopia, Neapolitan, HerbalBrews, Expanded Delight, Rustic Delight, Coffee Delight, Create Confectionery, and other correctly tagged mods.
- Added 24 new flavor profiles:
  - Coffee, Vanilla, Strawberry, Banana, Mint, Lime, Grapefruit, Cherry, Grape, Coconut, Blackberry, Raspberry
  - Cinnamon, Caramel, Apricot, Plum, Dragonfruit, Marshmallow, Tea, Hibiscus, Lavender, Peanut, Brownie, Custard

### Secret Blends
- Expanded hidden named blend support so recipes can require exact varieties plus minimum quality, cure method, and aromatic flavor.
- Added configurable per-blend visual styling with automatic fallback tinting.
- Added rare configurable built-up smoking bonuses for a small set of legendary secret blends.
- Create blending preserves component identity so qualifying secret blends remain detectable after mechanical processing.

### Ponder, JEI & Manual
- Expanded Create Ponder coverage for wooden and Industrial curing, homogenization, cutting/pressing, blending, Aqua Vitae, flavored molasses, Cigarette/Cigar production, Production Monitor use, and factory logistics.
- Expanded JEI coverage and cleaned misleading/fake processing representations.
- Updated The Tobacconist's Manual for current curing, automation, flavoring, monitoring, quality, and factory workflows.

### Visuals & Quality of Life
- Added per-flavor Flavoring Essence bottle colors while retaining the bottle-style appearance.
- Added/refined dedicated textures for Rough, Ribbon, Shag, and Flake tobacco cuts and refreshed multiple tobacco/processing items.
- Updated Drying Rack, Industrial Drying Rack, and Hanging Tobacco visuals to better represent load, cure progress, and tobacco variety.
- Improved Spectacles, Hookah, and other item/block visuals and cleaned obsolete or unused resources.

### Fixes & Compatibility
- Fixed multiple crop loot, automation, Drying Rack, Hookah, Tobacco Barrel, tobacco-quality, recipe, model, tag, and rendering issues found during the 4.0 development cycle.
- Fixed Double Apple, Double Golden Apple, and Double Royal Apple Create recipe selection/production.
- Prevented Blended Tobacco from entering single-variety quality-averaging recipes, preserving blend identity.
- Hardened optional integrations so Tobacconist remains usable without Create, Curios, JEI, Patchouli, or supported food-mod integrations installed where those dependencies are optional.
- Standardized Tobacconist's Spectacles around the **Curios Head slot** with the vanilla Head slot as fallback; Curios Mouth-slot smoking remains available when Curios is installed.


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
