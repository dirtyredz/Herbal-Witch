// Resource lookup for the model previewer: the mod's authored tree (src/main/resources) and the
// datagen tree (src/generated/resources), or explicitly given asset roots (a staged dev resource
// pack, a captured baseline), resolved the way the game sees one merged resource pack. A path
// present in two trees is a conflict and is reported, never silently picked.
import { existsSync, readFileSync, readdirSync, statSync } from 'node:fs';
import { join, relative, sep } from 'node:path';
import { decodePng } from '../../image/png-decode.mjs';

export const NAMESPACE = 'herbalwitch';

function walk(dir) {
  if (!existsSync(dir)) return [];
  const out = [];
  for (const name of readdirSync(dir).sort()) {
    const full = join(dir, name);
    if (statSync(full).isDirectory()) out.push(...walk(full));
    else out.push(full);
  }
  return out;
}

/** Splits 'ns:path' (default namespace minecraft, like the game) into [ns, path]. */
export function splitId(id) {
  const i = id.indexOf(':');
  return i < 0 ? ['minecraft', id] : [id.slice(0, i), id.slice(i + 1)];
}

/** The repository's two asset trees under a mod directory: datagen output, then authored. */
export const repositoryTrees = (root) => [join(root, 'src/generated/resources/assets'), join(root, 'src/main/resources/assets')];

export class Resources {
  /**
   * @param root  the mod directory (mods/HerbalWitch)
   * @param trees explicit asset roots (each holding <namespace>/...) instead of the repository's
   *              two, e.g. a staged resource pack or a captured baseline
   */
  constructor(root, { trees = null } = {}) {
    if (trees && !trees.length) throw new Error('no asset trees given');
    this.trees = trees ?? repositoryTrees(root);
    this.textureCache = new Map();
    this.diagnostics = [];
  }

  /** The single file for assets/<ns>/<kind>/<path><ext>, or null; conflicts are diagnosed. */
  file(ns, kind, path, ext) {
    const hits = this.trees.map((t) => join(t, ns, kind, path + ext)).filter((f) => existsSync(f));
    if (hits.length > 1) this.diagnose('conflict', `${ns}:${kind}/${path}${ext} exists in both resource trees`);
    return hits[0] ?? null;
  }

  json(kind, id) {
    const [ns, path] = splitId(id);
    const f = this.file(ns, kind, path, '.json');
    return f ? JSON.parse(readFileSync(f, 'utf8')) : null;
  }

  /** Every id of a kind ('blockstates', 'models') in this mod's namespace, sorted. */
  ids(kind) {
    const out = new Set();
    for (const tree of this.trees) {
      const base = join(tree, NAMESPACE, kind);
      for (const f of walk(base)) {
        if (f.endsWith('.json')) out.add(`${NAMESPACE}:${relative(base, f).split(sep).join('/').slice(0, -5)}`);
      }
    }
    return [...out].sort();
  }

  /** Decoded RGBA texture for 'ns:path' (textures/<path>.png), or null when missing. */
  texture(id) {
    if (this.textureCache.has(id)) return this.textureCache.get(id);
    const [ns, path] = splitId(id);
    const f = this.file(ns, 'textures', path, '.png');
    const tex = f ? decodePng(readFileSync(f), id) : null;
    this.textureCache.set(id, tex);
    return tex;
  }

  /** Raw bytes of every file a render depended on, for the manifest's input hashes. */
  bytes(ns, kind, path, ext) {
    const f = this.file(ns, kind, path, ext);
    return f ? readFileSync(f) : null;
  }

  diagnose(kind, message) {
    this.diagnostics.push({ kind, message });
  }
}
