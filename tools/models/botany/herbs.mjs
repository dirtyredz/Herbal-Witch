// Herb plant geometry: the planted cross for young stages (vanilla cross planes, rooted two pixels
// below the block so a plant sits in the soil of a herb bed, farmland or the ground).
import { box } from '../geometry.mjs';

const CROSS = { origin: [8, 8, 8], axis: 'y', angle: 45, rescale: true };

export function plantTemplates() {
  const plane = (from, to, faces) => box(from, to, '#cross', {
    rot: CROSS, shade: false, skip: ['up', 'down', ...faces], decal: { all: [0, 0, 16, 16] } });
  return [{
    path: 'block/botany/templates/planted_cross',
    textures: { particle: '#cross' },
    ao: false,
    open: ['cross'],
    cutout: true,
    elements: [
      plane([0.8, -2, 8], [15.2, 14, 8], ['east', 'west']),
      plane([8, -2, 0.8], [8, 14, 15.2], ['north', 'south']),
    ],
  }];
}
