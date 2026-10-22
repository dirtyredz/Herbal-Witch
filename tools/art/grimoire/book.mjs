// The grimoire item sprite: a closed witch's book seen at a slight angle. 16 x 16, alpha 0/255,
// outlined by the shared item kit. Thick dark plum leather cover, forest green spine band on the
// left, a gold clasp tab on the right edge, a gold crescent-and-leaf emblem, cream page edges along
// the bottom. Drawn from a character grid so the shape is readable in the source.
import { hex, set } from '../botany/sprites.mjs';
import { item } from '../preparations/materials.mjs';

const KEY = {
  P: hex('#4a2a55'), // plum leather
  p: hex('#6b3f7a'), // lit leather (top edge)
  d: hex('#3a1f44'), // shaded leather (right / lower edge)
  G: hex('#2f5a2c'), // spine band
  g: hex('#4a8040'), // spine highlight
  Y: hex('#e0b040'), // gold
  y: hex('#a87820'), // gold shadow
  C: hex('#efe4c6'), // page edge
  c: hex('#c9b88c'), // page edge line
};

const ROWS = [
  '................',
  '..GgpppppppppP..',
  '..GgPPPPPPPPPd..',
  '..GgPPPPPPPPPd..',
  '..GgPPPPYYPPPd..',
  '..GgPPPYPPPPPyY.',
  '..GGPPPYPPPPPYY.',
  '..GGPPPYPPPPPyY.',
  '..GgPPPPYYPPPd..',
  '..GgPPPPPPPPPd..',
  '..GgPPPPPPPPPd..',
  '..GGdddddddddd..',
  '..GGCCCCCCCCCC..',
  '...GcCcCcCcCcC..',
  '................',
  '................',
];

/** The closed grimoire, 16 x 16. */
export const book = item((cv) => {
  ROWS.forEach((row, y) => {
    [...row].forEach((ch, x) => {
      if (KEY[ch]) set(cv, x, y, KEY[ch], true);
    });
  });
});
