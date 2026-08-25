# Configuration and Debug Tools

## Server settings

The server config includes:

- `enableQualitySystem`: hides/ignores quality gameplay when disabled while preserving stored data.
- `enableNicotineEffects`: disables Tobacconist's Nicotine effect without disabling smoking or separately configured effects.
- `secretBlends`: defines hidden blend recipes.

The common config also provides `additionalEffects`, a list of extra smoking effects in `effect_id,duration,amplifier` form.

Secret blend component format is:

`variety|minQuality|cure|flavor`

Join 2–3 components with `+` and prefix them with `Display Name=`. Cure or flavor may use `*` as a wildcard. Flavor `none` explicitly requires unflavored tobacco. Legacy variety-only component definitions are still accepted as wildcards.

## Client setting

`particleDensity` controls the chance divisor for Curios mouth-slot ambient smoke. Lower values produce more smoke.

## Debug commands

All `/tobacconist` debug commands require command permission level 2. `/tobacconist debug` inspects the targeted tobacco crop, Drying Rack, Hanging Tobacco Bunch, or Tobacco Barrel.

The rack commands also recognize a targeted Hanging Tobacco Bunch (either half):

- `/tobacconist rack status`
- `/tobacconist rack addtime <ticks>`
- `/tobacconist rack finish`
- `/tobacconist debug`

Separate `/tobacconist hanging ...` curing commands remain available as well.

Barrel test commands are:

- `/tobacconist barrel ferment`
- `/tobacconist barrel age <days>`
- `/tobacconist barrel ruin`

`/tobacconist give help` documents the QA item generator for raw leaves, cured leaves, loose tobacco, blends, Cigarettes, Cigars, and Shisha with controlled metadata.

These commands are test utilities; ordinary gameplay should use the live processing conditions and Spectacles inspection.
