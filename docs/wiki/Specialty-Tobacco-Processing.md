# Specialty Tobacco Processing

Tobacconist keeps the player-facing metadata simple: specialty results replace the normal **Cure** label instead of adding a separate technical processing field.

| Result | Vanilla process | Create equivalent |
| --- | --- | --- |
| Pressed Plug | Rough Cut in a Tobacco Barrel under an extended downward-facing piston | Running Mechanical Press |
| Flake | Slice Pressed Plug with a Chaveta | Deployer holding a Chaveta |
| Perique | Air-Cured Burley + warm/humid barrel + required pressure | Mechanical Press may provide pressure |
| Cavendish | Pressed Plug + warm/humid barrel + pressure + Flue Firebox heat | Mechanical Press + heated Blaze Burner |
| Black Cavendish | Cavendish through a second heated pressure-fermentation cycle | Mechanical Press + heated Blaze Burner |
| Stoved Virginia | Flue-Cured Virginia + prolonged Flue Firebox barrel heat | Heated Blaze Burner |
| Latakia | Sun-Cured Oriental + extended ordinary-smoke finish on rack/bunch | Encased Fan smoking airflow |
| Dark Fired Kentucky | Fire-Cured Burley + extended ordinary-smoke finish on rack/bunch | Encased Fan smoking airflow |

## Barrel modes

- **Pressing**: physical compression of Rough Cut into Pressed Plug. It does not ferment or age the tobacco.
- **Fermenting**: ordinary warm/humid fermentation. Incidental pressure does not rename it.
- **Pressure Fermenting**: only for recipes that specifically require pressure, including Perique and Cavendish processing.
- **Stoving**: prolonged controlled-heat specialty processing.
- **Aging**: cool/dark cellar aging.
- **Idle**: no valid process is active.

The barrel itself emits **no smoke particles** in any mode. Visible smoke belongs to curing/fire sources, not sealed barrel processing.

## Vanilla Plug pressing

1. Put Rough Cut tobacco into a Tobacco Barrel.
2. Leave one block of piston travel above the barrel.
3. Place a downward-facing Piston two blocks above the barrel.
4. Power it so the extended piston head directly contacts the top of the barrel.
5. Keep pressure applied until the barrel finishes Pressing.

For QA, `/tobacconist barrel press` instantly completes a valid Rough Cut -> Pressed Plug batch in the targeted barrel.
