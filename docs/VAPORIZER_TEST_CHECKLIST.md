## Alpha 16 focused checks

1. Open the creative tab: growing/processing/storage first; cigarettes, traditional and novelty pipes together; hookahs together; vape supplies before Cigalike/Pen/Box Mod; paintings last. With Create absent, industrial rack/monitor/brass name tag stay hidden.
2. Fill essence reservoir 1, then shift-click an essence bottle: it goes to 2. Fill 2 as well: it goes to 3. Check a different flavor and a reservoir with under one full dose of free space; both are skipped. All full leaves the bottle in your inventory. Occupied incompatible item slots are skipped too.
3. Refill each empty vape with Vegetable Base: tank is 100% VG and bottle retains its unused base. Repeat with sized VG bottles. Finished glass has the original size. Full/partly filled incompatible tanks refuse the action. Full draws from pure VG make the largest clouds for that device; 40 W Box Mod gives the largest output.
4. Dye a loaded vape red, then blue: it equals a fresh vape dyed blue. Repeat with multiple dyes, then white. Check liquid/charge/name/power/puffs survive. Wash in a water cauldron. Tobacco pouches still replace color; leather still blends old color.

# Alpha 15 audit checks

- Replace the JAR on both sides, or apply the Alpha 15 source patch after Alpha 14.
- Save a 90 VG / High nicotine card and insert it into a different bench. Increase VG once: expect 100 / High. Change nicotine instead: expect 90 / None. Reverse actions should follow the shown values.
- With 105 mB Vegetable Base and 45 mB Clear Base, a nicotine-free 70/30 small bottle should enable Blend and produce 150 mB. The same supplies should not enable a normal or large bottle. A large bottle needs its full scaled costs.
- Run Auto with card A, replace it with card B, then remove B after a processing tick. Auto should keep B's formula, including after saving/reloading and while ingredients are missing.
- Break and immediately replace an open blending bench. The old menu should close; it must not control the replacement. Spectator/dead players cannot use processing, refill or charge buttons.
- Start a held vape/Bubble/toy draw, then open another menu or change to spectator. It should cancel and spend nothing. Recheck death, zero-time release, full draws and release after completion. Recheck all mouth items with draw animations enabled and the instant-use fallback disabled/enabled.
- Pour named liquid from a large bottle into empty small/normal glass. Names, product labels, formula and exact remaining volume should survive. A partially filled recipient should retain its own name when topped up.
- Pour a full large water bottle into normal glass: expect a Water Bottle and 750 mB remaining. Small water portions cannot create partial vanilla Water Bottles.
- Recheck held pen/mod/Cigalike/Bubble/Confetti/Wizard poses, mouth-key full draws, charge/refill buttons and offhand Box Mod wattage/refill. Old filled devices retain their liquid and bounded bars.
- Stage 150 mB Aqua Vitae in a cauldron, then try a normal 250 mB Aqua Vitae bottle for the remaining 100. It must refuse without consuming it. Use another sized bottle: expect an exact 50 mB remainder after collecting Clear Base. Older unfinished batches with an impossible partial debit should refund their inputs.
- Recheck particle cleanup on death, teleporting beyond the effect range, leaving the world and removing tracked players. Completed particles can fade naturally; queued new emissions should stop.

---

# Alpha 14 focused checks

- Replace the JAR on client and server, or apply the alpha 14 patch after alpha 13.
- Equip every novelty pipe in Curios mouth and watch for 20 seconds. Effects should arrive in short little puffs every 6–8 seconds, with visible quiet gaps. Bubble remains bubbles only; idle Whistling remains silent. Contents must not decrease.
- Compare specialty pipes with the wooden pipe from the front and side, crouching and looking up/down. They should share its angle against the face, with each mouthpiece at the lips. Cigalike and ordinary cigarette/cigar poses retain their previous alignment.
- Recheck tap/maximum mouth draws, empty/hidden/cosmetic slots, removing items and Minimal particles. Deliberate draws retain full effects and normal costs.
- Fill one Small Glass Bottle to 150 mB. Refill two empty 75 mB Cigalikes: the first leaves 75 mB in the bottle, the second returns Small Glass Bottle. Refill a 100 mB pen and a 150 mB Box Mod; check exact leftovers and no formula mixing.
- Load old saves with 100/250/500 mB in Cigalike/pen/box. All stored liquid must survive serialization and drain normally. Refilling is blocked while above the new capacity; UI/inventory bars stay within their frames.
- Existing 125 mB small bottles should still contain 125 mB after upgrading. A refill uses their actual remaining volume. Pouring, bench filling and Create Spouts use the new 150 mB capacity for newly filled small bottles.
- Collect a 250 mB cauldron batch in small bottles: 150 mB then 100 mB, leaving an empty cauldron. Recheck partial bottle/cauldron saves, ingredient leftovers, drains and Create processing.

---

# Alpha 13 focused checks

- Replace the old JAR on client and server, or apply the alpha 13 patch after alpha 12. Use the matching version on both sides.
- In third person, equip a cigarette, cigar and ordinary pipe, then Cigalike and every specialty pipe. Check wide/slim skins, front/side views, crouching and looking up/down. The mouthpiece should meet the lips and each model should render once.
- Existing players get a one-time mouth visibility reset on upgrade. Hide/show the mouth slot afterward and relog: your subsequent choice should persist. Cosmetic mouth contents override the functional model; the key consumes functional contents.
- Loaded items should produce miniature idle effects. Empty Cigalike still renders without vapor; loaded Bubble shows bubbles ONLY. Remove items and check particles stop. Idle display must spend nothing.
- Tap and hold the Smoke Mouth Item key on every old/new item. Verify one puff/charge per completed draw and no second consumption when releasing after auto-completion.
- Bubble should have its original lavender sprite; Confetti should have the magenta bowl/brass rim previously assigned to Bubble. Inspect all four new supplied sprites and the Petal's outer mouthpiece.
- Soulglass retains silver-blue/lavender glints and curls, with a few familiar soul flames. Sunkeeper adds small bright gold flecks to its warm smoke sunburst.
- Echo should look like soft blue-violet waves with traveling glints and light motes, rather than plain hoops. Compare one short ripple with three staggered maximum ripples, plus a small idle effect.
- Wizard keeps two real smoke rings on full draws. Shape/thickness/breakup should vary more, and ordinary smoke should be smaller. Check both enabled and disabled draw animations.
- Whistling: turn up Players sounds, take a short puff for one clear whistle, then a full draw for a five-note phrase lasting under a second. Repeat to hear both phrases. Idle notes stay silent; disable novelty sounds to suppress the melody.
- Recheck Create fan steering/capture and Reduced/Minimal particle settings for the changed effects.

---

# Alpha 12 focused checks

- In Curios mouth, verify Cigalike, Bubble and every specialty pipe actually appears on both player skin models, whether empty or loaded. Inspect front/side, head pitch and crouching. The mouthpiece should meet the lips. Check dyed Cigalike and the four supplied textures.
- Refill Bubble with Mundane Potion, then equip it: visible pipe and floating bubbles only. Cigalike needs liquid and charge for vapor. Models should render even with empty contents; passive effects should stop when empty.
- Check the functional render toggle. A cosmetic mouth stack should replace the functional model and its passive effects, following Curios’ native convention. The mouth key consumes functional contents only. Remove/deplete items and verify effects stop; neither idle nor cosmetic display should spend resources.
- Soulglass: normal smoke remains; new soft silver-blue wisps and ice/lavender spark glints replace green soul particles.
- Petal: craft with Stick + Clay Ball + Small Flower; refill with a small flower from the other hand. Confirm 24 blows, drifting petals, short/full intensity, and no tobacco effects.
- Whistling: craft with Stick + Copper + Glass Bottle. Sneak-use with a Water Bottle to refill 24 whistles and return glass. Wrong potions must be rejected. Compare flute pitch on short/full draws; idle notes should be silent. Check the novelty-sound config.
- Sunkeeper: find in Desert Pyramid treasure, pack ordinary tobacco or use a pouch, then compare short/full warm smoke sunbursts. Normal smoke remains; Create fans steer/capture the actual smoke when installed.
- Echo: find in Ancient City loot, pack tobacco, and compare one short blue pulse versus three on maximum draws. Check quiet chime/config, normal smoke and a small idle pulse.
- Discover both new relics to earn Hidden Histories. Original collection achievements should keep existing progress. Relics are not craftable; new toys are. Recheck server rare/mythic chance and loot-disable settings.
- Inspect placeholder sprites for Petal, Whistling, Sunkeeper and Echo before making final 16x16 replacements. Keep the mouthpiece at the upper right and the bowl at lower left.

---

# Alpha 11 focused checks

- Equip each loaded item in Curios mouth: Cigalike, Bubble, Confetti, Wizard, Soulglass and Astral. Close inventory, tap Smoke Mouth Item for a small puff; hold to maximum (pipes: 1.5 seconds, Cigalike: 2 seconds). Check exactly one charge/puff is spent. Release after auto-completion and confirm no second consumption. Cancel or remove during a draw and confirm no consumption.
- Recheck Cigalike position on both player skin models, front/side views, crouching and head pitch; test dyed and undyed. The black tip should meet the lips and the LED end should extend forward. Check a visible tiny passive vapor plume and a normal active exhale.
- Wait up to one second after equipping. Loaded items should emit small effects; remove/deplete them and effects should stop. Check another player's effects, entering tracking range, relogging and dimension changes. Hide the Curios render toggle, pause or turn invisible: no passive particles. Passive emission must never spend resources or award achievements.
- Bubble: only floating bubbles in hand, on small/max mouth draws and while idle. No white/gray smoke. Mundane Potion refills; Water does not.
- Confetti: saturated rainbow paper, no smoke. Maximum puff should be much fuller than a short puff (96 pieces on All particles versus at most 33 below max). Paper refills; dye cannot force one color. Check Decreased/Minimal settings reduce count.
- Astral: every puff below maximum and every idle emission should be scattered sparkles only. Repeat maximum puffs to see different connected star shapes from 20 designs, with scattered sparkles around each constellation. Ordinary smoke should accompany active and idle relic effects.
- Soulglass: curling, uneven cyan wisps and tiny twinkling glass glints; maximum draw has three curls. Check fading, upward drift and smoke alongside them. No rigid repeating double helix.
- Wizard: actual smoke forms complete rings with visible holes. Shapes/thickness should vary gently, then break up irregularly; maximum still gives two rings eight ticks apart. Verify Create fans steer/capture effects when installed.
- Fill the same VG, PG and flavored blend into small/normal/large bottles. The small bottle should look full; liquid shading/highlights and colors should match across sizes. Empty bottle icons remain empty. Transfer amounts, labels, brewing and bench filling remain correct.

Automated checks cover real Curios draw/tap/cancel/removal/resource state, packet codecs, particle geometry/budgets, asset palettes and vanilla/Create server workflows. Final mouth placement and rendered particle appearance still require an in-game client review.

---

# In-game checks

- Craft the Liquid Blending Bench, Vape Pen, and Liquid Recipe Card; extraction uses a vanilla water cauldron above a lit campfire.
- Add ingredients in order, test heating with the campfire lit/unlit, and collect with a glass bottle. Confirm the whole batch boils down to 250 mB, displays one bottle portion, and leaves an empty cauldron after collection.
- Cancel an incomplete batch with sneak + empty hand. Test save/reload and breaking a loaded cauldron. Verify no loss or duplication.
- Extract Vegetable Base in the cauldron. Brew Clear Base with Aqua Vitae + Sugar Cane; Aqua Vitae + Sugar must still produce Sugar Essence.
- Extract crude tobacco from raw and cured leaves, then refine the bottled crude with Paper in a Brewing Stand. Test all bottle sizes and partial amounts.
- Blend nicotine-free 50/50 liquid and 70/30 low-nicotine Berry + Vanilla liquid.
- Check that tooltips list flavors without individual flavor ratios.
- Run the bench on Auto with hoppers feeding ingredients/empty bottles and removing output/returns from below.
- Save a card, let a required essence run out, and verify production pauses instead of changing flavor.
- Rename a recipe card and verify its custom name is applied to finished bottles.
- Verify each selected flavor consumes a full essence dose, leaves no Flavor Concentrate, and returns only empty ingredient glass.
- Check Auto pauses when an essence is exhausted, even without a recipe card.
- Use the trash button to change flavors; verify it discards essences and stops Auto.
- Check the Create-style bench and pen layouts at multiple GUI scales: labels, ghost essence icons, buttons, inventory slots, and tooltips. VG/PG uses left/right click.
- Break and replace a partially filled bench and verify its reservoirs survive without duplicating inventory or glass bottles.
- Refill the pen partially, top it up from a second matching bottle, and check returned containers and remaining volume.
- Charge the pen with redstone. Move/swap attempts must not remove the active device while its menu is open.
- Compare short and long draws and low-VG/high-VG clouds. Check particle settings and fan ventilation.
- Confirm zero-nicotine liquid gives no nicotine effect and the existing server effect toggle is respected.
- Build the Create extraction and card-controlled staged mixing line, including a 100% single-carrier recipe.
- Bottle finished liquid through a Spout and drain it again; compare the formula before and after transport.
- Restart/unload and inspect reservoirs, recipe cards, partial bottles, and charged devices.
- Repeat standalone gameplay with Create absent.

## Alpha 3 additions

- Check empty and filled UI states: full-width headers, clean footer edges, essence dose counts, shortened flavor names, and tooltips.
- Check both selectors: left arrow/right-click decrease, center/right arrow increase, and scrolling works.
- Verify Save and Blend have distinct icons. Clear appears only when relevant.
- Verify Refill disables with an empty input, a full tank, or an incompatible formula; Charge disables without redstone or at 100%.
- Obtain both bases separately and confirm A Clear Start remembers each.
- Check usable liquid awards Small Batch; empty bottles and pure flavor concentrates do not.
- Save a Recipe Card, take a first puff, use two flavors, and use flavored nicotine-free liquid.
- For Cloud Chaser, use at least 80% VG and hold until the full draw completes. Short draws should not award it.
- Test cigarettes, cigars, pipes, and all hose types: short release, full draw, empty contents, offhand, left-hand settings, and third-person poses.
- Check Curios hold/release smoking and cancellation when opening a screen.
- Disable enableSmokingDrawAnimations and verify instant smoking returns. Re-enable it afterward.
- Compare short/full smoke and confirm no ember/redstone particles appear.

## Alpha 4 additions

- Compare the Vape Pen with a cigarette/cigar: hold use, release early, and complete a full draw. Confirm the same raised-hand pose and smooth lowering, with no vanilla eating/drinking bob or sounds.
- Test the pen in both hands, with left-handed settings, and in third person. Check another player's pose and vapor in multiplayer.
- Confirm vapor begins after the hand lowers. Draws under six ticks should consume nothing; normal draws still use liquid and charge once.
- Hover and keyboard-focus VG/PG and nicotine at GUI scales 2, 3, and 4. Confirm each has a single unbroken highlighted frame and intact arrows; test left/right clicks and scroll changes.
- Place the supplied bench facing all four directions. The front bottle should face the player; compare its outline/collision with the bottles and copper apparatus, including the empty space above the tabletop corners.
- Inspect the bench as a placed block, inventory item, held item, dropped item, and item-frame display. Confirm the supplied textures appear without missing faces or purple squares.
- Inspect the pale teal Recipe Card in the inventory and the bench recipe slot. Compare its scale and paper shape with vanilla Paper/Create Schematic; save and rename it as before.
- Repeat the visual checks with Create absent and verify the selectors retain their normal/hover appearance.

## Alpha 5 additions

- Craft the Cigalike and Box Mod. Check all supplied device/pipe sprites in inventory, both hands, and third person.
- Fill the pen to 100 mB, cigalike to 75 mB, and box mod to 150 mB. Verify partial bottles retain the unused liquid and incompatible formulas cannot be combined.
- Open the box with sneak-use. Change power between 10/20/30/40 W using arrows, right-click, and scroll; verify the setting stops at the endpoints and survives closing/reopening and world reloads.
- Compare 10 W and 40 W with the same liquid and draw duration. Higher power should make fuller clouds and use more liquid/charge; high VG should remain the strongest cloud factor.
- Confirm the lifetime counter increments once per successful short/full puff. Draws below six ticks, empty/undercharged devices, and repeated completion during cooldown must not increment it. Refill and recharge without resetting it.
- Attempt to pick up, shift-click, throw, number-key swap, and double-click the active device from its dedicated slot and hotbar. Test opening from the offhand as well.
- Craft the Brass Smoking Pipe from two brass ingots and one spruce plank. Pack loose tobacco, pack from a pouch, take short/full draws, and try Curios mouth smoking. Verify JEI includes it among fillable pipes.
- With Create absent and no other brass provider, device recipes should still work and the brass recipe should be omitted cleanly.
- Compare the sage Recipe Card against Paper and Create Schematic: same small folded-paper treatment with a distinct shade and list marks. Save/rename/use it as before.
- Hold the bench in first/third person with right/left-handed settings and in the offhand. Verify its size is reduced while the placed model and inventory item remain correct.
- Install matching alpha 5 client and server JARs for the updated vapor packet.

## Alpha 6 additions

- Craft the Bubble Pipe, then sneak-use with a Mundane Potion in the other hand. Confirm 24 blows, one returned glass, and no potion effects. Empty/invalid refills should not drink the offhand potion.
- Try releases below four ticks, a short blow, and a full 30-tick blow. Only successful blows consume one charge and award Pipe Dream. Bubbles should float in air, fade/pop, and appear to nearby players without smoke or nicotine effects.
- Try small/large water bottles as a refill: they must be rejected without spending water.
- Inspect the supplied table sprite in inventory, both hands, dropped items, and item frames. The placed bench should retain its existing model, front direction, collision, and atlas.
- Craft empty bottles in both directions: one normal ↔ two small; four normal ↔ one large. Filled bottles must not participate in empty-glass conversion recipes.
- Fill Small/normal/Large bottles to 150/250/1000 mB. Pour with the source in the other hand, including stacked empty glasses, stacked ingredient bottles, compatible partial bottles, and mismatched formulas. Check no fluid or containers disappear or duplicate.
- Fill empty small/large glass from source water. Test Create Spouts and Item Drains with each size and with partially filled bottles; verify exact volume, formula, tint, and returned glass.
- Use all sizes at the bench. Small/normal/large Auto times should be 40/80/320 ticks, with matching formulas and appropriately scaled costs. Check whole essence doses and exhaustion, saved cards, hopper output, and blocked return slots with different glass sizes.
- Refill an empty 150 mB Box Mod from a 1000 mB bottle. The first refill leaves 850 mB; six full refills and a final 100 mB refill return the large empty glass. Verify incompatible formulas cannot refill a partially loaded device.
- Box finished liquids of all three sizes, including partial bottles. Capacities should be 16 small, 8 normal, and 4 large. Rename the box and extract bottles; inspect their label, formula, amount, and name after a world reload. Reject different sizes, formulas, or remaining amounts in an occupied box.
- Extract Clear Base using a large Aqua Vitae bottle; collect in two small bottles. Confirm the finished batch empties the cauldron, only 250 mB ingredient is spent, and the large bottle returns with 750 mB. Save/reload between collections and collect the remaining 100 mB even if the last water level was used.
- Cancel/break a cauldron before collection and confirm full staged refunds. Repeat after a partial collection: remaining output is discarded, spent ingredients are not refunded, and no extra empty glass appears.
- Draw from each vape in both hands, with left-handed settings, and in third person. The textured tip should meet the lips, with the body extending away from the face and no vanilla eating/drinking animation.
- Check Refill/Charge in active, disabled, hovered, and keyboard-focus states at GUI scales 2, 3, and 4, with and without Create. Labels should be dark and readable.
- Equip the Cigalike in Curios mouth. Confirm fixed 8 W, visibly smaller clouds than the pen, a 40-unit cell, and 50% charge gained per redstone. A full draw uses 3 mB and two units; short successful draws use less. Test empty/undercharged states and save/reload.
- Use mouth hold/release, release before six ticks, complete a full draw, open a screen mid-draw, and swap/remove the equipped cigalike. Cancelled draws should consume nothing; successful draws should increment the counter once. Pen/Box Mod should not equip in the mouth slot.
- Install matching alpha 6 client/server JARs; review nearby-player poses, delayed vapor, and floating bubbles in multiplayer.

## Alpha 7 additions

- Dye the pen, cigalike, and box mod with single and multiple dyes; also recolor an already dyed device. Only the casing should change. Check inventory, both hands, dropped items, item frames, and the mouth-slot cigalike. Undyed devices should retain the supplied sprites.
- Dye and wash a loaded/charged/named device with a nonzero puff counter and selected wattage. Confirm formula, volume, charge, wattage, name, and counter survive crafting, washing, saving/reloading, refilling, and charging. Washing uses one ordinary water-cauldron level.
- Run all five extraction recipes. Before collecting, heating should visibly turn the cauldron into the correct colored extract. Check the dark crude extract and pale refined nicotine. Yields remain 250 mB per batch; the finished surface should show one bottle portion.
- Collect with small/normal/large bottles and compatible partial bottles. Partial amounts remain exact at the lowest surface level; fully draining always leaves an empty cauldron. Confirm exact amounts after a world reload and chunk unload.
- Check cancellation/breaking before heating finishes (staged ingredient refund) and after it finishes (finished fluid spill, only empty/unused input containers returned). Repeat after partial collection. The filled block should drop the ordinary cauldron item.
- Try pouring water/buckets into finished extract and leaving it in rain or below dripstone. Its volume and identity must remain unchanged. With Create installed, drain it through pipes and check exact fluid/amount plus one-time returned input containers.
- Load an alpha 6 world with a partially collected cauldron and collect the remainder. Verify no second water charge and no ingredient/container duplication.
- Equip the Bubble Pipe in Curios mouth and use the existing mouth key for short/full blows. Verify the anchored mouthpiece and visible bowl, drifting bubbles, one charge per successful blow, no smoking effects, and Pipe Dream. Zero-time mouth releases, opening screens, or removing/swapping the toy should cancel cleanly; deliberate short taps now work.
- Compare the cigalike directly with a cigarette in the same Curios mouth slot, while turning and looking up/down. They should share translation, rotation, and scale; inspect another player's view too.
- Install matching alpha 7 on both sides and review dye colors, filled cauldron colors/levels, mouth positions, and bubbles in multiplayer.

## Alpha 8 additions

- Craft Confetti; it should start with 24 blows. Refill with Paper, test full/partial/empty refills, and verify that dyes are rejected without spending them. Confetti should always contain several colors.
- Compare short/full Confetti blows: paper falls/flutters in mixed colors, without smoke or tobacco effects. Each successful blow costs one charge; cancellations and duplicate completion must spend nothing.
- Equip Confetti and Bubble and all three relics in Curios mouth. Check hold/release, full completion, screen cancellation, slot removal, and both hand/third-person poses. Refill toys while held.
- Pack each relic with loose tobacco and a Tobacco Pouch. Verify exact tobacco metadata, normal puff counts, pouch bonuses, quality/flavor effects, short/full draws, cooldown, and persistence. Full Wizard draws should award Ringmaster; empty/cancelled draws should not.
- Inspect full Wizard rings, Soulglass wisps, and Astral constellation against short draws, including looking straight up/down. Check nearby players, distance culling, reduced/minimal particle settings, quiet sound toggle, and Create fan movement/capture.
- Generate dungeon/mineshaft, Nether fortress/bastion treasure, and End City chest loot. Only the appropriate relic should appear, at most one per chest; ordinary loot remains. Check configured zero/guaranteed probabilities and disabled relic loot. Already generated chest inventories should not change.
- Obtain one relic for Curio Collector, then acquire the three relics separately for Across the Realms. Check source/lore hints and JEI information; relics should have filling information and no crafting recipe.
- Replace the three remaining placeholder PNGs with final 16x16 sprites if desired. Astral uses its own artwork. Inspect inventory/held/mouth views and missing-texture errors.
- Install matching alpha 8 on both sides and repeat the multiplayer pose/effect checks.

## Alpha 9 fixes

- Check the Cigalike and Bubble Pipe from front and side in Curios. Their mouthpieces should sit at the lips, with the body extending forward and the Bubble Pipe bowl visible below. Test head movement and the Curios visibility toggle.
- Start raw/cured extraction from a full water cauldron. It should boil down to one 250 mB bottle, show only the lowest liquid level, and become an empty cauldron after collection. Test two small bottles and a Create pipe drain too.
- Place Crude Extract bottles in a Brewing Stand, Paper above, Blaze Powder as fuel. Test three different bottle sizes in one 20-second brew; amounts, sizes and names must be preserved.
- Brew Aqua Vitae + Sugar Cane into Clear Base. Check that Aqua Vitae + Sugar still gives Sugar Essence and that all Create mixer recipes work as before.
- Water bottles and sized water bottles must not refill Bubble Pipes or lose contents. Only a Mundane Potion refills to 24 blows and returns a Glass Bottle.
- Grab Confetti from the creative tab and use immediately. Test empty /give pipes: Paper refill, hints, ordinary empty refill, sneak top-up and held/mouth draws.
- Pack Astral with loose tobacco in the other hand and use again to draw. Test normal tobacco-in-main/offhand-pipe and Tobacco Pouch routes too.
- Wizard rings should stay continuous, hollow and coherent as they expand/fade. A full draw emits two rings eight ticks apart. Check front/side views, short draws, movement, multiplayer and Create fans.
- Check Soulglass/Astral signatures alongside normal smoke. Tobacco effects and puff costs should remain unchanged. Confetti should look like fluttering multicolored paper.
- Disable smoking draw animations: held Bubble/Confetti should use the same instant-click fallback as traditional pipes. Confirm one charge per click and cooldown protection.
- Check All/Decreased/Minimal particle settings. Full rings reduce to sixteen evenly spaced sections; idle rates and other signatures are reduced too.

Automated checks cover common gameplay and ring/pose geometry. A graphical
Minecraft client was not available for final visual review; inspect mouth poses
and particle appearance in game before considering them final.

## Alpha 10 focused checks

- Verify Firefly is absent from creative/JEI, crafting and mouth-slot tags.
- Equip a loaded Cigalike, Bubble, Confetti and each relic. Without pressing the mouth key, inspect their small idle plumes/mini effects. Empty or uncharged items must stop emission.
- Watch puff, liquid, battery and toy counters for a minute of idle emission: no changes, nicotine effects or advancements should occur. Remove the item or toggle Curios visibility and verify emission stops.
- Check nearby players, crouching/head pitch, invisibility and drawing while equipped. Idle effects should pause during deliberate use; full draws must still spend resources once.
- Compare Bubble's mouth pose from front and side with the previous hanging pose. The stem should point forward and the bowl should be upright.
- Full Wizard draws produce two rings of real tobacco-style smoke eight ticks apart. Check the hole, expansion and breakup; look straight up/down and test Create fans.
- Confetti refills with Paper only and always produces mixed colors. An old dyed pipe must still produce mixed colors; dyes must not be consumed. It must produce no smoke.
- All relics should produce normal smoke alongside their active signatures. Bubble emits bubbles only, uses Mundane Potion and applies no tobacco effects.
- Inspect Astral's new item sprite and four animated sparkle frames in daylight/night, nearby-player views, mouth slot and reduced particle settings.
