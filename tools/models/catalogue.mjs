// Every generated model, and the model directories the generator owns outright.
import { mortar } from './stations/mortar.mjs';
import { kettle, infusionPot } from './stations/vessels.mjs';
import { dryingRack } from './stations/rack.mjs';
import { hearth } from './stations/hearth.mjs';
import { stove } from './stations/stove.mjs';
import { bed } from './botany/bed.mjs';
import { herbModels } from './botany/plants.mjs';
import { armchair } from './decor/armchair.mjs';
import { decorPieces } from './decor/pieces.mjs';
import { hungBundles } from './preparations/bundles.mjs';
import { HERB_LIST } from '../art/botany/herbs.mjs';

/** Directories (under models/) whose JSON comes only from this generator. */
export const OWNED = ['block/stations/mortar', 'block/stations/kettle', 'block/stations/infusion_pot', 'block/stations/drying_rack', 'block/stations/hearth', 'block/stations/stove', 'block/botany/garden', 'block/decor/templates', 'block/preparations/bundles',
  ...new Set(HERB_LIST.map(([group]) => `block/botany/${group}`))];

/** [{path, textures, elements, cutout?, display?, ao?}] in a stable order. */
export const MODELS = () => [...mortar(), ...kettle(), ...infusionPot(), ...dryingRack(), ...hearth(), ...stove(), ...bed(), ...herbModels(HERB_LIST), ...armchair(), ...decorPieces(), ...hungBundles(HERB_LIST)];
