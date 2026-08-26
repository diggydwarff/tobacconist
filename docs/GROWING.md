# Growing Tobacco

## Overview

Tobacconist has six varieties: Virginia, Burley, Oriental, Dokha, Shade, and Wild. Cultivated crops grow on farmland through stages 0–7. From stage 4 onward the plant occupies a second block above the base. Natural growth requires raw brightness 9 or higher.

Harvesting a mature plant creates raw leaves with `GrowthQuality`. Growth quality is limited to 0–70; later curing can raise the finished quality toward 100.

## Growth-quality calculation

Before the harvest random roll, growth potential is:

`28 + biome/2 + light*3/5 + temperature*3/5 + harvest*3/4`, clamped to 0–70.

Harvest then adds a random 0–10 and clamps the result to 0–70. Spectacles report the environmental condition score separately from crop maturity so a young plant is not treated as environmentally poor simply because it is not ready to harvest.

Harvest timing contributes:

- Mature: +8
- One stage early: -3
- Earlier than that: -12

## Light and temperature targets

| Variety | Ideal light | Acceptable light | Ideal temperature | Acceptable temperature |
| --- | --- | --- | --- | --- |
| Virginia | 13–15 | 12–15 | 0.80–1.20 | 0.65–1.35 |
| Burley | 12–15 | 11–15 | 0.60–0.95 | 0.45–1.10 |
| Oriental | 14–15 | 13–15 | 1.30–2.00 | 1.00–2.00 |
| Dokha | 14–15 | 13–15 | 1.70–2.00 | 1.30–2.00 |
| Shade | 9–12 | 8–13 | 0.90–1.20 | 0.75–1.35 |
| Wild | 11–15 | 9–15 | 0.50–1.50 | 0.20–2.00 |

Temperature values are Minecraft biome base-temperature values.

## Biome preferences

- **Virginia:** Plains/Savanna +20; Forest +10; Desert/Badlands -15.
- **Burley:** Plains/Forest +20; Jungle +10; Desert/Badlands -15.
- **Oriental:** Desert/Badlands +20; Savanna +10; Jungle/Forest -15.
- **Dokha:** Desert +20; Badlands +10; Jungle/Forest -15.
- **Shade:** Jungle +20; Forest +10; Desert/Badlands/Savanna -15.
- **Wild:** most biomes +10; Desert/Badlands -5.

## Finding seeds and Tobacconist trades

**Wild Tobacco** is the natural starting point. Flowering wild tobacco can generate in Forest, Birch Forest, and Dark Forest and can provide wild leaves and Wild Tobacco Seeds. Normal Tobacconist villagers do not sell Wild seeds; a Wandering Trader can occasionally provide Wild seeds or Wild Flowering Tobacco as a backup source.

Cultivated varieties are obtained most reliably from **Tobacconist villagers**, whose workstation is a Hookah. When a villager takes the profession, its novice seed offers are chosen from the biome where it became a Tobacconist:

| Region | Cultivated seed offers |
| --- | --- |
| Plains / default / unknown modded biome | Virginia + Burley |
| Savanna | Virginia + Oriental |
| Desert | Dokha + Oriental |
| Badlands | Oriental + Dokha |
| Jungle | Shade + Burley |
| Forest | Burley + Shade |
| Taiga | Burley + Shade |
| Snowy | Burley + Virginia |

Higher profession levels add a compact progression of practical trades: Apprentices buy raw leaf from the primary regional variety and sell Rolling Paper; Journeymen sell a Clay Smoking Pipe and Tobacco Pouch; Experts sell Bamboo Charcoal and Plain Molasses; Masters sell a Tobacco Box and Tobacconist's Spectacles. A single village is intentionally not expected to supply every cultivated variety, so collecting all seeds rewards visiting different climates.

## Harvesting and automation

Breaking a mature tall crop handles the upper/lower halves as one plant so it does not duplicate drops. Create Mechanical Harvesters use the same quality generation and seed/drop rules as normal harvesting.

Use Tobacconist's Spectacles to inspect light, temperature, biome suitability, maturity, and potential quality in-world.
