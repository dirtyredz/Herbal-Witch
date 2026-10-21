// Texture variables the generated models bind, and which of them are cutout (alpha 0/255).
const HW = 'herbalwitch:block/';

export const MATERIAL = {
  wood: `${HW}material/wood`,
  wood_post: `${HW}material/wood_post`,
  wood_end: `${HW}material/wood_end`,
  dark_wood: `${HW}material/dark_wood`,
  iron: `${HW}material/iron`,
  gold: `${HW}material/gold`,
  copper: `${HW}material/copper`,
  plum: `${HW}material/plum`,
  ivy: `${HW}material/ivy`,
};

export const STATION = {
  ...MATERIAL,
  plum_painted: `${HW}stations/plum_painted`,
  plum_lid: `${HW}stations/plum_lid`,
  stone_dark: `${HW}stations/stone_dark`,
  plaque: `${HW}stations/plaque`,
  fire_glow: `${HW}stations/fire_glow`,
  coals: `${HW}stations/coals`,
  flame: `${HW}stations/flame`,
  cloth: `${HW}stations/cloth_red`,
  mortar_stone: `${HW}stations/mortar_stone`,
  plinth_stone: `${HW}stations/plinth_stone`,
  pestle_stone: `${HW}stations/pestle_stone`,
  herb_pile: `${HW}stations/herb_pile`,
  bundle_green: `${HW}stations/bundle_green`,
  bundle_lavender: `${HW}stations/bundle_lavender`,
  bundle_white: `${HW}stations/bundle_white`,
  bundle_yellow: `${HW}stations/bundle_yellow`,
};

/** Variables whose textures have transparency: a model using any of them renders cutout. */
export const CUTOUT = new Set(['ivy', 'flame', 'cloth', 'bundle_green', 'bundle_lavender', 'bundle_white', 'bundle_yellow',
  'cross', 'leaf', 'glass', 'vine', 'runner', 'bundle']);

/** Whether elements use a cutout variable. */
export function needsCutout(elements) {
  return elements.some((e) => Object.values(e.faces).some((f) => CUTOUT.has(f.texture.slice(1))));
}
