## Alpha 15 — Vapor and specialty-pipe audit

- Fixed saved-card VG/nicotine selectors starting from hidden bench defaults instead of the displayed recipe. Editing either selector preserves the other saved setting.
- Fixed the client Blend button pricing small/large bottles as 250 mB. It now checks the selected bottle's real capacity.
- Auto retains the latest replacement card's formula after card removal and saves that change even while awaiting ingredients.
- Menus bind to their original bench inventory; replacing the block closes the old screen. Dead/spectator players cannot use production/refill buttons.
- Vape and Bubble draws cancel when a menu opens or the player dies/becomes a spectator. Completion callbacks and instant mouth draws validate the same conditions, including simultaneous held use.
- Instant mouth cooldown timestamps clear on logout, dimension changes and server shutdown; old world time cannot block a later session.
- Named liquid pouring preserves custom names and product labels in empty recipients; partially filled recipients retain their own names.
- Large water bottles can pour exact 250 mB portions into ordinary Water Bottles, retaining the remainder. Normal Water Bottles remain full-size vanilla potions.
- Fixed a cauldron duplication path when a sized ingredient was followed by a fixed-dose normal bottle. Fluid inputs must simulate the exact required debit; unsupported partial portions leave the source untouched. Affected older unfinished batches refund their inputs.
- Pending vapor/novelty particle queues discard dead, removed and distant sources.
- Updated in-game guide and test checklist. All Alpha 14 capacities, art, mouth angles, occasional puffs and previous features remain. Matching Alpha 15 clients/servers use network version 14.

## Alpha 14 — Occasional mouth puffs, wooden-pipe angles and smaller tanks

- Novelty mouth effects emit short, staggered cosmetic puffs every 6–8 seconds, with quiet gaps. Smoke and signature effects share the puff timing; Wizard emits one mini ring. Deliberate draws, visibility and resource rules remain unchanged.
- Specialty sprites match the wooden pipe’s final rendered angle while preserving their mouthpiece anchors. Existing cigarette/cigar/Cigalike poses remain.
- Cigalike/pen/Box Mod tanks hold 75/100/150 mB. Small bottles hold 150 mB, enough for two Cigalikes. Normal/large bottles remain 250/1000 mB; processing recipes retain their existing yields.
- Partial bottles retain actual contents. Overfilled legacy devices retain every stored unit and drain normally, refusing refills until below capacity; gauge/bar bounds and a tooltip handle this transition.
- Updated creative/JEI sample fills, glass returns, guides and test checklist. Matching alpha 14 clients/servers use network version 13.

## Alpha 13 — Curios recovery, supplied pipe art and effect polish

- Restored a direct mouth player layer with explicit client mod-bus registration on both skin models for cigarettes, cigars, normal pipes, Cigalike and specialty pipes. Removed native Curios mouth renderer registration. Lowered specialty bowls beside the lips instead of alongside the eyes. Model and particle selection share cosmetic/functional visibility rules, with missing flags defaulting to visible.
- Added a one-time upgrade repair for saved hidden mouth flags; synchronized through the public Curios inventory update path. Later hide/show choices are preserved. Current synced equipment wins over stale passive profiles.
- Restored the original Bubble sprite, assigned the previously mislabeled Bubble sprite to Confetti, and installed supplied Petal/Whistling/Sunkeeper/Echo artwork. Petal is normalized to mip-safe 16x16 without interpolating colors; the original 17x17 art is preserved.
- Soulglass keeps its new silver-blue wisps/glints and regains a few native soul flames. Sunkeeper gains a few small bright gold sparks.
- Echo now has soft layered blue-violet ripples, traveling highlights, orbiting pinpoints and drifting light motes. Maximum draws retain three staggered pulses; normal smoke remains.
- Wizard retains real smoke rings, with more uneven radii, mild oval shapes, varied thickness and breakup. Its accompanying ordinary smoke is reduced, including idle smoke and animation-disabled use.
- Whistling uses louder flute sound (0.65 Players volume), with a five-note phrase on full draws, a single note on short draws and silent idle notes. Music respects novelty sound settings and cancels on death/logout/level changes.
- Network version 12; install matching alpha 13 client/server JARs.

## Alpha 12 — Native Curios rendering, supplied art and four new pipes

- Explicitly registered Curios item capabilities and native renderer factories for every supported mouth item. Removed the manually injected mouth layer, preserving ordinary cigarette/pipe poses and adding anchored specialty poses.
- Models/passive effects now share Curios cosmetic override and functional render-toggle rules, including additional mouth indices. Idle effects can initialize immediately from synced visible contents while server profiles synchronize.
- Installed the supplied Bubble, Wizard, Soulglass and Galaxy sprites byte-for-byte; Galaxy maps to Astral. FIXED transforms are neutral for specialty sprites.
- Replaced Soulglass’s green soul sprite with soft native glow wisps and silver/blue/lavender spark glints; ordinary smoke and curling motion remain.
- Added Petal (flower refill, drifting petals), Whistling (Water Bottle refill/returned glass, notes and draw-dependent flute pitch), Sunkeeper’s (Desert Pyramid loot, warm actual-smoke sunburst), and Echo (Ancient City loot, blue pulses/quiet chime with smoke).
- New pipes support existing hold/release and Curios tap/full controls, bounded particles, optional Create fan effects, JEI, creative entries and persisted contents. New art remains placeholder art.
- Added Hidden Histories for the two new relics without changing the original three-relic milestone. Existing loot/config rules and at-most-one relic per eligible chest remain.
- Verified actual graphical client Curios rendering/particles, including empty/cosmetic cases; corrected observed lip alignment and reduced passive smoke/bubble/ring sizes.
- Network version 11; matching alpha 12 client/server JARs required.

## Alpha 11 — Curios consistency and specialty effect polish

- Fixed brief mouth-key taps being rejected by toys/Cigalike; all loaded specialty items accept a valid one-tick tap and still auto-complete maximum holds exactly once. Held item minimums remain unchanged. Cancel/removal/zero-time draws spend nothing.
- Added server-selected passive mouth profiles with immediate change updates, one-second tracking heartbeats and three-second client expiry. Visibility, drawing, range and particle budgets remain enforced. Increased the tiny Cigalike plume and adjusted its mouth anchor/angle toward normal cigarette positioning.
- Removed all Bubble smoke, including the active server smoke call and passive profile. Mundane Potion refills remain.
- Confetti now uses a saturated seven-color palette without white mixing. Maximum draws emit 96 pieces in four short waves; short and idle puffs stay small.
- Astral has 20 constellation silhouettes. Only maximum puffs draw a constellation, with a stable connected-star shape and surrounding randomized sparkles; shorter and idle puffs emit randomized sparkles only. Existing item/sparkle art is preserved.
- Reworked Soulglass into uneven curling cyan wisps plus mint/ice/silver glass glints, with three curls on maximum draws and gentler rising/fading motion.
- Wizard's actual smoke rings retain coherent movement and a visible hole, with subtle radius ripples, thickness/opacity variation and staggered breakup instead of a mathematically uniform ring.
- Corrected small/large filled bottles using the normal potion's glass and liquid layers resampled together. Full small bottles and matching native shading at every size.
- Network version 10; install matching alpha 11 client/server JARs.

# 4.2.0 alpha 10 — Real smoke rings and consistent mouth effects

- Replaced custom ring billboards with actual native campfire-smoke particles/textures, closed ring formation, shared initial motion and gradual breakup.
- Restored normal smoke alongside Bubble and relic signatures. Confetti remains smoke-free, refills with Paper and always emits a seven-color palette; removed dye selection.
- Removed Firefly registration, recipe, creative/JEI entries, tags and assets.
- Added unique Astral item art and four custom animated sparkle sprites with twinkle and slow rotation.
- Reoriented Bubble's mouth pose with a forward stem and upright bowl.
- Added bounded small idle mouth smoke/vapor/bubbles/confetti/relic effects for loaded, visible items, including fresh packs. Idle emission spends no resources and stops on empty/uncharged items.
- Updated guides/test checklist and network version to 9. Install matching alpha 10 clients/servers.

# 4.2.0 alpha 9 — Mouth poses, bottled refinement and relic polish

- Anchored the Cigalike and supplied Bubble Pipe to their actual mouthpieces, corrected sprite rotation, and removed the Bubble Pipe model's inherited fixed-context flip.
- Finished cauldron batches visibly boil down to one 250 mB portion and leave an empty cauldron when drained or discarded. Older partial batches follow the same rule; returned inputs remain exact.
- Added ordinary Brewing Stand refinement: Crude Extract + Paper → Nicotine Extract; Aqua Vitae + Sugar Cane → Clear Base. Vanilla fuel/time/three-slot controls apply. Partial amounts, all bottle sizes and labels survive. Existing Sugar Essence and Create recipes remain intact.
- Loaded creative Confetti/Firefly entries, added empty-use hints, direct other-hand refill and the normal instant-use fallback when draw animations are disabled. Relics accept loose tobacco directly from the other hand and show an empty packing hint.
- Replaced dotted Wizard effects with coherent, soft expanding rings; full draws exhale two rings eight ticks apart. Removed competing normal/passive smoke from relic signatures while retaining tobacco effects. Soulglass wisps emerge gradually and fade softly; Confetti pieces now tumble as rectangles.
- Bubble Pipe refills require Mundane Potion exclusively; water and sized water are rejected without loss.
- Updated JEI guides, tests and network version to 8. Install matching alpha 9 clients/servers.

# 4.2.0 alpha 8 — Novelty toys and exploration relics

- Added Confetti and Firefly pipes with 24-blow refills, short/full use, persistent charges, and Curios mouth support. Paper refills confetti; dyes select its particle color. Glow Berries refill fireflies. Toy recipes produce loaded pipes and apply no smoking effects.
- Added loot-only Wizard's Longpipe (dungeons/mineshafts), Soulglass Pipe (Nether fortresses/bastion treasure), and Astral Pipe (End Cities), preserving normal tobacco/pouch packing and effects. Their full-draw flourishes produce smoke rings, soul wisps, and a constellation; short draws are smaller.
- Added server-configured 4% Rare / 1.5% Mythic chest chances, a loot switch, source hints in tooltips/JEI, and quiet optional flourish sounds. The modifier adds at most one relic without replacing vanilla loot.
- Added Curio Collector, Ringmaster, and Across the Realms milestones. Collections remember separate discoveries; Ringmaster requires a successful full Wizard draw.
- Added bounded multiplayer particle bursts with native textures, particle-setting support, and optional Create fan airflow. No entities, world edits, or dynamic lights are created.
- Installed five separately named placeholder PNGs copied from existing pipe sprites. Final artwork can replace those files directly; sources and instructions are documented.
- Updated network version to 7. Install matching alpha 8 client/server versions.

Apply this follow-up after alpha 7. Compilation and dedicated-server checks pass with and without Create, covering toy resources/colors/persistence, relic tobacco/pouch behavior, Curios draws/cancellation/cooldown, all three milestones, packet encoding, and bounded geometry. Each configuration generated 7,500 eligible chest loots, confirming the intended sources, reasonable default availability, one-relic limits, and configured/disabled loot behavior. All 1340 resource JSON files parse. Client appearance and final-art mouth alignment remain in-game checks; the included checklist covers the new mechanics and loot.

# 4.2.0 alpha 7 — Dyed vapes and visible extraction

- Made all three vapes dyeable through vanilla dye crafting, including blended colors and water-cauldron washing. Dyed casing masks preserve mouthpieces, glass, screens, and indicators; undyed artwork remains unchanged. Every device component survives dyeing and washing.
- Changed finished extraction into visible liquid inside the same cauldron, colored by its fluid, with a declining fill level and normal bottle collection. Existing 250 mB yields, sized bottles, unused input returns, and remaining water are preserved.
- Added persistent/synchronized finished-cauldron contents and a drain-only capability for Create pipes. Finished extract cannot be overwritten by water/buckets, rain, or dripstone. Only unprocessed staged ingredients are refundable; completed output spills on cancellation/breaking.
- Migrated alpha 6 partially bottled batches without double-charging water. Kept nonfluid extraction recipe behavior reusable for other mods.
- Added Bubble Pipe mouth tags and Curios short/full hold/release use, with cancellation on screen opening or slot changes. It uses the normal pipe position and applies no smoking effects.
- Replaced the cigalike's custom Curios transform with the existing cigarette/cigar transform.
- Updated network version to 6; install matching alpha 7 client/server versions.

Apply this follow-up after alpha 6. Compilation, resource/model checks, and dedicated-server integration checks pass with and without Create, including native dye crafting/washing, data preservation, ready cauldron transitions, partial/save/drain/refund/migration behavior, and Bubble Pipe mouth controls. Visual client review remains an in-game check.

# 4.2.0 alpha 6 — Bubble toy, sized bottles, and mouth-slot cigalike

- Added the supplied Bubble Pipe as a water/mundane-potion toy with floating soap bubbles, a refill counter, and the Pipe Dream advancement. It uses no tobacco and applies no smoking effects.
- Installed the supplied table item sprite while preserving the placed bench model, texture, and collision.
- Added 125 mB Small Glass Bottles and 1000 mB Large Glass Bottles using vanilla bottle pixels. They support partial pouring, vanilla source water, ingredient loading, device refills, Create Spouts and Item Drains, and matching container returns.
- Scaled bench output and processing time to bottle capacity while preserving the saved formula and whole essence dosing. Fixed stacked ingredient handling and pauses when the returned glass cannot fit.
- Extended cauldrons to accept measured bottled-fluid ingredients and collect output in portions. Large inputs return unused fluid; saving, cancelling, and breaking distinguish staged ingredients from already collected output to prevent refunds or fluid duplication.
- Added finished vape liquids to Tobacco Boxes with size-based capacity, names, exact formulas, and partial volumes preserved through storage and saves.
- Anchored handheld vape tips at the lips and gave the cigalike a matching mouth-slot pose. Refill/Charge labels now use dark text without a shadow.
- Reduced the cigalike to fixed 8 W and 40% device output, with a 40-unit cell and 3 mB/2-unit full draws. Added Curios mouth hold/release use, including cancellation when opening screens or changing items.
- Updated the network version to 5. Install matching alpha 6 client/server JARs.

Apply this follow-up after vapor alpha 5. Dedicated-server checks pass with and without Create, including actual Create filling/draining and Curios draws. Compilation, resource checks, liquid invariants, and cloud scaling pass. Client poses, button contrast at different GUI scales, and multiplayer bubbles/vapor still need an in-game review.

# 4.2.0 alpha 5 — More vaporizers and supplied sprites

- Installed the supplied Vape Pen sprite unchanged and added Box Mod, Cigalike, and Brass Smoking Pipe using the other supplied 16x16 textures.
- Kept the pen at 250 mB. Cigalike holds 100 mB and produces smaller clouds. Box Mod holds 500 mB and offers 10/20/30/40 W settings; higher power increases vapor output and liquid/charge consumption.
- Added a persistent lifetime puff counter, displayed with wattage in the box menu. Cancelled, unaffordable, and duplicate completion actions do not count. The counter survives refills and charging.
- Shared refill, charging, formulation matching, and the cigarette/cigar draw pose across all vapor devices. Registered standalone recipes for the new vapes.
- Fixed active-device locking in both the dedicated device slot and its hotbar view. The old slot override compared the menu index rather than the player's inventory index.
- Added the brass pipe to standard packing, tobacco pouch use, smoking animations, Curios mouth tags, and JEI filling. Its recipe uses two brass ingots and one spruce plank; it is available when the common brass tag is populated.
- Replaced the generated card with a native recolor of Create's schematic pixels, retaining its folded-paper outline, with sage coloring, Minecraft paper highlights, and small recipe-list marks.
- Reduced the bench's first/third-person held scale in both hands and used normal block holding transforms. Placed geometry, atlas, collision, and GUI item size retain alpha 4 behavior.
- Added output strength to the vapor packet and updated the network version. Install matching client/server versions.

Apply this follow-up after vapor alpha 4. Compile/assembly, resource checks, and dedicated-server device/processing checks pass with and without Create. Cloud output and liquid invariants pass. Client poses, held bench size, mouse controls at different GUI scales, and multiplayer visuals remain in-game checks.

# 4.2.0 alpha 4 — Vape draw pose and supplied bench artwork

- Put the Vape Pen through the same first-person draw transform and third-person arm pose as cigarettes and cigars. Removed vanilla drinking/eating motion and its use sounds.
- Delayed vapor by the same six ticks used for smoking, so the hand lowers before exhalation. Liquid use, charge, nicotine strength, and cloud scaling retain their existing behavior.
- Installed the supplied blending/bottling bench model and embedded texture, retaining all twelve cubes, original UVs, and item display transforms. Added matching rotated collision/selection shapes and correct front-facing placement.
- Fixed the VG/PG and nicotine hover frames by giving them complete normal/hover sprites. Preserved both arrow caps while resizing the center.
- Replaced the Liquid Recipe Card with a transparent 16x16 pale teal folded-paper sprite, using vanilla paper and Create's schematic as style references. Kept the editable bench source and card-generation prompt in `art_source`.

Compilation and dedicated-server checks pass with and without Create, including rotated bench shapes and the removal of vanilla vape use animation. The exported model, texture, UVs, and item transforms match the supplied file; selector frames were inspected at both widths. Final client poses, hovering at different GUI scales, and multiplayer rendering remain in-game checks.

Apply this follow-up after vapor alpha 3.

# 4.2.0 alpha 3 — UI polish, smoking animations, and vapor advancements

- Fixed panel edges, reduced repeated labels, and aligned the pen panel with its inventory.
- Grouped essences with flavor names and standard item-style dose counts.
- Separated selector arrows from action buttons, added distinct Save/Blend icons, and hid unused clear controls.
- Added consistent arrow, right-click, and scroll controls for ratios and nicotine.
- Disabled unavailable pen actions; preserved partial refills and formulation matching.
- Merged the supplied smoking animation implementation, including all v2 follow-up changes.
- Retained short puffs, corrected pipe/hose poses, default-on config, Curios mouth controls, and delayed smoke scaled by draw duration. Removed ember/redstone particles.
- Added seven vapor advancements for carrier bases, usable liquid, saved recipes, first exhale, multiple flavors, nicotine-free flavor, and a full high-VG draw.
- Preserved vapor and smoking network/particle registrations together.

Compilation and dedicated-server integration checks pass with and without Create. Advancement triggers, cancelled/invalid actions, partial refill/charge behavior, short draws, and disabled-animation behavior were exercised on the server. Client-side poses, mouse controls, and multiplayer rendering still need in-game review.

Apply this follow-up after vapor alpha 2. It already contains both supplied animation patches.

# 4.2.0 alpha 2 — Cauldrons, Create UI, and clouds

- Replaced new Extraction Station crafting with heated vanilla cauldrons, saved batches, cancellation, and bottle collection.
- Retained heated Create Mixer/Basin extraction and staged fluid blending.
- Restyled the bench and pen around the existing Production Monitor panels, with Create widget/icon references and a standalone fallback.
- Consume whole essence doses per flavor; removed concentrate recovery controls. Auto retains its formula when flavors run out.
- Made high-VG vapor larger, denser, longer-lived, and streamed across an exhale.
- Made refined nicotine nearly clear and fixed fluid name translations.
- Preserved legacy alpha 1 block/item registrations for existing test worlds.

Dedicated-server integration checks pass with and without Create, including all five cauldron recipes and a four-stage Create blend. Client-side appearance and multiplayer visual review remain in-game checks.

# 4.2.0 alpha 1 — Vaporizer Update

- Added Vegetable Base (VG) and Clear Base (PG), retaining technical names in tooltips.
- Added a recipe-driven Extraction Station with raw/cured tobacco extraction and nicotine refinement.
- Added a Liquid Blending Bench with adjustable VG/PG, four nicotine strengths, up to three essence flavors, and reusable Recipe Cards.
- Added measured ingredient reservoirs, partial concentrate recovery, bottle returns, hopper/pipe support, and portable reservoir contents.
- Added one refillable Vape Pen with partial refills, redstone charging, short/long draws, and nicotine-free use.
- Added expanding vapor clouds influenced by VG and draw duration, with particle limits and Create fan ventilation.
- Added heated Create ingredient production, card-controlled staged fluid mixing, and metadata-preserving bottling.
- Added initial models/textures, JEI extraction recipes, English text, and an in-game manual entry.

Compilation, assembly, liquid invariants, and dedicated-server processing checks pass with and without Create. In-game UI, rendering, and multiplayer visual review remain pending.
