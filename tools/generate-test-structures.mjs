// Generates the GameTest structure templates (gzipped NBT, no dependencies).
//
//   node tools/generate-test-structures.mjs
//
// Writes src/gameTest/resources/data/herbalwitch/structures/<name>.nbt: only a size and an air
// palette; GameTests build their own floor. Adapted from Roundwood's generator (same author, MIT).
import { writeFileSync, mkdirSync } from 'node:fs';
import { gzipSync } from 'node:zlib';
import { dirname, join } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '..');
const outDir = join(root, 'src/gameTest/resources/data/herbalwitch/structures');
const DATA_VERSION = 3465; // 1.20.1

const TAG = { end: 0, int: 3, string: 8, list: 9, compound: 10 };
const u8 = (n) => Buffer.from([n]);
const u16 = (n) => {
  const b = Buffer.alloc(2);
  b.writeUInt16BE(n);
  return b;
};
const i32 = (n) => {
  const b = Buffer.alloc(4);
  b.writeInt32BE(n);
  return b;
};
const str = (s) => Buffer.concat([u16(Buffer.byteLength(s)), Buffer.from(s, 'utf8')]);
const named = (type, name, payload) => Buffer.concat([u8(type), str(name), payload]);

function structure([x, y, z]) {
  const size = Buffer.concat([u8(TAG.int), i32(3), i32(x), i32(y), i32(z)]);
  const air = Buffer.concat([named(TAG.string, 'Name', str('minecraft:air')), u8(TAG.end)]);
  const palette = Buffer.concat([u8(TAG.compound), i32(1), air]);
  const emptyList = Buffer.concat([u8(TAG.end), i32(0)]);
  const body = Buffer.concat([
    named(TAG.int, 'DataVersion', i32(DATA_VERSION)),
    named(TAG.list, 'size', size),
    named(TAG.list, 'palette', palette),
    named(TAG.list, 'blocks', emptyList),
    named(TAG.list, 'entities', emptyList),
    u8(TAG.end),
  ]);
  // mtime 0 keeps the gzip bytes reproducible.
  return gzipSync(named(TAG.compound, '', body), { level: 9 });
}

mkdirSync(outDir, { recursive: true });
// empty: per-feature tests. showcase: the bare /herbalwitch showcase boxes: both showcases one SEPARATION apart (plan width + entrance, plan depths + separation + 1).
// showcase_section: one small section and the boundary tests.
const TEMPLATES = { empty: [9, 6, 9], showcase: [128, 10, 100], showcase_section: [16, 10, 14] };
for (const [name, size] of Object.entries(TEMPLATES)) {
  writeFileSync(join(outDir, `${name}.nbt`), structure(size));
  console.log(`wrote ${name}.nbt ${size.join('x')}`);
}
