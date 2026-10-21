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
    box([3, 0, 3], [13, 1.5, 13], '#plinth'),
    box([4, 1.5, 4], [12, 2.25, 12], '#plinth', { skip: ['down'] }),
    ...octagon(C, C, 2.25, 3.25, 6.5, 6.5, 1.5, '#plinth', { skip: ['down', 'up'], third: false }),
    // the chunky bowl, tapering inward toward the bottom, then straight chamfered walls
    ...octagonTrue(C, C, 3.25, 4.75, 10, 10, 2.75, STONE, { skip: ['down'] }),
    ...octagonTrue(C, C, 4.75, 6.5, 12.75, 12.75, 3.5, STONE, { skip: ['down'] }),
    ...octagonRing(C, C, 6.5, 11.25, 14.5, 14.5, 4, 2.25, STONE, { skip: ['down'] }),
    // the stepped rim
    ...octagonRing(C, C, 11.25, 12.75, 15.5, 15.5, 4.5, 2.75, STONE),
    // ground herbs heaped inside
    ...octagon(C, C, 6.5, 8.75, 10.5, 10.5, 2, '#herbs', { skip: ['down'], third: false }),
    box([5, 8.75, 5.5], [9.5, 9.75, 9.5], '#herbs', { skip: ['down'] }),
    box([5.75, 8.75, 9], [8.25, 10.5, 11.25], '#herbs', { skip: ['down'] }),
    box([9.5, 8.75, 6], [11.5, 9.5, 8], '#herbs', { skip: ['down'] }),
  ];
  // the pestle: an octagonal stone shaft with a knobbed head, leaning back out of the bowl
  const lean = { origin: [9.5, 7, 7], axis: 'x', angle: 22.5 };
  els.push(
    box([8.25, 7, 5.75], [10.75, 14, 8.25], '#pestle', { rot: lean }),
    box([7.75, 7, 6.25], [11.25, 14, 7.75], '#pestle', { rot: lean, skip: ['up', 'down'] }),
    box([8.75, 7, 5.25], [10.25, 14, 8.75], '#pestle', { rot: lean, skip: ['up', 'down'] }),
    box([7.5, 13.25, 4.75], [11.5, 16, 8.75], '#pestle', { rot: lean }),
    box([8, 12.75, 5.25], [11, 13.25, 8.25], '#pestle', { rot: lean, skip: ['up'] }),
  );
  return [{
    path: 'block/stations/mortar/base',
    textures: { plinth: STATION.plinth_stone, stone: STATION.mortar_stone, herbs: STATION.herb_pile, pestle: STATION.pestle_stone, particle: STATION.mortar_stone },
    elements: els,
  }];
}
