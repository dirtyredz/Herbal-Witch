// Compiles every catalogued Blockbench project into Minecraft model JSON and owned textures, in
// memory: read and validate the project, resolve its textures, turn its outliner into elements
// (explicit UVs in Minecraft's 0..16 space, group paths, generated attachments expanded in place),
// check bounds, then produce the piece's state or cell outputs. Writing is the caller's job
// (tools/compile-blockbench.mjs through tools/art/output.mjs, or the dev-art staging pack).
import { createHash } from 'node:crypto';
import { existsSync, readFileSync } from 'node:fs';
import { join } from 'node:path';
import { decodePng } from '../image/png-decode.mjs';
import { modelJson, modelText, usedVariables } from '../models/model-output.mjs';
import { parseProject } from './project.mjs';
import { loadCatalogue, loadPieces } from './catalogue.mjs';
import { resolveTextures, hasTransparency, texturePath } from './textures.mjs';
import { expandAttachment } from './attachments.mjs';
import { singleOutputs, multiblockOutputs } from './assembly.mjs';

export const TEXTURE_ROOT = 'src/main/resources/assets/herbalwitch/textures';

/** Canonical textures read from a textures root, decoded once. */
export function canonicalTextures(textureRoot, extra = new Map()) {
  const cache = new Map();
  const bytes = (id) => {
    const rel = texturePath(id);
    if (extra.has(rel)) return extra.get(rel);
    const f = join(textureRoot, rel);
    return existsSync(f) ? readFileSync(f) : null;
  };
  const image = (id) => {
    if (!cache.has(id)) {
      const b = bytes(id);
      cache.set(id, b ? decodePng(b, id) : null);
    }
    return cache.get(id);
  };
  return { bytes, image };
}

/** Elements of the outliner in order, attachments expanded, UVs scaled to 0..16. */
function projectElements(where, project, piece) {
  const su = 16 / project.resolution.width;
  const sv = 16 / project.resolution.height;
  const out = [];
  const used = new Set();
  const walk = (nodes, path) => {
    for (const node of nodes) {
      if (node.element) {
        const e = node.element;
        const faces = {};
        for (const [f, face] of Object.entries(e.faces)) {
          faces[f] = {
            uv: [face.uv[0] * su, face.uv[1] * sv, face.uv[2] * su, face.uv[3] * sv], texture: `#${project.textures[face.texture].id}`,
            ...(face.rotation ? { rotation: face.rotation } : {}), ...(face.cullface ? { cullface: face.cullface } : {}),
          };
        }
        if (!Object.keys(faces).length) continue; // an element with every face cleared draws nothing
        out.push({ from: e.from, to: e.to, ...(e.rotation ? { rotation: e.rotation } : {}), ...(e.shade ? {} : { shade: false }), faces, path, name: e.name });
        continue;
      }
      const { name, origin } = node.group;
      const def = piece.attachments[name];
      if (def) {
        if (used.has(name)) throw new Error(`${where}: attachment group ${name} appears twice`);
        used.add(name);
        for (const e of expandAttachment(def, origin)) out.push({ ...e, path: [...path, name], generated: true });
        continue;
      }
      walk(node.children, [...path, name]);
    }
  };
  walk(project.tree, []);
  for (const name of Object.keys(piece.attachments)) if (!used.has(name)) throw new Error(`${where}: attachment ${name} has no group in the project`);
  const { min, max } = piece.bounds;
  for (const e of out) {
    if (e.from.some((v, i) => v < min[i] - 1e-9) || e.to.some((v, i) => v > max[i] + 1e-9)) {
      throw new Error(`${where} ${[...e.path, e.name ?? 'generated'].join('/')}: ${JSON.stringify(e.from)}..${JSON.stringify(e.to)} leaves the permitted bounds ${JSON.stringify(min)}..${JSON.stringify(max)}`);
    }
  }
  return out;
}

/** Resolves a texture variable through the descriptor's bindings (following #references). */
function resolveVariable(textures, v) {
  let value = `#${v}`;
  for (let i = 0; i < 10 && value?.startsWith('#'); i++) value = textures[value.slice(1)];
  return value ?? null;
}

/**
 * Compiles one project. Returns {models: Map<model file path, text>, textures: Map<texture path, PNG>}.
 * `text` is the .bbmodel content (so a staging run can compile unsaved-to-repo sources).
 */
export function compileProject({ dir, piece }, text, canonical) {
  const where = dir;
  const project = parseProject(text, where);
  const { owned, image } = resolveTextures(where, project, piece, canonical.image);
  const elements = projectElements(where, project, piece);
  const outputs = piece.multiblock ? multiblockOutputs(piece, elements) : singleOutputs(piece, elements);
  const models = new Map();
  for (const o of outputs) {
    const vars = usedVariables(o.elements);
    let cutout = false;
    for (const v of vars) {
      const id = resolveVariable(piece.textures, v) ?? (() => { throw new Error(`${where} ${o.path}: #${v} is not bound in piece.json`); })();
      const img = image(id) ?? (() => { throw new Error(`${where} ${o.path}: texture ${id} does not exist`); })();
      cutout ||= hasTransparency(img);
    }
    const display = o.item && Object.keys(project.display).length ? project.display : null;
    const json = modelJson({ textures: piece.textures, elements: o.elements, cutout, display, ao: project.ao });
    const path = `${o.path}.json`;
    if (models.has(path)) throw new Error(`${where}: ${o.path} is written twice`);
    models.set(path, modelText(json));
  }
  return { models, textures: owned };
}

/**
 * Compiles the whole catalogue. `readText(projectFile)` supplies project contents (default: the
 * file). With a `cache` (a Map kept between calls, as the dev-art watcher does) a project whose
 * text and descriptor are unchanged is not recompiled. Returns {models, textures, catalogue,
 * pieces, changed (project dirs compiled afresh), owners (model path -> project dir)}.
 */
export function compileAll(root, { readText = (f) => readFileSync(f, 'utf8'), textureRoot = join(root, TEXTURE_ROOT), cache = null } = {}) {
  const catalogue = loadCatalogue(root);
  const pieces = loadPieces(root, catalogue);
  const models = new Map();
  const textures = new Map();
  const canonical = canonicalTextures(textureRoot, textures);
  const changed = [];
  const owners = new Map();
  for (const p of pieces) {
    const text = readText(p.projectFile);
    const key = createHash('sha1').update(text).update(JSON.stringify(p.piece)).digest('hex');
    let out = cache?.get(p.dir)?.key === key ? cache.get(p.dir).out : null;
    if (!out) {
      out = compileProject(p, text, canonical);
      cache?.set(p.dir, { key, out });
      changed.push(p.dir);
    }
    for (const [k, v] of out.models) {
      if (models.has(k)) throw new Error(`${p.dir}: ${k} is also written by another project`);
      models.set(k, v);
      owners.set(k, p.dir);
    }
    for (const [k, v] of out.textures) {
      if (textures.has(k)) throw new Error(`${p.dir}: texture ${k} is also owned by another project`);
      textures.set(k, v);
    }
  }
  return { models, textures, catalogue, pieces, changed, owners };
}
