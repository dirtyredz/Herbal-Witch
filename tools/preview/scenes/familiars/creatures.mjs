import {posedQuads} from '../../../entities/poses.mjs';
import {attachedEquipment} from './equipment.mjs';

export const FAMILIAR_CREATURE_SCENES = ['raven', 'crow', 'owl', 'snake'].flatMap(appearance =>
  ['idle', 'walk', 'flight', 'rest', 'attack'].filter(pose => appearance !== 'snake' || pose !== 'flight').map(pose => ({
    name: `familiar-${appearance}-${pose}`, view: 'iso', scale: 16,
    entries: [{at: [0, 0, 0], creature: appearance === 'crow' ? 'raven' : appearance, appearance, pose}],
  })));

export function creatureQuads(entry, resources, baker) {
  const rig = resources.json('entity_models', `herbalwitch:familiars/${entry.creature}/rig`);
  if (!rig) throw new Error(`Missing compiled rig ${entry.creature}`);
  return [...posedQuads(rig, entry.appearance, entry.pose, 2), ...attachedEquipment(rig, entry.pose, baker)];
}
