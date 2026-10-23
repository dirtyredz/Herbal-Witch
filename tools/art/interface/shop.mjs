// The commerce screen atlas (Milestone 11), one 256 x 256 sheet in vanilla GUI units (the catalogue
// enlarges it 2x). Java blits these regions by 256-unit coordinates (client/stations/shop/ShopScreen):
//   (  0,   0) 252 x 200  the panel: wood frame, parchment, vine, a rule under the tabs (y 28) and above
//                         the notice line (y 178)
//   (  0, 204) 232 x  20  one list row: a pale strip with an 18 x 18 item frame at its left
//   (  0, 226)  18 x  18  a lone item frame (stock grid, inventory picker)
//   ( 20, 226)   9 x   9  available mark (green sprig)
//   ( 30, 226)   9 x   9  sold-out mark (plum bar)
//   ( 40, 226)   9 x   9  recovery mark (gold warning triangle)
import { C, Sheet, hex, screenPanel, ornament, slot } from './kit.mjs';
import { rng } from '../pixels.mjs';

const PLUM = hex('#6a2f5f');
const GOLD = hex('#e0b040');
const GOLD_LO = hex('#8a5e18');

function stamp(s, x0, y0, rows, key) {
  rows.forEach((row, y) => [...row].forEach((ch, x) => key[ch] && s.set(x0 + x, y0 + y, key[ch])));
}

const SPRIG = ['.....g...', '....gG...', '...gGg...', '..gGg.g..', '.gGg.gG..', '..g.gGg..', '...gGg...', '..s.g....', '.s.......'];
const SOLD = ['.........', '.........', '.........', 'ppppppppp', 'pPPPPPPPp', 'ppppppppp', '.........', '.........', '.........'];
const WARN = ['....y....', '...yGy...', '...yGy...', '..yyGyy..', '..yyGyy..', '.yyyyyyy.', '.yyyGyyy.', 'yyyyyyyyy', '.........'];

/** The commerce screen atlas. */
export const shop = () => {
  const s = new Sheet();
  const W = 252;
  const H = 200;
  screenPanel(s, W, H, rng(7311), 240);
  for (let x = 8; x < W - 8; x++) s.set(x, 28, x % 2 ? C.parchLo : C.line);
  for (let x = 8; x < W - 8; x += 2) s.set(x, H - 22, C.parchLo);
  ornament(s, W, H);
  // a list row: pale strip, darker rule beneath, an item frame at its left edge
  s.rect(0, 204, 232, 20, C.parchHi);
  for (let x = 0; x < 232; x++) s.set(x, 223, C.parchLo);
  slot(s, 1, 205);
  slot(s, 1, 227);
  stamp(s, 20, 226, SPRIG, { g: C.green, G: C.greenHi, s: C.woodLo });
  stamp(s, 30, 226, SOLD, { p: PLUM, P: C.pink });
  stamp(s, 40, 226, WARN, { y: GOLD, G: GOLD_LO });
  return s.image();
};
