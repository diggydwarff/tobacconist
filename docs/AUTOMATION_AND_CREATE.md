# Automation and Create

Create integration is optional. Common Tobacconist code avoids a hard Create dependency, and ordinary hand-crafted processing remains available without Create.

## Harvesting

Mechanical Harvesters can harvest mature two-block tobacco crops without duplicating the upper/lower halves. Harvest quality and seed behavior use the normal crop logic.

## Drying Rack automation

The wooden rack is a one-block-tall, 16-leaf traditional curing block. Its frame and interaction volume are fully contained inside that block, so the space directly above it is available for normal building.

Vanilla sided inventory behavior:

- Horizontal faces can insert valid raw leaves.
- Top and bottom cannot insert; top-down loading remains intentionally disabled.
- Bottom extraction is available only after the cure is finished.
- Capacity remains 16.
- Input must match the stored leaf item/components.
- Insertion locks at 10% cure progress.

NeoForge/Create capability-based consumers use the same validated item handler; completed output remains protected until the batch is finished.

Create Encased Fans can assist Air Curing and an already-valid Sun Cure at 4× progress. Smoke/heat airflow can assist Fire or Flue Curing at 6×. The wooden rack only needs one valid airflow path reaching the rack.

Hanging Tobacco Bunches can receive environmental fan assistance but expose no item automation or Display Link source.

### Industrial Drying Rack

The **Industrial Drying Rack** is the dedicated factory tier. It holds **32 matching leaves**, rejects manual right-click loading/removal, and is intended to sit directly in Create logistics lines. Both block levels expose the same inventory to side automation, and the industrial rack also permits top-down Chute/Hopper loading. Display Links and Spectacles report the same cure/status information with the 32-leaf capacity.

The industrial rack cannot cure passively and contains **no internal fan or power input**. Both rack tiers must receive the **same valid airflow type from two distinct Encased Fans** before curing can advance. Plain airflow on both tiers provides Air or an otherwise-valid Sun Cure; matching fan-blown Campfire smoke/heat provides Fire; matching fan-blown Lava heat provides Flue. One fan reaching both levels, one-sided airflow, or mismatched airflow types do not run the machine. Its assisted progress remains 5 ticks for Air/Sun and 7 for Fire/Flue, with no quality bonus. The two industrial halves are maintained as one paired structure; placing racks directly beside one another does not cause either upper tier to tear down or recreate.

## Production Monitor

The **Production Monitor** is a directional throughput counter for factory lines. Place it directly beside a supported transport with the monitor’s front face pointing at the transport it should read; the opposite face remains outward toward the operator. It currently supports Create Belts, Funnels, and Chutes plus vanilla Hoppers. The monitor only observes confirmed movement; it never stores, extracts, or inserts items itself.

Its ghost Filter may be empty (count everything), a normal item, or a Create Filter/Attribute Filter. The same ghost filter can be set Create-style from the **operator-facing side of the block**: hover the center target to highlight its value-box area, right-click it with an item/filter to copy it without consuming the stack, or right-click it with an empty hand to clear it. The rendered ghost item now follows Create's centered value-box scale, depth, and lighting conventions. **Items** mode adds the number of items moved, **Stacks** mode measures the same confirmed item flow in Create-style 64-item stack-equivalents (including partial-stack progress), and **Transfers** mode adds one per successful transfer operation. The rolling rate covers approximately the last 60 seconds and continues even when the accumulated count is stopped at its target. Changing the filter or Count Mode clears both the accumulated count and rolling-rate samples; changing the numeric target does not. All saved monitor settings are sent in the menu-open snapshot, so reopening the screen immediately reflects the block's real persisted configuration.

At the target, **Keep Counting** continues upward, **Stop Counting** caps the accumulated count while rate telemetry continues, and **Reset Count** starts the next batch while preserving overflow from a large transfer. Redstone output can be None, one **Pulse** per target crossing, or **Hold** until reset. Reset Count + Hold is automatically coerced to Pulse. Manual Reset clears only count/latch/output state; when External Reset is enabled, a rising redstone edge performs that same reset without repeatedly firing while power stays on. Comparator output reports progress toward the configured target. If the block is not facing a supported transport, the GUI keeps the count visible and reports **No valid target**. A Create Display Link can attach directly to the monitor and expose **Count/Target**, the rolling **Rate**, or **Status** (Counting, Target reached, or No valid target). These three live Production Monitor sources passively refresh every **20 ticks (about 1 second)** so Nixie Tubes and other displays keep pace without updating every game tick. Tobacconist's Spectacles and Create Goggles show the same live filter/count/rate/status telemetry in-world; the block entity sends this inspection snapshot at a low rate rather than one packet per transfer.

## Bulk quality homogenization

A Mechanical Mixer over a Basin can homogenize otherwise-compatible raw or cured tobacco leaves that differ only in quality. Homogenization is physical and iterative: each Mixer cycle standardizes one Basin batch rather than scanning an external chest or logistics network. A cycle only runs when the selected batch contains at least two quality values.

With no incoming signal the target is 64 leaves. Signals 1–14 select 16, 32, 48, 64, 96, 128, 160, 192, 256, 320, 384, 448, 512, or 576 leaves. Control may power either the Basin or the Mechanical Mixer; if both receive redstone, the stronger signal wins. Signal 15 is a rising-edge Finish command for the compatible tobacco already in the Basin. It accepts any current lot of at least 2 leaves: mixed tobacco is homogenized once, while an already-uniform lot is passed through unchanged.

The selected batch is count-proportional across available qualities, uses a count-weighted average rounded to the nearest whole quality, and is snapshotted when processing starts. Inputs are protected from generic Basin extraction. Tobacconist also permits duplicate identical leaf stacks in Mixer Basin input slots so a long run of one quality does not prevent later qualities from entering. A completely full 576-leaf uniform Basin auto-passes unchanged to avoid deadlock.

Large farms can run smaller primary homogenizers in parallel and combine their standardized batches into one larger-target downstream homogenizer; `64/128 → 256/576` is a predictable pattern. Small farms can set a high continuous target, let an entire harvest collect in the Basin, then pulse signal 15 to homogenize that physical lot in one pass; the same Finish command also handles 2–15 leaf leftovers. See [Homogenization and Quality Grading](HOMOGENIZATION_AND_GRADING.md) for the complete rules and grading strategies.

## Cutting and pressing

- Deployer + Chaveta: Cured Leaf → Rough → Ribbon → Shag. Chavetas are reusable machine tools in Deployers and do **not** lose durability from automated cutting.
- Mechanical Press: Rough → Flake.
- Deployers/Presses also support the implemented cigarette/cigar assembly paths while preserving product data.

## Mixers, fluids, and flavoring

- Mixers can blend 2–3 compatible single-variety loose tobaccos.
- Heated Mixers support Aqua Vitae and Flavoring Essence production.
- Flavoring Essence and Molasses factory containers represent 1000 mB batches.
- Spouts apply 1000 mB Essence as aromatic casing.
- Mixers combine flavored Molasses with suitable loose tobacco to make Shisha.

## Attribute Filters and logistics

Create Attribute Filters understand tobacco variety, cure, cut, exact quality score, quality tier, 5-point Quality Band, flavors, blends, labels, aging, supported finished-product metadata, and Tobacco Box fill state. Boxes expose `Empty`, `Partially Filled`, and `Full`, allowing filtered Funnels to feed fresh boxes to a packing Depot and remove completed boxes automatically. Quality Bands give farm sorters a tolerant option such as 46–50 or 51–55 rather than requiring a lane for every possible score. Aged tobacco also exposes its exact aged-day count plus `Aged at least 7/30/90/365 days` attributes for cellar automation.

Tobacco Barrels hold up to **64 compatible cured leaves or loose tobacco** per batch. They protect active **fermentation** from every capability-based extractor, including Funnels, Chutes, Packagers, and Mechanical Arms. Aging remains extractable by design, allowing a Smart Funnel, Arm, or other filtered Create component to release stock at a chosen age threshold. Unfiltered extraction from an aging barrel can remove the tobacco immediately. Display Links include a dedicated **Barrel Count** source showing the current batch against the 64-item capacity.

For automatic Tobacco Box packing, keep the box on a Depot beneath a Deployer and feed matching Cigars, Cigarettes, Shisha, or loose tobacco to the Deployer. This is especially important for non-stackable Cigars and Cigarettes: repeated Deployer cycles fill the same stationary box. Use `Box fill` Attribute Filters to supply empty boxes and extract full ones.

Factory Gauge/Frogport tobacco requests use Tobacconist's tier-aware matching. Different numeric scores in the same quality tier can satisfy a request when the other identity fields match. Factory Gauges do not accept Attribute Filter items, so exact-score or Quality-Band package requests should be implemented by sorting stock into separate logistics networks before the gauge.

## Tall Hookah logistics

Two-block Hookahs expose the same lower master inventory from **both occupied levels**. Hoppers, Funnels, Mechanical Arms, Packagers/Stock Links, normal item capabilities, and Display Links can therefore target either half without creating a duplicate inventory. Only the lower Hookah entity ticks, stores contents, and produces drops; the upper entity is a delegation proxy.

## Display Links

Display Links can report status from traditional and Industrial Drying Racks, Tobacco Barrels, Flue Fireboxes, Hookahs, and Production Monitors. Tobacco Barrels expose Status, Progress, Humidity, Age, and **Count/64**. Hookahs expose **Status, Fuel, Shisha, and Water**; on tall Hookahs either occupied block level resolves to the same lower master, so a Display Link can attach to either half. Production Monitors expose Count/Target, rolling Rate, and Status sources and deliberately refresh those live values every **20 ticks / 1 second**. Other Tobacconist Display Sources use Create's normal **100-tick / 5-second** passive cadence, avoiding unnecessary multiplayer polling for slower-changing rack, barrel, firebox, Hookah, Basin, and Vault telemetry. Create Basins expose homogenization status/average and Item Vaults expose tobacco count/average quality. Homogenizer status distinguishes continuous targets, filling/ready/processing states, uniform lots, incompatible tobacco, and whether signal 15 must be re-armed. Vault average quality is only reported as a single value when the stored leaf tobacco is one compatible homogenization lot.

## Smoke ventilation

Create fan airflow can push/pull Tobacconist smoke. Indoor smoke can drift along ceilings toward extraction airflow and is removed when it reaches a pulling fan intake.
