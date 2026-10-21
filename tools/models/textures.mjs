// Texture variables the procedural models bind, and which of them are cutout (alpha 0/255).
const HW = 'herbalwitch:block/';

/** The shared house materials the herb bed's wood is built from. */
export const MATERIAL = {
  wood: `${HW}material/wood`,
  wood_post: `${HW}material/wood_post`,
  wood_end: `${HW}material/wood_end`,
  gold: `${HW}material/gold`,
};

/** Whether elements use a cutout texture: a hung bundle's `bundle_<herb>` sprite. */
export function needsCutout(elements) {
  return elements.some((e) => Object.values(e.faces).some((f) => f.texture.startsWith('#bundle_')));
}
