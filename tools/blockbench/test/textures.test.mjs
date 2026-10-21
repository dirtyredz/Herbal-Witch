// Texture ownership: several slots per project, UVs converted from the project resolution, owned
// textures exported deterministically, shared previews checked against the canonical PNG, binary
// alpha required, transparency deciding cutout, and one owner per texture.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { compileAll } from '../compile.mjs';
import { decodeDataUrl, refreshPreviews, samePixels } from '../textures.mjs';
import { decodePng } from '../../image/png-decode.mjs';
import { cube, group, projectText, simplePiece, tempRoot, pngOf, SOLID, STRIPES, HOLES } from './helpers.mjs';

const canonical = { 'block/test/clay.png': SOLID, 'block/test/gold.png': STRIPES, 'block/test/leaf.png': HOLES };

/** One project with `tree` and `textures`, compiled in a temp root. */
function compile(tree, textures, piece = {}, extraPieces = {}, resolution = null) {
  let text = projectText('p', tree, textures);
  if (resolution) {
    const json = JSON.parse(text);
    json.resolution = resolution;
    text = JSON.stringify(json);
  }
  const root = tempRoot({ 'test/p': { piece: simplePiece('p.bbmodel', textures.map((t) => t.id), piece), text }, ...extraPieces }, canonical);
  return { root, ...compileAll(root) };
}

const outputOf = (models) => JSON.parse(models.get('block/test/piece.json'));

test('several slots resolve through their ids, whatever the texture list order', () => {
  const tree = [cube([0, 0, 0], [8, 8, 8], 0), cube([8, 0, 0], [16, 8, 8], 1)];
  const a = outputOf(compile(tree, [{ id: 'clay', png: SOLID }, { id: 'gold', png: STRIPES }]).models);
  const b = outputOf(compile(tree, [{ id: 'gold', png: STRIPES }, { id: 'clay', png: SOLID }]).models);
  assert.equal(a.elements[0].faces.north.texture, '#clay');
  assert.equal(a.elements[1].faces.north.texture, '#gold');
  assert.equal(b.elements[0].faces.north.texture, '#gold'); // the faces point at list positions; the ids follow them
  assert.deepEqual(Object.keys(a.textures).sort(), ['clay', 'gold', 'particle']);
});

test('UVs convert from the project resolution to the 0..16 model space', () => {
  const tree = [{ element: { name: 'c', from: [0, 0, 0], to: [4, 4, 4], faces: { north: { uv: [0, 8, 32, 16], texture: 0 } } } }];
  const out = outputOf(compile(tree, [{ id: 'clay', png: SOLID }], {}, {}, { width: 32, height: 64 }).models);
  assert.deepEqual(out.elements[0].faces.north.uv, [0, 2, 16, 4]);
});

test('an owned texture is exported deterministically through the repository encoder', () => {
  const painted = pngOf(16, (x, y) => [x * 16, y * 16, 128, 255]);
  const piece = { owned_textures: { clay: 'herbalwitch:block/test/clay' } };
  const first = compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: painted }], piece);
  const again = compileAll(first.root);
  const bytes = first.textures.get('block/test/clay.png');
  assert.ok(bytes.equals(again.textures.get('block/test/clay.png')));
  assert.ok(samePixels(decodePng(bytes), decodePng(painted)));
});

test('a shared preview must show the canonical pixels; refresh re-embeds them', () => {
  const stale = pngOf(16, () => [121, 60, 30, 255]);
  assert.throws(() => compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: stale }]), /test\/p texture clay: the embedded preview differs from the shared herbalwitch:block\/test\/clay.*--refresh-previews/);
  const json = JSON.parse(projectText('p', [cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: stale }]));
  const { changed } = refreshPreviews(json, simplePiece('p.bbmodel', ['clay']), (id) => canonical[`${id.split(':')[1]}.png`]);
  assert.deepEqual(changed, ['clay']);
  assert.ok(samePixels(decodeDataUrl(json.textures[0].source, 'clay'), decodePng(SOLID)));
});

test('colour under a fully transparent pixel is not compared (an editor canvas drops it)', () => {
  const zeroed = pngOf(16, (x, y) => ((x + y) % 2 ? [40, 160, 40, 255] : [255, 0, 255, 0]));
  assert.doesNotThrow(() => compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'leaf', png: zeroed }]));
});

test('owned textures keep binary alpha and their resolution', () => {
  const soft = pngOf(16, () => [10, 10, 10, 128]);
  const piece = { owned_textures: { clay: 'herbalwitch:block/test/clay' } };
  assert.throws(() => compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: soft }], piece), /alpha must be fully opaque or fully transparent/);
  assert.throws(() => compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: pngOf(32, () => [1, 2, 3, 255]) }], piece), /keep their resolution \(16 x 16, found 32 x 32\)/);
});

test('cutout follows the resolved pixels, not the variable names', () => {
  const solid = outputOf(compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'leaf', png: SOLID }], { textures: { leaf: 'herbalwitch:block/test/clay', particle: 'herbalwitch:block/test/clay' } }).models);
  assert.equal(solid.render_type, undefined);
  const holes = outputOf(compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: HOLES }], { textures: { clay: 'herbalwitch:block/test/leaf', particle: 'herbalwitch:block/test/leaf' } }).models);
  assert.equal(holes.render_type, 'minecraft:cutout');
});

test('a texture has one owner and every bound slot exists', () => {
  const other = { piece: simplePiece('q.bbmodel', ['clay'], { owned_textures: { clay: 'herbalwitch:block/test/clay' }, outputs: [{ model: 'block/test/other', item: true }] }), text: projectText('q', [cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: SOLID }]) };
  assert.throws(() => compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: SOLID }], { owned_textures: { clay: 'herbalwitch:block/test/clay' } }, { 'test/q': other }),
    /texture block\/test\/clay.png is also owned by another project/);
  assert.throws(() => compile([cube([0, 0, 0], [16, 16, 16])], [{ id: 'clay', png: SOLID }], { textures: { particle: 'herbalwitch:block/test/clay' } }), /texture clay: not bound in piece.json/);
});

test('the committed rug and lamp shade are exactly what their projects export', () => {
  const root = join(import.meta.dirname, '../../..');
  const { textures } = compileAll(root);
  for (const rel of ['block/material/rug.png', 'block/material/lamp_shade.png']) {
    assert.ok(textures.get(rel).equals(readFileSync(join(root, 'src/main/resources/assets/herbalwitch/textures', rel))), rel);
  }
});

test('groups do not change texture resolution', () => {
  const out = outputOf(compile([group('a', [group('b', [cube([0, 0, 0], [16, 16, 16], 1)])])], [{ id: 'clay', png: SOLID }, { id: 'gold', png: STRIPES }]).models);
  assert.equal(out.elements[0].faces.up.texture, '#gold');
});
