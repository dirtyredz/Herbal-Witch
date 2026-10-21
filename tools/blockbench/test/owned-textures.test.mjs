// Every texture a Blockbench piece uses is that piece's own PNG, under its own path: painting one
// piece's wood, glaze or gold cannot change another piece, and nothing but the herb atlas and the
// bundle sprites (the procedural herbs' art, drawn into generated attachments) is shared.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { compileAll } from '../compile.mjs';
import { loadCatalogue, loadPieces, validatePiece, isSharedTexture } from '../catalogue.mjs';
import { dataUrl, decodeDataUrl } from '../textures.mjs';
import { png } from '../../image/png-encode.mjs';

const root = join(import.meta.dirname, '../../..');
const catalogue = loadCatalogue(root);
const pieces = loadPieces(root, catalogue);

test('each piece owns every texture it binds, at a path unique to it', () => {
  assert.equal(catalogue.ownedTextureDir, 'block/pieces');
  const seen = new Map();
  for (const { dir, piece } of pieces) {
    for (const [variable, id] of Object.entries(piece.textures)) {
      if (id.startsWith('#') || isSharedTexture(id)) continue;
      assert.equal(piece.owned_textures[variable] ?? (variable === 'particle' ? id : undefined), id, `${dir} ${variable} is not owned`);
      assert.equal(id, `herbalwitch:block/pieces/${dir}/${variable === 'particle' ? id.split('/').pop() : variable}`, `${dir} ${variable} is outside the piece's folder`);
      if (variable === 'particle') continue;
      assert.ok(!seen.has(id), `${id} is bound by ${seen.get(id)} and ${dir}`);
      seen.set(id, dir);
    }
    for (const id of Object.values(piece.owned_textures)) assert.ok(existsSync(join(root, 'src/main/resources/assets', id.replace(':', '/textures/') + '.png')), `${id} is not written`);
  }
  assert.ok(seen.size >= 60);
});

test('what stays shared is only the herb atlas and the bundle sprites', () => {
  const shared = new Set(pieces.flatMap(({ piece }) => Object.values(piece.textures).filter((id) => !id.startsWith('#') && isSharedTexture(id))));
  assert.ok(shared.has('herbalwitch:block/botany/parts'));
  for (const id of shared) assert.match(id, /^herbalwitch:(block\/botany\/parts|item\/preparations\/herbs\/)/);
});

test('a shared house material cannot be bound by a piece, and an owned texture must live in its piece folder', () => {
  const { piece } = pieces.find((p) => p.dir === 'stations/kettle');
  const shared = { ...piece, textures: { ...piece.textures, gold: 'herbalwitch:block/material/gold' }, owned_textures: { ...piece.owned_textures } };
  delete shared.owned_textures.gold;
  assert.throws(() => validatePiece(shared, 'stations/kettle', catalogue), /texture gold .* must be owned by the piece/);
  const elsewhere = { ...piece, textures: { ...piece.textures, gold: 'herbalwitch:block/pieces/stations/mortar/gold' }, owned_textures: { ...piece.owned_textures, gold: 'herbalwitch:block/pieces/stations/mortar/gold' } };
  assert.throws(() => validatePiece(elsewhere, 'stations/kettle', catalogue), /must live at herbalwitch:block\/pieces\/stations\/kettle\/gold/);
});

test('painting one piece\'s texture changes only that piece\'s outputs', () => {
  const before = compileAll(root);
  const paint = (projectName, variable) => compileAll(root, {
    readText: (f) => {
      const text = readFileSync(f, 'utf8');
      if (!f.replaceAll(String.fromCharCode(92), '/').endsWith(`/${projectName}`)) return text;
      const json = JSON.parse(text);
      const t = json.textures.find((x) => x.id === variable);
      const img = decodeDataUrl(t.source, variable);
      img.rgba.set([1, 2, 3, 255], 0);
      t.source = dataUrl(png({ width: img.width, height: img.height, px: img.rgba }));
      return JSON.stringify(json);
    },
  });
  // the kettle and the infusion pot both have a "gold" and a "copper"; the armchair and the shelf both have a "wood"
  for (const [project, variable, id] of [['kettle.bbmodel', 'gold', 'block/pieces/stations/kettle/gold.png'], ['wall_shelf.bbmodel', 'wood', 'block/pieces/decor/wall_shelf/wood.png']]) {
    const after = paint(project, variable);
    const changed = [...after.textures].filter(([k, v]) => !v.equals(before.textures.get(k))).map(([k]) => k);
    assert.deepEqual(changed, [id]);
    assert.deepEqual([...after.models].filter(([k, v]) => v !== before.models.get(k)), [], 'repainting changes no model');
    assert.equal(after.textures.size, before.textures.size);
  }
});
