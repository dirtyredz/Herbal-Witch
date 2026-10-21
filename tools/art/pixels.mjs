// Seeded pixel helpers shared by the texture generators: the deterministic random source, clamping,
// shading and image construction. PNG encoding lives in tools/image/png-encode.mjs.

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

/** An opaque grey pixel. */
export const grey = (v) => [clamp(v), clamp(v), clamp(v), 255];

/** Multiplies the RGB channels of an opaque colour by {@code f}. */
export const shade = ([r, g, b], f) => [clamp(r * f), clamp(g * f), clamp(b * f), 255];

/** Linear blend of two colours, t = 0 gives a. */
export const mix = (a, b, t) => [0, 1, 2].map((i) => a[i] + (b[i] - a[i]) * t);

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
