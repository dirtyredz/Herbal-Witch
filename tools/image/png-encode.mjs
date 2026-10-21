// Deterministic 8-bit RGBA PNG encoding for every generator and the model previewer.
// Adapted from Roundwood's tools/generate-textures.mjs (same author, MIT): RGBA instead of RGB so
// cutout textures (glass) can carry transparency. No dependencies, no timestamps: identical input
// gives identical bytes.
import { deflateSync } from 'node:zlib';

const CRC_TABLE = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k++) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});

export function crc32(buf) {
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

/** Width and height of an image given as {size} (square) or {width, height}. */
export function dimensions(img) {
  const width = img.width ?? img.size;
  const height = img.height ?? img.size;
  if (!Number.isInteger(width) || !Number.isInteger(height) || width < 1 || height < 1) {
    throw new Error(`bad image dimensions ${width}x${height}`);
  }
  return { width, height };
}

/**
 * Encodes an image as an 8-bit RGBA PNG. The image is {size, px} or {width, height, px}; px is a
 * flat row-major array of [r, g, b, a] pixels, or a Uint8Array of RGBA bytes.
 */
export function png(img) {
  const { width, height } = dimensions(img);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(height, 4);
  ihdr[8] = 8; // bit depth
  ihdr[9] = 6; // RGBA
  const stride = 1 + width * 4;
  const raw = Buffer.alloc(height * stride);
  const bytes = img.px instanceof Uint8Array;
  for (let y = 0; y < height; y++) {
    raw[y * stride] = 0; // filter: none
    for (let x = 0; x < width; x++) {
      const o = y * stride + 1 + x * 4;
      const i = y * width + x;
      if (bytes) {
        raw[o] = img.px[i * 4];
        raw[o + 1] = img.px[i * 4 + 1];
        raw[o + 2] = img.px[i * 4 + 2];
        raw[o + 3] = img.px[i * 4 + 3];
      } else {
        const [r, g, b, a] = img.px[i];
        raw[o] = r;
        raw[o + 1] = g;
        raw[o + 2] = b;
        raw[o + 3] = a;
      }
    }
  }
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(raw, { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
}
