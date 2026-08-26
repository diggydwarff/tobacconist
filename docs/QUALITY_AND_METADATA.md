# Quality and Metadata

## Quality stages

Raw leaves store `GrowthQuality` in the 0–70 range. Curing converts that into final processed `Quality` and a cure type, capped at 100 during curing. Fermentation and aging can then raise processed quality as high as 120.

Processed quality tiers are:

- 0–30 Poor
- 31–60 Common
- 61–80 Good
- 81–89 Excellent
- 90–100 Perfect
- 101–120 Exceptional

The server can disable quality gameplay. Existing quality data is retained so enabling the system later does not erase prior processing information.

## Metadata carried through processing

Depending on the item/process, Tobacconist tracks values such as:

- botanical variety,
- GrowthQuality / final Quality,
- cure type,
- cut type,
- fermentation state,
- aged days,
- ruined state,
- aromatic flavor,
- blend component data and intrinsic/secret blend name,
- Shisha flavor slots,
- product-quality score,
- Tobacco Box/product label,
- packed-pipe tobacco data.

Recipes and Create integrations that transform tobacco copy or rebuild this data rather than returning generic stacks.

## Averaging vs homogenizing vs blending vs crating

These systems solve different problems:

- **Crafting-grid averaging:** 2–8 compatible occupied slots; averages the participating slot qualities and rounds to the nearest whole value.
- **Create bulk homogenizing:** standardizes one physical Basin batch at a time, requires visible quality variation, uses a count-weighted average rounded to the nearest whole value, and locks the selected inputs once mixing starts. Analog redstone received by either the Mixer or Basin controls continuous batch size; signal 15 finishes the current Basin once and accepts any lot of at least 2 leaves.
- **Blending:** combines 2–3 distinct compatible loose-tobacco components and records their identities.
- **Crating:** losslessly packages nine units of the same registry item even if their NBT/components differ; nothing is averaged.

For Create grading, exact quality scores remain available and a 5-point Quality Band attribute provides tolerant ranges such as 46–50 and 51–55. Aged tobacco also exposes exact aged days plus `Aged at least 7/30/90/365 days` attributes for automated cellars. Factory Gauge/Frogport restocking remains quality-tier aware; exact/band stock should be separated into dedicated logistics networks when precision package requests are required.

## Finished-product quality

Cigarettes, cigars, and Shisha store a 0–10 `ProductQuality` score. Leaf quality remains the dominant factor and cut suitability applies a modest preparation adjustment. Cigars use a **75% filler / 25% wrapper** component quality before the cut adjustment, so wrapper quality contributes directly to the finished cigar. The same stored score drives the tooltip, Create quality Attributes, Tobacco Box quality display, and quality-based smoking bonus. See `CUTTING_AND_PRODUCTS.md` for the exact adjustments and caps.
