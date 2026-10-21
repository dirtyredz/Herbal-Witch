// Mature herbs built from small cubes (owner references: reference-herb-bed-lavender.png and
// reference-joined-bed-2x2.png): chunky stems, leaves and flower heads in each herb's own
// silhouette and colours, rooted two pixels below the block so the plant stands in the soil. Every
// face samples one colour swatch of the parts atlas (tools/art/botany/parts.mjs); directional
// shading does the modelling. Budgets: about 40 elements a plant, so a full garden stays cheap.
import { box } from '../geometry.mjs';
import { swatchUv } from '../../art/botany/parts.mjs';
import { rng } from '../../art/pixels.mjs';

const ROOT = -2;

/** A cube or bar of one swatch (all faces), optionally rotated. */
function part(from, to, swatch, rot) {
  return box(from, to, '#parts', { decal: { all: swatchUv(swatch) }, ...(rot ? { rot } : {}) });
}

const stem = (x, z, y1, swatch = 'stem', w = 1, y0 = ROOT) => part([x - w / 2, y0, z - w / 2], [x + w / 2, y1, z + w / 2], swatch);
const cube = (x, y, z, s, swatch, h = s) => part([x - s / 2, y, z - s / 2], [x + s / 2, y + h, z + s / 2], swatch);

/**
 * A flat leaf from (x, y, z) pointing along dir ('n','s','e','w'), len long and w wide, tilted
 * up (positive) or drooping (negative) by 22.5 degrees about its base.
 */
function leaf(x, y, z, dir, len, w, swatch, tilt = 22.5) {
  const t = 0.5;
  const along = { e: [1, 0], w: [-1, 0], s: [0, 1], n: [0, -1] }[dir];
  const [x0, x1] = along[0] ? [x, x + along[0] * len].sort((a, b) => a - b) : [x - w / 2, x + w / 2];
  const [z0, z1] = along[1] ? [z, z + along[1] * len].sort((a, b) => a - b) : [z - w / 2, z + w / 2];
  // tilting the tip up: about z for east/west leaves, about x for north/south ones
  const sign = dir === 'e' || dir === 'n' ? 1 : -1;
  const rot = tilt ? { origin: [x, y, z], axis: along[0] ? 'z' : 'x', angle: (along[0] ? 1 : -1) * sign * tilt } : null;
  return part([x0, y, z0], [x1, y + t, z1], swatch, rot);
}

/** A diagonal flat leaf (rotated 45 degrees about y), len long from its centre out. */
function diagLeaf(x, y, z, len, w, swatch, angle = 45) {
  return part([x - len / 2, y, z - w / 2], [x + len / 2, y + 0.5, z + w / 2], swatch, { origin: [x, y, z], axis: 'y', angle });
}

/** A flower spike: stacked cubes tapering upward from y0 for n florets. */
function spike(x, z, y0, n, a, b) {
  const out = [];
  for (let i = 0; i < n; i++) {
    const s = i === n - 1 ? 1 : 1.5 - (i % 2) * 0.25;
    out.push(cube(x + (i % 2 ? 0.25 : -0.25), y0 + i * 1.1, z, s, i % 2 ? b : a, 1.1));
  }
  return out;
}

/** A daisy: a white cross of petals around a raised yellow centre. */
function daisy(x, y, z) {
  return [
    part([x - 1.5, y, z - 0.5], [x + 1.5, y + 0.5, z + 0.5], 'white'),
    part([x - 0.5, y, z - 1.5], [x + 0.5, y + 0.5, z + 1.5], 'white'),
    cube(x, y + 0.25, z, 1, 'yellow', 0.75),
  ];
}

/** A flat-topped umbel (yarrow, valerian): a disc of florets on a short neck. */
function umbel(x, y, z, w, swatch) {
  return [part([x - w / 2, y, z - w / 2 + 0.5], [x + w / 2, y + 1, z + w / 2 - 0.5], swatch), part([x - w / 2 + 0.5, y, z - w / 2], [x + w / 2 - 0.5, y + 1, z + w / 2], swatch)];
}

/**
 * A leafy dome of small cubes around (cx, cz): radius r, top height top, count cubes of size
 * about s, cycling through the given swatches. Fills a plant out without one big block.
 */
function mass(cx, cz, r, top, count, swatches, seed, s = 2) {
  const rand = rng(seed);
  const out = [];
  for (let i = 0; i < count; i++) {
    const a = rand() * Math.PI * 2;
    const d = Math.sqrt(rand()) * r;
    const x = Math.min(14 - s / 2, Math.max(2 + s / 2, cx + Math.cos(a) * d));
    const z = Math.min(14 - s / 2, Math.max(2 + s / 2, cz + Math.sin(a) * d));
    const h = Math.max(1, (top - ROOT) * (1 - (d / r) ** 2 * 0.6));
    const size = s * (0.75 + rand() * 0.6);
    out.push(cube(x, ROOT + h - size - rand() * 1.5, z, size, swatches[i % swatches.length], size));
  }
  return out;
}

/** Each herb's mature form. */
const HERBS = {
  lavender() {
    const out = [];
    for (const [x, z, h] of [[8, 8, 13], [5.5, 7, 11], [10.5, 9.5, 11.5], [7, 11, 10], [10, 5.5, 10]]) {
      out.push(stem(x, z, h - 4, 'stem', 0.75), ...spike(x, z, h - 4.5, 5, 'lavender', 'lavender_light'));
    }
    out.push(...mass(8, 8, 5.5, 6.5, 18, ['grey_green', 'leaf', 'sage', 'leaf_light'], 101, 1.75));
    return out;
  },
  sage() {
    const out = [stem(8, 8, 8, 'stem'), stem(5.5, 9, 6.5, 'stem', 0.75), stem(10.5, 7, 7, 'stem', 0.75)];
    const pairs = [[8, 8, 2, 4.5], [5.5, 9, 4, 3.5], [10.5, 7, 4.5, 3.5], [8, 8, 6, 3.5]];
    pairs.forEach(([x, z, y, len], i) => {
      const [a, b] = i % 2 ? ['n', 's'] : ['e', 'w'];
      out.push(leaf(x, y, z, a, len, 2.5, 'sage'), leaf(x, y + 0.5, z, b, len, 2.5, 'sage_light'));
    });
    out.push(...spike(8, 8, 8, 4, 'lavender_deep', 'lavender'), ...spike(5.5, 9, 6.5, 3, 'lavender_deep', 'lavender'));
    out.push(...mass(8, 8, 5.5, 7, 16, ['sage', 'sage_light', 'grey_green'], 102, 2.25));
    return out;
  },
  chamomile() {
    const out = mass(8, 8, 6, 8, 24, ['leaf_light', 'leaf', 'leaf_bright'], 103, 1.75);
    for (const [x, z, h] of [[5, 6, 8.5], [8, 8, 9.5], [11, 6.5, 8], [6, 11, 7.5], [10.5, 10.5, 9], [8.5, 4.5, 7.5], [4, 9, 7]]) {
      out.push(stem(x, z, h, 'stem', 0.75), ...daisy(x, h, z));
    }
    return out;
  },
  yarrow() {
    const out = [];
    for (const [x, z, h, w] of [[7, 7, 12, 5], [10.5, 9.5, 10, 4], [5, 10.5, 9, 3.5]]) {
      out.push(stem(x, z, h, 'stem', 0.75), ...umbel(x, h, z, w, 'umbel'));
    }
    for (const [dir, x, z, y] of [['e', 8, 8, 1], ['w', 8, 8, 2], ['s', 8, 8, 3.5], ['n', 8, 8, 0.5], ['e', 8, 9, 5]]) {
      out.push(leaf(x, y, z, dir, 5, 1.5, 'leaf_light'));
    }
    out.push(...mass(8, 8, 5, 5, 14, ['leaf_light', 'leaf', 'leaf_bright'], 104, 1.5));
    return out;
  },
  rosemary() {
    const out = [];
    const rand = rng(41);
    for (const [x, z, h] of [[5.5, 5.5, 12], [9.5, 6.5, 13], [6.5, 10.5, 11], [11, 10.5, 12], [4, 9, 9.5], [8.5, 8.5, 10]]) {
      out.push(stem(x, z, h, 'needle', 1));
      for (let y = 1, i = 0; y < h - 1; y += 2, i++) {
        const [w, d] = i % 2 ? [0.25, 1.75] : [1.75, 0.25];
        out.push(part([x - w, y, z - d], [x + w, y + 0.75, z + d], rand() < 0.5 ? 'needle' : 'leaf_dark'));
      }
      out.push(cube(x + 0.5, h - 2, z, 1, 'lavender_light'), cube(x, h, z, 1, 'lavender_light'));
    }
    return out;
  },
  thyme() {
    const out = [];
    const rand = rng(42);
    for (let i = 0; i < 36; i++) {
      const x = 3 + rand() * 10;
      const z = 3 + rand() * 10;
      const d = Math.hypot(x - 8, z - 8);
      const top = 8.5 - d * 0.55;
      const s = 1 + rand() * 1.25;
      out.push(cube(x, ROOT + Math.max(0, top - s + rand() * 1.5), z, s, ['thyme', 'thyme_light', 'leaf_dark'][i % 3], s));
    }
    rand();
    for (const [x, z] of [[6, 7], [9, 6], [10, 10], [7, 10.5], [8, 8.5]]) out.push(cube(x, 5, z, 1, 'thyme_flower'));
    return out;
  },
  mint() {
    const out = [];
    for (const [x, z, h] of [[6, 7, 11], [9.5, 8, 12.5], [7.5, 11, 10]]) {
      out.push(stem(x, z, h, 'stem', 1));
      for (let y = 1, i = 0; y < h - 2; y += 3, i++) {
        const [a, b] = i % 2 ? ['n', 's'] : ['e', 'w'];
        out.push(leaf(x, y, z, a, 3, 2.25, 'mint'), leaf(x, y + 0.25, z, b, 3, 2.25, 'mint_light'));
      }
      out.push(cube(x, h, z, 1.25, 'lavender_light', 1.5));
    }
    return out;
  },
  nettle() {
    const out = [];
    for (const [x, z, h] of [[7, 7, 13], [10, 9.5, 12], [5.5, 10, 11]]) {
      out.push(stem(x, z, h, 'nettle', 1));
      for (let y = 1, i = 0; y < h - 1; y += 2.75, i++) {
        const [a, b] = i % 2 ? ['n', 's'] : ['e', 'w'];
        out.push(leaf(x, y, z, a, 3.5, 2, 'nettle', -22.5), leaf(x, y + 0.25, z, b, 3.5, 2, 'leaf_dark', -22.5));
      }
    }
    return out;
  },
  valerian() {
    const out = [];
    for (const [x, z, h] of [[8, 8, 13], [5, 6.5, 11], [11, 10, 11.5]]) {
      out.push(stem(x, z, h, 'stem', 0.75), cube(x, h, z, 3, 'blush', 1.25), cube(x, h + 1, z, 2, 'white', 0.75));
    }
    for (const [dir, x, z, y] of [['e', 8, 8, 0], ['w', 8, 8, 0.5], ['s', 8, 8, 1], ['n', 8, 8, 1.5], ['e', 8, 8, 4], ['w', 8, 8, 5]]) {
      out.push(leaf(x, y, z, dir, 5, 2, y > 2 ? 'leaf' : 'leaf_dark'));
    }
    out.push(...mass(8, 8, 4.5, 3, 8, ['leaf', 'leaf_dark'], 105, 1.75));
    return out;
  },
  mugwort() {
    const out = [];
    const rand = rng(43);
    for (const [x, z, h] of [[8, 8, 13], [5.5, 9.5, 11], [10.5, 6, 11.5], [10, 11, 10]]) {
      out.push(stem(x, z, h, 'stem_dark', 1));
      for (let y = 2; y < h; y += 2.5) {
        const dir = ['e', 'w', 'n', 's'][Math.floor(rand() * 4)];
        out.push(leaf(x, y, z, dir, 3, 1.5, rand() < 0.5 ? 'silver' : 'grey_green'));
      }
      out.push(cube(x, h, z, 1.25, 'bark'));
    }
    return out;
  },
  belladonna() {
    const out = [stem(8, 8, 10, 'stem_dark', 1.25)];
    for (const [x, y, z, s] of [[5.5, 3, 6.5, 4.5], [10.5, 4, 9.5, 4.5], [8, 7.5, 8, 5], [6, 6, 11, 4], [10.5, 1.5, 5.5, 4], [4.5, 1, 10, 3.5], [11.5, 6.5, 6.5, 3]]) {
      out.push(cube(x, y, z, s, s > 4.2 ? 'leaf_dark' : 'leaf', 2));
    }
    for (const [x, y, z] of [[4.5, 5.5, 8], [11.5, 7, 10], [8, 10.5, 6], [9.5, 4.5, 11.5]]) {
      out.push(cube(x, y, z, 1.5, 'berry_black'), cube(x - 0.4, y + 1, z - 0.4, 0.5, 'berry_shine', 0.5));
    }
    for (const [x, y, z] of [[7, 10, 10], [11, 9, 7]]) out.push(cube(x, y - 1.5, z, 1.25, 'bell', 1.5), stem(x, z, y, 'stem', 0.5, y - 0.25));
    return out;
  },
  nightshade() {
    const out = [stem(8, 8, 9, 'stem_dark', 1), stem(5.5, 9, 7, 'stem_dark', 0.75), stem(10.5, 7, 8, 'stem_dark', 0.75)];
    for (const [x, y, z, len, dir] of [[8, 3, 8, 4.5, 'e'], [8, 4, 8, 4.5, 'w'], [5.5, 5, 9, 3.5, 's'], [10.5, 5.5, 7, 3.5, 'n'], [8, 7, 8, 3.5, 's']]) {
      out.push(leaf(x, y, z, dir, len, 2.5, 'leaf_dark'));
    }
    for (const [x, y, z] of [[6, 7, 7], [6.75, 6.25, 8], [10, 8.5, 9.5], [10.75, 7.75, 10.25], [9, 9.5, 6]]) {
      out.push(cube(x, y, z, 1.25, 'berry_red'), cube(x - 0.3, y + 0.85, z - 0.3, 0.5, 'berry_red_light', 0.4));
    }
    out.push(...daisy(8, 9.5, 8).map((e, i) => (i < 2 ? part(e.from, e.to, 'bell') : e)));
    return out;
  },
  wolfsbane() {
    const out = [];
    for (const [x, z, h] of [[8, 9, 13.5], [4.5, 6, 11], [11.5, 5.5, 12], [5, 12, 10]]) {
      out.push(stem(x, z, h - 5, 'stem', 0.75));
      for (let i = 0; i < 4; i++) {
        const y = h - 5 + i * 1.3;
        out.push(part([x - 1, y, z - 0.75], [x + 1, y + 1.25, z + 1], i % 2 ? 'hood_light' : 'hood'));
      }
    }
    for (const [dir, x, z, y] of [['e', 8, 8, 0], ['w', 8, 8, 1], ['s', 8, 8, 0.5], ['n', 8, 8, 2]]) out.push(leaf(x, y, z, dir, 5, 2.5, 'leaf_dark'));
    out.push(...mass(8, 8, 4.5, 2.5, 6, ['leaf_dark', 'leaf'], 106, 1.75));
    return out;
  },
  mandrake() {
    const out = [cube(8, ROOT, 8, 3, 'root', 2.5)];
    for (const dir of ['e', 'w', 'n', 's']) {
      out.push(leaf(8, 0, 8, dir, 7, 3, 'rosette', 22.5));
      out.push(leaf(8, 1, 8, dir, 6, 1, 'rosette_light', 22.5)); // the pale midrib
    }
    for (const angle of [45, -45]) out.push(diagLeaf(8, 0.5, 8, 13, 2.5, 'rosette_light', angle), diagLeaf(8, 2.5, 8, 9, 2, 'rosette', angle));
    for (const [dir, x, z] of [['e', 9, 7], ['w', 7, 9], ['n', 7, 7], ['s', 9, 9]]) out.push(leaf(x, 2.5, z, dir, 4.5, 2, 'leaf', 22.5));
    out.push(cube(8, 2, 8, 2, 'leaf', 2), cube(7.5, 4, 7.5, 1, 'lavender'), cube(9, 3.75, 8.5, 1, 'lavender_light'), cube(8, 3.5, 9.5, 1, 'yellow'));
    return out;
  },
};

/** [{path, textures, elements, ao, cutout}] for every herb's mature model. */
export function matureHerbs(herbs) {
  return herbs.map(([group, herb]) => {
    const make = HERBS[herb];
    if (!make) throw new Error(`no mature form for ${herb}`);
    return {
      path: `block/botany/${group}/${herb}/mature`,
      textures: { parts: 'herbalwitch:block/botany/parts', particle: `herbalwitch:block/botany/${group}/${herb}/stage_2` },
      elements: make(),
      ao: false,
      cutout: false,
    };
  });
}
