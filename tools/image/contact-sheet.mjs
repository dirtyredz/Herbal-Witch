// Labelled review sheets: a title and a grid of equally sized cells, each an RGBA image with a
// caption under it. Deterministic; shared by the texture generator and the model previewer.
import { drawText, textWidth, GLYPH_H } from './bitmap-font.mjs';

export const SHEET_BACKGROUND = [58, 60, 64];
const TEXT = [232, 232, 226];
const MUTED = [170, 172, 176];

/** A blank RGBA canvas filled with a colour. */
export function blank(width, height, colour = SHEET_BACKGROUND, alpha = 255) {
  const px = new Uint8Array(width * height * 4);
  for (let i = 0; i < width * height; i++) px.set([colour[0], colour[1], colour[2], alpha], i * 4);
  return { width, height, px };
}

/** Alpha-composites src over dst at (x, y), with optional integer nearest-neighbour scale. */
export function blit(dst, src, x, y, scale = 1) {
  for (let sy = 0; sy < src.height * scale; sy++) {
    for (let sx = 0; sx < src.width * scale; sx++) {
      const dx = x + sx;
      const dy = y + sy;
      if (dx < 0 || dy < 0 || dx >= dst.width || dy >= dst.height) continue;
      const si = (Math.floor(sy / scale) * src.width + Math.floor(sx / scale)) * 4;
      const a = src.px[si + 3] / 255;
      if (a === 0) continue;
      const di = (dy * dst.width + dx) * 4;
      for (let c = 0; c < 3; c++) dst.px[di + c] = Math.round(src.px[si + c] * a + dst.px[di + c] * (1 - a));
      dst.px[di + 3] = 255;
    }
  }
}

/** Shortens a caption to fit maxWidth pixels at the given text scale, keeping its tail. */
function fit(text, maxWidth, scale) {
  let t = text;
  while (t.length > 3 && textWidth(t, scale) > maxWidth) t = t.slice(1);
  return t === text ? t : '..' + t.slice(2);
}

/**
 * Composes cells [{image, label, note?}] into a sheet with `columns` columns. Every cell occupies
 * cellW x cellH (images are centred, never rescaled: callers render at a common world scale so
 * sizes stay comparable). Returns an RGBA canvas.
 */
export function contactSheet({ title, cells, columns, cellW, cellH, textScale = 2, gap = 12 }) {
  const lineH = (GLYPH_H + 3) * textScale;
  const rowsN = Math.max(1, Math.ceil(cells.length / columns));
  const headH = title ? lineH * 2 : gap;
  const width = gap + columns * (cellW + gap);
  const height = headH + rowsN * (cellH + lineH * 2 + gap) + gap;
  const sheet = blank(width, height);
  if (title) drawText(sheet, gap, gap, fit(title, width - 2 * gap, textScale), TEXT, textScale);
  cells.forEach((cell, i) => {
    const cx = gap + (i % columns) * (cellW + gap);
    const cy = headH + Math.floor(i / columns) * (cellH + lineH * 2 + gap);
    const ox = cx + Math.floor((cellW - cell.image.width) / 2);
    const oy = cy + Math.floor((cellH - cell.image.height) / 2);
    blit(sheet, cell.image, ox, oy);
    drawText(sheet, cx, cy + cellH + 4, fit(cell.label, cellW, textScale), TEXT, textScale);
    if (cell.note) drawText(sheet, cx, cy + cellH + 4 + lineH, fit(cell.note, cellW, textScale), MUTED, textScale);
  });
  return sheet;
}
