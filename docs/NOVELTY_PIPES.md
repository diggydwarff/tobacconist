# Novelty and relic pipes — alpha 15

The Bubble Pipe and Confetti Pipe are refillable toys. Wizard's Longpipe,
Soulglass and Astral are rare exploration finds packed with normal tobacco.
All support handheld draws and the optional Curios mouth slot. Firefly Pipe
has been removed from registration, crafting, creative, tags and assets.

| Pipe | Obtain it | Refill or pack | Effects |
| --- | --- | --- | --- |
| Bubble Pipe | Crafting | Mundane Potion in the other hand, sneak-use; 24 blows | Floating bubbles only, no smoke |
| Confetti Pipe | Stick + Copper + Paper + Magenta Dye | Paper in the other hand; 24 blows | Always multicolored fluttering paper |
| Wizard's Longpipe | Dungeon / abandoned mineshaft chest | Normal loose tobacco or Tobacco Pouch | Real smoke rings alongside normal smoke; full draws release two |
| Soulglass Pipe | Nether fortress / bastion treasure chest | Normal loose tobacco or Tobacco Pouch | Curling cyan soul wisps and glass glints alongside normal smoke |
| Astral Pipe | End City treasure chest | Normal loose tobacco or Tobacco Pouch | Random celestial sparkles; maximum puff adds one of 20 constellations plus surrounding sparkles, alongside smoke |
| Petal Pipe | Stick + Clay Ball + Small Flower | One small flower in the other hand; 24 blows | Drifting cherry petals; no smoke or tobacco effects |
| Whistling Pipe | Stick + Copper Ingot + Glass Bottle | One Water Bottle in the other hand; 24 whistles, returns the glass | Rising colored notes; short draws whistle, full draws play a five-note melody |
| Sunkeeper’s Pipe | Desert Pyramid chest | Normal loose tobacco or Tobacco Pouch | Warm actual-smoke sunburst, tiny bright gold sparks, and ordinary smoke |
| Echo Pipe | Ancient City chest | Normal loose tobacco or Tobacco Pouch | Layered blue-violet ripples, traveling highlights, drifting light motes, and a soft chime alongside ordinary smoke |

Confetti is always multicolored. Maximum bursts release 96 saturated paper pieces over four ticks using a seven-color palette;
short and idle bursts use a rotating subset. It does not accept dyes or emit
smoke. Older stored dye colors do not affect its output. Paper refills an empty
pipe on ordinary use; sneak-use tops up a partially loaded pipe. A refill
consumes one Paper and supplies 24 blows. Crafting/creative entries are loaded.

Held toy draws last 4–30 ticks and spend one charge. A deliberate Curios mouth-key tap of at least one tick also works, producing a small puff; hold for 30 ticks to reach maximum. Cigalike reaches maximum at 40 ticks. Zero-time releases cancel. Cancelled, empty,
invalid or cooldown-blocked attempts spend nothing. Draw-animation-disabled
mode supports the ordinary instant-use fallback. Bubble/Confetti particles
never apply nicotine or tobacco effects.

Relics accept loose tobacco directly from the other hand, or use the normal
tobacco-in-main/pipe-in-offhand and Tobacco Pouch routes. Packing retains
metadata, quality/flavor effects and pouch puff bonuses. Each deliberate draw
spends one normal puff. Relics are not craftable or sold by villagers.

Wizard rings now consist of actual CampfireSmokeParticle sections using the
same twelve big-smoke sprites as ordinary tobacco smoke. Full draws create two
closed 32-section rings eight ticks apart. Stronger seeded radius ripples, mild oval shapes and varied section thicknesses, opacity and breakup times soften the perfect circle. The accompanying ordinary smoke plume is smaller, without weakening the rings. Sections share their initial motion,
expand with a visible hole, then separate into rising smoke. Reduced particle
settings use sixteen evenly spaced sections per ring. Create fan airflow moves
or captures the smoke. This replaces the previous custom ring billboard.

Astral has its own indigo/silver item sprite and four custom 16x16 sparkle
textures. Sparkles animate between the sprites, twinkle, slowly rotate and
render full-bright without dynamic lighting. Only a maximum draw forms a constellation, selected from 20 sky silhouettes. Its stars and dotted links share a stable drift so the drawing stays recognizable. Random animated sparkles also surround the full drawing; all shorter draws and idle effects use scattered sparkles only.
Soulglass releases uneven curling silver-blue wisps and tiny silver, ice-blue and pale lavender glints, with a few familiar native soul flames restored. A maximum draw releases three staggered curls. Wisps rise, expand and fade; glints pulse and rotate. Source artwork and prompts
are recorded in `art_source/astral_art_notes.md`.

## Curios rendering

All supported mouth items retain explicit Curios capabilities. A direct Tobacconist player layer, registered explicitly on the client mod event bus, draws mouth equipment on both wide and slim player models, restoring the original cigarette/cigar path and extending it to the newer items. Native Curios item renderer factories are no longer registered, so there is one mouth draw path and no double rendering. Cigalike and specialty sprites use anchored mouthpieces and identity FIXED transforms. Specialty pipes use exactly the wooden pipe’s final rendered angle, including its inherited Minecraft FIXED rotation, while keeping each sprite’s mouthpiece anchored to the lips. Petal uses a 16x16 sprite and an adjusted anchor; its original 17x17 artwork is preserved in art_source.

The visible-slot reader handles cosmetic overrides, functional render toggles, and additional mouth indices. Missing render flags default to visible as in the original renderer. Cosmetics only display cosmetic effects; the mouth key consumes functional contents. A one-time alpha 13 migration restores the mouth render flag and synchronizes it through Curios when upgrading an existing player. Later hide/show choices remain intact. Removing an item or hiding its functional slot stops its model and idle effects.

Client idle effects use the current synchronized visible stack, so a heartbeat for the previous item cannot give a newly equipped pipe the wrong particles. Server profiles still refresh on change and once per second.

## Idle mouth effects

Loaded, visible mouth items produce small cosmetic effects while equipped:
normal cigarettes/cigars/pipes produce smoke; Cigalike produces a tiny VG-scaled
vapor plume; Bubble produces only bubbles; Confetti produces a few mixed
paper pieces; Wizard produces smoke and occasional mini rings; Soulglass and
Astral produce smoke with a few signature wisps/sparkles. Petal and Whistling emit a few petals/notes without sound while idle. Sunkeeper emits smoke with a small warm flourish; Echo emits smoke with a tiny blue pulse.

Specialty effects now arrive in brief puffs every 6–8 seconds, with quiet gaps between them. Nearby players have staggered timing. Smoke and signature particles share the same short burst; Wizard adds one miniature ring per burst. Minimal/sparse particle settings reduce each puff. Deliberate draws retain their existing effects and controls.

Idle emission does not consume puffs, liquid, battery or toy charges, apply
smoking effects or award advancements. Empty items and a flat Cigalike battery
stop it. It pauses during deliberate draws/use, honors Curios visibility,
player invisibility, particle settings and density configuration, and uses the
player's eye/head position when crouching or looking up/down. Nearby players use their synchronized equipment for current item/loaded state, supported by server change updates and one-second heartbeats. Stale states expire after three seconds; removing/depleting an item clears the state. Cosmetic emission itself stays local. Emission is limited to 64
particles per client tick within 64 blocks. Full novelty bursts retain their
96-per-tick budget and bounded queue/lifetimes.

## Loot, milestones and artwork

Wizard has a default 4% chance in eligible dungeon/mineshaft chests. Soulglass
and Astral have a default 1.5% chance in their listed treasure chests. Loot adds
at most one relic without replacing normal loot. Existing unlooted chests can
qualify; generated inventories are not rewritten. Server settings remain in
`tobacconistmod-novelties-server.toml`: `enableRelicLoot`, `rarePipeChance`,
`mythicPipeChance`, and `enableNoveltySounds`.

Curio Collector awards any first relic, including the new two; Ringmaster requires a successful full
Wizard draw; Across the Realms remembers collecting the original three relics. Hidden Histories is a separate milestone for discovering Sunkeeper and Echo; existing progress is preserved.

Supplied artwork is installed for all nine pipes. Eight PNGs are copied unchanged; Petal is normalized to 16x16 with nearest-neighbor sampling. The magenta/brass `bubblepipe(1).png` is actually Confetti; Bubble is restored to the original `bubblepipe.png`. Galaxy maps to Astral. All runtime pipe sprites are 16x16. Source mappings are recorded in `art_source/novelty_pipe_placeholder_notes.md`.

Whistling now plays at volume 0.65 through the Players sound category. A full draw releases one of two five-note flute phrases over 18 ticks; a short draw gives one note. Idle notes remain silent. The novelty-sound setting disables melodies as well as other signature sounds. Queued music cancels on death, logout, level change or disabling sounds.

Install matching alpha 15 client/server JARs (network version 14). See the focused test checklist for rendering, mouth-key consumption, effects and sound checks.
