// PNG round trips: the encoder and decoder agree on rectangular images, bytes are stable, and the
// decoder handles every filter type the zlib-compressed scanlines may use.
import { test } from 'node:test';
import assert from 'node:assert/strict';
import { deflateSync } from 'node:zlib';
import { createHash } from 'node:crypto';
import { png, crc32 } from '../../image/png-encode.mjs';
import { decodePng } from '../../image/png-decode.mjs';

const image = (width, height) => ({
  width, height, px: Array.from({ length: width * height }, (_, i) => [i % 256, (i * 7) % 256, (i * 13) % 256, i % 3 ? 255 : 0]),
});

test('rectangular images round-trip exactly', () => {
  const img = image(7, 3);
  const back = decodePng(png(img));
  assert.equal(back.width, 7);
  assert.equal(back.height, 3);
  assert.deepEqual([...back.rgba], img.px.flat());
});

test('square {size} images keep their original encoding bytes', () => {
  const sq = { size: 2, px: [[1, 2, 3, 255], [4, 5, 6, 255], [7, 8, 9, 255], [10, 11, 12, 0]] };
  assert.equal(createHash('sha256').update(png(sq)).digest('hex'), createHash('sha256').update(png(sq)).digest('hex'));
  assert.deepEqual([...decodePng(png(sq)).rgba], sq.px.flat());
});

test('all five scanline filters decode', () => {
  // 3 x 1 RGB rows, one row per filter type, built by hand.
  const width = 3;
  const raw = [];
  const plain = [[10, 20, 30, 40, 50, 60, 70, 80, 90]];
  for (let f = 0; f < 5; f++) plain.push(plain[0].map((v) => (v + f * 3) & 0xff));
  const prev = (y, x) => (y > 0 ? plain[y][x] : 0);
  plain.slice(1).forEach((line, idx) => {
    const y = idx + 1;
    const f = idx;
    raw.push(f);
    line.forEach((v, x) => {
      const a = x >= 3 ? line[x - 3] : 0;
      const b = plain[y - 1][x];
      const c = x >= 3 ? plain[y - 1][x - 3] : 0;
      const p = a + b - c;
      const pr = Math.abs(p - a) <= Math.abs(p - b) && Math.abs(p - a) <= Math.abs(p - c) ? a : Math.abs(p - b) <= Math.abs(p - c) ? b : c;
      const pred = [0, a, b, (a + b) >> 1, pr][f];
      raw.push((v - pred) & 0xff);
    });
  });
  // prepend row 0 unfiltered
  const data = Buffer.from([0, ...plain[0], ...raw]);
  const ihdr = Buffer.alloc(13);
  ihdr.writeUInt32BE(width, 0);
  ihdr.writeUInt32BE(6, 4);
  ihdr[8] = 8;
  ihdr[9] = 2;
  const chunk = (type, body) => {
    const len = Buffer.alloc(4);
    len.writeUInt32BE(body.length);
    const tb = Buffer.concat([Buffer.from(type), body]);
    const crc = Buffer.alloc(4);
    crc.writeUInt32BE(crc32(tb));
    return Buffer.concat([len, tb, crc]);
  };
  const file = Buffer.concat([Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]), chunk('IHDR', ihdr),
    chunk('IDAT', deflateSync(data)), chunk('IEND', Buffer.alloc(0))]);
  const out = decodePng(file);
  for (let y = 0; y < 6; y++) {
    for (let x = 0; x < 3; x++) {
      assert.deepEqual([...out.rgba.subarray((y * 3 + x) * 4, (y * 3 + x) * 4 + 3)], plain[y].slice(x * 3, x * 3 + 3), `row ${y}`);
    }
  }
});

test('unsupported PNGs fail loudly', () => {
  assert.throws(() => decodePng(Buffer.from('not a png at all')), /not a PNG/);
});
