// The .bbmodel contract (Blockbench 5.2.2, format 5.0, java_block 1.9.0): what is read, what is
// ignored as editor metadata, and what is rejected with the project, group/element and field named.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, readdirSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { parseProject, writeProject } from '../project.mjs';
import { compileAll } from '../compile.mjs';
import { cube, group, projectText, SOLID } from './helpers.mjs';

const here = dirname(fileURLToPath(import.meta.url));
const fixture = (name) => readFileSync(join(here, 'fixtures', name), 'utf8');

test('a Blockbench-shaped save: nested groups, texture indices, rotations, omitted faces, display; editor metadata ignored', () => {
  const p = parseProject(fixture('editor-fields/editor-fields.bbmodel'), 'editor-fields');
  assert.deepEqual(p.elements.map((e) => [e.name, e.path.join('/')]), [['body', 'pot'], ['lean', 'pot/handle']]); // export:false dropped
  const [body, lean] = p.elements;
  assert.deepEqual(Object.keys(body.faces).sort(), ['east', 'north', 'south', 'up', 'west']); // texture null = omitted
  assert.deepEqual(body.faces.up, { uv: [12, 12, 4, 4], texture: 0, rotation: 90 }); // flipped and rotated UVs kept
  assert.equal(body.rotation, null);
  assert.deepEqual(lean.rotation, { angle: -22.5, axis: 'z', origin: [8, 6, 8] });
  assert.equal(lean.faces.north.cullface, 'north');
  assert.equal(p.textures[0].id, 'clay');
  assert.deepEqual(p.display.gui, { rotation: [30, 225, 0], translation: [0, 0, 0], scale: [0.625, 0.625, 0.625] });
  assert.deepEqual(p.display.ground, { rotation: [0, 0, 0], translation: [0, 3, 0], scale: [0.25, 0.25, 0.25] });
  assert.equal(p.display.head, undefined); // an empty slot is no override
});

test('every committed project parses under the pinned contract', () => {
  const root = join(here, '../../../src/art/blockbench');
  for (const area of readdirSync(root, { withFileTypes: true }).filter((d) => d.isDirectory())) {
    for (const piece of readdirSync(join(root, area.name))) {
      const p = parseProject(readFileSync(join(root, area.name, piece, `${piece}.bbmodel`), 'utf8'), `${area.name}/${piece}`);
      assert.ok(p.elements.length > 0, piece);
      assert.ok(Object.keys(p.display).length >= 6, `${piece} carries its item display`);
    }
  }
});

test('the writer and the parser agree: write -> parse -> write is stable', () => {
  const text = projectText('stable', [group('body', [cube([0, 0, 0], [16, 2, 16]), cube([4, 2, 4], [12, 2, 12])]),
    cube([7, 2, 7], [9, 10, 9], 0, { rotation: { angle: 45, axis: 'y', origin: [8, 2, 8], rescale: true } })], [{ id: 'clay', png: SOLID }],
  { gui: { rotation: [30, 225, 0], translation: [0, 0, 0], scale: [0.5, 0.5, 0.5] } });
  const p = parseProject(text, 'stable');
  assert.equal(p.elements.length, 3);
  assert.deepEqual(p.elements[1].to, [12, 2, 12]); // a plane survives
  assert.deepEqual(p.elements[2].rotation, { angle: 45, axis: 'y', origin: [8, 2, 8], rescale: true });
  const again = writeProject({
    name: 'stable', display: p.display, tree: p.tree.map(function strip(n) {
      return n.element ? { element: n.element } : { group: n.group, children: n.children.map(strip) };
    }), textures: [{ id: 'clay', name: 'clay.png', namespace: 'herbalwitch', folder: 'block/test', png: SOLID }],
  });
  assert.equal(again, text);
});

/** The fixture with one change applied to its raw JSON. */
function mutate(fn) {
  const json = JSON.parse(fixture('editor-fields/editor-fields.bbmodel'));
  fn(json);
  return JSON.stringify(json);
}

test('unsupported features are rejected naming the project, element and field', () => {
  const cases = [
    [(j) => { j.meta.format_version = '5.1'; }, /editor-fields: project format 5.1; .*Blockbench 5.2.2/],
    [(j) => { j.meta.model_format = 'bedrock'; }, /model format bedrock/],
    [(j) => { j.java_block_version = '1.21.11'; }, /Minecraft target 1.21.11.*1.9 - 1.21.5/],
    [(j) => { j.animations = [{ name: 'spin' }]; }, /animations is not supported/],
    [(j) => { j.elements[0].type = 'mesh'; }, /pot\/body: mesh elements are not supported/],
    [(j) => { j.elements[1].rotation = [22.5, 0, 22.5]; }, /pot\/handle\/lean: rotation .* more than one axis/],
    [(j) => { j.elements[1].rotation = [0, 0, 30]; }, /lean: rotation angle 30/],
    [(j) => { j.elements[0].faces.north.tint = 0; }, /body: north face: tint index 0/],
    [(j) => { j.elements[0].faces.north.rotation = 45; }, /north face: uv rotation 45/],
    [(j) => { j.elements[0].light_emission = 15; }, /light_emission/],
    [(j) => { j.elements[0].inflate = 0.5; }, /inflate is not supported/],
    [(j) => { j.groups[1].rotation = [0, 45, 0]; }, /group pot\/handle: group rotation/],
    [(j) => { delete j.textures[0].source; }, /texture 0 \(clay\.png\): must be embedded/],
    [(j) => { j.textures.push({ ...j.textures[0], uuid: 'x' }); }, /two textures use the id clay/],
    [(j) => { j.elements[0].faces.north.texture = 4; }, /north face: texture reference 4/],
    [(j) => { j.display.gui.rotation_pivot = [0, 1, 0]; }, /display gui: rotation_pivot/],
    [(j) => { j.display.on_shelf = { scale: [1, 1, 1] }; }, /unknown display slot on_shelf/],
    [(j) => { j.outliner.pop(); }, /element hidden from export is not in the outliner/],
  ];
  for (const [fn, message] of cases) assert.throws(() => parseProject(mutate(fn), 'editor-fields'), message, String(message));
  assert.throws(() => parseProject('{"meta": {"format_version": "5.0"', 'half-saved'), /half-saved: not valid JSON.*still saving/);
});

test('genuine Blockbench 5.2.2 saves (fixtures/editor-save/<piece>.bbmodel) compile to the committed models', (t) => {
  const dir = join(here, 'fixtures/editor-save');
  const saves = readdirSync(dir).filter((f) => f.endsWith('.bbmodel'));
  if (!saves.length) {
    t.skip('no editor-saved project committed yet: docs/BLOCKBENCH.md "First session" adds one');
    return;
  }
  const root = join(here, '../../..');
  for (const file of saves) {
    const piece = file.replace('.bbmodel', '');
    const text = readFileSync(join(dir, file), 'utf8');
    const p = parseProject(text, `editor-save/${file}`);
    assert.ok(p.elements.length, file);
    const result = compileAll(root, { readText: (f) => (f.split('\\').join('/').endsWith(`/${piece}/${file}`) ? text : readFileSync(f, 'utf8')) });
    for (const [rel, model] of result.models) {
      assert.equal(model, readFileSync(join(root, 'src/main/resources/assets/herbalwitch/models', rel), 'utf8'), `${file}: ${rel}`);
    }
  }
});
