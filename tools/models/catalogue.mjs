// Every generated model, and the model directories the generator owns outright.
import { mortar } from './stations/mortar.mjs';
import { kettle, infusionPot } from './stations/vessels.mjs';
import { dryingRack } from './stations/rack.mjs';
import { hearth } from './stations/hearth.mjs';
import { stove } from './stations/stove.mjs';
import { bed } from './botany/bed.mjs';
import { plantTemplates } from './botany/herbs.mjs';
import { matureHerbs } from './botany/plants.mjs';
import { HERB_LIST } from '../art/botany/herbs.mjs';

/** Directories (under models/) whose JSON comes only from this generator. */
export const OWNED = ['block/stations/mortar', 'block/stations/kettle', 'block/stations/infusion_pot', 'block/stations/drying_rack', 'block/stations/hearth', 'block/stations/stove', 'block/botany/garden', 'block/botany/templates'];

/** [{path, textures, elements, cutout?, display?, ao?}] in a stable order. */
export const MODELS = () => [...mortar(), ...kettle(), ...infusionPot(), ...dryingRack(), ...hearth(), ...stove(), ...bed(), ...plantTemplates(), ...matureHerbs(HERB_LIST)];
