// The Blockbench catalogue (src/art/blockbench/catalog.json) and each project's descriptor
// (piece.json). The catalogue lists the projects explicitly and the model directories the
// compiler owns; nothing is inferred from filenames. A descriptor owns the Minecraft side of a
// piece: permanent output paths, texture bindings, owned textures, permitted bounds, state groups
// (which groups an output keeps, which glow), multiblock cells and overlays, and generated
// attachments. Coordinates, UVs and display transforms live only in the .bbmodel.
import { existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { FORMAT_VERSION, JAVA_BLOCK_VERSION } from './project.mjs';
import { declaredCells, cellProblems } from './geometry/cells.mjs';

export const SOURCE_DIR = 'src/art/blockbench';
const KEYS = new Set(['project', 'textures', 'owned_textures', 'bounds', 'outputs', 'multiblock', 'attachments']);
const OUTPUT_KEYS = new Set(['model', 'item', 'exclude', 'emissive']);
const MULTIBLOCK_KEYS = new Set(['dir', 'cells', 'overflow', 'overlays', 'variants']);
const VARIANT_KEYS = new Set(['name', 'emissive', 'exclude', 'mirror']);
const ATTACHMENT_KINDS = { bundle: ['herb', 'width', 'height', 'across'] };

/** Textures a piece may bind without owning: the procedural herbs' art that generated bundle attachments draw from. */
export const isSharedTexture = (id) => id === 'herbalwitch:block/botany/parts' || id.startsWith('herbalwitch:item/preparations/herbs/');
/** Where a piece's own textures live: textures/<owned_texture_dir>/<project dir>/<texture variable>.png. */
export const ownedTextureId = (ownedDir, dir, variable) => `herbalwitch:${ownedDir}/${dir}/${variable}`;

const fail = (where, message) => {
  throw new Error(`${where}: ${message}`);
};
const readJson = (file, where) => {
  if (!existsSync(file)) fail(where, `missing ${file}`);
  try {
    return JSON.parse(readFileSync(file, 'utf8'));
  } catch (e) {
    return fail(where, `invalid JSON: ${e.message}`);
  }
};
const vec3 = (v) => Array.isArray(v) && v.length === 3 && v.every(Number.isFinite);
const inside = (path, dir) => path.startsWith(`${dir}/`);

/** The catalogue: {projects: [dir], modelDirs: [dir under models/]}. */
export function loadCatalogue(root) {
  const where = `${SOURCE_DIR}/catalog.json`;
  const json = readJson(join(root, where), where);
  if (json.format_version !== FORMAT_VERSION || json.java_block_version !== JAVA_BLOCK_VERSION) {
    fail(where, `supports project format ${FORMAT_VERSION} / Java target ${JAVA_BLOCK_VERSION} only`);
  }
  if (!Array.isArray(json.projects) || !json.projects.length) fail(where, 'lists no projects');
  if (new Set(json.projects).size !== json.projects.length) fail(where, 'lists a project twice');
  if (!Array.isArray(json.model_dirs) || !json.model_dirs.length) fail(where, 'declares no model_dirs');
  if (json.owned_texture_dir !== undefined && !/^[a-z0-9_/-]+$/.test(json.owned_texture_dir)) fail(where, 'owned_texture_dir must be a directory under textures/');
  return { blockbench: json.blockbench, projects: json.projects, modelDirs: json.model_dirs, ownedTextureDir: json.owned_texture_dir ?? null };
}

/** Validates one piece.json; `dir` is the project's directory relative to SOURCE_DIR. */
export function validatePiece(json, dir, catalogue) {
  const where = `${dir}/piece.json`;
  for (const k of Object.keys(json)) if (!KEYS.has(k)) fail(where, `unknown key ${k}`);
  if (typeof json.project !== 'string' || !json.project.endsWith('.bbmodel') || json.project.includes('/')) fail(where, 'project must name a .bbmodel beside it');
  const textures = json.textures ?? fail(where, 'no textures');
  if (!textures.particle) fail(where, 'textures must bind particle');
  for (const [k, v] of Object.entries(textures)) {
    if (typeof v !== 'string' || !(v.startsWith('#') || /^[a-z0-9_.-]+:[a-z0-9_./-]+$/.test(v))) fail(where, `texture ${k}: bad id ${v}`);
  }
  const owned = json.owned_textures ?? {};
  for (const [k, v] of Object.entries(owned)) if (textures[k] !== v) fail(where, `owned texture ${k} must equal its binding`);
  // With an owned_texture_dir, every texture belongs to exactly one piece: it is painted in that
  // piece's project, at the piece's own path (the herb atlas and bundle sprites stay shared).
  if (catalogue.ownedTextureDir) {
    if (!textures.particle.startsWith('#') && !Object.values(owned).includes(textures.particle)) fail(where, 'particle must be a #reference or one of the piece own textures');
    for (const [k, v] of Object.entries(textures)) {
      if (k === 'particle' || v.startsWith('#') || isSharedTexture(v)) continue;
      const at = ownedTextureId(catalogue.ownedTextureDir, dir, k);
      if (!owned[k]) fail(where, `texture ${k} (${v}) must be owned by the piece (owned_textures); only the herb atlas and bundle sprites are shared`);
      if (v !== at) fail(where, `owned texture ${k} must live at ${at}, not ${v}`);
    }
  }
  const bounds = json.bounds ?? fail(where, 'no bounds');
  if (!vec3(bounds.min) || !vec3(bounds.max)) fail(where, 'bounds needs min and max');
  const attachments = json.attachments ?? {};
  for (const [name, a] of Object.entries(attachments)) {
    const keys = ATTACHMENT_KINDS[a.kind] ?? fail(where, `attachment ${name}: unknown kind ${a.kind}`);
    for (const k of Object.keys(a)) if (k !== 'kind' && !keys.includes(k)) fail(where, `attachment ${name}: unknown key ${k}`);
  }
  const owns = (path) => catalogue.modelDirs.some((d) => inside(path, d));
  let outputs;
  if (json.multiblock) {
    if (json.outputs) fail(where, 'a multiblock derives its outputs; remove outputs');
    const m = json.multiblock;
    for (const k of Object.keys(m)) if (!MULTIBLOCK_KEYS.has(k)) fail(where, `multiblock: unknown key ${k}`);
    const legacy = Array.isArray(m.cells) && m.cells.length === 2 && m.cells.every((n) => Number.isInteger(n) && n > 0);
    const explicit = Array.isArray(m.cells) && m.cells.length > 0 && m.cells.every((c) => Array.isArray(c) && c.length === 3);
    if (!legacy && !explicit) fail(where, 'multiblock.cells is [width, height] or a list of [right, up, depth] cells');
    if (explicit) {
      const problems = cellProblems(declaredCells(m.cells));
      if (problems.length) fail(where, `multiblock.cells: ${problems.join('; ')}`);
    }
    if (!owns(`${m.dir}/x`)) fail(where, `multiblock.dir ${m.dir} is outside the compiler's model_dirs`);
    const names = new Set(['part', 'assembly']);
    for (const o of m.overlays ?? []) {
      if (!o.name || !o.group) fail(where, 'each overlay needs name and group');
      if (names.has(o.name)) fail(where, `output name ${o.name} is used twice`);
      names.add(o.name);
    }
    for (const v of m.variants ?? []) {
      for (const k of Object.keys(v)) if (!VARIANT_KEYS.has(k)) fail(where, `variant ${v.name}: unknown key ${k}`);
      if (!/^[a-z0-9_]+$/.test(v.name ?? '')) fail(where, 'each variant needs a lower-case name');
      if (names.has(v.name)) fail(where, `output name ${v.name} is used twice`);
      names.add(v.name);
    }
    outputs = null;
  } else {
    outputs = json.outputs ?? fail(where, 'no outputs');
    if (!outputs.length) fail(where, 'no outputs');
    if (outputs.filter((o) => o.item).length !== 1) fail(where, 'exactly one output is the item model (item: true)');
    for (const o of outputs) {
      for (const k of Object.keys(o)) if (!OUTPUT_KEYS.has(k)) fail(where, `output ${o.model}: unknown key ${k}`);
      if (!owns(o.model)) fail(where, `output ${o.model} is outside the compiler's model_dirs`);
    }
  }
  return { ...json, textures, owned_textures: owned, attachments, outputs };
}

/** Every project: [{dir, piece, projectFile}] in catalogue order. */
export function loadPieces(root, catalogue = loadCatalogue(root)) {
  return catalogue.projects.map((dir) => {
    const piece = validatePiece(readJson(join(root, SOURCE_DIR, dir, 'piece.json'), `${dir}/piece.json`), dir, catalogue);
    return { dir, piece, projectFile: join(root, SOURCE_DIR, dir, piece.project) };
  });
}
