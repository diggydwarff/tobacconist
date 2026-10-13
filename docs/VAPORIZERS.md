# Vaporizers and liquid blending — 4.2 alpha 16

This patch adds three refillable vaporizers, two carrier bases, nicotine extraction, custom liquids, a standalone blending bench, and optional Create production.

## Ingredients and extraction

Vegetable Base is vegetable glycerin (VG). Clear Base is propylene glycol (PG). The technical names appear in their tooltips. These recipes are Minecraft abstractions.

Fill a vanilla cauldron with water and place a lit campfire directly below it. Right-click ingredients in recipe order. Heating takes 200 ticks and pauses without heat. When finished, the same cauldron visibly holds the resulting extract, with its actual fluid color. The entire batch boils down to 250 mB (one normal bottle), displayed at the lowest cauldron level. Right-click with glass bottles to collect it; small bottles collect 150 mB first, then the remaining 100 mB. Draining or discarding the finished batch leaves an empty vanilla cauldron, regardless of its initial water level. Staged batches and finished liquid survive world reloads. Before heating completes, sneak-use with an empty hand cancels and refunds staged ingredients. After completion, it spills finished liquid and returns only unused bottled ingredients/empty input glass; spent ingredients are not refunded. Breaking follows the same distinction and drops an ordinary cauldron. JEI shows the recipes. There is no new extraction block to craft or extraction screen to open.

Finished extract also exposes a drain-only fluid capability for Create pipes. Filling it with other fluids, rain, dripstone, or buckets cannot overwrite or multiply its contents. Returned input containers are delivered on the first collection, or dropped beside the cauldron on its first pipe drain/break. Older partially collected alpha 6 batches migrate without charging water again. Nonfluid extraction recipes retain their existing collection behavior.

| First ingredient | Second ingredient | Cauldron | Bottled output |
| --- | --- | --- | --- |
| 2 Sugar Cane | 2 Wheat Seeds | Water | 250 mB Vegetable Base |
| 4 Raw Tobacco Leaves | Paper | Water | 250 mB Crude Tobacco Extract |
| 3 Cured Tobacco Leaves | Paper | Water | 250 mB Crude Tobacco Extract |

Bottled refinement uses a normal **Brewing Stand**, with Blaze Powder fuel:

| Bottle input | Top ingredient | Bottle output |
| --- | --- | --- |
| Crude Tobacco Extract | Paper | Nicotine Extract |
| Aqua Vitae | Sugar Cane | Clear Base |

One 400-tick brew handles up to three bottles and consumes one top ingredient.
Normal, small and large bottles work, including partially filled bottles. Each
keeps its existing liquid amount, glass size and custom label. No additional water
or collection bottle is needed. Aqua Vitae + Sugar still makes Sugar Essence.
Create's existing heated mixer recipes retain their ingredients and 250 mB yields.
The old clear-base/nicotine cauldron recipes remain available for compatibility
with existing processing/data packs; JEI now directs new production to brewing.

Cured leaves have a lower ingredient cost. Quality-dependent extraction bonuses, fermentation, and aging are not required in this first patch.

Cauldron extraction is recipe-driven: add recipes of type `tobacconistmod:extraction` from any data-pack namespace. Each recipe declares one to three ordered `inputs` with an `ingredient` and `count`, a `result` ItemStack, and a `duration`. Water-potion ingredients use cauldron water instead. Bottled-fluid inputs are measured by their recipe volume, so two 150 mB bottles can supply a 250 mB ingredient; their remaining 50 mB is returned. A large ingredient bottle is reserved during processing; collecting returns its unused liquid in the same sized glass. Normal Aqua Vitae/essence/molasses bottles pour whole 250 mB doses. They cannot supply a fractional remainder after a sized input; use another sized bottle, or decant a normal bottle into large glass first. Unsupported partial attempts spend nothing. Old unfinished batches with an unrepresentable partial debit refund their inputs. Fluid outputs accept small, normal, large, or compatible partially filled bottles. Nonfluid recipe outputs retain the normal glass-bottle collection rule. The extraction process contains no tobacco-specific selection logic. Herbalist can supply additional recipes when both mods are present; its standalone implementation can reuse this recipe and processing design without adopting the vapor features.

## Blending bench

The bench has six 2000 mB reservoirs: Vegetable Base, Clear Base, Nicotine Extract, and three essence slots. Inserting an ingredient bottle transfers its contents into its reservoir and returns the empty glass in Glass. Create pipes can also fill the reservoirs. Shift-clicking an essence bottle skips full or incompatible flavor reservoirs and chooses the next input with room for its whole dose. If all three are unavailable, the bottle stays in your inventory.

1. Supply the bases needed for your selected ratio, optional nicotine, and up to three essences. Pure VG needs no Clear Base.
2. Add empty glass bottles in Bottles.
3. Left-click VG/PG for more VG, right-click for more PG, in 10% steps. Cycle nicotine through None, Low, Medium, and High. Left-click increases it; right-click decreases it. Both selectors also support scrolling.
4. Press Blend Bottle, or enable Auto to produce bottles while ingredients and output space are available. Small, normal, and large bottles take 40, 80, and 320 ticks respectively.
5. Put a blank Liquid Recipe Card in Recipe and press Save Recipe to retain the exact formula. Inserting a saved card pulls matching essence bottles from your main inventory and hotbar for one batch at the selected bottle size. Stored or queued essences count toward that batch. Existing incompatible flavors, missing ingredients, and blocked returned-glass space are left alone; spare inventory bottles stay with you. This happens once on insertion, including shift-click and hotbar swaps, rather than continuously while Auto runs.

A 250 mB batch uses 0/5/10/15 mB nicotine for the four strength choices, one full 250 mB essence dose per populated flavor slot, condensed into a hidden 10 mB contribution, and carrier bases for the remaining volume. The unused essence is spent as part of processing. No recovered Flavor Concentrate is produced. VG/PG is the ratio of those carrier bases, excluding additives. Flavor amounts stay in the data; tooltips list flavor names without proportions.

Select the batch size by inserting Small Glass Bottles (150 mB), normal Glass Bottles (250 mB), or Large Glass Bottles (1000 mB). Formulas remain identical across sizes. Reservoir costs scale with capacity and round up to whole mB. Essence costs round up to whole 250 mB doses: one per flavor for small/normal bottles and four per flavor for large bottles. Ingredient input stacks transfer one bottle at a time and pause if the matching returned glass cannot fit.

A saved card or Auto fixes the formula even when one flavor runs out, preventing automation from silently switching recipes. Changing VG/PG or nicotine clears the card's saved formula. Rename the card in an anvil to apply that name to produced bottles. Auto state is saved, and active processing safely restarts after unloading.

The trash button discards stored essences and clears the flavor recipe; it also stops Auto. Legacy partial concentrates still load, but a flavor batch needs a full 250 mB dose. Small legacy residues left after a full dose are discarded. Legacy Extraction Stations remain registered for alpha 1 worlds, but are removed from crafting and the creative tab.

Hoppers/Funnels insert inputs through non-bottom faces. Bottom extraction exposes finished output and returned containers. Breaking the bench drops its inventory and stores the remaining reservoirs on the dropped bench item, including partial amounts.

## Vaporizers

Craft the devices using copper, iron, glass, quartz, and redstone. Each has a built-in redstone cell and uses the same refill menu and draw controls.

| Device | Tank | Output |
| --- | --- | --- |
| Vape Pen | 100 mB | Fixed, matching the original pen. |
| Cigalike | 75 mB | Fixed 8 W, 40% device output, and a compact 40-unit cell. Curios mouth support. |
| Box Mod | 150 mB | Adjustable 10/20/30/40 W and a lifetime puff counter. |

The box menu has a single wattage selector. Arrow/left/right clicks or scrolling adjust it within the bounds. Higher power produces more/larger particles and consumes more liquid and charge. A full draw at 10 W uses 5 mB and 3 charge units; at 40 W it uses 9 mB and 5 units. The pen retains 6 mB/3 units for a full draw; the cigalike uses 3 mB/2 units. The pen and box cells hold 100 units; the cigalike holds 40. Puff counts survive refills, charging, and item/world saves. Only successful draws count.

- Sneak-use opens the refill and charging menu.
- Load finished Vape Liquid and press Refill. Unused liquid remains in the bottle; empty bottles are returned.
- One redstone dust restores 20 charge units: 20% on the pen/box, 50% on the cigalike, capped at a full cell.
- Hold use for a draw of up to 40 ticks. Shorter draws are possible; draws below 6 ticks do nothing.
- The devices use a raised-hand draw with the texture's tip anchored at the lips, smooth lowering, and delayed exhalation instead of vanilla drinking motion. Refill and Charge buttons use dark, shadow-free text.
- Longer draws consume more liquid and charge. VG increases particle count, size, and lifetime.
- A partially filled pen accepts the same formulation. Empty it through use before changing liquids.
- Nicotine-free liquids emit vapor normally. Nicotine concentration and consumed volume determine the capped Nicotine effect duration. The existing server nicotine toggle applies; leaf-quality regeneration is not inherited.

Vapor is soft, pale, and expands while fading. Create fans can move or remove it. Emission is capped per client tick, respects vanilla particle settings, and is sent only to tracking players.

Equip a loaded, charged cigalike in the optional Curios mouth slot and use the existing mouth-smoking hold/release key. The same minimum six ticks, liquid costs, charge costs, cooldown, and counter apply. Opening a screen or changing the equipped item cancels an unfinished draw. The pen and box mod remain handheld devices.

All three vaporizers can be dyed through the native crafting recipe: combine one device with one or more dyes. Each craft replaces the previous color. Multiple dyes in the same craft blend normally; the old color is excluded. Only the casing changes; mouthpieces, glass, screens, and indicators retain their supplied artwork. Undyed devices retain their original sprites. Dyeing preserves the liquid formula, remaining volume, cell charge, wattage, name, and puff counter. Use a dyed device on an ordinary water cauldron to remove the dye, spending one water level and preserving all device data.

Pure Vegetable Base bottles can refill any empty vape directly, including small and large bottles filled with VG. The tank becomes 100% VG, with no nicotine or flavors, and uses the largest VG cloud profile for its device/power/draw. Refills transfer only the amount needed and retain leftover base in the original bottle. A partly filled tank still requires the same formula; use its remaining blend before switching to pure VG. Raw Clear Base, nicotine extract and essences remain bench ingredients.

## Glass bottles and labels

Two small empty bottles craft from one normal glass bottle; four normal empty bottles craft into one large bottle. Reverse conversions preserve the amount of empty glass. Filled bottles cannot be converted through crafting. Small and large sprites reuse the vanilla glass-bottle pixels at different sizes, with tinted liquid inside.

Small bottles hold 150 mB: two empty Cigalike tanks, one and a half pen tanks, or one Box Mod tank. Normal bottles hold 250 mB and large bottles hold 1000 mB. Refills transfer only the amount needed, retaining unused liquid in the same bottle. Existing partial bottles retain their actual contents; no liquid is added on upgrade. Existing devices filled above the new limit retain all stored liquid and drain normally, but cannot refill until below capacity. Their gauge stays bounded at full, and their tooltip explains the legacy fill.

Sized glass accepts vapor ingredients, finished vape liquid, existing essences, Aqua Vitae, molasses, and water through fluid capabilities. Use a bottle with the source bottle in the other hand to pour between compatible containers. Source water fills empty small/large bottles. Create Spouts and Item Drains use the actual capacity or remaining volume and retain formulas.

Tobacco Boxes accept finished vape liquid in all three bottle sizes. They hold up to 16 small, 8 normal, or 4 large bottles. A box groups only matching bottle sizes, formulas, and remaining volumes; incompatible batches are rejected. Rename the box using its existing label flow to name extracted bottles. A named incoming vape bottle can supply the initial label. Formula and partial volume survive boxing and world saves.

## Bubble Pipe

The Bubble Pipe is a reusable toy crafted from a stick, copper ingot, glass bottle, and magenta dye. Sneak-use with a Mundane Potion in the other hand refills it to 24 blows and returns the glass. Water bottles and sized water bottles cannot refill it. Hold use for at least four ticks, up to 30, then release to blow drifting soap bubbles. Each successful blow spends one charge and awards the optional Pipe Dream advancement. It needs no tobacco and applies no smoking or nicotine effects. It also fits the Curios mouth slot, using an upright bowl and forward-facing stem anchored to its supplied mouthpiece, plus the existing mouth hold/release key. Screen opening, item changes, and under-four-tick draws cancel without spending a blow. Refill it while held before re-equipping.

## Create factory production

Heated Mixers produce the two bases, crude extract, and refined nicotine using equivalent ingredient costs and 250 mB batches. These ingredients and finished liquids support Item Drains and Spouts through fluid capabilities.

For custom mixing, place exactly one saved Liquid Recipe Card in each Basin input inventory. Leave its output filter empty, or use a suitable fluid filter. The card remains reusable.

1. Combine Vegetable Base and Clear Base in the first Basin.
2. Combine the carrier result with Nicotine Extract, if the card calls for nicotine.
3. Introduce one essence in each following Basin, or combine essences separately before joining the carrier stream.
4. Pipe the finished mixture into a tank and bottle it with a Spout.

Each Basin stage uses at most two fluid types. Carrier and nicotine stages consume the exact contribution for one final 250 mB batch; each flavor stage consumes a whole 250 mB essence dose and outputs the resulting intermediate volume. For example, a batch with 5 mB nicotine and two flavor contributions of 10 mB has a 225 mB carrier stage, a 230 mB nicotine stage, a 240 mB first-flavor stage, and a 250 mB finished stage. Intermediates carry their composition through pipes and tanks. A 100% single-carrier, unflavored recipe can convert that base directly into usable liquid.

Different fluid formulations are separate FluidStacks; pipes and tanks never silently average them. Intentional staged mixing validates the card and all current inputs, simulates output capacity, and rolls back input extraction if output acceptance fails.

## Vapor advancements

These optional milestones join the existing tobacco advancement tree. They do not unlock mechanics. Both vanilla and Create products qualify.

| Advancement | Requirement |
| --- | --- |
| A Clear Start | Obtain both carrier bases, together or separately. |
| Small Batch | Carry a nonempty bottle of usable vape liquid. Legacy essence concentrates do not count. |
| Recipe for Success | Save a valid formula onto a Recipe Card. |
| First Exhale | Successfully draw from a loaded, charged pen. |
| Flavor Composer | Draw from a liquid with two or more different essences. |
| Just the Flavor | Draw from flavored liquid with zero nicotine. |
| Cloud Chaser | Complete a full 40-tick draw with at least 80% VG. |
| Pipe Dream | Successfully blow bubbles from a refilled Bubble Pipe. |

## Merged smoking animations

The supplied smoking patch and its v2 follow-up are included. Cigarettes, cigars, pipes, and hookah hoses support short or full draws; longer draws produce fuller delayed smoke. Handheld vaporizers retain the six-tick exhale delay and use tip-anchored draw poses. The cigalike now uses exactly the same Curios mouth translation, rotation, and scale as cigarettes/cigars. The Bubble Pipe uses the normal pipe position. Pipe and hose hand poses point the mouthpiece toward the player. No ember/redstone particles are added. Curios mouth use cancels when a screen opens. Set server `enableSmokingDrawAnimations` to false for the original instant smoking behavior of traditional smoking items; it defaults to true. Vaporizers retain their hold-to-draw mechanics with the pose and delay disabled.

## Bench and card artwork

The placed Liquid Blending Bench uses the supplied `blending_and_bottling_bench.bbmodel` and its embedded texture, with matching outline and collision boxes in all four directions. The original editable file is retained in `art_source`. Its item now uses the separately supplied 16x16 table sprite and normal generated-item transforms. The Recipe Card is a sage recolor of the actual Create schematic pixels, with Minecraft paper highlights and short list marks; its outline, folds, alpha and 16x16 grid are preserved. Both blend selectors use complete normal and hover frames so highlighting does not split the border. The supplied device, brass-pipe, bubble-pipe, and table-item textures are copied unchanged.

## Brass Smoking Pipe

Craft the brass pipe with two ingots from `c:ingots/brass` and one Spruce Plank. The recipe appears when a brass provider such as Create supplies the tag. It uses the existing reusable pipe behavior: loose tobacco and pouch packing, tobacco metadata and puff counts, short/full draw animations, and optional Curios mouth smoking. It is a traditional tobacco pipe.

## Implementation

The novelty-pipe expansion adds the multicolored Confetti toy plus loot-only
Wizard's Longpipe, Soulglass Pipe, and Astral Pipe. See [NOVELTY_PIPES.md](NOVELTY_PIPES.md)
for refills, effects, loot chances, milestones, configuration, and placeholder
texture replacement. The optional display rack and other proposed pipe types
remain future additions.

- `vapor/LiquidBlend`: immutable composition and volume-weighted blend math.
- `vapor/VaporContent`: common registrations, typed components, fluids, capabilities, menus, and extraction recipe type.
- `vapor/ExtractionRecipe`: reusable data-pack recipe schema.
- `vapor/CauldronExtraction`: reusable saved cauldron batches.
- `vapor/ExtractCauldronBlock` and `ExtractCauldronEntity`: the vanilla cauldron's finished-extract state, synchronized liquid levels, collection, and pipe drainage.
- `vapor/BottledFluid`, `SizedBottleItem`, and `SizedBottleHandler`: immutable sized bottle contents, partial transfers, and fluid capabilities.
- `vapor/BubblePipeItem` and `vapor/client/SoapBubbleParticle`: refilled toy and air-safe floating bubbles.
- `vapor/LiquidStationEntity`: legacy extraction and reservoir processing with whole essence dosing.
- `compat/create/CreateVaporMixing`: optional, card-controlled Basin mixing.
- `vapor/client`: the two screens, bottle colors, and expanding vapor particles.

All new identifiers are added without renaming existing items or changing old tobacco processing recipes. New English strings fall back normally for untranslated languages.

## Validation and remaining work

Java compilation and JAR assembly pass with NeoForge 21.1.235 and Create 6.0.10. A standalone verification program checks nicotine/flavor dilution, immutable data, flavor limits, invalid compositions, and 5000 randomized volume-conserving mixtures. Dedicated-server loading and recipe parsing are checked with Create and without it. A temporary server-side integration probe also verifies actual extraction, exact 70/30 low-nicotine Berry + Vanilla dosing, partial drains and returned bottles, simulated versus real fluid transfers, Spout-compatible bottle filling, item component persistence, portable bench reservoir data, whole essence dosing, cauldron heating/cancellation/persistence, and a complete four-stage Create blend that matches the bench output. The probe is excluded from the delivered mod.

Before treating this as a release, test the two screens, partial refill/charging, hopper and Spout behavior, a complete multi-stage Create factory, bench relocation, and multiplayer vapor appearance in-game. The bench and pen panels follow the Production Monitor style. With Create installed, controls reference its actual widget/icon textures directly; standalone mode uses the existing local monitor widgets. High-VG exhales stream larger, denser billows over several client ticks. Visual multiplayer review remains a player-side check. Additional devices, coils, charging docks, and deeper extraction-quality behavior remain later additions.


Alpha 10 adds consistent cosmetic idle mouth effects for all loaded supported
items. Normal smoke accompanies the Bubble and relic signatures; Confetti emits
only mixed-color paper, and Cigalike emits vapor. Idle effects spend no resources.
Wizard rings are made from actual native campfire smoke, and Astral has custom
item/animated particle artwork. Firefly has been removed. See NOVELTY_PIPES.md.


## Alpha 11 mouth controls and bottle appearance

With Curios, tap the Smoke Mouth Item key for a small puff from any loaded specialty pipe or Cigalike. Hold to maximum for the full flourish: 30 ticks for pipes, 40 for Cigalike. Zero-time release, cancellation, item removal and invalid use spend nothing. The Cigalike mouthpiece is anchored at the normal cigarette lip position, with a more visible sprite angle. Its passive vapor is slightly larger and longer lived. The server supplies each equipped item's passive-effect state; the client respects visibility, distance, pauses and particle settings without consuming resources.

Bubble Pipe emits bubbles only, in hand and in the mouth. Confetti emits paper only. Wizard, Soulglass and Astral retain ordinary tobacco smoke alongside their special effects. Astral produces constellations only on maximum puffs.

Small and large filled bottles now reuse the normal Minecraft potion's liquid shading and glass highlights, scaled together. The small bottle looks full and the same blend receives the same colors at all three sizes. Empty bottle sprites and capacities stay the same.


## Alpha 12 follow-up

Mouth items now use explicit Curios item capabilities and its native renderer registry, replacing the old custom player layer. Models and passive effects share cosmetic/functional visibility rules. The supplied Bubble/Wizard/Soulglass/Galaxy item sprites are installed unchanged; Galaxy is Astral’s texture. Soulglass’s green soul sprite is replaced by softer native glow wisps with silver-blue/lavender colors and neutral spark frames.

Petal and Whistling are refillable toys (small flower / Water Bottle), while Sunkeeper’s and Echo are normal-tobacco relics from Desert Pyramid and Ancient City chests. See NOVELTY_PIPES.md for effects and refills. New item textures are placeholders. Matching alpha 12 client/server JARs use network version 11.


## Alpha 13 follow-up

The direct Tobacconist player mouth layer is restored and covers the entire collection, including ordinary cigarettes/cigars/pipes. Models and idle particles use the same visible equipment, with a one-time upgrade reset for saved hidden mouth flags. Later visibility choices are respected. Native Curios renderer factories are no longer used.

Bubble's original sprite is restored; its wrongly assigned magenta/brass sprite belongs to Confetti. Supplied artwork replaces all four new pipe placeholders, normalizing Petal to mip-safe 16x16 while retaining its original artwork in the source archive. Soulglass adds back a few soul flames; Sunkeeper adds tiny gold sparks; Echo gains layered blue-violet waves and light motes; Wizard's normal plume is smaller and its smoke rings more uneven. Full Whistling draws play a louder five-note phrase.

Matching alpha 13 clients/servers use network version 12. The current NOVELTY_PIPES guide describes final behavior; earlier follow-up sections record the development history.

## Alpha 14 — Occasional mouth puffs, wooden-pipe angles and smaller tanks

- Novelty mouth effects emit short, staggered cosmetic puffs every 6–8 seconds, with quiet gaps. Smoke and signature effects share the puff timing; Wizard emits one mini ring. Deliberate draws, visibility and resource rules remain unchanged.
- Specialty sprites match the wooden pipe’s final rendered angle while preserving their mouthpiece anchors. Existing cigarette/cigar/Cigalike poses remain.
- Cigalike/pen/Box Mod tanks hold 75/100/150 mB. Small bottles hold 150 mB, enough for two Cigalikes. Normal/large bottles remain 250/1000 mB; processing recipes retain their existing yields.
- Partial bottles retain actual contents. Overfilled legacy devices retain every stored unit and drain normally, refusing refills until below capacity; gauge/bar bounds and a tooltip handle this transition.
- Updated creative/JEI sample fills, glass returns, guides and test checklist. Matching alpha 14 clients/servers use network version 13.

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
