# Blending, Flavoring, and Shisha

## Tobacco blending

Blends use 2 or 3 single-variety loose tobaccos. Inputs must:

- use the same cut,
- have the same fermented state,
- have the same ruined state, and
- represent distinct blend components.

A component identity is variety + cure + aromatic flavor. Quality-only differences of otherwise identical tobacco are not a blend; use quality averaging/homogenizing instead. The same botanical variety may still appear twice if its cure or aromatic casing differs.

The result count equals the number of inputs. Blend quality and age are averaged. If component cure types differ, the blend cure is recorded as Mixed. Component snapshots are retained so blend identity is not lost.

Server-defined secret blends can match variety, minimum quality, cure, and flavor requirements.

## Aqua Vitae and Flavoring Essence

Brewing path:

1. Water Potion + Sugar → Mundane Potion.
2. Mundane Potion + Wheat → Aqua Vitae.
3. Aqua Vitae + a supported flavor ingredient → Flavoring Essence.

Flavor ingredients are tag-driven so supported third-party ingredients can satisfy the same flavor tag without becoming hard dependencies.

Each Essence bottle represents one full single-use 1000 mB batch for Create fluid processing.

## Aromatic tobacco

One Flavoring Essence can be applied directly to suitable loose tobacco as a light aromatic casing. The original variety, cure, cut, quality, age, fermentation, blend data, and other tobacco metadata remain attached.

With Create, a Spout applies the same treatment using 1000 mB of the corresponding Essence fluid.

## Molasses

Plain molasses is made from a Water Potion and Sugar Cane. One full Flavoring Essence flavors one full plain Molasses bottle. Create uses 1000 mB plain Molasses + 1000 mB Essence for the equivalent operation.

## Shisha

Shisha accepts any suitable loose cut or blend plus 1–3 full flavored Molasses bottles. Cut still affects final product quality; Rough is optimal.

Unused Shisha can receive additional flavored Molasses until all three flavor slots are filled. Re-flavoring is blocked after the Shisha has been used/damaged.

Create Mixers can perform the same Shisha operation with full 1000 mB flavored-Molasses batches.
