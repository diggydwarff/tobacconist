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

## Averaging vs blending vs crating

These systems solve different problems:

- **Averaging/homogenizing:** standardizes quality for otherwise-compatible tobacco.
- **Blending:** combines 2–3 distinct compatible tobacco components and records their identities.
- **Crating:** losslessly packages nine units of the same registry item even if their NBT/components differ; nothing is averaged.

## Finished-product quality

Cigarettes, cigars, and Shisha store a 0–10 product score derived from tobacco quality plus the appropriateness of its cut. See `CUTTING_AND_PRODUCTS.md` for the exact cut modifiers/caps.
