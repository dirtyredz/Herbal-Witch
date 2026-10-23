// Anvil region files (.mca) for save-upgrade qualification: read every chunk, decode block-state
// palettes, and write chunks back after sanitising. Pure Node, no Minecraft classes.
import { readFileSync, writeFileSync, readdirSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { inflateSync, gunzipSync, deflateSync } from 'node:zlib';
import { parse, serialize, get, T } from './nbt.mjs';

const SECTOR = 4096;

/** Every stored chunk in one region file: { index, timestamp, doc }. */
export function readRegion(file) {
  const buf = readFileSync(file);
  const chunks = [];
  if (buf.length < 2 * SECTOR) return chunks;
  for (let i = 0; i < 1024; i++) {
    const loc = buf.readUInt32BE(i * 4);
    const offset = loc >>> 8;
    const sectors = loc & 0xff;
    if (!offset || !sectors) continue;
    const start = offset * SECTOR;
    if (start + 5 > buf.length) throw new Error(`${file}: chunk ${i} points past the file`);
    const length = buf.readUInt32BE(start);
    const kind = buf.readUInt8(start + 4);
    if (kind & 0x80) throw new Error(`${file}: chunk ${i} is stored externally (.mcc), not supported`);
    const data = buf.subarray(start + 5, start + 4 + length);
    const raw = kind === 2 ? inflateSync(data) : kind === 1 ? gunzipSync(data) : kind === 3 ? data : null;
    if (!raw) throw new Error(`${file}: chunk ${i} has unknown compression ${kind}`);
    chunks.push({ index: i, timestamp: buf.readUInt32BE(SECTOR + i * 4), doc: parse(raw) });
  }
  return chunks;
}

/** Writes a region file holding exactly the given chunks (zlib), sector aligned. */
export function writeRegion(file, chunks) {
  const header = Buffer.alloc(2 * SECTOR);
  const bodies = [];
  let sector = 2;
  for (const { index, timestamp, doc } of [...chunks].sort((a, b) => a.index - b.index)) {
    const data = deflateSync(serialize(doc));
    const payload = Buffer.alloc(5 + data.length);
    payload.writeUInt32BE(data.length + 1, 0);
    payload.writeUInt8(2, 4);
    data.copy(payload, 5);
    const sectors = Math.ceil(payload.length / SECTOR);
    if (sectors > 255) throw new Error(`chunk ${index} too large for one region entry`);
    const padded = Buffer.alloc(sectors * SECTOR);
    payload.copy(padded);
    header.writeUInt32BE((sector << 8) | sectors, index * 4);
    header.writeUInt32BE(timestamp >>> 0, SECTOR + index * 4);
    bodies.push(padded);
    sector += sectors;
  }
  writeFileSync(file, Buffer.concat([header, ...bodies]));
}

export const regionFiles = (dir) => (existsSync(dir) ? readdirSync(dir).filter((f) => /^r\.-?\d+\.-?\d+\.mca$/.test(f)).map((f) => join(dir, f)) : []);

/** Palette index of each of the 4096 cells of a section (y, z, x order), or null when uniform. */
export function sectionIndices(states) {
  const palette = get(states, 'palette').v.items;
  const data = get(states, 'data');
  if (palette.length <= 1 || !data) return null;
  const bits = Math.max(4, Math.ceil(Math.log2(palette.length)));
  const perLong = Math.floor(64 / bits);
  const mask = (1n << BigInt(bits)) - 1n;
  const out = new Uint16Array(4096);
  for (let i = 0; i < 4096; i++) {
    const word = BigInt.asUintN(64, data.v[Math.floor(i / perLong)]);
    out[i] = Number((word >> BigInt((i % perLong) * bits)) & mask);
  }
  return out;
}

/** Calls visit(state, x, y, z) for every cell whose palette entry matches the filter. */
export function forEachBlock(chunkDoc, filter, visit) {
  const root = chunkDoc.root;
  const cx = get(root, 'xPos').v, cz = get(root, 'zPos').v;
  const sections = get(root, 'sections');
  if (!sections) return;
  for (const section of sections.v.items) {
    const sec = { t: T.compound, v: section };
    const states = get(sec, 'block_states');
    if (!states) continue;
    const sy = get(sec, 'Y').v;
    const palette = get(states, 'palette').v.items;
    const wanted = palette.map((entry) => filter(entry));
    if (!wanted.some(Boolean)) continue;
    const idx = sectionIndices(states);
    for (let i = 0; i < 4096; i++) {
      const p = idx ? idx[i] : 0;
      if (!wanted[p]) continue;
      visit(palette[p], cx * 16 + (i & 15), sy * 16 + (i >> 8), cz * 16 + ((i >> 4) & 15));
    }
  }
}

/** "namespace:path[prop=value,...]" in sorted property order. */
export function stateString(entry) {
  const name = entry.get('Name').v;
  const props = entry.get('Properties');
  if (!props) return name;
  const pairs = [...props.v].map(([k, v]) => `${k}=${v.v}`).sort();
  return `${name}[${pairs.join(',')}]`;
}
