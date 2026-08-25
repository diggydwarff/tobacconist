# Automation and Create

Create integration is optional. Common Tobacconist code avoids a hard Create dependency, and ordinary hand-crafted processing remains available without Create.

## Harvesting

Mechanical Harvesters can harvest mature two-block tobacco crops without duplicating the upper/lower halves. Harvest quality and seed behavior use the normal crop logic.

## Drying Rack automation

Vanilla sided inventory behavior:

- Horizontal faces can insert valid raw leaves.
- Top and bottom cannot insert.
- Bottom extraction is available only after the cure is finished.
- Capacity is 16.
- Input must match the stored leaf item/components.
- Insertion locks at 10% cure progress.

NeoForge/Create capability-based consumers can query the rack's validated item handler; completed output remains protected until the batch is finished.

Create Encased Fans can assist Air Curing and an already-valid Sun Cure at 4× progress. Smoke/heat airflow can assist Fire or Flue Curing at 6×. The fan resolver checks the taller rack so airflow touching either its lower or upper section can count.

Hanging Tobacco Bunches can receive environmental fan assistance but expose no item automation or Display Link source.

### Industrial Drying Rack

The **Industrial Drying Rack** is the dedicated factory tier. It holds **32 matching leaves**, rejects manual right-click loading/removal, and is intended to sit directly in Create logistics lines. Mechanical Arms, Funnels, Packagers/Stock Links, and the validated item capability can load raw leaves and remove only finished output. Display Links and Spectacles report the same cure/status information with the 32-leaf capacity.

The industrial rack cannot cure passively and contains **no internal fan or power input**. External Create airflow is the machine: plain airflow provides Air or an otherwise-valid Sun Cure, fan-blown Campfire smoke/heat provides Fire, and fan-blown Lava heat provides Flue. Its assisted progress is modestly faster than a fan-assisted wooden rack (5 vs 4 progress ticks for Air/Sun; 7 vs 6 for Fire/Flue), with no quality bonus. This keeps the wooden rack useful for traditional/passive curing while the industrial rack trades cost and infrastructure for density and throughput.

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

Tobacco Barrels protect active **fermentation** from every capability-based extractor, including Funnels, Chutes, Packagers, and Mechanical Arms. Aging remains extractable by design, allowing a Smart Funnel, Arm, or other filtered Create component to release stock at a chosen age threshold. Unfiltered extraction from an aging barrel can remove the tobacco immediately.

For automatic Tobacco Box packing, keep the box on a Depot beneath a Deployer and feed matching Cigars, Cigarettes, Shisha, or loose tobacco to the Deployer. This is especially important for non-stackable Cigars and Cigarettes: repeated Deployer cycles fill the same stationary box. Use `Box fill` Attribute Filters to supply empty boxes and extract full ones.

Factory Gauge/Frogport tobacco requests use Tobacconist's tier-aware matching. Different numeric scores in the same quality tier can satisfy a request when the other identity fields match. Factory Gauges do not accept Attribute Filter items, so exact-score or Quality-Band package requests should be implemented by sorting stock into separate logistics networks before the gauge.

## Display Links

Display Links can report status from traditional and Industrial Drying Racks, Tobacco Barrels, Flue Fireboxes, and Hookahs. They also expose homogenization status/average from Create Basins and tobacco count/average quality from Item Vaults. Homogenizer status distinguishes continuous targets, filling/ready/processing states, uniform lots, incompatible tobacco, and whether signal 15 must be re-armed. Vault average quality is only reported as a single value when the stored leaf tobacco is one compatible homogenization lot.

## Smoke ventilation

Create fan airflow can push/pull Tobacconist smoke. Indoor smoke can drift along ceilings toward extraction airflow and is removed when it reaches a pulling fan intake.
