# Novelty pipe sprite sources — alpha 13

All nine pipe sprites now use supplied artwork. Eight are copied byte-for-byte; Petal uses nearest-neighbor normalization to 16x16:

| Runtime sprite | Supplied file | Size |
| --- | --- | --- |
| bubble_pipe.png | bubblepipe.png (original restored) | 16x16 |
| confetti_pipe.png | bubblepipe(1).png (corrected assignment) | 16x16 |
| wizards_longpipe.png | wizxardpipe.png | 16x16 |
| soulglass_pipe.png | soulglasspipe.png | 16x16 |
| astral_pipe.png | galaxypipe.png | 16x16 |
| petal_pipe.png | petalpipe.png (normalized; original retained) | 16x16 |
| whistling_pipe.png | whistlepipe.png | 16x16 |
| sunkeepers_pipe.png | sunkeeperpipe.png | 16x16 |
| echo_pipe.png | echopipe.png | 16x16 |

Petal was supplied as 17x17. The client warned that this reduces the entire texture atlas mip level to zero. Its runtime image is normalized to 16x16 with nearest-neighbor sampling and an adjusted mouthpiece anchor; no colors are interpolated. The exact original is preserved as art_source/petal_pipe_original.png. No placeholder item sprites remain in this collection. The direct player mouth layer uses identity FIXED transforms for specialty sprites. Keep future replacements oriented with bowl lower left and mouthpiece upper right.
