// PNG decoding for the model previewer and reference comparisons: 8-bit greyscale, grey+alpha,
// RGB, RGBA and palette images, all five scanline filters, non-interlaced. Anything else fails
// loudly rather than rendering a wrong texture.
import { inflateSync } from 'node:zlib';

const SIGNATURE = Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]);
const CHANNELS = { 0: 1, 2: 3, 3: 1, 4: 2, 6: 4 };

function paeth(a, b, c) {
  const p = a + b - c;
  const pa = Math.abs(p - a);
  const pb = Math.abs(p - b);
  const pc = Math.abs(p - c);
  return pa <= pb && pa <= pc ? a : pb <= pc ? b : c;
}

/** Decodes PNG bytes to {width, height, rgba: Uint8Array}. */
export function decodePng(buf, name = 'png') {
  if (!buf.subarray(0, 8).equals(SIGNATURE)) throw new Error(`${name}: not a PNG`);
  let pos = 8;
  let width = 0;
  let height = 0;
  let depth = 0;
  let type = 0;
  let palette = null;
  let trns = null;
  const idat = [];
  while (pos < buf.length) {
    const len = buf.readUInt32BE(pos);
    const kind = buf.toString('ascii', pos + 4, pos + 8);
    const data = buf.subarray(pos + 8, pos + 8 + len);
    pos += 12 + len;
    if (kind === 'IHDR') {
      width = data.readUInt32BE(0);
      height = data.readUInt32BE(4);
      depth = data[8];
      type = data[9];
      if (data[12] !== 0) throw new Error(`${name}: interlaced PNGs are not supported`);
    } else if (kind === 'PLTE') palette = data;
    else if (kind === 'tRNS') trns = data;
    else if (kind === 'IDAT') idat.push(data);
    else if (kind === 'IEND') break;
  }
  if (depth !== 8 || !(type in CHANNELS)) throw new Error(`${name}: unsupported PNG (depth ${depth}, colour type ${type})`);
  const channels = CHANNELS[type];
  const stride = width * channels;
  const raw = inflateSync(Buffer.concat(idat));
  const out = new Uint8Array(width * height * channels);
  for (let y = 0; y < height; y++) {
    const filter = raw[y * (stride + 1)];
    const line = raw.subarray(y * (stride + 1) + 1, (y + 1) * (stride + 1));
    for (let x = 0; x < stride; x++) {
      const a = x >= channels ? out[y * stride + x - channels] : 0;
      const b = y > 0 ? out[(y - 1) * stride + x] : 0;
      const c = x >= channels && y > 0 ? out[(y - 1) * stride + x - channels] : 0;
      let v = line[x];
      if (filter === 1) v += a;
      else if (filter === 2) v += b;
      else if (filter === 3) v += (a + b) >> 1;
      else if (filter === 4) v += paeth(a, b, c);
      else if (filter !== 0) throw new Error(`${name}: bad filter ${filter}`);
      out[y * stride + x] = v & 0xff;
    }
  }
  const rgba = new Uint8Array(width * height * 4);
  for (let i = 0; i < width * height; i++) {
    let r;
    let g;
    let b;
    let al = 255;
    if (type === 6) [r, g, b, al] = out.subarray(i * 4, i * 4 + 4);
    else if (type === 2) [r, g, b] = out.subarray(i * 3, i * 3 + 3);
    else if (type === 0) r = g = b = out[i];
    else if (type === 4) {
      r = g = b = out[i * 2];
      al = out[i * 2 + 1];
    } else {
      const idx = out[i];
      if (!palette || idx * 3 + 2 >= palette.length) throw new Error(`${name}: palette index ${idx} out of range`);
      [r, g, b] = palette.subarray(idx * 3, idx * 3 + 3);
      al = trns && idx < trns.length ? trns[idx] : 255;
    }
    rgba.set([r, g, b, al], i * 4);
  }
  return { width, height, rgba };
}
