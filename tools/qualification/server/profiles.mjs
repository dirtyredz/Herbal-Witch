// Production-server profiles for qualify-server.mjs. Every boot lists its console commands and the
// markers its log must contain; "wait <ms>" pauses without sending anything.
import { readdirSync, statSync } from 'node:fs';
import { join, relative, dirname, sep } from 'node:path';
import { fileURLToPath } from 'node:url';

const root = join(dirname(fileURLToPath(import.meta.url)), '../../..');

/** Every Herbal Witch block id, from the shipped blockstate files (one per block). */
export function blockIds() {
  const dir = join(root, 'src/generated/resources/assets/herbalwitch/blockstates');
  const ids = [];
  const walk = (d) => readdirSync(d).forEach((f) => {
    const p = join(d, f);
    if (statSync(p).isDirectory()) walk(p);
    else if (f.endsWith('.json')) ids.push('herbalwitch:' + relative(dir, p).split(sep).join('/').slice(0, -5));
  });
  walk(dir);
  return ids.sort();
}

const Y = -60;
const grid = (i) => [(i % 20) * 3, Y, 16 + Math.floor(i / 20) * 3];
const ids = blockIds();
const placeAll = ids.map((id, i) => `setblock ${grid(i).join(' ')} ${id}`);
const KETTLE = `herbalwitch:stations/kettle/station{Inventory:{Size:5,Items:[{Slot:0b,id:"herbalwitch:preparations/herbs/temperate/chamomile/powder",Count:3b}]}}`;
const stable = 'herbalwitch:stations/mortar/station';
const stableAt = [70, Y, 0];

export const PROFILES = {
  // The release jar alone: every block placed, stateful pieces saved, a second boot reads them back.
  base: {
    jars: [],
    boots: [
      {
        commands: ['forceload add -16 -16 79 79', 'gamerule doDaylightCycle false',
          `setblock 64 ${Y} 0 herbalwitch:decor/lighting/table_lamp[lit=true]`, `setblock 66 ${Y} 0 ${KETTLE}`, `setblock ${stableAt.join(' ')} ${stable}`,
          ...placeAll, 'wait 3000', `say HWQ placed ${ids.length} block ids`, 'save-all flush', 'wait 3000'],
        expect: [`HWQ placed ${ids.length} block ids`, 'Saved the game'],
      },
      {
        commands: [`execute if block 64 ${Y} 0 herbalwitch:decor/lighting/table_lamp[lit=true] run say HWQ lamp lit persisted`,
          `data get block 66 ${Y} 0 Inventory`, `execute if block ${stableAt.join(' ')} ${stable} run say HWQ mortar persisted`, 'say HWQ second boot done'],
        expect: ['HWQ lamp lit persisted', 'herbalwitch:preparations/herbs/temperate/chamomile/powder', 'HWQ mortar persisted', 'HWQ second boot done'],
      },
    ],
  },
  // The supported optional mods together, release jars as players install them.
  optional: {
    jars: ['curios-forge-5.14.1+1.20.1.jar', 'FarmersDelight-1.20.1-1.3.4.jar', 'ThirstWasTaken-1.20.1-1.4.0.jar',
      'jei-1.20.1-forge-15.62.0.219.jar', 'emi-1.1.24+1.20.1+forge.jar'],
    boots: [{
      commands: ['forceload add 0 0 31 31', `setblock 0 ${Y} 0 herbalwitch:stations/kettle/station`, `setblock 2 ${Y} 0 farmersdelight:stove`,
        `setblock 4 ${Y} 0 herbalwitch:magic/ward/stone`, 'say HWQ optional done'],
      expect: ['HWQ optional done'],
    }],
    // Foreign mods' own warnings are theirs; Herbal Witch errors still fail the profile.
    tolerate: [],
  },
  // Doggy Talents Next stays separate from the ordinary optional set (docs/COMPATIBILITY.md).
  doggy: {
    jars: ['DoggyTalentsNext-1.20.1-1.19.1.jar'],
    boots: [{ commands: ['say HWQ doggy done'], expect: ['HWQ doggy done'] }],
  },
  // KubeJS adds and removes processing recipes through ordinary recipe JSON; a mortar then grinds
  // the added recipe on a running server. Restart-only: KubeJS recipe events run at datapack load.
  kubejs: {
    jars: ['kubejs-forge-2001.6.5-build.26.jar', 'rhino-forge-2001.2.3-build.10.jar', 'architectury-9.2.14-forge.jar'],
    scripts: 'kubejs',
    boots: [{
      commands: ['forceload add 0 0 15 15',
        `setblock 0 ${Y} 0 herbalwitch:stations/mortar/station{Inventory:{Size:5,Items:[{Slot:0b,id:"minecraft:wheat",Count:2b}]}}`,
        'wait 6000', `data get block 0 ${Y} 0 Inventory`, 'say HWQ kubejs done'],
      expect: ['HWQ kubejs added=true', 'HWQ kubejs removed=true', 'minecraft:bread', 'HWQ kubejs done'],
    }],
  },
};
