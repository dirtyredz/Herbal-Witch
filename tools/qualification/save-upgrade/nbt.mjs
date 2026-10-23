// Typed, lossless NBT reader and writer (Java edition, big-endian), for save-upgrade qualification.
//
// A tag is { t: <type id>, v: <value> }. Compounds hold an ordered Map (keys such as "0" keep their
// order), lists hold { of: <element type>, items: [values] }, longs are BigInt. Reading and writing a
// file returns the same bytes, which save-upgrade comparisons and fixture sanitising rely on.
import { gunzipSync, gzipSync, inflateSync, deflateSync } from 'node:zlib';

export const T = { end: 0, byte: 1, short: 2, int: 3, long: 4, float: 5, double: 6, byteArray: 7, string: 8, list: 9, compound: 10, intArray: 11, longArray: 12 };

class Reader {
  constructor(buf) { this.b = buf; this.o = 0; }
  u8() { return this.b.readUInt8(this.o++); }
  i8() { return this.b.readInt8(this.o++); }
  i16() { const v = this.b.readInt16BE(this.o); this.o += 2; return v; }
  u16() { const v = this.b.readUInt16BE(this.o); this.o += 2; return v; }
  i32() { const v = this.b.readInt32BE(this.o); this.o += 4; return v; }
  i64() { const v = this.b.readBigInt64BE(this.o); this.o += 8; return v; }
  f32() { const v = this.b.readFloatBE(this.o); this.o += 4; return v; }
  f64() { const v = this.b.readDoubleBE(this.o); this.o += 8; return v; }
  str() { const n = this.u16(); const s = this.b.toString('utf8', this.o, this.o + n); this.o += n; return s; }
  payload(t) {
    switch (t) {
      case T.byte: return this.i8();
      case T.short: return this.i16();
      case T.int: return this.i32();
      case T.long: return this.i64();
      case T.float: return this.f32();
      case T.double: return this.f64();
      case T.byteArray: { const n = this.i32(); const v = Buffer.from(this.b.subarray(this.o, this.o + n)); this.o += n; return v; }
      case T.string: return this.str();
      case T.list: { const of = this.u8(); const n = this.i32(); const items = []; for (let i = 0; i < n; i++) items.push(this.payload(of)); return { of, items }; }
      case T.compound: {
        const m = new Map();
        for (;;) { const ct = this.u8(); if (ct === T.end) return m; const name = this.str(); m.set(name, { t: ct, v: this.payload(ct) }); }
      }
      case T.intArray: { const n = this.i32(); const v = new Int32Array(n); for (let i = 0; i < n; i++) v[i] = this.i32(); return v; }
      case T.longArray: { const n = this.i32(); const v = new BigInt64Array(n); for (let i = 0; i < n; i++) v[i] = this.i64(); return v; }
      default: throw new Error(`unknown NBT tag type ${t} at ${this.o}`);
    }
  }
}

class Writer {
  constructor() { this.parts = []; }
  raw(b) { this.parts.push(b); }
  u8(n) { const b = Buffer.alloc(1); b.writeUInt8(n); this.raw(b); }
  i8(n) { const b = Buffer.alloc(1); b.writeInt8(n); this.raw(b); }
  i16(n) { const b = Buffer.alloc(2); b.writeInt16BE(n); this.raw(b); }
  u16(n) { const b = Buffer.alloc(2); b.writeUInt16BE(n); this.raw(b); }
  i32(n) { const b = Buffer.alloc(4); b.writeInt32BE(n); this.raw(b); }
  i64(n) { const b = Buffer.alloc(8); b.writeBigInt64BE(n); this.raw(b); }
  str(s) { const b = Buffer.from(s, 'utf8'); this.u16(b.length); this.raw(b); }
  payload(t, v) {
    switch (t) {
      case T.byte: return this.i8(v);
      case T.short: return this.i16(v);
      case T.int: return this.i32(v);
      case T.long: return this.i64(v);
      case T.float: { const b = Buffer.alloc(4); b.writeFloatBE(v); return this.raw(b); }
      case T.double: { const b = Buffer.alloc(8); b.writeDoubleBE(v); return this.raw(b); }
      case T.byteArray: this.i32(v.length); return this.raw(Buffer.from(v));
      case T.string: return this.str(v);
      case T.list: this.u8(v.items.length ? v.of : (v.of ?? T.end)); this.i32(v.items.length); for (const item of v.items) this.payload(v.of, item); return undefined;
      case T.compound: for (const [name, tag] of v) { this.u8(tag.t); this.str(name); this.payload(tag.t, tag.v); } return this.u8(T.end);
      case T.intArray: this.i32(v.length); for (const n of v) this.i32(n); return undefined;
      case T.longArray: this.i32(v.length); for (const n of v) this.i64(n); return undefined;
      default: throw new Error(`unknown NBT tag type ${t}`);
    }
  }
  done() { return Buffer.concat(this.parts); }
}

/** Parses an uncompressed named root tag. */
export function parse(buf) {
  const r = new Reader(buf);
  const t = r.u8();
  if (t !== T.compound) throw new Error(`root tag is type ${t}, not a compound`);
  const name = r.str();
  return { name, root: { t, v: r.payload(t) } };
}

export function serialize({ name, root }) {
  const w = new Writer();
  w.u8(root.t); w.str(name ?? ''); w.payload(root.t, root.v);
  return w.done();
}

export const readGzip = (buf) => parse(gunzipSync(buf));
export const writeGzip = (doc) => gzipSync(serialize(doc));
export const readZlib = (buf) => parse(inflateSync(buf));
export const writeZlib = (doc) => deflateSync(serialize(doc));

/** Compound helpers. */
export const get = (tag, ...path) => {
  let cur = tag;
  for (const key of path) {
    if (!cur) return undefined;
    if (cur.t === T.compound) cur = cur.v.get(key);
    else if (cur.t === T.list) { const item = cur.v.items[key]; cur = item === undefined ? undefined : { t: cur.v.of, v: item }; }
    else return undefined;
  }
  return cur;
};

/** Plain-JSON projection for receipts and diffs (BigInt as strings, arrays as plain arrays, types dropped). */
export function plain(tag) {
  switch (tag.t) {
    case T.compound: { const o = {}; for (const [k, v] of tag.v) o[k] = plain(v); return o; }
    case T.list: return tag.v.items.map((item) => plain({ t: tag.v.of, v: item }));
    case T.long: return `${tag.v}L`;
    case T.longArray: return Array.from(tag.v, (n) => `${n}L`);
    case T.intArray: return Array.from(tag.v);
    case T.byteArray: return Array.from(tag.v);
    case T.byte: return `${tag.v}b`;
    case T.short: return `${tag.v}s`;
    case T.float: return `${tag.v}f`;
    default: return tag.v;
  }
}
