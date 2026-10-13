# Vape casing dye layers

Undyed models still use the original supplied 16x16 sprites without alteration.
A `tobacconistmod:dyed` item predicate selects a second model when the native
`minecraft:dyed_color` component is present. Its layer 0 is a neutral casing
mask; layer 1 contains the original untinted hardware pixels.

The masks were derived directly from the supplied sprite pixels. They retain
the original silhouette and transparency, with brighter neutral casing values
to keep dye colors readable. Mouthpieces, glass, screens, indicator lights,
metal collars, and dark outlines remain on the untinted overlay. Both models
use the same display transforms, including the existing mouth-slot handling.
