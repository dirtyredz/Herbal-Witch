// Blockstate evaluation: variant keys and multipart conditions against a property map, and the
// enumeration of every state a blockstate file can distinguish (for coverage).

function parseKey(key) {
  if (key === '' || key === 'normal') return {};
  return Object.fromEntries(key.split(',').map((kv) => kv.split('=')));
}

function matchesWhen(when, props) {
  if (!when) return true;
  if (when.OR) return when.OR.some((w) => matchesWhen(w, props));
  if (when.AND) return when.AND.every((w) => matchesWhen(w, props));
  return Object.entries(when).every(([k, v]) => String(v).split('|').includes(String(props[k])));
}

const first = (entry) => (Array.isArray(entry) ? entry[0] : entry);

/** The [{model, x, y, uvlock}] a blockstate selects for `props` (string values). */
export function select(blockstate, props) {
  if (blockstate.variants) {
    for (const [key, entry] of Object.entries(blockstate.variants)) {
      const want = parseKey(key);
      if (Object.entries(want).every(([k, v]) => String(props[k]) === v)) return [first(entry)];
    }
    throw new Error(`no variant matches ${JSON.stringify(props)}`);
  }
  if (blockstate.multipart) {
    return blockstate.multipart.filter((part) => matchesWhen(part.when, props)).map((part) => first(part.apply));
  }
  throw new Error('blockstate has neither variants nor multipart');
}

function collect(when, values) {
  if (!when) return;
  for (const [k, v] of Object.entries(when)) {
    if (k === 'OR' || k === 'AND') v.forEach((w) => collect(w, values));
    else for (const s of String(v).split('|')) (values[k] ??= new Set()).add(s);
  }
}

/** Every property combination the file distinguishes, as string maps, in a stable order. */
export function enumerate(blockstate) {
  const values = {};
  if (blockstate.variants) {
    for (const key of Object.keys(blockstate.variants)) {
      for (const [k, v] of Object.entries(parseKey(key))) (values[k] ??= new Set()).add(v);
    }
  } else {
    for (const part of blockstate.multipart ?? []) collect(part.when, values);
  }
  // A boolean named by only one of its values still has both states.
  for (const set of Object.values(values)) if ([...set].every((v) => v === 'true' || v === 'false')) set.add('true').add('false');
  const names = Object.keys(values).sort();
  let combos = [{}];
  for (const name of names) {
    const next = [];
    for (const c of combos) for (const v of [...values[name]].sort()) next.push({ ...c, [name]: v });
    combos = next;
  }
  return combos;
}

/** A stable label for a property map. */
export const stateLabel = (props) =>
  Object.entries(props).sort(([a], [b]) => a.localeCompare(b)).map(([k, v]) => `${k}=${v}`).join(',');
