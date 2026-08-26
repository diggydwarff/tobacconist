# Storage and Packaging

## Tobacco Pouch

The Tobacco Pouch stores up to 128 units of one exact loose-tobacco batch while preserving its processing data. Pipes packed directly from a filled pouch receive a 1–5 puff packing bonus. Pouches can be dyed; recoloring starts from the new dye selection rather than mixing in the previous pouch color.

## Tobacco Box

A Tobacco Box stores one exact batch of a supported finished/loose product:

- Cigars: 8
- Cigarettes: 12
- Shisha: 16
- Loose tobacco, including Blended Tobacco: 16

The first inserted product defines the accepted item/components. Boxes preserve product metadata. Named Labels can brand a box; extracted products inherit the box label. Finished Cigars, Cigarettes, and Shisha display the same stored `ProductQuality` used by the item itself rather than falling back to the filler tobacco's raw quality.

### Create automatic packing

A Deployer can insert matching Cigars, Cigarettes, Shisha, or loose tobacco into a Tobacco Box while preserving exact metadata. For non-stackable Cigars and Cigarettes, keep the box stationary on a **Depot beneath the Deployer** so repeated Deployer cycles fill the same box.

Create Attribute Filters expose `Box fill: Empty`, `Box fill: Partially Filled`, and `Box fill: Full`. A typical line uses a filtered Funnel to place empty boxes on the Depot, the Deployer to fill them, and another filtered Funnel to remove only full boxes. Capacities remain 8 Cigars, 12 Cigarettes, 16 Shisha, or 16 loose tobacco.

## Tobacco Crates

Fill a 3×3 crafting grid with nine units of the **same registry item**. Their NBT/components do not have to match.

Supported inputs include:

- Raw leaves → matching green Raw Tobacco Crate.
- Cured leaves → matching cured-variety crate.
- Single-variety loose tobacco → matching cured-variety crate.
- Blended Tobacco → Blended Tobacco Crate.

Each of the nine inputs is serialized individually into the crate item. When a filled placed crate is broken in Survival, it spills the exact nine stored units back out, including each unit's own quality, cure, cut, age, fermentation, flavor, blend, and other stored data. It does not flatten those units into an averaged batch.

Crates are packaging/storage blocks, not live inventory machines. They do not expose a nine-slot hopper/funnel inventory. Creative breaking suppresses their stored drops.

A full 3×3 same-item grid is reserved for crating, so the quality-averaging recipe uses 2–8 compatible inputs instead.
