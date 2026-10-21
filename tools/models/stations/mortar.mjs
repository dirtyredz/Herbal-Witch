// Mortar and pestle (owner reference: reference-mortar.png): a chunky mottled stone bowl, octagonal
// with chamfered corners, tapering to a narrow foot on a stepped square plinth, a stepped rim,
// moss and small pink flowers creeping over it, a heap of bright ground herbs inside, and a thick
// knob-headed stone pestle leaning out of the bowl. One block.
import { box } from '../geometry.mjs';
import { octagon, octagonTrue, octagonRing } from '../shapes.mjs';
import { STATION } from '../textures.mjs';

export function mortar() {
  const C = 8;
  const STONE = { side: '#stone', top: '#plinth', bottom: '#plinth' };
  const els = [
    // stepped square plinth and narrow foot
    box([1.5, 0, 1.5], [14.5, 1.5, 14.5], '#plinth'),
    box([2.75, 1.5, 2.75], [13.25, 2.25, 13.25], '#plinth', { skip: ['down'] }),
    ...octagon(C, C, 2.25, 3.5, 6.5, 6.5, 1.5, '#plinth', { skip: ['down', 'up'], third: false }),
    // the bowl, tapering inward toward the bottom, then straight chamfered walls
    ...octagonTrue(C, C, 3.5, 5, 9.5, 9.5, 2.5, STONE, { skip: ['down'] }),
    ...octagonTrue(C, C, 5, 6.75, 12, 12, 3.25, STONE, { skip: ['down'] }),
    ...octagonRing(C, C, 6.75, 10, 13.5, 13.5, 3.75, 2, STONE, { skip: ['down'] }),
    // the stepped rim
    ...octagonRing(C, C, 10, 11.25, 14.5, 14.5, 4.25, 2.5, STONE),
    // ground herbs heaped inside
    ...octagon(C, C, 6.75, 8.5, 10, 10, 2, '#herbs', { skip: ['down'], third: false }),
    box([5.25, 8.5, 5.5], [9.5, 9.5, 9.5], '#herbs', { skip: ['down'] }),
    box([6, 8.5, 9], [8.25, 10, 11], '#herbs', { skip: ['down'] }),
    box([9.5, 8.5, 6.25], [11.25, 9.25, 8], '#herbs', { skip: ['down'] }),
  ];
  // the pestle: an octagonal stone shaft with a knobbed head, leaning back out of the bowl
  const lean = { origin: [9.5, 6, 6.5], axis: 'x', angle: 22.5 };
  els.push(
    box([8.5, 6, 5.5], [10.5, 14, 7.5], '#pestle', { rot: lean }),
    box([8, 6, 6], [11, 14, 7], '#pestle', { rot: lean, skip: ['up', 'down'] }),
    box([9, 6, 5], [10, 14, 8], '#pestle', { rot: lean, skip: ['up', 'down'] }),
    box([7.5, 13.5, 4.5], [11.5, 16, 8.5], '#pestle', { rot: lean }),
    box([8, 13, 5], [11, 13.5, 8], '#pestle', { rot: lean, skip: ['up'] }),
  );
  return [{
    path: 'block/stations/mortar/base',
    textures: { plinth: STATION.plinth_stone, stone: STATION.mortar_stone, herbs: STATION.herb_pile, pestle: STATION.pestle_stone, particle: STATION.mortar_stone },
    elements: els,
  }];
}
