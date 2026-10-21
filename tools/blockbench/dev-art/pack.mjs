// The live dev-art resource pack: compile the Blockbench projects in memory, stage the resource
// pack in memory (the compiled models and owned textures, plus the item models and blockstates that
// show them), validate it laid over the repository's trees as the pack lies over the jar (coverage
// and the z-fighting sweep, as the previewer runs them, for what the save changed), and only then
// publish it into a trial instance's resourcepacks/HerbalWitch-dev-art/. A failed compile or
// validation leaves the last good pack in place. Publication touches nothing outside that one
// folder, refuses a folder it did not create (ownership marker), and removes only files its own
// manifest recorded.
import { existsSync, mkdirSync, readFileSync, readdirSync, rmSync, rmdirSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, resolve, sep } from 'node:path';
import { compileAll } from '../compile.mjs';
import { Resources, repositoryTrees } from '../../preview/assets/resources.mjs';
import { resolveModel } from '../../preview/assets/models.mjs';
import { Baker } from '../../preview/geometry/bake.mjs';
import { coverage } from '../../preview/report/coverage.mjs';
import { zFighting } from '../../preview/report/zfighting.mjs';

export const PACK_NAME = 'HerbalWitch-dev-art';
export const PACK_FORMAT = 15; // Minecraft Java 1.20 / 1.20.1
export const MARKER = '.herbalwitch-dev-art.json';
const OWNER = 'herbalwitch dev-art (tools/compile-blockbench.mjs --dev-pack)';
const NS = 'herbalwitch';

const overlayKey = (rel) => rel.slice('assets/'.length); // pack path -> Resources overlay key
const toOverlay = (files) => new Map([...files].filter(([rel]) => rel.startsWith('assets/')).map(([rel, b]) => [overlayKey(rel), b]));
const modelId = (rel) => `${NS}:${rel.slice(0, -'.json'.length)}`;

/**
 * Which item models and blockstates show the compiled models: {items: Map<id, [model ids]>,
 * blocks: Map<id, [model ids]>} (the models each reaches). Read once per session.
 */
export function viewers(root, compiledIds, trees = repositoryTrees(root)) {
  const res = new Resources(root, { trees });
  const items = new Map();
  const blocks = new Map();
  for (const item of res.ids('models').filter((id) => id.startsWith(`${NS}:item/`))) {
    const hit = resolveModel(res, item).chain.filter((c) => compiledIds.has(c));
    if (hit.length) items.set(item, hit);
  }
  for (const block of res.ids('blockstates')) {
    const text = JSON.stringify(res.json('blockstates', block));
    const hit = [...compiledIds].filter((id) => text.includes(`"${id}"`));
    if (hit.length) blocks.set(block, hit);
  }
  return { items, blocks };
}

/**
 * The pack's files (pack path -> bytes): the compiled models and owned textures, the repository's
 * item models and blockstates that show them (so the pack matches the workspace, not the installed
 * jar), and pack.mcmeta. Shared textures and everything else come from the jar. No server data,
 * lang or sources.
 */
export function stageFiles(root, compiled, shown, { trees = repositoryTrees(root), description = 'HerbalWitch dev art: live Blockbench edits' } = {}) {
  const files = new Map();
  for (const [rel, text] of compiled.models) files.set(`assets/${NS}/models/${rel}`, Buffer.from(text, 'utf8'));
  for (const [rel, bytes] of compiled.textures) files.set(`assets/${NS}/textures/${rel}`, bytes);
  const res = new Resources(root, { trees });
  for (const [kind, ids] of [['models', shown.items.keys()], ['blockstates', shown.blocks.keys()]]) {
    for (const id of ids) {
      const path = id.slice(NS.length + 1);
      const bytes = res.read(NS, kind, path, '.json') ?? (() => { throw new Error(`${kind} ${id} disappeared; restart the watcher`); })();
      files.set(`assets/${NS}/${kind}/${path}.json`, bytes);
    }
  }
  files.set('pack.mcmeta', Buffer.from(`${JSON.stringify({ pack: { pack_format: PACK_FORMAT, description } }, null, 2)}\n`));
  return files;
}

/**
 * Coverage and z-fighting diagnostics for the staged pack laid over the repository's trees, for the
 * given compiled model ids and the items and blockstates that show them. Empty when it is good to publish.
 */
export function validateStage(root, files, models, shown, { trees = repositoryTrees(root) } = {}) {
  const res = new Resources(root, { trees, overlay: toOverlay(files) });
  const reaches = (map) => new Set([...map].filter(([, hit]) => hit.some((m) => models.has(m))).map(([id]) => id));
  const scope = { models: new Set(models), items: reaches(shown.items), blocks: reaches(shown.blocks) };
  const baker = new Baker(res);
  return [...coverage(res, baker, scope).errors, ...zFighting(res, baker, scope)];
}

/** The pack folder inside a trial instance, after checking the instance looks like a .minecraft. */
export function packDestination(instance) {
  const root = resolve(instance);
  if (!existsSync(root) || !statSync(root).isDirectory()) throw new Error(`instance folder ${root} does not exist`);
  if (!['options.txt', 'resourcepacks', 'saves'].some((n) => existsSync(join(root, n)))) {
    throw new Error(`${root} does not look like a .minecraft folder (no options.txt, resourcepacks or saves)`);
  }
  return join(root, 'resourcepacks', PACK_NAME);
}

const contained = (dest, rel) => {
  const full = resolve(dest, rel);
  return full.startsWith(resolve(dest) + sep) ? full : null;
};

/**
 * Publishes `files` into `dest` (a PACK_NAME folder): writes changed files, removes files the
 * previous manifest recorded that are gone now, writes the manifest last. Returns {written, removed}.
 */
export function publish(dest, files) {
  if (!resolve(dest).endsWith(`${sep}${PACK_NAME}`)) throw new Error(`refusing to publish into ${dest}`);
  let previous = [];
  if (existsSync(dest)) {
    const marker = join(dest, MARKER);
    if (!existsSync(marker)) throw new Error(`${dest} exists but was not created by dev-art; move it away first`);
    const json = JSON.parse(readFileSync(marker, 'utf8'));
    if (json.owner !== OWNER) throw new Error(`${dest}: unexpected ownership marker`);
    previous = json.files ?? [];
  }
  for (const rel of files.keys()) if (!contained(dest, rel)) throw new Error(`pack path ${rel} leaves the pack folder`);
  const written = [];
  for (const [rel, bytes] of files) {
    const full = contained(dest, rel);
    if (existsSync(full) && readFileSync(full).equals(bytes)) continue;
    mkdirSync(dirname(full), { recursive: true });
    writeFileSync(full, bytes);
    written.push(rel);
  }
  const removed = [];
  for (const rel of previous) {
    if (files.has(rel)) continue;
    const full = contained(dest, rel);
    if (!full || !existsSync(full)) continue; // a tampered manifest never steers a deletion outside the pack
    rmSync(full);
    removed.push(rel);
    for (let d = dirname(full); d !== resolve(dest) && readdirSync(d).length === 0; d = dirname(d)) rmdirSync(d);
  }
  writeFileSync(join(dest, MARKER), `${JSON.stringify({ owner: OWNER, files: [...files.keys()].sort() }, null, 1)}\n`);
  return { written, removed };
}

/**
 * A publishing session for one instance. `run()` performs one cycle (compile, stage, validate,
 * publish) and returns {ok, errors, written, removed, dest}; on failure nothing in the instance
 * changes. Unchanged projects are not recompiled or revalidated. `compile` and `trees` are
 * injectable for tests.
 */
export function devPackSession({ root, instance, trees = repositoryTrees(root), compile = null }) {
  const cache = new Map();
  let shown = null;
  const doCompile = compile ?? (() => compileAll(root, { cache }));
  return {
    run() {
      const dest = packDestination(instance);
      let compiled;
      let files;
      try {
        compiled = doCompile();
        const ids = new Set([...compiled.models.keys()].map(modelId));
        shown ??= viewers(root, ids, trees);
        files = stageFiles(root, compiled, shown, { trees });
      } catch (e) {
        return { ok: false, errors: [e.message] };
      }
      const changed = new Set(compiled.changed ?? compiled.pieces?.map((p) => p.dir) ?? []);
      const models = new Set([...compiled.models.keys()].filter((rel) => !compiled.owners || changed.has(compiled.owners.get(rel))).map(modelId));
      const errors = validateStage(root, files, models, shown, { trees });
      if (errors.length) {
        for (const dir of changed) cache.delete(dir); // revalidate on the next save even if unchanged
        return { ok: false, errors };
      }
      return { ok: true, errors: [], ...publish(dest, files), dest: relative(resolve(instance), dest) };
    },
  };
}
