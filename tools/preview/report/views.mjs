// Rendering one subject: a block state, an item in the GUI, or a scene, at a fixed world scale so
// sizes stay comparable across a sheet (never fitted per object).
import { select } from '../scenes/blockstates.mjs';
import { sceneQuads, proxyTexture } from '../scenes/world.mjs';
import { variantRotation, displayTransform } from '../geometry/transforms.mjs';
import { generatedItemImage } from '../geometry/generated-items.mjs';
import { resolveTexture } from '../assets/models.mjs';
import { render } from '../render/rasterizer.mjs';
import { VIEWS, project } from '../render/camera.mjs';

/** Texture lookup for the rasterizer: mod textures plus synthetic proxies. */
export const textureLookup = (res) => (id) => (id.startsWith('proxy:') ? proxyTexture(id) : res.texture(id));

/** The projected frame of a box (model pixels) under a camera: keeps one scale for a whole family. */
export function boxFrame(cam, min, max, margin = 1) {
  let minX = Infinity;
  let minY = Infinity;
  let maxX = -Infinity;
  let maxY = -Infinity;
  for (const x of [min[0], max[0]]) {
    for (const y of [min[1], max[1]]) {
      for (const z of [min[2], max[2]]) {
        const [px, py] = project(cam, [x, y, z]);
        minX = Math.min(minX, px);
        minY = Math.min(minY, py);
        maxX = Math.max(maxX, px);
        maxY = Math.max(maxY, py);
      }
    }
  }
  return { minX: minX - margin, minY: minY - margin, maxX: maxX + margin, maxY: maxY + margin };
}

/** A block state rendered in its own cell box ([-pad..16+pad]^3 by default). */
export function renderState(res, baker, block, props, view = 'iso', scale = 8, box = [[-2, -2, -2], [18, 18, 18]]) {
  const bs = res.json('blockstates', block);
  const picks = select(bs, props);
  const quads = picks.flatMap((p) => variantRotation(baker.bake(p.model).quads, p));
  const cam = VIEWS[view];
  return render(quads, cam, textureLookup(res), { scale, frame: boxFrame(cam, box[0], box[1]) });
}

/** An item as the GUI shows it (display.gui on a 16 x 16 slot), at `size` pixels. */
export function renderItem(res, baker, id, size = 96) {
  const { model, quads } = baker.bake(id);
  if (model.builtin === 'generated') return { image: generatedItemImage(res, model, resolveTexture, size), missing: new Set() };
  const shown = displayTransform(quads, model.display.gui);
  return render(shown, VIEWS.gui, textureLookup(res), { scale: size / 16, frame: { minX: 0, minY: -16, maxX: 16, maxY: 0 } });
}

/** A scene (see scenes/world.mjs) rendered from `view` at `scale` px per model pixel, fitted. */
export function renderScene(res, baker, entries, view = 'iso', scale = 6) {
  const { quads } = sceneQuads(entries, baker, res);
  return render(quads, VIEWS[view], textureLookup(res), { scale, margin: 4 });
}
