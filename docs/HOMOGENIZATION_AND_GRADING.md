# Homogenization and Quality Grading

This guide describes the Create-assisted workflow for turning naturally variable farm lots into predictable bulk tobacco. Create is optional; the ordinary crafting-grid averaging recipe remains available without it.

## Why homogenize

Large farms often produce the same leaf variety at several nearby quality scores. Exact sorting can turn ordinary agricultural variation into many small logistics streams. Homogenization reduces that variation by standardizing one physical Mixer batch at a time.

Homogenization is not blending. It never combines different tobacco varieties or intentionally distinct processing identities.

## Compatibility

The Create homogenizer accepts raw leaves with raw leaves or cured leaves with cured leaves. Inputs must:

- be the same registry item/variety,
- be in the same raw/cured stage,
- match all non-quality processing metadata.

Only the numeric quality field may differ. For raw leaves this is `GrowthQuality`; for cured leaves it is final `Quality`.

A homogenizing Mixer cycle requires **at least two visible quality values** in the selected batch. A stack that is already entirely Q50 is already homogeneous and is not remixed merely because it reaches a numeric batch target.

## Continuous batch control

A Mechanical Mixer over a Basin uses analog redstone received by **either the Basin or the Mixer** to choose the continuous physical batch size. If both halves are powered, the stronger signal wins. Powering the Mixer is usually the cleanest layout because the Basin's native Comparator output can remain dedicated to fullness sensing:

| Signal | Target leaves |
| ---: | ---: |
| 0 | 64 |
| 1 | 16 |
| 2 | 32 |
| 3 | 48 |
| 4 | 64 |
| 5 | 96 |
| 6 | 128 |
| 7 | 160 |
| 8 | 192 |
| 9 | 256 |
| 10 | 320 |
| 11 | 384 |
| 12 | 448 |
| 13 | 512 |
| 14 | 576 |
| 15 | one-shot Finish command |

Signals 0–14 are continuous settings. Once enough compatible tobacco is physically present in the Basin and the selected batch contains quality variation, the Mixer claims exactly that target count.

The selection is count-proportional across the quality stacks available in the Basin. If pure proportional rounding would accidentally select only one quality while another quality is present, the selector swaps one minority leaf into the batch so every homogenizing cycle actually reduces variation.

When a Mixer cycle begins, the exact selected inputs are snapshotted. New arrivals wait for a later cycle.

## Signal 15: finish the Basin

Signal strength **15** is not another continuous size. A new rise to 15 snapshots the compatible tobacco **already physically inside the Basin**. Unlike automatic signals 0–14, Finish accepts any current lot of **2 or more leaves**, making it useful for small farms and end-of-run leftovers.

- If that snapshot contains multiple qualities, it is homogenized as one final physical batch.
- If it is already one quality, it is passed through unchanged without a pointless Mixer cycle.
- Holding signal 15 high does not repeatedly claim later arrivals. Drop below 15 before issuing another Finish command.
- A one-leaf lot does nothing.
- External chests, Vaults, Funnels, belts, Arms, Stock Links, and logistics networks are not searched or treated as invisible lot storage.

This keeps the machine deterministic: the Basin processes what it actually holds. A small farm can set a high continuous target such as 576, let one harvest collect, then pulse 15 to reduce that entire physical Basin lot to one quality in one cycle.

## Uniform-input deadlock protection

Create normally prevents a second identical stack from occupying another Basin slot. Tobacconist relaxes that rule **only for compatible tobacco leaves in a Mechanical Mixer Basin**, allowing repeated same-quality stacks to fill additional slots while the machine waits for another quality.

Generic output extraction from homogenizer input is also blocked, so a Chute, Hopper, Funnel, or similar extractor cannot pull raw input leaves through the Basin before the Mixer claims them.

If all nine input slots become completely full — **576 leaves** — and the compatible lot is still only one quality, no different quality could enter. In that one deadlock case the uniform tobacco automatically passes through unchanged so a continuous farm can keep moving.

At smaller uniform amounts the Basin waits for quality variation, or the player can pulse signal 15 to flush any lot of at least 2 leaves.

## Quality calculation

The output uses the count-weighted average:

`sum(quality × item count) / total item count`

The result is rounded to the **nearest whole quality value**. The output count equals the selected input count.

Nearest rounding is intentional for iterative factories. The previous always-round-down rule created a systematic quality loss every time tobacco passed through another homogenizer. Nearest rounding keeps normal Minecraft integer quality values while avoiding that downward bias.

Example:

- 32 leaves at Q49
- 32 leaves at Q59
- weighted average = Q54
- output = 64 leaves at Q54

A non-integer average such as 48.4 becomes Q48; 48.6 becomes Q49.

## Multi-stage homogenization

One homogenizer can process an arbitrarily long input stream, but it still works in physical Basin-sized batches. A large harvest may therefore leave the first stage as several nearby standardized qualities.

For example, a long mixed Q40–56 harvest might leave a first-stage line as batches of Q46, Q48, Q49, and Q47. That is expected: each individual batch is homogeneous even though the entire harvest is not yet one grade.

There are three normal ways to use the same machine logic:

1. **Small harvest / one-pass lot:** set a high target such as 576 so the Mixer waits, let the harvest collect physically in the Basin, then pulse signal 15. Any 2–576 leaf lot is finished as one quality in one cycle.
2. **Small continuous farm / one Mixer:** process normal automatic batches, collect the first-pass output, raise the target, and recirculate it for another pass when tighter standardization is desired.
3. **Larger farm:** run several smaller first-stage homogenizers in parallel, combine their outputs, and feed one larger-target downstream homogenizer.

Downstream targets should normally be **larger than upstream targets** so several already-uniform primary batches are physically present before the next Mixer starts. A reliable example is `64 or 128 → 256 or 576`. At the end of the run, pulse signal 15 to homogenize or flush the leftover tail that is below the normal downstream target.

A typical scalable line is:

`harvest → grading → 64/128-leaf primary homogenizers → intermediate storage → 256/576-leaf final homogenizer → standardized storage`

Multi-stage homogenization is therefore a throughput tool, not an arbitrary requirement. A small player can reuse one Mixer with a larger second-pass target; a plantation can build a homogenization tree.

## Grading before homogenization

Create Attribute Filters expose:

- exact `Growth Quality Score` for raw leaves,
- exact `Quality Score` for processed tobacco,
- quality tier,
- 5-point `Quality Band` values such as 46–50 and 51–55.

This lets a farm siphon unusual outliers away before bulk homogenization. A common strategy is to send exceptionally low tobacco to a cheap-product line, the normal crop range to homogenization, and unusually high tobacco to premium storage.

There is no required cutoff. The player decides how much grading complexity is worth building.

## Factory Gauges and Frogports

Tobacconist's Factory Gauge matching remains quality-tier based. A request for tobacco in a tier may be fulfilled by another numeric score in the same tier as long as the other tobacco identity fields match.

Create Factory Gauges do **not** accept Attribute Filter items. If an exact score or Quality Band must be requested through package logistics, first sort that stock into a dedicated storage/logistics network. The network itself then guarantees the grade the gauge can see.

## Display Links and inspection

With Spectacles, looking at either the relevant Basin or its Mechanical Mixer shows the current selected count, target, average quality, predicted output quality, incompatible tobacco, and whether the lot is filling, ready, processing, already uniform, or waiting for a new signal-15 Finish pulse.

Create Display Links can report:

- Basin homogenization status,
- Basin current average and predicted output quality,
- tobacco count in an Item Vault,
- average leaf quality in a Vault when its tobacco is one compatible lot.

A Vault containing multiple incompatible tobacco lots reports that condition instead of presenting a misleading combined average. Basin and Vault Display Link telemetry uses Create's normal 100-tick passive refresh; only the live Production Monitor sources intentionally use the faster 20-tick cadence.
