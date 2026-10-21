// The armchair (owner reference: reference-armchair.png, also the vine standard for furniture):
// a dark brown frame with chunky square corner posts capped above the arms and back, magenta
// inlays and thin gold and plum bands on the posts; a forest-green velvet seat and tall back with
// small magenta tufting studs and magenta piping; draped arm runners and a head runner with pointed
// hems; dense trailing ivy with small pink flowers climbing the back posts, spilling over the top
// rail and winding down the front posts and arms at varied lengths.
import { box } from '../geometry.mjs';
import { band, ivyDrape, rivet } from '../shapes.mjs';
import { DECOR } from './textures.mjs';

function post(x0, z0, top, capped) {
  const x1 = x0 + 2.5;
  const z1 = z0 + 2.5;
  const els = [
    box([x0, 1.5, z0], [x1, top, z1], '#wood_v'),
    box([x0 - 0.25, 0, z0 - 0.25], [x1 + 0.25, 1.5, z1 + 0.25], '#wood'), // foot
    band(x0, z0, x1, z1, 2, 2.5, '#gold'),
    band(x0, z0, x1, z1, 2.5, 3, '#plum'),
  ];
  if (capped) els.push(box([x0 - 0.25, top, z0 - 0.25], [x1 + 0.25, top + 1, z1 + 0.25], '#wood'));
  return els;
}

export function armchair() {
  const els = [
    // posts: front pair capped above the arms, back pair above the back
    ...post(1, 1, 11.5, true), ...post(12.5, 1, 11.5, true),
    ...post(1, 12.5, 15, true), ...post(12.5, 12.5, 15, true),
    // magenta inlays and gold accents on the front posts
    box([1.75, 9, 0.75], [2.75, 10, 1], '#pink'), box([13.25, 9, 0.75], [14.25, 10, 1], '#pink'),
    rivet('north', 2.25, 6.5, 1, '#gold', 0.75, 0.25), rivet('north', 13.75, 6.5, 1, '#gold', 0.75, 0.25),
    // seat frame and velvet cushion with magenta piping and gold tabs
    box([3.5, 2, 1.25], [12.5, 5, 13], '#wood'),
    box([4.5, 2.75, 1], [11.5, 4.25, 1.25], '#plum', { skip: ['up', 'down', 'south'] }), // carved panel
    box([3.5, 5, 1.75], [12.5, 7.75, 12.5], '#velvet'),
    box([3.5, 7.25, 1.5], [12.5, 7.75, 1.75], '#pink', { skip: ['south'] }),
    rivet('north', 5.5, 6.5, 1.75, '#gold', 0.75, 0.25), rivet('north', 10.5, 6.5, 1.75, '#gold', 0.75, 0.25),
    // the tall back: rails, cushion, piping
    box([3.5, 14, 12.75], [12.5, 15.75, 15], '#wood'),
    box([3.75, 7.75, 11.5], [12.25, 14, 13.75], '#velvet'),
    box([5.25, 8, 11.35], [5.75, 13.75, 11.5], '#pink', { skip: ['south', 'up', 'down'] }),
    box([10.25, 8, 11.35], [10.75, 13.75, 11.5], '#pink', { skip: ['south', 'up', 'down'] }),
    // arms over carved side panels
    box([1, 10, 3.5], [3.5, 11.5, 12.5], '#wood'), box([12.5, 10, 3.5], [15, 11.5, 12.5], '#wood'),
    box([1.25, 3, 3.5], [3.25, 10, 12.5], '#wood'), box([12.75, 3, 3.5], [14.75, 10, 12.5], '#wood'),
    // draped runners: over each arm and down its outside, and over the back rail
    box([0.75, 11.5, 5], [3.75, 11.75, 9.5], '#runner', { decal: { all: [0, 0, 16, 5] } }),
    box([0.6, 7, 5], [0.75, 11.75, 9.5], '#runner', { decal: { west: [0, 2, 16, 16], east: [0, 2, 16, 16] }, skip: ['up', 'down', 'north', 'south'] }),
    box([12.25, 11.5, 5], [15.25, 11.75, 9.5], '#runner', { decal: { all: [0, 0, 16, 5] } }),
    box([15.25, 7, 5], [15.4, 11.75, 9.5], '#runner', { decal: { west: [0, 2, 16, 16], east: [0, 2, 16, 16] }, skip: ['up', 'down', 'north', 'south'] }),
    box([6, 15.75, 12.5], [10, 16, 15.25], '#runner', { decal: { all: [0, 0, 16, 5] } }),
    box([6, 10.5, 12.35], [10, 16, 12.5], '#runner', { decal: { north: [0, 2, 16, 16], south: [0, 2, 16, 16] }, skip: ['up', 'down', 'east', 'west'] }),
  ];
  // ivy: up the back posts, over the top rail, down the front posts and arms, dangling past the legs
  els.push(
    ...ivyDrape({ face: 'north', at: 12.5, along: [0.75, 3.75], top: 16, lengths: [9, 14] }),
    ...ivyDrape({ face: 'north', at: 12.5, along: [12.25, 15.25], top: 16, lengths: [13, 7] }),
    ...ivyDrape({ face: 'north', at: 12.75, along: [3.5, 6], top: 15.5, lengths: [5, 3], clump: false }),
    ...ivyDrape({ face: 'east', at: 15.25, along: [12.5, 15], top: 15.5, lengths: [15], clump: false }),
    ...ivyDrape({ face: 'north', at: 1, along: [0.75, 3.75], top: 12.5, lengths: [7, 11] }),
    ...ivyDrape({ face: 'west', at: 0.75, along: [9.5, 12.5], top: 11.5, lengths: [10], clump: false }),
    ...ivyDrape({ face: 'east', at: 15.25, along: [1, 3.5], top: 12.5, lengths: [12], clump: false }),
  );
  return [{ path: 'block/decor/templates/armchair', textures: { ...DECOR, particle: DECOR.wood }, elements: els }];
}
