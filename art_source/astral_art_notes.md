> Alpha 12: the item sprite below was replaced by the supplied `galaxypipe.png`. This remains the historical generation record. The four custom sparkle textures are still used.

# Astral artwork — alpha 10

Generated with the built-in image-generation tool. Original outputs are retained here; runtime sprites are imported with nearest-neighbor sampling.

Runtime assets:
- `src/main/resources/assets/tobacconistmod/textures/item/astral_pipe.png` (16×16)
- `src/main/resources/assets/tobacconistmod/textures/particle/astral_sparkle_0.png` through `astral_sparkle_3.png` (16×16 each)

The four particle sprites are animated together with full-bright rendering, twinkle opacity and slow rotation. No dynamic lights are added. The item replaces the old Lapis Pipe placeholder. Other surviving novelty sprites remain temporary.

Item prompt:

Use case: precise-object-edit. Asset type: Minecraft 16x16 transparent item sprite. Edit target: the referenced existing pipe sprite; keep its silhouette, diagonal orientation, mouthpiece position, one-pixel margin and simple Minecraft item proportions. Replace the blue/gold placeholder treatment with a unique Astral Pipe: deep midnight-indigo body, restrained antique-silver bands, tiny cyan and lavender star inlays, one or two warm-white glints on its bowl. Keep recognizable hollow pipe bowl and slender stem, mouthpiece at upper-right and bowl at lower-left. Exact low-resolution pixel art: sixteen by sixteen logical square pixels, nearest-neighbor enlarged for output, limited palette, crisp square cells, no anti-aliasing, no blur, no floating particles outside the pipe, no text, no shadows, no background. Truly transparent background. This must remain readable as a small Minecraft inventory icon.

Particle atlas prompt:

Use case: stylized-concept. Asset type: Minecraft custom particle sprite atlas. Produce one square 32x32 logical-pixel atlas, nearest-neighbor enlarged, with four distinct 16x16 sprite cells in an exact 2-by-2 grid. Each cell centers one simple celestial sparkle. Top-left: bright four-point diamond star with warm white core, pale cyan long vertical/horizontal rays and tiny lavender corner pixels. Top-right: slightly smaller four-point lavender and silver twinkle. Bottom-left: tiny warm-white stardust sparkle with cyan tips. Bottom-right: small icy-white star with sparse purple satellite pixels, all inside its own cell. All four have clean square pixel steps and transparent empty space. This is actual sprite artwork, not a labeled diagram: no grid lines, borders, text, glow blur, anti-aliasing, background, other objects or gradients. Limited luminous white/cyan/lavender/pale-gold palette. Strong distinct silhouettes suitable for animated twinkling particles at 16x16. Output with a genuinely transparent background.
