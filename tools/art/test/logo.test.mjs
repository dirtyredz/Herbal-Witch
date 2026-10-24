import { test } from 'node:test';
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { logo, preview } from '../branding/logo.mjs';
import { upscale } from '../pixels.mjs';
import { png } from '../../image/png-encode.mjs';
import { decodePng } from '../../image/png-decode.mjs';

test('Forge names the reproducible transparent 256px logo', () => {
  const root = new URL('../../../', import.meta.url);
  const metadata = readFileSync(new URL('src/main/resources/META-INF/mods.toml', root),'utf8');
  assert.match(metadata,/^logoFile="herbalwitch-logo.png"$/m);
  const bytes = readFileSync(new URL('src/main/resources/herbalwitch-logo.png',root));
  assert.deepEqual(bytes,png(upscale(logo(),8)));
  const img = decodePng(bytes);
  assert.equal(img.width,256); assert.equal(img.height,256);
  assert.equal(img.rgba[3],0);
  assert.ok(logo().px.filter(c => c[3]).length > 300);
});

test('preview includes exact native pixels at 256, 64 and 32px', () => {
  const sheet = preview();
  for (const [size,left] of [[256,8],[64,280],[32,368]]) {
    const img = upscale(logo(),size/32);
    img.px.forEach((c,i) => {
      if(c[3]) assert.deepEqual(sheet.px[((288-size)/2+Math.floor(i/size))*416+left+i%size],c);
    });
  }
});
