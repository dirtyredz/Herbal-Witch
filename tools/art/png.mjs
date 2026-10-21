// PNG encoding and the seeded random source shared by the texture generators.
// Adapted from Roundwood's tools/generate-textures.mjs (same author, MIT): RGBA instead of RGB so
// cutout textures (glass) can carry transparency. No dependencies; identical input gives identical bytes.
import { deflateSync } from 'node:zlib';

/** Small deterministic PRNG (mulberry32). */
export function rng(seed) {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export const clamp = (v) => Math.max(0, Math.min(255, Math.round(v)));

/** An opaque grey pixel: tinted textures stay neutral so the finish colour alone sets the hue. */
export const grey = (v) => [clamp(v), clamp(v), clamp(v), 255];

/** Multiplies the RGB channels of an opaque colour by {@code f}. */
export const shade = ([r, g, b], f) => [clamp(r * f), clamp(g * f), clamp(b * f), 255];

/** A size x size image as a flat row-major array of [r, g, b, a] pixels. */
export function image(size, fill) {
  const px = [];
  for (let y = 0; y < size; y++) {
    for (let x = 0; x < size; x++) {
      px.push(fill(x, y));
    }
  }
  return { size, px };
}

const CRC_TABLE = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});

function crc32(buf) {
  let c = 0xffffffff;
  for (const b of buf) c = CRC_TABLE[(c ^ b) & 0xff] ^ (c >>> 8);
  return (c ^ 0xffffffff) >>> 0;
}

function chunk(type, data) {
  const len = Buffer.alloc(4);
  len.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type, 'ascii'), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(body));
  return Buffer.concat([len, body, crc]);
}

/** Encodes an image as an 8-bit RGBA PNG. */
export function png({ size, px }) {
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(size, 0);
  ihdr.writeUInt32BE(size, 4);
  ihdr[8] = 8; // bit depth
  ihdr[9] = 6; // RGBA
  const stride = 1 + size * 4;
  const raw = Buffer.alloc(size * stride);
  for (let y = 0; y < size; y++) {
    raw[y * stride] = 0; // filter: none
    for (let x = 0; x < size; x++) {
      const [r, g, b, a] = px[y * size + x];
      const o = y * stride + 1 + x * 4;
      raw[o] = r;
      raw[o + 1] = g;
      raw[o + 2] = b;
      raw[o + 3] = a;
    }
  }
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}
