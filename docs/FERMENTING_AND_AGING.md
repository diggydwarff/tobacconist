# Fermenting and Aging

## Tobacco Barrel

The Tobacco Barrel holds up to 16 cured leaves or loose tobacco. Raw leaves and Spoiled Tobacco are rejected. Additional items must match the stored item and components exactly. Adding tobacco resets the current fermentation/aging cycle.

The barrel automatically chooses a mode. **Fermentation has priority over aging** when both could otherwise be considered.

## Fermentation

Requirements:

- Warmth at least 3.
- Internal Barrel Humidity at least 25.
- Tobacco is not already fermented.
- Tobacco is not ruined.

Fermentation requires 48,000 valid ticks (about two Minecraft days). A successful cycle sets the Fermented flag and adds **+7 quality**, up to the overall processed-quality cap of 120. A batch ferments only once.

Internal Barrel Humidity is a stored reservoir that changes with the environment; it is not the same value as the environmental Humidity shown by the environment helper.

## Aging

Aging requires:

- Warmth 0 or lower.
- Environmental Humidity 1–3.
- No sky access above the barrel.
- Block light above the barrel 7 or lower.

Every 24,000 uninterrupted aging ticks adds one aged day.

| Age | Label | Quality gain schedule |
| --- | --- | --- |
| 0–6 days | Fresh | +1 on days 3 and 6 |
| 7–29 days | Light Aged | +1 on days 14, 21, and 28 |
| 30–89 days | Deep Aged | +1 on days 45, 60, and 75 |
| 90–364 days | Vintage | +1 on day 90, then each 30-day multiple through day 360 |
| 365+ days | Cellared | day 360 is the last aging-quality gain |

Aging and fermentation can raise quality to 120.

After day 365, no further aging-quality gains are possible. The first non-zero extreme-age spoil check occurs when the internal month index reaches 1 (day 396), then at later 30-day month-index changes. The chance is `0.5% × month index`, capped at 10%. Spoilage from extreme age reduces quality by 15 and converts the batch to Spoiled Tobacco.

## Environment controls

### Warmth

- Hot biome (temperature ≥1.5): +2.
- Warm biome (≥0.9): +1.
- Cool biome (≥0.4): 0.
- Cold biome: -1.
- Daylight with direct sky and sky light ≥14: +2.
- Direct sky with sky light ≥10: +1.
- Each nearby lit Flue Firebox: +3.
- Each nearby lit Campfire/Soul Campfire: +1.
- Blue Ice contributes 3 cold points, Packed Ice 2, Ice/Snow Block 1; nearby cold reduces Warmth by at most 2.
- Cool/dark storage: -1.

Heat/cold sources are scanned within 2 blocks horizontally and 1 vertically.

### Environmental humidity

- Rain biome precipitation: +2; snow precipitation: +1.
- Active rain at the barrel: +1.
- Hot biome temperature ≥1.5: -2; temperature ≥0.9: +1.
- Nearby water within 2 blocks horizontally and 1 vertically increases humidity. Water cauldrons count as two water sources.
- Cool/dark storage: +1.

Water bonus is +2 for at least 1 source, +3 for 2, +4 for 4, and +5 for 8 or more counted sources.

## Overheating

Warmth 7 or higher advances an overheat counter. Remaining that hot for 6,000 ticks (5 minutes) ruins the batch and reduces quality by 25. Below 7 warmth, the overheat counter decays by 2 per tick.
