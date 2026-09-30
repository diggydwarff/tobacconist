# Tobacco Barrel

The Tobacco Barrel stores compatible tobacco batches and supports Pressing, Fermenting, Pressure Fermenting, Stoving, Aging, and Idle states. It preserves tobacco variety, cure, cut, flavor, blend, quality, and other processing data.

## Pressing

Rough Cut under valid pressure becomes Pressed Plug after the pressing cycle. Pressing is physical compression only: it does not mark tobacco fermented or aged. Vanilla pressure comes from a powered downward-facing piston whose extended head presses directly onto the barrel. Create Mechanical Presses can provide the automated equivalent.

## Fermentation

Ordinary fermentation requires sufficient warmth and internal barrel humidity. Pressure does not make generic fermentation special. **Pressure Fermenting** is only shown when the active specialty recipe actually requires pressure.

## Aging

Cool, dark storage ages tobacco by completed Minecraft days. Aging remains open-ended, so Create extraction should use age Attribute Filters when an automated cellar needs a specific target age.

## Visual behavior

The barrel is treated as a sealed processor/storage block and therefore emits no smoke while Pressing, Fermenting, Pressure Fermenting, Stoving, Aging, or Idle. Smoke effects remain on actual curing/fire/smoking sources.

## QA commands

- `/tobacconist barrel finish` - instantly complete the active finite process (Pressing, Fermenting, Pressure Fermenting, or Stoving).
- `/tobacconist barrel press` - instantly finish valid Rough Cut -> Pressed Plug.
- `/tobacconist barrel ferment` - force fermentation completion.
- `/tobacconist barrel age <days>` - add aging days.
- `/tobacconist barrel ruin` - ruin the targeted batch.
