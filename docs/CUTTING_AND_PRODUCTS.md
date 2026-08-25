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

Rough Cut is the branch point for the Mechanical Press. Pressing Rough Cut produces Flake without changing the tobacco's processing metadata.

## Product quality by cut

Finished-product score starts from the tobacco's quality rounded to a 0–10 base, then applies the cut modifier and cap.

| Cut | Cigarette | Cigar | Shisha |
| --- | ---: | ---: | ---: |
| Shag | modifier 0, cap 10 | -4, cap 4 | -4, cap 4 |
| Ribbon | -1, cap 9 | -2, cap 7 | -1, cap 8 |
| Rough | -3, cap 6 | -1, cap 9 | 0, cap 10 |
| Flake | -4, cap 5 | 0, cap 10 | -2, cap 7 |

That makes Shag the ideal cigarette cut, Flake the ideal cigar cut, and Rough the ideal shisha cut.

## Quality averaging

The custom averaging recipe accepts 2–8 compatible items of one registry item:

- Raw leaves: same raw leaf item; averages GrowthQuality.
- Cured leaves: same cured leaf item and same cure type; averages final Quality.
- Single-variety loose tobacco: same loose item, same cure, and same cut; averages final Quality.

The arithmetic average is rounded to the nearest whole number. Output count equals the number of occupied tobacco slots.

A full 3×3 same-item grid is reserved for Tobacco Crates instead of averaging.
