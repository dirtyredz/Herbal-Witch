// Deterministic, ownership-checked output writing shared by the three art producers
// (generate-textures, generate-models, compile-blockbench). A producer declares exactly which
// files it writes and which directories it owns outright; this module refuses a file outside the
// producer's ownership, writes only files whose bytes changed (reporting them), and removes stale
// files only inside the owned directories, so running one producer can never delete or overwrite
// another producer's assets. `check` compares instead of writing.
import { existsSync, mkdirSync, readFileSync, readdirSync, rmSync, statSync, writeFileSync } from 'node:fs';
import { dirname, join, relative, sep } from 'node:path';

/** Every file under `dir`, slash-separated relative to `base`, sorted. */
export function listFiles(base, dir) {
  const full = join(base, dir);
  if (!existsSync(full)) return [];
  const out = [];
  const walk = (d) => {
    for (const n of readdirSync(d).sort()) {
      const f = join(d, n);
      if (statSync(f).isDirectory()) walk(f);
      else out.push(relative(base, f).split(sep).join('/'));
    }
  };
  walk(full);
  return out;
}

const inside = (path, dir) => path === dir || path.startsWith(`${dir}/`);

/**
 * Writes (or with `check`, compares) one producer's outputs.
 * @param root     directory the relative paths are under (the assets namespace root)
 * @param files    Map of relative path -> Buffer | string, in a stable order
 * @param owner    {name, dirs: directories owned outright (stale files of `ext` there are removed),
 *                  files: further single files owned (e.g. textures in a shared directory), ext}
 * @returns {written, removed, unchanged, problems} relative paths; `problems` lists what `check` found
 */
export function writeOutputs(root, files, owner, { check = false, log = console.log } = {}) {
  const ext = owner.ext ?? '.json';
  const owned = (p) => owner.dirs.some((d) => inside(p, d)) || (owner.files ?? []).includes(p);
  for (const p of files.keys()) {
    if (p.includes('..') || p.startsWith('/') || p.includes('\\')) throw new Error(`${owner.name}: bad output path ${p}`);
    if (!owned(p)) throw new Error(`${owner.name}: ${p} is outside the paths this producer owns`);
  }
  const result = { written: [], removed: [], unchanged: [], problems: [] };
  for (const [p, content] of files) {
    const f = join(root, p);
    const bytes = Buffer.isBuffer(content) ? content : Buffer.from(content, 'utf8');
    const old = existsSync(f) ? readFileSync(f) : null;
    if (old && old.equals(bytes)) {
      result.unchanged.push(p);
      continue;
    }
    if (check) {
      result.problems.push(`${old ? 'stale' : 'missing'} ${p}`);
      continue;
    }
    mkdirSync(dirname(f), { recursive: true });
    writeFileSync(f, bytes);
    result.written.push(p);
    log(`${old ? 'updated' : 'wrote'} ${p}`);
  }
  for (const dir of owner.dirs) {
    for (const p of listFiles(root, dir)) {
      if (!p.endsWith(ext) || files.has(p)) continue;
      if (check) {
        result.problems.push(`unexpected ${p}`);
        continue;
      }
      rmSync(join(root, p));
      result.removed.push(p);
      log(`removed ${p}`);
    }
  }
  return result;
}
