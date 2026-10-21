// The Blockbench project contract: reads, validates and writes `.bbmodel` files saved by desktop
// Blockbench 5.2.2 (project container format 5.0, the "Java Block/Item" format with the
// "1.9 - 1.21.5" Minecraft target, java_block_version 1.9.0). The format is Blockbench's internal
// save format, so this parser is deliberately narrow: it accepts exactly the features the mod's
// models use and names the project, group or element and field of anything else. Editor-only
// metadata (selection, collections, reference images, visibility, colours, lock state) is ignored.
// Nothing here knows about Minecraft resource ids; piece.json binds those (catalogue.mjs).
import { createHash } from 'node:crypto';

export const FORMAT_VERSION = '5.0';
export const JAVA_BLOCK_VERSION = '1.9.0';
export const BLOCKBENCH_VERSION = '5.2.2';
export const FACES = ['north', 'east', 'south', 'west', 'up', 'down'];
const LEGAL_ANGLES = new Set([-45, -22.5, 0, 22.5, 45]);
const CULLFACES = new Set(['', 'north', 'south', 'west', 'east', 'up', 'down']);
export const DISPLAY_SLOTS = ['thirdperson_righthand', 'thirdperson_lefthand', 'firstperson_righthand', 'firstperson_lefthand', 'ground', 'gui', 'head', 'fixed'];
const FORBIDDEN_ROOT = ['animations', 'animation_controllers', 'overrides', 'skin_model', 'model_identifier'];

const fail = (where, message) => {
  throw new Error(`${where}: ${message}`);
};
const isVec = (v, n = 3) => Array.isArray(v) && v.length === n && v.every((x) => typeof x === 'number' && Number.isFinite(x));
const zero = (v) => !v || v.every((x) => x === 0);

/** Validates one display slot: rotation/translation/scale only (no pivots, which 1.20.1 ignores). */
function readDisplaySlot(where, slot) {
  for (const k of Object.keys(slot)) {
    if (k === 'rotation_pivot' || k === 'scale_pivot') {
      if (!zero(slot[k])) fail(where, `${k} is not supported by Minecraft 1.20.1`);
    } else if (!['rotation', 'translation', 'scale'].includes(k)) fail(where, `unknown display field ${k}`);
  }
  const out = {
    rotation: slot.rotation ?? [0, 0, 0],
    translation: slot.translation ?? [0, 0, 0],
    scale: slot.scale ?? [1, 1, 1],
  };
  for (const [k, v] of Object.entries(out)) if (!isVec(v)) fail(where, `${k} must be three numbers`);
  // The editor wraps angles into -180..180 (225 is saved as -135); both turn the item the same
  // way, so one canonical range keeps an unchanged save compiling to the committed model.
  out.rotation = out.rotation.map((a) => ((a % 360) + 360) % 360);
  return out;
}

/** The element rotation of a Blockbench cube as Minecraft's single-axis form, or null. */
function readRotation(where, el) {
  const r = el.rotation ?? [0, 0, 0];
  if (!isVec(r)) fail(where, 'rotation must be three numbers');
  const axes = [0, 1, 2].filter((i) => r[i] !== 0);
  if (axes.length > 1) fail(where, `rotation ${JSON.stringify(r)} turns about more than one axis (1.20.1 allows one)`);
  if (!axes.length) {
    if (el.rescale) fail(where, 'rescale without a rotation');
    return null;
  }
  const angle = r[axes[0]];
  if (!LEGAL_ANGLES.has(angle)) fail(where, `rotation angle ${angle} is not one of 0, +-22.5, +-45`);
  if (!isVec(el.origin)) fail(where, 'a rotated element needs an origin');
  return { angle, axis: 'xyz'[axes[0]], origin: [...el.origin], ...(el.rescale ? { rescale: true } : {}) };
}

function readFace(where, dir, face, textureCount) {
  if (face.tint !== undefined && face.tint !== -1) fail(where, `${dir} face: tint index ${face.tint} (models are untinted)`);
  if (face.enabled === false) fail(where, `${dir} face is disabled; clear its texture instead`);
  if (face.texture === null || face.texture === undefined || face.texture === false) return null;
  if (!Number.isInteger(face.texture) || face.texture < 0 || face.texture >= textureCount) {
    fail(where, `${dir} face: texture reference ${JSON.stringify(face.texture)} is not a texture of this project`);
  }
  if (!isVec(face.uv, 4)) fail(where, `${dir} face: uv must be four numbers`);
  const rotation = face.rotation ?? 0;
  if (![0, 90, 180, 270].includes(rotation)) fail(where, `${dir} face: uv rotation ${rotation}`);
  const cullface = face.cullface ?? '';
  if (!CULLFACES.has(cullface)) fail(where, `${dir} face: cullface ${cullface}`);
  return { uv: [...face.uv], texture: face.texture, ...(rotation ? { rotation } : {}), ...(cullface ? { cullface } : {}) };
}

function readElement(where, el, textureCount) {
  if ((el.type ?? 'cube') !== 'cube') fail(where, `${el.type} elements are not supported (cubes only)`);
  if (el.box_uv) fail(where, 'box UV is not supported (per-face UVs only)');
  if (el.inflate) fail(where, 'inflate is not supported; change from/to instead');
  if (el.stretch && !el.stretch.every((v) => v === 1)) fail(where, 'stretch is not supported');
  if (el.light_emission) fail(where, 'light_emission is not part of Minecraft 1.20.1; glow comes from piece.json emissive groups');
  if (el.shade_direction_override) fail(where, 'shade_direction_override is not part of Minecraft 1.20.1');
  if (!isVec(el.from) || !isVec(el.to)) fail(where, 'from/to must be three numbers');
  if (el.from.some((v, i) => v > el.to[i])) fail(where, `from ${JSON.stringify(el.from)} exceeds to ${JSON.stringify(el.to)}`);
  const faces = {};
  for (const dir of Object.keys(el.faces ?? {})) {
    if (!FACES.includes(dir)) fail(where, `unknown face ${dir}`);
    const face = readFace(where, dir, el.faces[dir], textureCount);
    if (face) faces[dir] = face;
  }
  return {
    uuid: el.uuid, name: el.name ?? 'cube', from: [...el.from], to: [...el.to],
    rotation: readRotation(where, el), shade: el.shade !== false, faces,
  };
}

function readTexture(where, t, index) {
  const at = `${where} texture ${index} (${t.name ?? t.id})`;
  if (!t.id || typeof t.id !== 'string') fail(at, 'needs an id (the model texture variable)');
  if (typeof t.source !== 'string' || !t.source.startsWith('data:image/png;base64,')) {
    fail(at, 'must be embedded (File > Preferences: embed textures, or Save Project with the texture loaded)');
  }
  return { index, uuid: t.uuid, id: t.id, name: t.name, namespace: t.namespace, folder: t.folder, particle: !!t.particle, source: t.source };
}

/**
 * Parses and validates a project. Returns {name, resolution, ao, textures, tree, elements, display}:
 * `tree` is the outliner as [{group: {name, origin, uuid}, children}] / {element} nodes, and
 * `elements` lists every exported element in outliner order with its group path (names).
 */
export function parseProject(text, where = 'project') {
  let json;
  try {
    json = JSON.parse(text);
  } catch (e) {
    fail(where, `not valid JSON (${e.message}); if Blockbench is still saving, try again`);
  }
  const meta = json.meta ?? fail(where, 'no meta block; not a Blockbench project');
  if (meta.format_version !== FORMAT_VERSION) {
    fail(where, `project format ${meta.format_version}; this pipeline reads ${FORMAT_VERSION} (Blockbench ${BLOCKBENCH_VERSION}). Re-save it with Blockbench ${BLOCKBENCH_VERSION}`);
  }
  if (meta.model_format !== 'java_block') fail(where, `model format ${meta.model_format}; use "Java Block/Item"`);
  if (meta.box_uv) fail(where, 'box UV projects are not supported');
  if (json.java_block_version !== JAVA_BLOCK_VERSION) {
    fail(where, `Minecraft target ${json.java_block_version}; set File > Project > Minecraft version to "1.9 - 1.21.5" (${JAVA_BLOCK_VERSION})`);
  }
  for (const k of FORBIDDEN_ROOT) {
    const v = json[k];
    if (v && (!Array.isArray(v) || v.length) && !(typeof v === 'object' && !Array.isArray(v) && !Object.keys(v).length)) fail(where, `${k} is not supported`);
  }
  if (json.parent) fail(where, `parent ${json.parent}: projects are complete models`);
  const resolution = json.resolution ?? { width: 16, height: 16 };
  if (!(resolution.width > 0 && resolution.height > 0)) fail(where, 'bad resolution');
  const textures = (json.textures ?? []).map((t, i) => readTexture(where, t, i));
  const ids = new Set();
  for (const t of textures) {
    if (ids.has(t.id)) fail(where, `two textures use the id ${t.id}`);
    ids.add(t.id);
  }
  const rawElements = new Map((json.elements ?? []).map((e) => [e.uuid, e]));
  const rawGroups = new Map((json.groups ?? []).map((g) => [g.uuid, g]));
  const seen = new Set();
  const elements = [];
  const walk = (nodes, path, exporting = true) => nodes.map((node) => {
    if (typeof node === 'string') {
      const raw = rawElements.get(node) ?? fail(where, `outliner names a missing element ${node}`);
      if (seen.has(node)) fail(where, `element ${raw.name} appears twice in the outliner`);
      seen.add(node);
      const el = readElement(`${where} ${[...path, raw.name ?? 'cube'].join('/')}`, raw, textures.length);
      if (raw.export === false || !exporting) return null;
      el.path = path;
      elements.push(el);
      return { element: el };
    }
    const g = rawGroups.get(node.uuid) ?? (node.name !== undefined ? node : fail(where, `outliner names a missing group ${node.uuid}`));
    const gwhere = `${where} group ${[...path, g.name].join('/')}`;
    if (!zero(g.rotation)) fail(gwhere, `group rotation ${JSON.stringify(g.rotation)} is not supported; rotate the elements`);
    const children = walk(node.children ?? [], [...path, g.name], exporting && g.export !== false).filter(Boolean);
    if (g.export === false || !exporting) return null;
    return { group: { name: g.name, origin: isVec(g.origin) ? [...g.origin] : [8, 8, 8], uuid: g.uuid }, children };
  }).filter(Boolean);
  const tree = walk(json.outliner ?? [], []);
  for (const uuid of rawElements.keys()) if (!seen.has(uuid)) fail(where, `element ${rawElements.get(uuid).name} is not in the outliner`);
  const display = {};
  for (const [slot, value] of Object.entries(json.display ?? {})) {
    if (!DISPLAY_SLOTS.includes(slot)) fail(where, `unknown display slot ${slot}`);
    if (value && Object.keys(value).length) display[slot] = readDisplaySlot(`${where} display ${slot}`, value);
  }
  return { name: json.name, resolution, ao: json.ambientocclusion !== false, textures, tree, elements, display };
}

/** A stable UUID for the n-th thing of a kind in a project (reproducible imports and fixtures). */
export function stableUuid(project, kind, n) {
  const h = createHash('sha1').update(`${project}:${kind}:${n}`).digest('hex');
  return `${h.slice(0, 8)}-${h.slice(8, 12)}-4${h.slice(13, 16)}-${'89ab'[parseInt(h[16], 16) % 4]}${h.slice(17, 20)}-${h.slice(20, 32)}`;
}

const DISPLAY_DEFAULTS = { rotation: [0, 0, 0], translation: [0, 0, 0], scale: [1, 1, 1] };

/**
 * Writes a project the way Blockbench 5.2.2 saves one (embedded textures, groups listed apart
 * from the outliner). `spec`: {name, textures: [{id, name, namespace, folder, png (Buffer),
 * particle}], tree: [{group: {name, origin}, children} | {element: {name, from, to, rotation,
 * shade, faces: {dir: {uv, texture (index), rotation?, cullface?}}}}], display, ao}.
 */
export function writeProject(spec) {
  const elements = [];
  const groups = [];
  let ne = 0;
  let ng = 0;
  const walk = (nodes) => nodes.map((node) => {
    if (node.element) {
      const e = node.element;
      const uuid = stableUuid(spec.name, 'element', ne++);
      const faces = {};
      for (const dir of FACES) {
        const f = e.faces[dir];
        faces[dir] = f
          ? { uv: f.uv, ...(f.rotation ? { rotation: f.rotation } : {}), ...(f.cullface ? { cullface: f.cullface } : {}), texture: f.texture }
          : { uv: [0, 0, 0, 0], texture: null };
      }
      const rot = [0, 0, 0];
      if (e.rotation) rot['xyz'.indexOf(e.rotation.axis)] = e.rotation.angle;
      elements.push({
        name: e.name ?? 'cube', box_uv: false, rescale: !!e.rotation?.rescale, locked: false, render_order: 'default',
        from: e.from, to: e.to, autouv: 0, color: (ne - 1) % 8, ...(e.shade === false ? { shade: false } : {}),
        ...(e.rotation ? { rotation: rot } : {}), origin: e.rotation ? e.rotation.origin : e.origin ?? [8, 8, 8],
        faces, type: 'cube', uuid,
      });
      return uuid;
    }
    const g = node.group;
    const uuid = stableUuid(spec.name, 'group', ng++);
    groups.push({
      name: g.name, origin: g.origin ?? [8, 8, 8], color: (ng - 1) % 8, uuid, export: true, mirror_uv: false,
      isOpen: false, locked: false, visibility: true, autouv: 0, selected: false,
    });
    return { uuid, isOpen: false, children: walk(node.children) };
  });
  const outliner = walk(spec.tree);
  const textures = spec.textures.map((t, i) => ({
    path: '', name: t.name, folder: t.folder ?? '', namespace: t.namespace ?? '', id: t.id, group: '',
    width: t.width ?? 16, height: t.height ?? 16, uv_width: 16, uv_height: 16, particle: !!t.particle,
    use_as_default: false, layers_enabled: false, sync_to_project: '', render_mode: 'default', render_sides: 'auto',
    pbr_channel: 'color', frame_time: 1, frame_order_type: 'loop', frame_order: '', frame_interpolate: false,
    visible: true, internal: true, saved: true, uuid: stableUuid(spec.name, 'texture', i),
    source: `data:image/png;base64,${t.png.toString('base64')}`,
  }));
  const display = {};
  for (const slot of DISPLAY_SLOTS) {
    const d = spec.display?.[slot];
    if (!d) continue;
    const out = {};
    for (const k of ['rotation', 'translation', 'scale']) if (d[k] && d[k].some((v, i) => v !== DISPLAY_DEFAULTS[k][i])) out[k] = d[k];
    display[slot] = out;
  }
  const model = {
    meta: { format_version: FORMAT_VERSION, model_format: 'java_block', box_uv: false },
    name: spec.name, parent: '', ambientocclusion: spec.ao !== false, front_gui_light: false, visible_box: [1, 1, 0],
    variable_placeholders: '', variable_placeholder_buttons: [], unhandled_root_fields: {},
    java_block_version: JAVA_BLOCK_VERSION, resolution: { width: 16, height: 16 },
    elements, groups, outliner, textures, ...(Object.keys(display).length ? { display } : {}),
  };
  return compileJson(model);
}

/**
 * JSON in Blockbench's layout: tab indentation, short arrays of numbers on one line. Formatting
 * only; the parser accepts any JSON.
 */
export function compileJson(value) {
  const text = JSON.stringify(value, null, '\t');
  return text.replace(/\[\s+([-\d.e,\s]+?)\s+\]/g, (m, inner) => (inner.includes('\n') ? `[${inner.split(/,\s*/).join(', ')}]` : m)) + '\n';
}
