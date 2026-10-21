// Turning one authored assembly into cells and states: explicit (flipped, rotated) UVs cropped with
// the cut, pivots moved with their cell, cut faces omitted, the stove's overflow kept, overlays only
// where they belong, the idle item assembly, and generated attachments at their anchors.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { cropUv, splitCells, rectangle, multiblockOutputs, singleOutputs, shift } from '../assembly.mjs';
import { expandAttachment } from '../attachments.mjs';
import { bakeElement } from '../../preview/geometry/elements.mjs';
import { compileAll } from '../compile.mjs';
import { cube, group, projectText, simplePiece, tempRoot, SOLID } from './helpers.mjs';

const near = (a, b) => Math.abs(a - b) < 1e-6;

/** Independent oracle: the uv a face of `e` shows at point p (Minecraft's corner order and rotation). */
function uvAt(e, face, p) {
  const [x0, y0, z0] = e.from;
  const [x1, y1, z1] = e.to;
  const k = (v, a, b) => (b === a ? 0 : (v - a) / (b - a));
  const st = {
    north: [1 - k(p[0], x0, x1), 1 - k(p[1], y0, y1)], south: [k(p[0], x0, x1), 1 - k(p[1], y0, y1)],
    west: [k(p[2], z0, z1), 1 - k(p[1], y0, y1)], east: [1 - k(p[2], z0, z1), 1 - k(p[1], y0, y1)],
    up: [k(p[0], x0, x1), k(p[2], z0, z1)], down: [k(p[0], x0, x1), 1 - k(p[2], z0, z1)],
  }[face];
  const [s, t] = st;
  const [rs, rt] = { 0: [s, t], 90: [t, 1 - s], 180: [1 - s, 1 - t], 270: [1 - t, s] }[e.faces[face].rotation ?? 0];
  const uv = e.faces[face].uv;
  return [uv[0] + (uv[2] - uv[0]) * rs, uv[1] + (uv[3] - uv[1]) * rt];
}

test('a cropped face shows exactly the texels it showed before the cut, for every UV rotation and flip', () => {
  for (const rotation of [0, 90, 180, 270]) {
    for (const uv of [[2, 3, 14, 11], [14, 3, 2, 11], [2, 11, 14, 3]]) {
      const faces = Object.fromEntries(['north', 'south', 'west', 'east', 'up', 'down'].map((f) => [f, { uv, texture: '#t', ...(rotation ? { rotation } : {}) }]));
      const whole = { from: [-6, 2, 3], to: [10, 14, 13], faces };
      const piece = { from: [0, 5, 4], to: [7, 14, 9] };
      for (const f of Object.keys(faces)) {
        const cropped = { ...piece, faces: { [f]: { ...faces[f], uv: cropUv(f, faces[f].uv, rotation, whole, piece) } } };
        const [q] = bakeElement(cropped, () => 't');
        q.pos.forEach((p, i) => {
          const want = uvAt(whole, f, p);
          assert.ok(near(q.uv[i][0], want[0]) && near(q.uv[i][1], want[1]), `${f} rot ${rotation} uv ${uv}: ${q.uv[i]} vs ${want}`);
        });
      }
    }
  }
});

test('splitting moves pieces and pivots into their cells and omits the faces the cut creates', () => {
  const faces = Object.fromEntries(['north', 'south', 'west', 'east', 'up', 'down'].map((f) => [f, { uv: [0, 0, 8, 8], texture: '#t' }]));
  const lean = { from: [-4, 0, 6], to: [4, 12, 10], faces, rotation: { angle: 22.5, axis: 'x', origin: [0, 0, 8] } };
  const [cell0, cell1] = splitCells([lean], rectangle(2, 1));
  assert.deepEqual([cell0[0].from, cell0[0].to], [[0, 0, 6], [4, 12, 10]]);
  assert.deepEqual([cell1[0].from, cell1[0].to], [[12, 0, 6], [16, 12, 10]]);
  assert.deepEqual(cell0[0].rotation.origin, [0, 0, 8]);
  assert.deepEqual(cell1[0].rotation.origin, [16, 0, 8]); // the same pivot, in cell 1's coordinates
  assert.ok(!('west' in cell0[0].faces) && !('east' in cell1[0].faces), 'cut faces are omitted');
  assert.ok('east' in cell0[0].faces && 'west' in cell1[0].faces);
  // an authored omission stays omitted: never six faces back
  const open = { from: [-4, 0, 0], to: [4, 4, 4], faces: { north: faces.north } };
  assert.deepEqual(splitCells([open], rectangle(2, 1)).map((c) => Object.keys(c[0].faces)), [['north'], ['north']]);
});

test('the top row keeps what rises above the structure only with overflow', () => {
  const faces = { north: { uv: [0, 0, 4, 16], texture: '#t' } };
  const pipe = { from: [2, 10, 12], to: [6, 30, 16], faces };
  assert.equal(splitCells([pipe], rectangle(2, 1), { overflow: true })[0][0].to[1], 30);
  assert.equal(splitCells([pipe], rectangle(2, 1))[0][0].to[1], 16);
});

test('overlays reach only their cells; the item assembly is the idle parts put back', () => {
  const faces = Object.fromEntries(['north', 'south', 'west', 'east', 'up', 'down'].map((f) => [f, { uv: [0, 0, 4, 4], texture: '#t' }]));
  const el = (from, to, path) => ({ from, to, faces, path });
  const piece = { multiblock: { dir: 'block/x', cells: [2, 2], overlays: [{ name: 'fire', group: 'fire', emissive: true }] } };
  const outputs = multiblockOutputs(piece, [
    el([-14, 0, 0], [14, 4, 16], ['body']), el([2, 16, 2], [6, 20, 6], ['body']), el([-12, 18, 0], [-8, 20, 4], ['body']),
    el([2, 2, 2], [6, 6, 6], ['fire']),
  ]);
  const byPath = Object.fromEntries(outputs.map((o) => [o.path, o]));
  assert.deepEqual(Object.keys(byPath).sort(), ['block/x/assembly', 'block/x/fire_0', 'block/x/part_0', 'block/x/part_1', 'block/x/part_2', 'block/x/part_3']);
  assert.ok(byPath['block/x/fire_0'].elements.every((e) => e.forge_data?.block_light === 15));
  assert.ok(byPath['block/x/assembly'].item);
  const assembly = byPath['block/x/assembly'].elements;
  assert.ok(assembly.every((e) => !e.forge_data && !e.path.includes('fire')), 'the item shows the idle station');
  assert.equal(assembly.length, 4); // the slab in two pieces, the two blocks above
  // per-cell culling: the slab's bottom culls in every cell; its cut ends never touch a cell side face
  assert.equal(byPath['block/x/part_1'].elements[0].faces.down.cullface, 'down');
  assert.ok(!byPath['block/x/part_1'].elements[0].faces.east);
});

test('single-block state outputs keep, drop and light groups', () => {
  const faces = { up: { uv: [0, 0, 4, 4], texture: '#t' } };
  const elements = [{ from: [0, 0, 0], to: [4, 4, 4], faces, path: ['body'] }, { from: [5, 0, 5], to: [6, 4, 6], faces, path: ['lit', 'flame'] }];
  const [off, on] = singleOutputs({ outputs: [{ model: 'off', item: true, exclude: ['lit'] }, { model: 'on', emissive: ['flame'] }] }, elements);
  assert.equal(off.elements.length, 1);
  assert.deepEqual(on.elements.map((e) => !!e.forge_data), [false, true]);
});

test('a generated attachment follows its anchor', () => {
  const def = { kind: 'bundle', herb: 'lavender', width: 5, height: 8, across: 'x' };
  const a = expandAttachment(def, [8, 14, 8]);
  const b = expandAttachment(def, [9, 13, 8]);
  assert.ok(a.length >= 2 && a.some((e) => Object.values(e.faces).some((f) => f.texture === '#bundle_lavender')));
  a.forEach((e, i) => assert.deepEqual(b[i].from, [e.from[0] + 1, e.from[1] - 1, e.from[2]]));
  // moving the anchor group in the project moves the compiled bundle; its preview cubes are ignored
  const tree = (origin) => [cube([0, 0, 0], [16, 4, 16]), group('bundle', [cube([0, 0, 0], [1, 1, 1])], origin)];
  const piece = simplePiece('p.bbmodel', ['clay'], { attachments: { bundle: def },
    textures: { clay: 'herbalwitch:block/test/clay', particle: 'herbalwitch:block/test/clay', bundle_lavender: 'herbalwitch:item/preparations/herbs/temperate/lavender/bundle', parts: 'herbalwitch:block/botany/parts' } });
  const repoTextures = join(import.meta.dirname, '../../../src/main/resources/assets/herbalwitch/textures');
  const sprites = Object.fromEntries(['item/preparations/herbs/temperate/lavender/bundle.png', 'block/botany/parts.png']
    .map((rel) => [rel, readFileSync(join(repoTextures, rel))]));
  const run = (origin) => JSON.parse(compileAll(tempRoot({ 'test/p': { piece, text: projectText('p', tree(origin), [{ id: 'clay', png: SOLID }]) } },
    { 'block/test/clay.png': SOLID, ...sprites })).models.get('block/test/piece.json'));
  const here = run([8, 14, 8]).elements;
  const moved = run([9, 13, 8]).elements;
  assert.equal(here.length, 1 + a.length, 'the slab plus the generated bundle; the preview cube is dropped');
  here.slice(1).forEach((e, i) => assert.deepEqual(moved[i + 1].from, [e.from[0] + 1, e.from[1] - 1, e.from[2]]));
  assert.equal(run([8, 14, 8]).render_type, 'minecraft:cutout'); // the sprite has transparent pixels
});

test('every committed multiblock: its cells put back are exactly its item assembly', () => {
  const root = join(import.meta.dirname, '../../..');
  const models = join(root, 'src/main/resources/assets/herbalwitch/models/block/stations');
  const read = (p) => JSON.parse(readFileSync(join(models, `${p}.json`), 'utf8'));
  const round = (k, v) => (typeof v === 'number' ? Math.round(v * 1e4) / 1e4 : v);
  const key = (e) => JSON.stringify({ ...e, faces: Object.fromEntries(Object.entries(e.faces).map(([f, { cullface, ...rest }]) => [f, rest])) }, round);
  for (const [station, w, h] of [['drying_rack', 2, 2], ['hearth', 2, 2], ['stove', 2, 1]]) {
    const cells = rectangle(w, h);
    const parts = cells.flatMap((c, i) => shift(read(`${station}/part_${i}`).elements, [-16 * c.right, 16 * c.up, 0]));
    const assembly = read(`${station}/assembly`).elements;
    assert.deepEqual(parts.map(key).sort(), assembly.map(key).sort(), station);
  }
});
