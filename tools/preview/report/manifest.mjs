// The review manifest: what was rendered (coverage), what failed (diagnostics), the settings, and
// a SHA-256 of every input file so a review can be tied to exact assets.
import { createHash } from 'node:crypto';
import { readFileSync, readdirSync, statSync, existsSync } from 'node:fs';
import { join, relative, sep } from 'node:path';

function walk(dir) {
  if (!existsSync(dir)) return [];
  return readdirSync(dir).sort().flatMap((n) => {
    const f = join(dir, n);
    return statSync(f).isDirectory() ? walk(f) : [f];
  });
}

/** SHA-256 of every model, blockstate and texture under the two asset trees, keyed by repo path. */
export function inputHashes(root) {
  const out = {};
  for (const tree of ['src/main/resources/assets', 'src/generated/resources/assets']) {
    for (const f of walk(join(root, tree))) {
      if (!/\.(json|png)$/.test(f) || f.includes(`${sep}lang${sep}`)) continue;
      out[relative(root, f).split(sep).join('/')] = createHash('sha256').update(readFileSync(f)).digest('hex');
    }
  }
  return out;
}

/** One digest over all inputs, for a one-line review record. */
export function combinedHash(hashes) {
  const h = createHash('sha256');
  for (const [k, v] of Object.entries(hashes)) h.update(`${k} ${v}\n`);
  return h.digest('hex');
}
