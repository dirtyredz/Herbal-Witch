// Texture ownership for Blockbench projects. Every project texture is embedded in the .bbmodel and
// identified by its id (the model's texture variable); piece.json binds each id to a resource:
//   shared slots point at an existing canonical PNG (the procedural house materials, the botany
//     atlas, bundle sprites). The embedded copy is only an editor preview and must show the same
//     pixels; it never overwrites the canonical PNG. `refreshPreviews` updates the copies after an
//     intentional palette change.
//   owned slots (piece.json `owned_textures`) are painted in Blockbench: the compiler exports the
//     embedded bitmap through the repository's deterministic PNG encoder.
// Transparency (and so `render_type: cutout`) is read from the resolved pixels, never from names.
import { decodePng } from '../image/png-decode.mjs';
import { png } from '../image/png-encode.mjs';

const PREFIX = 'data:image/png;base64,';

/** Decodes an embedded texture's data URL to {width, height, rgba}. */
export function decodeDataUrl(source, where) {
  if (!source?.startsWith(PREFIX)) throw new Error(`${where}: not an embedded PNG`);
  return decodePng(Buffer.from(source.slice(PREFIX.length), 'base64'), where);
}

/** A data URL embedding PNG bytes. */
export const dataUrl = (bytes) => `${PREFIX}${bytes.toString('base64')}`;

/** Whether any pixel is not fully opaque. */
export const hasTransparency = (img) => img.rgba.some((v, i) => i % 4 === 3 && v !== 255);

/** Whether every pixel is fully opaque or fully transparent. */
export const binaryAlpha = (img) => img.rgba.every((v, i) => i % 4 !== 3 || v === 0 || v === 255);

/**
 * Whether two images show the same pixels. Colour under a fully transparent pixel is invisible
 * (and an editor canvas does not keep it), so only alpha is compared there.
 */
export function samePixels(a, b) {
  if (a.width !== b.width || a.height !== b.height) return false;
  for (let i = 0; i < a.rgba.length; i += 4) {
    if (a.rgba[i + 3] !== b.rgba[i + 3]) return false;
    if (a.rgba[i + 3] === 0) continue;
    if (a.rgba[i] !== b.rgba[i] || a.rgba[i + 1] !== b.rgba[i + 1] || a.rgba[i + 2] !== b.rgba[i + 2]) return false;
  }
  return true;
}

/** 'ns:path' -> 'path.png' relative to assets/<ns>/textures. */
export function texturePath(id) {
  const [ns, path] = id.includes(':') ? id.split(':') : ['minecraft', id];
  if (ns !== 'herbalwitch') throw new Error(`texture ${id} is outside the herbalwitch namespace`);
  return `${path}.png`;
}

/**
 * Resolves a project's embedded textures against its descriptor.
 * @param where       project label for errors
 * @param project     parseProject() result
 * @param piece       the validated descriptor
 * @param canonical   (textureId) -> decoded canonical image or null
 * @returns {owned: Map<texture path, PNG Buffer>, image: (textureId) -> decoded image}
 */
export function resolveTextures(where, project, piece, canonical) {
  const owned = new Map();
  const ownedImages = new Map();
  for (const t of project.textures) {
    const at = `${where} texture ${t.id}`;
    const id = piece.textures[t.id];
    if (!id) throw new Error(`${at}: not bound in piece.json textures`);
    if (id.startsWith('#')) throw new Error(`${at}: a project texture must bind a texture id, not ${id}`);
    const img = decodeDataUrl(t.source, at);
    if (piece.owned_textures[t.id]) {
      const was = canonical(id);
      if (was && (img.width !== was.width || img.height !== was.height)) {
        throw new Error(`${at}: owned textures keep their resolution (${was.width} x ${was.height}, found ${img.width} x ${img.height})`);
      }
      if (img.width !== img.height || img.width < 16 || (img.width & (img.width - 1))) throw new Error(`${at}: textures are square powers of two (found ${img.width} x ${img.height})`);
      if (!binaryAlpha(img)) throw new Error(`${at}: alpha must be fully opaque or fully transparent (cutout)`);
      const bytes = png({ width: img.width, height: img.height, px: img.rgba });
      const path = texturePath(id);
      if (owned.has(path)) throw new Error(`${at}: ${id} is written twice`);
      owned.set(path, bytes);
      ownedImages.set(id, img);
      continue;
    }
    const ref = canonical(id);
    if (!ref) throw new Error(`${at}: shared texture ${id} has no canonical PNG (run node tools/generate-textures.mjs)`);
    if (!samePixels(img, ref)) {
      throw new Error(`${at}: the embedded preview differs from the shared ${id}. Shared materials are edited in tools/art; `
        + 'run node tools/compile-blockbench.mjs --refresh-previews after a palette change, or bind a piece-owned texture');
    }
  }
  for (const [v, id] of Object.entries(piece.owned_textures)) {
    if (!project.textures.some((t) => t.id === v)) throw new Error(`${where}: owned texture ${v} (${id}) is not embedded in the project`);
  }
  return { owned, image: (id) => ownedImages.get(id) ?? canonical(id) };
}

/**
 * Rewrites a project's shared-texture previews from the canonical PNGs (the only operation that
 * writes a .bbmodel). Returns the updated JSON object and the ids it changed.
 */
export function refreshPreviews(json, piece, canonicalBytes) {
  const changed = [];
  for (const t of json.textures ?? []) {
    if (piece.owned_textures[t.id] || !piece.textures[t.id]) continue;
    const bytes = canonicalBytes(piece.textures[t.id]);
    if (!bytes) throw new Error(`no canonical PNG for ${piece.textures[t.id]}`);
    const url = dataUrl(bytes);
    if (t.source !== url) {
      t.source = url;
      const img = decodePng(bytes, t.id);
      t.width = img.width;
      t.height = img.height;
      changed.push(t.id);
    }
  }
  return { json, changed };
}
