// item/generated sprites: the GUI shows layer textures flat at 16 x 16, stacked in layer order.
// The in-hand extrusion (one-pixel edge quads per opaque texel) is not drawn; item sprites are
// reviewed in the GUI context only.

/** Composites the model's layerN textures into one RGBA image at `scale` pixels per texel unit. */
export function generatedItemImage(res, model, resolveTexture, size) {
  const out = { width: size, height: size, px: new Uint8Array(size * size * 4) };
  for (let layer = 0; ; layer++) {
    const ref = model.textures[`layer${layer}`];
    if (!ref) {
      if (layer === 0) throw new Error(`${model.id}: item/generated without layer0`);
      break;
    }
    const id = resolveTexture(model, `#layer${layer}`);
    const tex = id && res.texture(id);
    if (!tex) throw new Error(`${model.id}: missing texture ${id ?? ref}`);
    for (let y = 0; y < size; y++) {
      for (let x = 0; x < size; x++) {
        const tx = Math.floor((x * tex.width) / size);
        const ty = Math.floor((y * tex.height) / size);
        const si = (ty * tex.width + tx) * 4;
        if (tex.rgba[si + 3] < 26) continue;
        out.px.set(tex.rgba.subarray(si, si + 4), (y * size + x) * 4);
        out.px[(y * size + x) * 4 + 3] = 255;
      }
    }
  }
  return out;
}
