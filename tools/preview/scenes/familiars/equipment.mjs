import {poseAngles, transformBonePoint} from '../../../entities/poses.mjs';

// Mirrors FamiliarRigRenderer's attachment in model pixels, including ItemRenderer's centre.
const ATTACHMENTS = {raven: ['raven_satchel', 2.5, 1, 0, .25], owl: ['owl_lens', 1.5, -1, -3.5, .25], snake: ['snake_ring', 0, 0, 0, .25]};
export function attachedEquipment(rig, pose, baker) {
  const [gear, x, y, z, scale] = ATTACHMENTS[rig.species];
  const bone = rig.bones.find(b => b.name === rig.equipmentBone);
  const angles = poseAngles(rig.species, pose, 2);
  return baker.bake(`herbalwitch:block/familiars/equipment/${gear}/base`).quads.map(q => ({...q,
    pos: q.pos.map(p => transformBonePoint([
      bone.pivot[0] + x + (p[0] - 8) * scale,
      bone.pivot[1] - y + (p[1] - 8) * scale,
      bone.pivot[2] + z - (p[2] - 8) * scale,
    ], bone.name, rig, angles)),
    normal: transformBonePoint([q.normal[0], q.normal[1], -q.normal[2]], bone.name, rig, angles, true),
  }));
}

export const FAMILIAR_EQUIPMENT_SCENES = ['cat_bell', 'raven_satchel', 'owl_lens', 'fox_pouch', 'wolf_harness', 'snake_ring', 'rabbit_sachet', 'bat_chime'].map(gear => ({
  name: `familiar-equipment-${gear}`, view: 'iso', scale: 16,
  entries: [{at: [0, 0, 0], model: `herbalwitch:block/familiars/equipment/${gear}/base`}],
}));
