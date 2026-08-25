# Cutting and Finished Products

## Hand cutting with a Chaveta

Use exactly one cured leaf and one Chaveta placed orthogonally in the crafting grid. The Chaveta position relative to the leaf chooses the cut, and one leaf yields three loose tobacco.

- Chaveta above leaf → **Shag**
- Chaveta left of leaf → **Ribbon**
- Chaveta right of leaf → **Rough**
- Chaveta below leaf → **Flake**

Diagonal placement does not match the recipe.

## Create cutting

A Deployer holding a Chaveta advances one mechanical cutting step:

`Cured Leaf → Rough → Ribbon → Shag`

When used by a Deployer, the Chaveta is treated as a reusable industrial machine tool: automated cutting does **not** consume Chaveta durability. Hand crafting retains the normal Chaveta durability rules.

Rough Cut is the branch point for the Mechanical Press. Pressing Rough Cut produces Flake without changing the tobacco's processing metadata.

## Finished-product quality

Leaf quality is the dominant input to the final 1–10 product score. Cut choice is a **preparation adjustment**, not a replacement for growing, curing, fermentation, or aging quality.

Cigarettes and Shisha score the tobacco directly. Cigars first calculate a component quality of **75% filler + 25% wrapper**, rounded to the nearest whole quality point, then apply the filler-cut adjustment. Cigar crafting requires a **dry cured Tobacco Leaf** wrapper in both manual and Create assembly.

The preparation adjustments below are applied to the underlying 0–120 tobacco quality before conversion to the displayed 1–10 score. Caps only prevent clearly unsuitable preparations from reaching the very top score.

| Cut | Cigarette | Cigar | Shisha |
| --- | ---: | ---: | ---: |
| Shag | +0 Q, cap 10 | -15 Q, cap 8 | -15 Q, cap 8 |
| Ribbon | -3 Q, cap 10 | -3 Q, cap 10 | -3 Q, cap 10 |
| Rough | -12 Q, cap 8 | +0 Q, cap 10 | +0 Q, cap 10 |
| Flake | -20 Q, cap 7 | -8 Q, cap 9 | -10 Q, cap 9 |

That makes **Shag** the ideal cigarette preparation, **Rough** the ideal cigar filler preparation, and **Rough** the ideal Shisha preparation. Ribbon remains a strong alternative for all three instead of heavily downgrading otherwise excellent tobacco.

For cigars, the wrapper is now a real quality component rather than display-only metadata. Overall cigar age is also weighted 75% filler / 25% wrapper instead of taking whichever component is older. Ruined-state and other processing metadata continue to propagate through the finished product.

The stored `ProductQuality` is authoritative for finished products: the tooltip, Create quality Attributes, Tobacco Box displays, and quality-based smoking bonus all use the same final score.

## Quality averaging

The custom averaging recipe accepts 2–8 compatible items of one registry item:

- Raw leaves: same raw leaf item; averages GrowthQuality.
- Cured leaves: same cured leaf item and same cure type; averages final Quality.
- Single-variety loose tobacco: same loose item, same cure, and same cut; averages final Quality.

The arithmetic average is rounded to the nearest whole number. Output count equals the number of occupied tobacco slots.

A full 3×3 same-item grid is reserved for Tobacco Crates instead of averaging.
