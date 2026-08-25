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

## Cutting and pressing

- Deployer + Chaveta: Cured Leaf → Rough → Ribbon → Shag.
- Mechanical Press: Rough → Flake.
- Deployers/Presses also support the implemented cigarette/cigar assembly paths while preserving product data.

## Mixers, fluids, and flavoring

- Mechanical Mixers can homogenize compatible leaf quality.
- Mixers can blend 2–3 compatible single-variety loose tobaccos.
- Heated Mixers support Aqua Vitae and Flavoring Essence production.
- Flavoring Essence and Molasses factory containers represent 1000 mB batches.
- Spouts apply 1000 mB Essence as aromatic casing.
- Mixers combine flavored Molasses with loose tobacco/Shisha.

## Logistics and display

Supported Create integration includes Mechanical Arms, Funnels, Chutes, Belts, Packagers, Stock Links, Factory Gauges, Attribute Filters, and Display Links where the target block exposes a meaningful inventory/status source.

Attribute matching understands tobacco variety, cure, cut, quality, flavors, blends, labels, and finished-product data. Factory Gauge resolution can satisfy requests by Tobacconist quality tier while still preserving meaningful tobacco identity.

Display Links can report status from Drying Racks, Tobacco Barrels, Flue Fireboxes, and Hookahs. Drying Rack display data includes leaf count/status.

## Smoke ventilation

Create fan airflow can push/pull Tobacconist smoke. Indoor smoke can drift along ceilings toward extraction airflow and is removed when it reaches a pulling fan intake.
