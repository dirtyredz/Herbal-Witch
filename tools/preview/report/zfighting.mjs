// The z-fighting sweep: every block model on its own, and every blockstate combination as the game
// assembles it (multipart parts and overlays together, after their rotations), checked for
// overlapping same-facing coplanar faces (geometry/coplanar.mjs). Returns diagnostics, never fixes.
import { enumerate, select, stateLabel } from '../scenes/blockstates.mjs';
import { variantRotation } from '../geometry/transforms.mjs';
import { coplanarOverlaps, describeQuad } from '../geometry/coplanar.mjs';

function report(where, quads, errors) {
  for (const { a, b, area } of coplanarOverlaps(quads)) {
    errors.push(`${where}: coplanar faces overlap (${area.toFixed(2)} px2): ${describeQuad(quads[a])} and ${describeQuad(quads[b])}`);
  }
}

/** Diagnostics for every block model and every blockstate combination of the mod. */
export function zFighting(res, baker) {
  const errors = [];
  for (const id of res.ids('models').filter((m) => m.startsWith('herbalwitch:block/'))) {
    let quads;
    try {
      quads = baker.bake(id).quads;
    } catch {
      continue; // unresolvable models are coverage's diagnostics, not this sweep's
    }
    report(id, quads, errors);
  }
  for (const block of res.ids('blockstates')) {
    const bs = res.json('blockstates', block);
    if (!bs.multipart) continue; // a variant is one model, already checked above
    for (const props of enumerate(bs)) {
      const quads = select(bs, props).flatMap((p) => variantRotation(baker.bake(p.model).quads, p));
      report(`${block} [${stateLabel(props)}]`, quads, errors);
    }
  }
  return [...new Set(errors)];
}
