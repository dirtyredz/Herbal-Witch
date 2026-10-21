// Shared test helpers: small textures, small projects and throwaway repository roots for the
// compiler (a catalogue, descriptors and canonical textures in a temp directory).
import { mkdtempSync, mkdirSync, writeFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { dirname, join } from 'node:path';
import { png } from '../../image/png-encode.mjs';
import { writeProject } from '../project.mjs';

/** A size x size PNG filled by fn(x, y) -> [r, g, b, a]. */
export const pngOf = (size, fn) => png({ size, px: Array.from({ length: size * size }, (_, i) => fn(i % size, Math.floor(i / size))) });
export const SOLID = pngOf(16, () => [120, 60, 30, 255]);
export const STRIPES = pngOf(16, (x) => (x % 2 ? [200, 180, 40, 255] : [90, 80, 20, 255]));
export const HOLES = pngOf(16, (x, y) => ((x + y) % 2 ? [40, 160, 40, 255] : [0, 0, 0, 0]));

/** A cube element for writeProject: every face textured `tex` (index) with its default-sized UV. */
export function cube(from, to, tex = 0, extra = {}) {
  const faces = {};
  for (const f of ['north', 'east', 'south', 'west', 'up', 'down']) faces[f] = { uv: [0, 0, 4, 4], texture: tex };
  return { element: { name: 'cube', from, to, faces, ...extra } };
}

export const group = (name, children, origin = [8, 8, 8]) => ({ group: { name, origin }, children });

/** Project text for a small tree with the given textures [{id, png}]. */
export function projectText(name, tree, textures, display = {}) {
  return writeProject({ name, tree, display, textures: textures.map((t) => ({ name: `${t.id}.png`, namespace: 'herbalwitch', folder: 'block/test', ...t })) });
}

/**
 * A throwaway repository root: `pieces` is {dir: {piece, text}}, `textures` {path under textures/: PNG}.
 * Returns the root (catalogue model_dirs default to block/test).
 */
export function tempRoot(pieces, textures = {}, modelDirs = ['block/test']) {
  const root = mkdtempSync(join(tmpdir(), 'hw-bb-'));
  const write = (rel, data) => {
    mkdirSync(dirname(join(root, rel)), { recursive: true });
    writeFileSync(join(root, rel), data);
  };
  write('src/art/blockbench/catalog.json', JSON.stringify({
    blockbench: '5.2.2', format_version: '5.0', java_block_version: '1.9.0', model_dirs: modelDirs, projects: Object.keys(pieces),
  }));
  for (const [dir, { piece, text }] of Object.entries(pieces)) {
    write(`src/art/blockbench/${dir}/piece.json`, JSON.stringify(piece));
    write(`src/art/blockbench/${dir}/${piece.project}`, text);
  }
  for (const [rel, bytes] of Object.entries(textures)) write(`src/main/resources/assets/herbalwitch/textures/${rel}`, bytes);
  return root;
}

/** A one-output descriptor binding texture ids to herbalwitch:block/test/<id>. */
export function simplePiece(project, ids, extra = {}) {
  const textures = Object.fromEntries(ids.map((id) => [id, `herbalwitch:block/test/${id}`]));
  return { project, textures: { ...textures, particle: textures[ids[0]] }, bounds: { min: [0, 0, 0], max: [16, 16, 16] }, outputs: [{ model: 'block/test/piece', item: true }], ...extra };
}
