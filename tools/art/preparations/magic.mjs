// Charm and ritual sprites (Milestone 7): the protective sachet, the undead and arthropod amulets and
// the ritual circle kit. 16 x 16, alpha 0/255, outlined, drawn with the kit in preparations/materials.mjs.
// Exports MAGIC as [[path relative to textures/item/, sprite function]].
import { hex, set, line, leaf, R } from '../botany/sprites.mjs';
import { P, rect, spans, item } from './materials.mjs';

const ramp = (a, b, c) => [hex(a), hex(b), hex(c)];
const GOLD = ramp('#8a5a14', '#d8a62c', '#f6dc6a');
const CORD = ramp('#2a1a10', '#4a3220', '#6b4a2f');

/** A linen sachet tied with pink twine, a sprig of lavender and a sage leaf peeking from the neck. */
const sachet = item((cv) => {
  for (const x of [7, 9]) {
    line(cv, x, 5, x, 1, R.grey[1]);
    set(cv, x, 1, R.violet[2]);
    set(cv, x, 2, R.violet[1]);
    set(cv, x, 3, R.violet[0]);
  }
  set(cv, 8, 2, R.violet[1]);
  leaf(cv, 6, 5, 140, 4, 1, R.sage);
  spans(cv, [
    [5, 7, 9, P.cream[1]],
    [6, 6, 10, P.cream[2]],
    [8, 4, 11, P.cream[1]],
    ...Array.from({ length: 5 }, (_, i) => [9 + i, 3, 12, P.cream[i < 2 ? 1 : 0]]),
    [14, 4, 11, P.cream[0]],
  ]);
  rect(cv, 5, 7, 10, 7, P.pink[1], false); // the twine
  set(cv, 4, 8, P.pink[2]);
  set(cv, 11, 8, P.pink[2]);
  set(cv, 4, 9, P.pink[0]);
  set(cv, 11, 9, P.pink[0]);
  set(cv, 10, 7, P.pink[2], false);
  for (const x of [5, 7, 9, 11]) set(cv, x, 13, P.pink[0], false); // a stitched hem
  leaf(cv, 8, 12, 90, 3, 1, R.sage); // an embroidered sage leaf
});

/** A gold pendant on a dark cord: `rows` is the pendant's outline, `stone` the gem ramp. */
function amulet(cv, rows, stone, gem) {
  line(cv, 3, 0, 7, 4, CORD[1]);
  line(cv, 12, 0, 8, 4, CORD[1]);
  set(cv, 7, 4, CORD[0]);
  set(cv, 8, 4, CORD[0]);
  for (const [y, x0, x1] of rows) {
    for (let x = x0; x <= x1; x++) set(cv, x, y, y < 8 ? GOLD[2] : y > 11 ? GOLD[0] : GOLD[1], true);
  }
  for (const [x, y, c] of gem) set(cv, x, y, stone[c], false);
}

// a round sun medallion around a bone-white stone
const undead = item((cv) => {
  amulet(cv, [[5, 7, 8], [6, 5, 10], [7, 4, 11], [8, 3, 12], [9, 3, 12], [10, 3, 12], [11, 3, 12], [12, 4, 11], [13, 5, 10], [14, 7, 8]],
    ramp('#bfb59a', '#e8e0c8', '#fffdf2'),
    [[7, 8, 1], [8, 8, 2], [6, 9, 1], [7, 9, 2], [8, 9, 1], [9, 9, 1], [6, 10, 0], [7, 10, 1], [8, 10, 1], [9, 10, 0], [7, 11, 0], [8, 11, 0]]);
});

// a broad diamond amulet with side studs around an amber stone
const arthropod = item((cv) => {
  amulet(cv, [[5, 7, 8], [6, 6, 9], [7, 5, 10], [8, 4, 11], [9, 3, 12], [10, 4, 11], [11, 5, 10], [12, 6, 9], [13, 7, 8], [14, 7, 8]],
    P.amber, [[7, 8, 1], [8, 8, 2], [6, 9, 1], [7, 9, 2], [8, 9, 1], [9, 9, 0], [7, 10, 0], [8, 10, 0], [6, 10, 1]]);
  set(cv, 4, 9, GOLD[2]);
  set(cv, 11, 9, GOLD[0]);
});

/** A ring of carved charcoal stones seen from above, a pink rune glowing on every stone. */
const circle = item((cv) => {
  const stone = ramp('#1c1a20', '#34323c', '#5a5666');
  const c = 7.5;
  for (let y = 0; y < 16; y++) {
    for (let x = 0; x < 16; x++) {
      const dx = x - c;
      const dy = y - c;
      const d = Math.hypot(dx, dy);
      if (d > 7.4 || d < 3.4) continue;
      const a = ((Math.atan2(dy, dx) * 180) / Math.PI + 360 + 22.5) % 360; // sector origin at -22.5 deg
      const seam = a % 45 < 3.2 && d > 4;
      const lit = dx + dy < -1.5;
      const dim = dx + dy > 3;
      set(cv, x, y, seam ? stone[0] : lit ? stone[2] : dim ? stone[0] : stone[1], true);
    }
  }
  const rune = ramp('#ff3a8c', '#ff7ab4', '#ffd0e6');
  for (let k = 0; k < 8; k++) {
    const a = (k * 45 * Math.PI) / 180;
    set(cv, Math.round(c + Math.cos(a) * 5.4), Math.round(c + Math.sin(a) * 5.4), k % 2 ? rune[1] : rune[0], false);
    if (k % 2 === 0) set(cv, Math.round(c + Math.cos(a) * 5.9), Math.round(c + Math.sin(a) * 5.9), rune[2], false);
  }
});

/** [path relative to textures/item/, sprite function] for the charms and the ritual circle. */
export const MAGIC = [
  ['magic/charms/protective_sachet.png', sachet],
  ['magic/charms/undead_amulet.png', undead],
  ['magic/charms/arthropod_amulet.png', arthropod],
  ['magic/ritual/circle.png', circle],
];
