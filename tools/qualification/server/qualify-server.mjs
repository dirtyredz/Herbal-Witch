// Production dedicated-server qualification (Milestone 12, `qualifyDedicatedServer`, ANU only).
//
//   node tools/qualification/server/qualify-server.mjs --server <installed Forge 1.20.1-47.4.10 server>
//        --inputs <dir of optional-mod jars> --jar <reobfuscated candidate jar> --out <fresh run dir> [--profiles base,optional,doggy,kubejs]
//
// Unlike the GameTest server (userdev, deobfuscated, test classes on the classpath) this starts the
// real installed Forge server with the release jar and nothing else of ours, drives it through its
// console, and reads its log. Each profile gets its own fresh world. A profile passes only when every
// boot reaches "Done", every console check prints its marker, the server stops by itself with exit
// code 0, and the log holds no exception, crash or Herbal Witch error.
import { spawn } from 'node:child_process';
import { createHash } from 'node:crypto';
import { cpSync, existsSync, mkdirSync, readFileSync, readdirSync, symlinkSync, writeFileSync, copyFileSync } from 'node:fs';
import { join, basename, resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { PROFILES } from './profiles.mjs';

const here = dirname(fileURLToPath(import.meta.url));
const args = Object.fromEntries(process.argv.slice(2).reduce((acc, a, i, all) => (a.startsWith('--') ? [...acc, [a.slice(2), all[i + 1]]] : acc), []));
for (const required of ['server', 'inputs', 'jar', 'out']) if (!args[required]) { console.error(`missing --${required}`); process.exit(2); }
const selected = (args.profiles ?? Object.keys(PROFILES).join(',')).split(',');
const sha256 = (file) => createHash('sha256').update(readFileSync(file)).digest('hex');
const java = process.env.JAVA_HOME ? join(process.env.JAVA_HOME, 'bin', 'java') : 'java';

/** A fresh server directory: shared libraries by junction, our own config, world, mods and scripts. */
function prepare(profile, spec) {
  const dir = resolve(args.out, profile);
  mkdirSync(join(dir, 'mods'), { recursive: true });
  symlinkSync(resolve(args.server, 'libraries'), join(dir, 'libraries'), 'junction');
  copyFileSync(join(args.server, 'user_jvm_args.txt'), join(dir, 'user_jvm_args.txt'));
  writeFileSync(join(dir, 'eula.txt'), 'eula=true\n');
  writeFileSync(join(dir, 'server.properties'), [
    'level-name=world', 'level-type=minecraft\\:flat', 'online-mode=false', 'spawn-protection=0', 'max-tick-time=-1',
    'view-distance=4', 'simulation-distance=4', 'spawn-monsters=false', 'enable-command-block=false', 'motd=Herbal Witch qualification',
  ].join('\n') + '\n');
  const jars = [resolve(args.jar), ...spec.jars.map((name) => resolve(args.inputs, name))];
  const manifest = {};
  for (const jar of jars) {
    if (!existsSync(jar)) throw new Error(`${profile}: missing input ${jar}`);
    copyFileSync(jar, join(dir, 'mods', basename(jar)));
    manifest[basename(jar)] = sha256(jar);
  }
  if (spec.scripts) cpSync(join(here, spec.scripts), join(dir, 'kubejs'), { recursive: true });
  return { dir, manifest };
}

/** One boot: wait for "Done", run each console command (a short pause apart), then stop. */
function boot(dir, commands, label) {
  return new Promise((resolveBoot) => {
    const argsFile = process.platform === 'win32' ? 'win_args.txt' : 'unix_args.txt';
    const child = spawn(java, ['@user_jvm_args.txt', `@libraries/net/minecraftforge/forge/1.20.1-47.4.10/${argsFile}`, 'nogui'], { cwd: dir });
    let out = '';
    let done = false;
    const started = Date.now();
    const timer = setTimeout(() => { out += `\n[qualification] ${label} timed out\n`; child.kill(); }, 420_000);
    const onData = (chunk) => {
      out += chunk.toString();
      if (!done && /\]: Done \(/.test(out)) {
        done = true;
        let delay = 2000;
        for (const command of [...commands, 'stop']) {
          if (command.startsWith('wait ')) { delay += Number(command.slice(5)); continue; }
          setTimeout(() => child.stdin.write(command + '\n'), delay);
          delay += command.startsWith('setblock') ? 60 : 1500;
        }
      }
    };
    child.stdout.on('data', onData);
    child.stderr.on('data', onData);
    child.on('exit', (code) => {
      clearTimeout(timer);
      writeFileSync(join(dir, `${label}.console.log`), out);
      resolveBoot({ code, done, seconds: Math.round((Date.now() - started) / 1000), out });
    });
  });
}

/**
 * Problems in a console log: crashes, mods that failed to load, Herbal Witch errors, and any exception
 * whose message or stack trace (the next 40 lines) names Herbal Witch. Other exceptions belong to the
 * foreign mods (their own data packs referencing absent mods) and are reported, not failed.
 */
function problems(text) {
  const lines = text.split(/\r?\n/);
  const ours = [], foreign = [];
  lines.forEach((line, i) => {
    if (/Crash report|FATAL|has failed to load|ModLoadingException/.test(line)) ours.push(line.trim().slice(0, 300));
    else if (/\/ERROR\]/.test(line) && /herbalwitch|dirtyredz/i.test(line)) ours.push(line.trim().slice(0, 300));
    else if (/Exception/.test(line) && !/\bWARN\b/.test(line)) {
      const trace = lines.slice(i, i + 40).join('\n');
      (/herbalwitch|dirtyredz/i.test(trace) ? ours : foreign).push(line.trim().slice(0, 300));
    }
  });
  return { ours, foreign: [...new Set(foreign)] };
}

const report = { candidate: { jar: basename(args.jar), sha256: sha256(args.jar) }, java, profiles: {} };
let failed = false;
mkdirSync(args.out, { recursive: true });
for (const profile of selected) {
  const spec = PROFILES[profile];
  if (!spec) throw new Error(`unknown profile ${profile}`);
  const { dir, manifest } = prepare(profile, spec);
  const result = { mods: manifest, boots: [], failures: [] };
  for (const [i, step] of spec.boots.entries()) {
    const label = `boot${i + 1}`;
    const run = await boot(dir, step.commands, label);
    const log = existsSync(join(dir, 'logs', 'latest.log')) ? readFileSync(join(dir, 'logs', 'latest.log'), 'utf8') : '';
    const kubejs = existsSync(join(dir, 'logs', 'kubejs', 'server.log')) ? readFileSync(join(dir, 'logs', 'kubejs', 'server.log'), 'utf8') : '';
    const everything = run.out + '\n' + log + '\n' + kubejs;
    const missing = step.expect.filter((marker) => !everything.includes(marker));
    const found = problems(run.out + '\n' + log);
    const bad = found.ours;
    result.boots.push({ label, exit: run.code, reachedDone: run.done, seconds: run.seconds, missing, problems: bad.slice(0, 20), foreignExceptions: found.foreign.slice(0, 20) });
    if (!run.done) result.failures.push(`${label}: never reached Done`);
    if (run.code !== 0) result.failures.push(`${label}: exit ${run.code}`);
    for (const m of missing) result.failures.push(`${label}: missing "${m}"`);
    for (const p of bad.slice(0, 5)) result.failures.push(`${label}: ${p}`);
  }
  report.profiles[profile] = result;
  console.log(`${result.failures.length ? 'FAIL' : 'PASS'} ${profile}: ${result.boots.map((b) => `${b.label} exit ${b.exit} in ${b.seconds}s`).join(', ')}`);
  for (const f of result.failures) console.log(`  ${f}`);
  if (result.failures.length) failed = true;
}
writeFileSync(join(args.out, 'report.json'), JSON.stringify(report, null, 1));
if (failed) process.exit(1);
console.log(`PASS dedicated server: ${selected.join(', ')} with ${report.candidate.jar} (${report.candidate.sha256.slice(0, 16)})`);
