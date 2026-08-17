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