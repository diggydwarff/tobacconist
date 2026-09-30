# Haunted Harvest Update — Implementation Notes

This document describes the 1.21.1 implementation added in this source patch.

## Design rules

The player-facing tobacco metadata stays simple. Specialty results replace the normal cure label, e.g. `Cure: Latakia` or `Cure: Perique`. There is no separate secondary-processing tooltip.

`Haunted` is a true raw-leaf cure and is intentionally discoverable rather than explained by the in-game manual. The traditional named styles build on existing tobacco/processing states.

## Processing routes

| Result | Vanilla route | Create equivalent |
| --- | --- | --- |
| Haunted | Raw leaf on rack/hanging bunch over a lit Soul Campfire | Haunting airflow from an Encased Fan |
| Latakia | Sun-Cured Oriental returned to a rack/bunch for an extended ordinary-smoke finish | Encased Fan smoking airflow |
| Dark Fired Kentucky | Fire-Cured Burley returned for an extended ordinary-smoke finish | Encased Fan smoking airflow |
| Pressed Plug | Rough Cut in Tobacco Barrel + powered downward Piston pressure. This is **Pressing**, not fermentation or aging. | Running Mechanical Press provides pressure; normal press recipes also output Plug |
| Perique | Intact Air-Cured Burley + warm/humid barrel + pressure | Running Mechanical Press may provide pressure |
| Cavendish | Pressed Plug + warm/humid barrel + pressure + Flue Firebox heat | Mechanical Press + heated Blaze Burner |
| Black Cavendish | Cavendish through a second heated pressure-fermentation cycle | Mechanical Press + heated Blaze Burner |
| Stoved Virginia | Flue-Cured Virginia + prolonged Flue Firebox barrel heat | Heated Blaze Burner |
| Flake | Slice Pressed Plug with Chaveta | Deployer holding Chaveta |

## Barrel modes

Barrel status now distinguishes the physical process taking place:

- `Pressing` — Rough Cut is being physically compressed into Pressed Plug. This requires pressure only and does **not** mark the tobacco fermented or aged.
- `Fermenting` — ordinary warm/humid fermentation without pressure.
- `Pressure Fermenting` — reserved for fermentation recipes where pressure is actually required, such as Perique and Cavendish. Ordinary fermentation remains `Fermenting` even if a piston/Mechanical Press happens to be present.
- `Stoving` — prolonged low-heat specialty processing.
- `Aging` — cool/dark cellar aging.

Barrel processing is visually sealed: **the Tobacco Barrel itself emits no smoke particles in any mode**. Smoke remains associated with curing racks, campfires, Flue Fireboxes, and Haunted soul-fire curing.

After Plug finishes pressing, it simply remains a Plug. It can later age if the barrel is in aging conditions, or enter a separate specialty fermentation route when those requirements are deliberately met.

## Seasonal content

The six built-in autumn secret blends are intentionally hidden from Patchouli. Two are non-aromatic (`Witching Hour`, `Graveyard Watch`); the remaining four mix base-game flavor profiles and optional mod-supplied autumn profiles. They do not receive legendary blend status-effect bonuses.

New flavor profiles are `maple`, `nutmeg`, `ginger`, `clove`, and `pecan`. Ingredient lookup is tag-first and external references are optional, so the mod remains loadable without the supplying food mods.

## Haunted presentation

Haunted tobacco does not gain a direct quality/power advantage. Its distinguishing behavior is recipe access and presentation: normal tobacco smoke is mixed with restrained soul-fire wisps, and the Haunted state is detected through loose tobacco, blends, packed product data, and cigar wrapper data. Finished cigarettes and cigars containing Haunted tobacco are named Haunted Cigarette / Haunted Cigar while preserving aromatic, blend, and custom-label naming.

## Seasonal smoking equipment

The **Jack-o'-Lantern Hookah** is a two-block seasonal Hookah built from a standard Hookah and a Jack o'Lantern. It uses the supplied pumpkin model/texture and otherwise follows normal Hookah inventory, fuel, water, Shisha, hose, automation, and Display Link behavior.

The **Corn Cob Pipe** is a reusable harvest-season pipe. It uses the normal pipe packing, pouch bonus, puff counter, tobacco metadata, and Curios Mouth-slot behavior.

## Backport plan

Do not backport this patch to 1.20.1 yet. First build and test the 1.21.1 implementation in the full Gradle/NeoForge project. Once verified, use that finished source as the basis for the 1.20.1 Forge backport.

### Barrel QA command

While looking at an active Tobacco Barrel, `/tobacconist barrel finish` immediately completes the current Pressing, Fermenting, Pressure Fermenting, or Stoving process. `/tobacconist barrel press` remains available for specifically forcing a valid Rough Cut → Pressed Plug operation.

## Barrel visual behavior

Fermenting, Pressure Fermenting, Pressing, Stoving, Aging, and Idle barrels do not generate smoke particles. This is intentional; the barrel is a sealed processing/storage block. Haunted/special smoke presentation stays on curing and smoking sources.
