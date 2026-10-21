// Save watching for the dev-art pack: one cycle per burst of saves under src/art/blockbench (a
// Blockbench save can touch the file more than once), retrying a cycle whose project was caught
// mid-write (unreadable JSON) a few times before reporting it. A failed cycle keeps the last
// published pack; the next save tries again.
import { watch } from 'node:fs';

/**
 * Calls `run()` after `delay` ms of quiet following changes under `dir`. `run` returns
 * {ok, errors}; a result whose errors look like a partial write is retried `retries` times, and
 * only the final result goes to `report`. Returns a function that stops watching.
 */
export function watchSources(dir, run, report, { delay = 400, retries = 4, retryDelay = 500, log = console.log } = {}) {
  let timer = null;
  let running = false;
  let pending = false;
  const partial = (errors) => errors.some((e) => /not valid JSON|Unexpected end|EBUSY|EPERM/.test(e));
  const attempt = async (left) => {
    const result = await run();
    if (!result.ok && left > 0 && partial(result.errors)) {
      await new Promise((r) => setTimeout(r, retryDelay));
      return attempt(left - 1);
    }
    return result;
  };
  const fire = async () => {
    if (running) {
      pending = true;
      return;
    }
    running = true;
    try {
      report(await attempt(retries));
    } catch (e) {
      log(`dev-art: ${e.message}`);
    } finally {
      running = false;
      if (pending) {
        pending = false;
        schedule();
      }
    }
  };
  const schedule = () => {
    clearTimeout(timer);
    timer = setTimeout(fire, delay);
  };
  const watcher = watch(dir, { recursive: true }, (event, file) => {
    if (file && !/\.(bbmodel|json)$/.test(String(file))) return;
    schedule();
  });
  return () => {
    clearTimeout(timer);
    watcher.close();
  };
}
