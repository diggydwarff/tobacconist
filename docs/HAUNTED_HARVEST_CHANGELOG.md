# Haunted Harvest Update — Changelog

## Tobacco processing

- Added **Haunted** curing for raw tobacco over Soul Fire, with a Create Haunting-airflow equivalent.
- Added **Latakia** from Sun-Cured Oriental finished through extended ordinary smoke.
- Added **Dark Fired Kentucky** from Fire-Cured Burley finished through extended ordinary smoke.
- Added **Perique** pressure fermentation.
- Added **Cavendish** and **Black Cavendish** heated pressure-fermentation stages.
- Added **Stoved Virginia** processing.
- Added **Pressed Plug** and corrected Flake production to **Rough Cut -> Pressed Plug -> Flake**.
- Added vanilla piston pressure for Tobacco Barrels and Create Mechanical Press equivalents.
- Added distinct barrel modes for **Pressing**, **Fermenting**, **Pressure Fermenting**, **Stoving**, **Aging**, and **Idle**.
- Removed ambient smoke particles from Tobacco Barrels; barrel processes are now visually sealed.

## Haunted Harvest content

- Added Haunted smoke/soul-wisp presentation without granting a direct power/quality bonus.
- Haunted tobacco now propagates into finished cigar/cigarette naming and smoke behavior.
- Added autumn flavor profiles: **Maple, Nutmeg, Ginger, Clove, and Pecan**.
- Added six hidden autumn secret blends: **Witching Hour**, **Graveyard Watch**, **Headless Horseman**, **Harvest Moon**, **Maple Hollow**, and **All Hallows' Eve**. Witching Hour and Graveyard Watch are non-aromatic.
- Added tag-first optional flavor compatibility so external food mods can supply autumn ingredients without becoming required dependencies.
- Added the **Jack-o'-Lantern Hookah**, a seasonal two-block Hookah using the supplied pumpkin model and texture while retaining normal Hookah mechanics and automation.
- Added the **Corn Cob Pipe**, a rustic seasonal reusable smoking pipe.

## Create integration

- Added Create equivalents for Haunted curing, specialty smoke finishing, barrel pressure, heated specialty processing, Plug production, and Plug-to-Flake slicing.
- Preserved tobacco metadata across Create processing routes.

## Manual / QA

- Expanded Patchouli documentation for Plug, Flake, barrel modes, pressure fermentation, and traditional named tobacco styles.
- Added `/tobacconist barrel finish` for instantly completing the current finite barrel process during QA; `/tobacconist barrel press` remains available for forced Plug testing.
- Documented that barrels do not emit smoke; Haunted/smoke effects stay on curing and smoking sources.
- Added repository-ready wiki pages for Haunted Harvest, specialty processing, and Tobacco Barrel behavior.

## Bug fixes / audit

- Fixed immature age 4-6 tobacco plants dropping mature leaves or bonus seeds; mature harvest behavior now requires age 7.
- Fixed Tobacco Box crafting counting entire input stacks instead of one consumed item per crafting slot, and preserved box labels when emptied.
- Moved barrel process timing to monotonic game time with save migration so sleep or `/time` changes cannot rewind or distort progress.
- Rebuilt blend spoil-check metadata after averaged aging changes.
- Restored compact Flue curing clearance: one clear air block above the rack with a roof 2-4 blocks above.
- Fixed Create flavor recipe conditions for Double Apple, Double Eden's Apple, Double Royal Apple, and Berry.
- Removed the obsolete duplicate compostables data-map path.
- Restored Latakia and Dark Fired Kentucky smoke-finishing on hanging tobacco bunches (ordinary Campfire/Create smoke only), matching Drying Rack behavior.
- Restored immediate client sync for the legacy `/tobacconist barrel ferment` QA helper.
- Added the Jack-o'Lantern Hookah to Hookah Collector, corrected its mining tool to an axe, and gave it a dedicated 16x16 inventory sprite consistent with the other Hookahs.
