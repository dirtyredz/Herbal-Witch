// The review set: family contact sheets (every state family at one fixed scale, front and
// isometric), the item GUI sheet, herbs-on-beds by age, scenes and reference comparisons.
// Pure composition: returns named images; the CLI writes them.
import { readFileSync, existsSync } from 'node:fs';
import { join } from 'node:path';
import { contactSheet, blank, blit } from '../../image/contact-sheet.mjs';
import { decodePng } from '../../image/png-decode.mjs';
import { drawText } from '../../image/bitmap-font.mjs';
import { stateLabel } from '../scenes/blockstates.mjs';
import { SCENES, beds } from '../scenes/catalogue.mjs';
import { renderState, renderItem, renderScene } from './views.mjs';

/** Which sheet a blockstate belongs on. */
export function family(block) {
  const path = block.split(':')[1];
  if (/^botany\/(?!garden)/.test(path)) return 'herbs';
  return path.split('/')[0] === 'botany' ? 'garden' : path.split('/')[0];
}

/** One representative per distinct model selection, facing north where the block faces. */
function representatives(states) {
  const seen = new Set();
  return states.filter((s) => {
    if (s.props.facing && s.props.facing !== 'north') return false;
    const key = `${s.block}|${s.models.join('+')}`;
    if (seen.has(key)) return false;
    seen.add(key);
    return true;
  });
}

const shortLabel = (block) => block.split(':')[1].replace(/^(decor|stations|preparations|botany)\//, '').replace(/\/(station|plant)$/, '');
const shortProps = (props) => stateLabel(Object.fromEntries(Object.entries(props).filter(([k]) => k !== 'facing')))
  .replace(/north=|east=|south=|west=/g, (m) => m[0]).replace(/true/g, 'T').replace(/false/g, 'F');

/** Builds every review image. Returns {images: Map(name -> rgba canvas), errors: [string]}. */
export function reviewSet(res, baker, cov, { referenceDir = null, scale = 8 } = {}) {
  const images = new Map();
  const errors = [];
  const byFamily = new Map();
  for (const s of representatives(cov.states)) {
    const f = family(s.block);
    if (f === 'herbs') continue;
    if (!byFamily.has(f)) byFamily.set(f, []);
    byFamily.get(f).push(s);
  }
  for (const [fam, states] of byFamily) {
    for (const view of ['iso', 'front']) {
      const cells = states.map((s) => ({
        image: renderState(res, baker, s.block, s.props, view, scale).image,
        label: shortLabel(s.block),
        note: shortProps(s.props),
      }));
      const cell = cells[0].image;
      images.set(`sheets/${fam}-${view}.png`, contactSheet({
        title: `${fam} - ${view} - ${scale} px per model pixel`, cells, columns: Math.min(6, cells.length),
        cellW: Math.max(cell.width, 220), cellH: cell.height,
      }));
    }
  }

  // Herbs: each age planted in a real dry bed, so ground contact is visible.
  const herbs = [...new Set(cov.states.filter((s) => family(s.block) === 'herbs').map((s) => s.block))];
  const herbCells = [];
  for (const block of herbs) {
    for (let age = 0; age < 4; age++) {
      const entries = [...beds([[0, 0]]), { at: [0, 1, 0], block, props: { age } }];
      herbCells.push({ image: renderScene(res, baker, entries, 'iso', 5).image, label: `${shortLabel(block)} ${age}` });
    }
  }
  if (herbCells.length) {
    const w = Math.max(...herbCells.map((c) => c.image.width));
    const h = Math.max(...herbCells.map((c) => c.image.height));
    images.set('sheets/herbs-on-beds.png', contactSheet({ title: 'herbs by age, planted in a dry herb bed', cells: herbCells, columns: 8, cellW: w, cellH: h }));
  }

  const itemCells = cov.items.map((id) => ({ image: renderItem(res, baker, id, 96).image, label: id.split(':')[1].replace(/^item\//, '') }));
  images.set('sheets/items-gui.png', contactSheet({ title: 'items - GUI', cells: itemCells, columns: 10, cellW: 150, cellH: 96, textScale: 1 }));

  for (const scene of SCENES) {
    try {
      const shot = renderScene(res, baker, scene.entries, scene.view, scene.scale ?? 8);
      if (shot.missing.size) errors.push(`scene ${scene.name}: missing ${[...shot.missing].join(' ')}`);
      images.set(`scenes/${scene.name}.png`, shot.image);
      const ref = scene.reference && referenceDir && join(referenceDir, scene.reference);
      if (ref && existsSync(ref)) images.set(`compare/${scene.name}.png`, sideBySide(decodePng(readFileSync(ref), ref), shot.image, scene.name));
    } catch (e) {
      errors.push(`scene ${scene.name}: ${e.message}`);
    }
  }
  return { images, errors };
}

/** Reference (left, scaled to the render's height) and render (right), labelled. */
function sideBySide(ref, img, name) {
  const h = Math.max(img.height, 360);
  const k = h / ref.height;
  const rw = Math.round(ref.width * k);
  const scaled = { width: rw, height: h, px: new Uint8Array(rw * h * 4) };
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < rw; x++) {
      const si = (Math.min(ref.height - 1, Math.floor(y / k)) * ref.width + Math.min(ref.width - 1, Math.floor(x / k))) * 4;
      scaled.px.set(ref.rgba.subarray(si, si + 4), (y * rw + x) * 4);
    }
  }
  const pad = 16;
  const out = blank(rw + img.width + pad * 3, h + pad * 2 + 24);
  drawText(out, pad, pad, `${name}: owner reference | render`, [232, 232, 226], 2);
  blit(out, scaled, pad, pad + 24);
  blit(out, img, rw + pad * 2, pad + 24 + Math.floor((h - img.height) / 2));
  return out;
}

