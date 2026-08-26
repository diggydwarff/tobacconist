# Curing Tobacco

## Methods and base times

| Method | Progress required | Approx. Minecraft days |
| --- | ---: | ---: |
| Fire | 24,000 ticks | 1 |
| Flue | 36,000 ticks | 1.5 |
| Sun | 48,000 ticks | 2 |
| Sun under glass (rack only) | 54,000 ticks | 2.25 |
| Air | 72,000 ticks | 3 |

Only time spent under a valid curing condition advances that method. A Create fan can accelerate progress: normal airflow advances at 4 curing ticks per game tick; heated/smoke airflow advances at 6.

## Drying Rack

A wooden rack holds up to 16 matching raw leaves and now fits completely inside one block. Horizontal Hoppers/Funnels can insert from the rack sides, top and bottom insertion remain closed, and a hopper underneath can extract only after curing is complete. Blocks can be placed normally directly above the rack. Its visible fill level changes with inventory count, and inspection reports `X/16 Leaves`. Once the cure reaches 10%, additional insertion is blocked so the batch cannot be changed mid-process.

Cure precedence per tick is **Fire → Flue → Sun → Air**.

### Air curing

- Not directly rained on.
- Sky light above the rack is at least 8.
- Direct sky is not required.

### Sun curing

- Daytime.
- Sky light above is at least 14.
- At least two horizontal adjacent sides are open air.
- No direct rain exposure.
- The exposure space immediately above the rack can see the sky, or that exposure block is glass/stained glass or a glass pane.

Glass shelter uses the 54,000-tick Sun time. Plain Create airflow accelerates an otherwise-valid Sun cure but does not create sunlight by itself.

### Fire curing

A lit Campfire or Soul Campfire directly under the rack starts Fire Curing. Fire has priority over the other cure methods. The rack's collision is limited to its frame so campfire smoke can pass through the open structure.

### Flue curing

A normal rack can Flue Cure when all of these are true:

- No direct rain.
- It is not directly over a lit campfire.
- The rack does not have direct sky access above.
- The open space immediately above the rack is clear.
- A sturdy roof underside exists 3–5 blocks above the rack.
- No fire, soul fire, campfire, or soul campfire is present in the nearby 3×3×3 smoke-contamination area.
- At least one lit Flue Firebox is at the rack's level or one block below, within horizontal Manhattan distance 3.

The Flue Firebox emits a small active smoke effect while lit.

## Industrial Drying Rack

The **Industrial Drying Rack** is the Create-era factory counterpart to the wooden rack. It holds **32 matching leaves** and deliberately cannot be loaded or unloaded by player right-clicks; use Funnels, Mechanical Arms, Packagers, or other validated item automation. Breaking the block still returns its stored contents. The rack has **no internal fan, motor, or power connector**: it is an open forced-air frame whose curing assistance must come from external Create machinery. Create Display Links can read rack Status, cure Progress, and Leaf Count from the shared rack state; these slower-changing rack sources use Create's normal 100-tick passive refresh.

Unlike the wooden rack, the industrial rack makes **no curing progress without dual-tier Create airflow**. The lower and upper rack levels must each be reached by a **different Encased Fan**, and both airflow paths must resolve to the same cure type:

- Plain airflow on both tiers can drive Air Curing or an otherwise-valid Sun Cure. Sunlight is still required for Sun Curing.
- Matching fan-blown Campfire smoke/heat on both tiers drives Fire Curing.
- Matching fan-blown Lava heat on both tiers drives Flue Curing.
- One fan reaching both tiers, airflow on only one tier, or mismatched cure airflow pauses the Industrial Rack.
- Passive campfires, passive flue-barn heat, sunlight alone, and ordinary air exposure do not cure an Industrial Rack.

Assisted throughput is intentionally only modestly better than the wooden rack: plain fan Air/Sun progress runs at 5 curing ticks per game tick instead of 4, while heated Fire/Flue assistance runs at 7 instead of 6. There is **no quality bonus** for using the industrial block. Its advantages are doubled capacity, compact automated handling, and somewhat higher assisted throughput.

The recipe is Create-gated and upgrades a normal Drying Rack using Iron Sheets, Brass Casing, Andesite Alloy, and a Precision Mechanism.

## Hanging Tobacco Bunches

Sneak-use the underside of a sturdy block while holding at least 16 matching raw leaves. Exactly 16 leaves are stored in the bunch. In Creative mode, one held leaf is enough to place the decorative 16-leaf bunch.

The hanging structure uses two block positions internally but has a compact hanging model. It has no inventory automation. Breaking either half returns the stored leaves; already-cured leaves can also be hung as decoration/storage and keep their NBT/components.

### Hanging Sun Curing

The support block necessarily blocks vertical sky, so hanging tobacco uses side skylight instead. During daytime, with no rain, at least one horizontal neighboring air block beside either half must:

- be air,
- see the sky, and
- have sky light 14 or higher.

This allows long pergola/slatted structures such as alternating `beam / gap / beam / gap` rows. A fully covered structure with no qualifying side opening falls back to Air Curing.

### Hanging Fire and Flue Curing

- Fire: a lit campfire is directly below the bottom of the bunch.
- Flue: no side rain, no direct campfire below, no nearby smoke-contamination blocks, and at least one lit Flue Firebox within horizontal Manhattan distance 3 of the bunch center at the same Y level or one below.

Once a hanging batch has entered Fire Curing, Fire remains the final-priority method. Flue likewise remains above later Sun/Air exposure.

## Quality and rain

Curing starts from `GrowthQuality` (0–70) and calculates a finished value capped at 100. Base method bonuses are Air +7, Fire +8, Sun +9, and Flue +10. A clean uninterrupted environment, interruptions, mixed methods, a random 0–10 bonus, and rain damage modify the final result. The total curing bonus added to growth quality is capped at 30 before later mix/wet penalties.

Direct rain adds wet damage every 200 ticks. When the direct-rain exposure counter reaches 1,200 ticks (60 seconds at uninterrupted exposure), the batch becomes Spoiled Tobacco. Once sheltered, the live rain-exposure counter decays by 5 per tick.

For best quality, load the whole batch before 10%, keep one cure method valid, and keep it dry.
