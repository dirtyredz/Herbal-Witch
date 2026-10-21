// Generated attachments: geometry that stays procedural inside a hand-authored piece, so there is
// one source for it across the mod. Today that is the dried herb bundle (tools/models/preparations/
// bundles.mjs), shared by the hung bundle blocks, the drying rack, the stove and the apothecary
// counter. In the .bbmodel an attachment is a group named like its piece.json `attachments` entry;
// the group's pivot is the anchor (x, top of the bundle, z). Any cubes inside the group are an
// editor preview only and are ignored: compilation regenerates the bundle at the anchor.
import { bundle } from '../models/preparations/bundles.mjs';
import { explicitUvs } from '../models/geometry.mjs';

/** Generated elements (explicit UVs) for attachment `def` anchored at `origin`. */
export function expandAttachment(def, origin) {
  if (def.kind !== 'bundle') throw new Error(`unknown attachment kind ${def.kind}`);
  const [x, top, z] = origin;
  return explicitUvs(bundle(def.herb, { at: [x, z], top, width: def.width, height: def.height, across: def.across }));
}
