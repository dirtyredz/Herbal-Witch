// Round trips: an existing model imported into a project compiles back to the same model; the
// committed projects recompile byte-identically and match the committed resources; an edited
// project (the owner's kind of change: a moved cube, a re-mapped face, a Display tweak, a painted
// texel) changes exactly what was edited; and the migrated item displays keep their dev.5 values.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { join } from 'node:path';
import { importProject } from '../migration/import.mjs';
import { compileAll } from '../compile.mjs';
import { parseProject, compileJson } from '../project.mjs';
import { decodeDataUrl, dataUrl } from '../textures.mjs';
import { png } from '../../image/png-encode.mjs';
import { decodePng } from '../../image/png-decode.mjs';
import { tempRoot, SOLID, HOLES } from './helpers.mjs';

const here = import.meta.dirname;
const root = join(here, '../../..');
const models = join(root, 'src/main/resources/assets/herbalwitch/models');

test('an imported model compiles back to the same model (geometry, UVs, rotations, omissions, cullfaces, display)', () => {
  const source = JSON.parse(readFileSync(join(here, 'fixtures/migration/source.json'), 'utf8'));
  const text = importProject({
    name: 'fixture',
    entries: source.elements.map((element, i) => ({ element, path: [['floor', 'spout', 'leaf'][i]] })),
    textures: [{ variable: 'clay', resource: 'herbalwitch:block/test/clay', png: SOLID, particle: true }, { variable: 'leaf', resource: 'herbalwitch:block/test/leaf', png: HOLES }],
    display: source.display,
  });
  const piece = { project: 'fixture.bbmodel', textures: source.textures, bounds: { min: [0, 0, 0], max: [16, 16, 16] }, outputs: [{ model: 'block/test/fixture', item: true }] };
  const tmp = tempRoot({ 'test/fixture': { piece, text } }, { 'block/test/clay.png': SOLID, 'block/test/leaf.png': HOLES });
  const out = JSON.parse(compileAll(tmp).models.get('block/test/fixture.json'));
  assert.deepEqual(out, source);
  assert.deepEqual(parseProject(text, 'fixture').tree.map((n) => n.group.name), ['floor', 'spout', 'leaf']);
});

test('the committed projects recompile byte-identically and match the committed resources', () => {
  const a = compileAll(root);
  const b = compileAll(root);
  assert.equal(a.models.size, 365); // 34 before Milestone 5, then waves A (58), B (93) and C (173), then the M6 altar (7)
  for (const [p, text] of a.models) {
    assert.equal(b.models.get(p), text, p);
    assert.equal(readFileSync(join(models, p), 'utf8'), text, `${p} is stale: run node tools/compile-blockbench.mjs`);
  }
});

/** The kettle and rug projects with an owner-style edit applied, compiled from memory. */
function compileEdited(edit) {
  const files = new Map();
  return compileAll(root, {
    readText: (f) => {
      const text = readFileSync(f, 'utf8');
      const name = f.replace(/\\/g, '/').split('/').pop();
      if (!edit[name]) return text;
      const json = JSON.parse(text);
      edit[name](json);
      files.set(name, compileJson(json));
      return files.get(name);
    },
  });
}

test('an edited project round-trips: exactly the edited cube, face, display and texel change', () => {
  const before = compileAll(root);
  const after = compileEdited({
    'kettle.bbmodel': (j) => {
      const lid = j.groups.find((g) => g.name === 'lid');
      const outline = (nodes) => nodes.flatMap((n) => (typeof n === 'string' ? [] : n.uuid === lid.uuid ? n.children : outline(n.children)));
      const cube = j.elements.find((e) => e.uuid === outline(j.outliner).find((c) => typeof c === 'string'));
      cube.from[1] += 0.5;
      cube.to[1] += 0.5;
      const face = j.elements[0].faces.north;
      face.uv = [face.uv[2], face.uv[1], face.uv[0], face.uv[3]]; // mirrored
      j.display.gui.scale = [0.7, 0.7, 0.7];
    },
    'rug.bbmodel': (j) => {
      const t = j.textures.find((x) => x.id === 'rug');
      const img = decodeDataUrl(t.source, 'rug');
      img.rgba.set([255, 255, 255, 255], 0);
      t.source = dataUrl(png({ width: img.width, height: img.height, px: img.rgba }));
    },
  });
  const changed = [...after.models].filter(([p, t]) => before.models.get(p) !== t).map(([p]) => p);
  assert.deepEqual(changed, ['block/stations/kettle/base.json']);
  const was = JSON.parse(before.models.get('block/stations/kettle/base.json'));
  const now = JSON.parse(after.models.get('block/stations/kettle/base.json'));
  assert.deepEqual(now.display.gui.scale, [0.7, 0.7, 0.7]);
  const differing = now.elements.filter((e, i) => JSON.stringify(e) !== JSON.stringify(was.elements[i]));
  assert.equal(differing.length, 2, 'the moved lid cube and the re-mapped face');
  const rug = decodePng(after.textures.get('block/pieces/decor/rug/rug.png'));
  assert.deepEqual([...rug.rgba.subarray(0, 4)], [255, 255, 255, 255]);
  assert.ok(!after.textures.get('block/pieces/decor/table_lamp/lamp_shade.png').equals(after.textures.get('block/pieces/decor/rug/rug.png')));
});

test('the migrated item displays keep their 0.2.0-dev.5 values (later Display edits change only these fixtures)', () => {
  const gui = (rel) => JSON.parse(readFileSync(join(models, rel), 'utf8')).display.gui;
  for (const rel of ['block/stations/kettle/base.json', 'block/stations/infusion_pot/base.json', 'block/stations/mortar/base.json',
    'block/decor/templates/armchair.json', 'block/decor/templates/apothecary_counter.json', 'block/decor/templates/table_lamp_off.json']) {
    assert.deepEqual(gui(rel), { rotation: [30, 225, 0], translation: [0, 0, 0], scale: [0.625, 0.625, 0.625] }, rel);
  }
  for (const rel of ['block/decor/templates/rug.json', 'block/decor/templates/wall_shelf.json']) assert.deepEqual(gui(rel).scale, [0.75, 0.75, 0.75], rel);
  assert.deepEqual(gui('block/preparations/templates/oil_lantern_off.json').scale, [0.9, 0.9, 0.9]);
  for (const station of ['drying_rack', 'hearth', 'stove']) assert.deepEqual(gui(`block/stations/${station}/assembly.json`).scale, [0.3125, 0.3125, 0.3125], station);
  assert.equal(JSON.parse(readFileSync(join(models, 'block/decor/templates/table_lamp_on.json'), 'utf8')).display, undefined, 'only the item model carries display');
});
