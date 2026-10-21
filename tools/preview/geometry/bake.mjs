// Model baking with caching: resolve a model, bake its elements to quads with resolved texture ids,
// and record what it used. Tint indices fail: this mod registers no colour handlers, so a tinted
// face would render differently in game than any fixed colour we could guess.
import { resolveModel, resolveTexture } from '../assets/models.mjs';
import { bakeElement } from './elements.mjs';

export class Baker {
  constructor(res) {
    this.res = res;
    this.cache = new Map();
    this.used = new Set(); // every model id baked (including parents)
  }

  /** {model, quads} for a model id; quads are in model pixels, unrotated. */
  bake(id) {
    if (this.cache.has(id)) return this.cache.get(id);
    const model = resolveModel(this.res, id);
    model.chain.forEach((c) => this.used.add(c));
    const quads = [];
    for (const element of model.elements) {
      for (const q of bakeElement(element, (ref) => resolveTexture(model, ref))) {
        if (q.tint >= 0) throw new Error(`${id}: face uses tintindex ${q.tint}, but the mod registers no colour handlers`);
        quads.push(q);
      }
    }
    const baked = { model, quads };
    this.cache.set(id, baked);
    return baked;
  }
}
