# Smoking, Pipes, and Hookahs

## Pipes, cigarettes, and cigars

- Cigarette durability/puff count: 15.
- Cigar durability/puff count: 100.
- A normally packed pipe receives 40 puffs.
- Packing a pipe from a Tobacco Pouch adds a random 1–5 bonus puffs, for 41–45 total.

Packed products retain the tobacco data used to make/pack them so smoking effects can use the correct variety, quality, cure, cut, age, fermentation, flavor, and blend information.

When enabled by server config, smoking applies the Nicotine status effect for 500 ticks (25 seconds). High-quality tobacco can also grant Regeneration; aging can extend the Regeneration duration.

Curios is optional. When installed, cigarettes, cigars, and packed pipes can be used from the Mouth slot with the Tobacconist puff keybind. Spectacles use Curios Head; without Curios, Spectacles fall back to the vanilla Head slot.

## Hookah inventory

Hookahs use three validated slots:

1. Fuel.
2. Shisha Tobacco.
3. Water Potion or Dirty Hookah Water.

The hookah becomes active only while Shisha and usable water are present and fuel time remains. Hold a Hookah Hose and use the active hookah to draw from it; hose draws have a 20-tick interaction cooldown.

## Fuel duration

The base burn unit is 5,000 ticks multiplied by fuel type:

| Fuel | Multiplier | Burn time |
| --- | ---: | ---: |
| Bamboo Charcoal | 5.0× | 25,000 ticks |
| Charcoal | 2.5× | 12,500 ticks |
| Coal | 0.5× | 2,500 ticks |

A Shisha item has 6,500 durability ticks of active hookah use.

## Water

After a random 2–5 completed Shisha loads, a Water Potion becomes Dirty Hookah Water. Dirty water still lets the hookah operate, but drawing through the hose applies Nausea for 120 ticks (6 seconds). It does not directly damage the player.

## Blocks and variants

Base/tall hookahs and material/ornate variants share the same core inventory and smoking behavior. Copper variants preserve block-entity data across oxidation/wax transitions. Breaking hookahs in Creative mode suppresses both the block drop and stored-content drops.
