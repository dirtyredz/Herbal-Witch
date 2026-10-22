// The procedural model families and the model directories the generator owns outright: herbs,
// herb-bed parts and hung bundles. The one-off pieces (stations, furniture, the oil lantern) are
// Blockbench projects compiled by tools/compile-blockbench.mjs (src/art/blockbench/catalog.json).
import { bed } from './botany/bed.mjs';
import { herbModels } from './botany/plants.mjs';
import { hungBundles } from './preparations/bundles.mjs';
import { HERB_LIST } from '../art/content/botany/herbs.mjs';
import { dropBuriedFaces, explicitUvs } from './geometry.mjs';
import { modelJson, modelText } from './model-output.mjs';
import { needsCutout } from './textures.mjs';

/** Directories (under models/) whose JSON comes only from this generator. */
export const OWNED = ['block/botany/garden', 'block/preparations/bundles',
  ...new Set(HERB_LIST.map(([group]) => `block/botany/${group}`))];

/** [{path, textures, elements, cutout?, display?, ao?}] in a stable order. */
export const MODELS = () => [...bed(), ...herbModels(HERB_LIST), ...hungBundles(HERB_LIST)];

/** The generator's complete output: model file path (relative to models/) -> JSON text. */
export function proceduralModels() {
  const files = new Map();
  for (const m of MODELS()) {
    const path = `${m.path}.json`;
    if (files.has(path)) throw new Error(`two models write ${m.path}`);
    const elements = explicitUvs(dropBuriedFaces(m.elements, (e) => !needsCutout([e])));
    files.set(path, modelText(modelJson({ textures: m.textures, elements, cutout: m.cutout ?? needsCutout(m.elements), display: m.display, ao: m.ao ?? true, open: m.open })));
  }
  return files;
}
