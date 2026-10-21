// One-time conversion of existing Minecraft model JSON into a Blockbench project (kept for future
// migrations, e.g. herb or bed bases). The caller supplies the elements in output order with the
// group path each belongs in, the texture variables with their canonical PNG bytes, attachment
// anchors and the effective item display; the importer keeps coordinates, pivots, explicit UVs,
// UV rotations, omitted faces, shading and (optionally) cullfaces exactly, so the compiled result
// can be compared with the source for appearance parity (compare.mjs).
import { writeProject } from '../project.mjs';
import { decodePng } from '../../image/png-decode.mjs';

/**
 * Builds the outliner tree from elements carrying group paths (groups ordered by first use).
 * @param entries [{element (model JSON element), path: [group names], name?}] in output order
 * @param variables [variable] texture order of the project (face textures index into it)
 * @param keepCullfaces false for multiblocks (the compiler recomputes them per cell)
 * @param anchors {groupName: {origin, path: [parent group names], preview: [model JSON elements]}}
 */
export function buildTree(entries, variables, { keepCullfaces = true, anchors = {} } = {}) {
  const root = { children: [], index: new Map() };
  const groupAt = (path) => {
    let node = root;
    for (const name of path) {
      if (!node.index.has(name)) {
        const g = { group: { name, origin: anchors[name]?.origin ?? [8, 8, 8] }, children: [], index: new Map() };
        node.index.set(name, g);
        node.children.push(g);
      }
      node = node.index.get(name);
    }
    return node;
  };
  const convert = (el, name) => {
    const faces = {};
    for (const [dir, f] of Object.entries(el.faces)) {
      const texture = variables.indexOf(f.texture.slice(1));
      if (texture < 0) throw new Error(`no texture record for ${f.texture}`);
      faces[dir] = { uv: f.uv, texture, ...(f.rotation ? { rotation: f.rotation } : {}), ...(keepCullfaces && f.cullface ? { cullface: f.cullface } : {}) };
    }
    if (el.forge_data) throw new Error('forge_data comes from piece.json emissive groups; strip it before importing');
    if (Object.values(el.faces).some((f) => f.tintindex !== undefined)) throw new Error('tinted faces are not supported');
    return {
      element: {
        name: name ?? el.name ?? (Object.values(el.faces)[0]?.texture.slice(1) ?? 'cube'), from: el.from, to: el.to,
        ...(el.rotation ? { rotation: el.rotation } : {}), ...(el.shade === false ? { shade: false } : {}), faces,
        origin: el.rotation ? el.rotation.origin : el.from.map((v, i) => (v + el.to[i]) / 2),
      },
    };
  };
  for (const { element, path, name } of entries) groupAt(path).children.push(convert(element, name));
  for (const [name, a] of Object.entries(anchors)) {
    const node = groupAt([...a.path, name]);
    for (const el of a.preview ?? []) node.children.push(convert(el, 'preview (generated)'));
  }
  const strip = (nodes) => nodes.map((n) => (n.element ? n : { group: n.group, children: strip(n.children) }));
  return strip(root.children);
}

/**
 * A complete .bbmodel text. `textures`: [{variable, resource ('ns:path'), png (Buffer), particle}].
 */
export function importProject({ name, entries, textures, display, anchors, keepCullfaces = true }) {
  const variables = textures.map((t) => t.variable);
  const tree = buildTree(entries, variables, { keepCullfaces, anchors });
  return writeProject({
    name,
    textures: textures.map((t) => {
      const [namespace, path] = t.resource.split(':');
      const slash = path.lastIndexOf('/');
      const { width, height } = decodePng(t.png, t.variable);
      return { id: t.variable, name: `${path.slice(slash + 1)}.png`, namespace, folder: path.slice(0, slash), png: t.png, width, height, particle: !!t.particle };
    }),
    tree,
    display,
  });
}
