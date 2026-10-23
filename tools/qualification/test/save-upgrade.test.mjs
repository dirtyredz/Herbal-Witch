import { test } from 'node:test';
import assert from 'node:assert/strict';
import { mkdtempSync, writeFileSync, readFileSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join } from 'node:path';
import { T, serialize, parse, plain } from '../save-upgrade/nbt.mjs';
import { readRegion, writeRegion, forEachBlock, stateString } from '../save-upgrade/anvil.mjs';

const tag = (t, v) => ({ t, v });
const compound = (entries) => tag(T.compound, new Map(entries));

/** One chunk at (1, -2) whose section 0 holds stone everywhere except one herb bed at (3, 5, 7). */
function chunk() {
  const bits = 4, perLong = 16, data = new BigInt64Array(4096 / perLong);
  const index = 5 * 256 + 7 * 16 + 3;
  data[Math.floor(index / perLong)] = BigInt.asIntN(64, 1n << BigInt((index % perLong) * bits));
  const palette = tag(T.list, { of: T.compound, items: [
    new Map([['Name', tag(T.string, 'minecraft:stone')]]),
    new Map([['Name', tag(T.string, 'herbalwitch:botany/garden/herb_bed')], ['Properties', compound([['moisture', tag(T.string, '7')], ['north', tag(T.string, 'true')]])]]),
  ] });
  const section = new Map([['Y', tag(T.byte, 0)], ['block_states', compound([['palette', palette], ['data', tag(T.longArray, data)]])]]);
  return { name: '', root: compound([['DataVersion', tag(T.int, 3465)], ['xPos', tag(T.int, 1)], ['zPos', tag(T.int, -2)],
    ['Status', tag(T.string, 'minecraft:full')], ['sections', tag(T.list, { of: T.compound, items: [section] })],
    ['block_entities', tag(T.list, { of: T.compound, items: [] })], ['big', tag(T.long, -5n)], ['f', tag(T.float, 0.5)]]) };
}

test('NBT round-trips byte for byte', () => {
  const bytes = serialize(chunk());
  assert.deepEqual(serialize(parse(bytes)), bytes);
  assert.equal(plain(parse(bytes).root).big, '-5L');
});

test('a region file round-trips and its packed palette indices decode to world positions', () => {
  const dir = mkdtempSync(join(tmpdir(), 'hwq-'));
  const file = join(dir, 'r.0.-1.mca');
  writeRegion(file, [{ index: 1 + 30 * 32, timestamp: 7, doc: chunk() }]);
  const [read] = readRegion(file);
  assert.equal(read.index, 1 + 30 * 32);
  assert.equal(read.timestamp, 7);
  const found = [];
  forEachBlock(read.doc, (e) => e.get('Name').v.startsWith('herbalwitch:'), (e, x, y, z) => found.push([stateString(e), x, y, z]));
  assert.deepEqual(found, [['herbalwitch:botany/garden/herb_bed[moisture=7,north=true]', 16 + 3, 5, -32 + 7]]);
  writeRegion(file, readRegion(file));
  assert.deepEqual(readRegion(file)[0].doc.root.v.get('xPos'), tag(T.int, 1));
});
