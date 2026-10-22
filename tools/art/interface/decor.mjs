// Decor storage GUI atlases: one 256 x 256 sheet per menu size, in the house panel style
// (interface/kit.mjs) and vanilla's chest geometry (FurnitureStorageMenu): the piece's slot grid from
// y 18, the player inventory below it, panel height 114 + 18 per row. Cabinets lay 27 or 54 slots
// in rows of nine; display shelves 3, 6 or 9 in rows of three, centred, each row resting on a
// drawn shelf board. Deterministic.
import { C, Sheet, hex, panel, slot, inventory } from './kit.mjs';

const GRID_TOP = 18;
const SLOT = 18;

function atlas(slots, columns, display) {
  const rows = slots / columns;
  const s = new Sheet();
  panel(s, 114 + rows * SLOT);
  const left = 8 + ((9 - columns) * SLOT) / 2;
  for (let i = 0; i < slots; i++) slot(s, left + (i % columns) * SLOT, GRID_TOP + Math.floor(i / columns) * SLOT);
  if (display) {
    // a dark wood shelf board under each row of a display, with gold end studs
    for (let r = 0; r < rows; r++) {
      const y = GRID_TOP + r * SLOT + 17;
      s.rect(left - 4, y, columns * SLOT + 6, 1, C.woodLo);
      s.set(left - 4, y, hex('#c9a23a'));
      s.set(left + columns * SLOT + 1, y, hex('#c9a23a'));
    }
  }
  inventory(s, GRID_TOP + rows * SLOT + 13);
  return s.image();
}

export const cabinet_27 = () => atlas(27, 9, false);
export const cabinet_54 = () => atlas(54, 9, false);
export const display_3 = () => atlas(3, 3, true);
export const display_6 = () => atlas(6, 3, true);
export const display_9 = () => atlas(9, 3, true);
