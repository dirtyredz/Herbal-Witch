// Native 32px emblem; uses the same botanical ramps as the herb item sprites.
// node tools/art/branding/logo.mjs [--check] [--output <job directory>]
import { readFileSync, writeFileSync, mkdirSync } from 'node:fs';
import { resolve, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';
import { image, upscale } from '../pixels.mjs';
import { R } from '../drawing/plant-motifs.mjs';
import { png } from '../../image/png-encode.mjs';

export function logo() {
  const img = image(32, () => [0, 0, 0, 0]);
  const rect = (x, y, w, h, c) => {
    for (let j = y; j < y + h; j++) for (let i = x; i < x + w; i++)
      img.px[j * 32 + i] = [...c, 255];
  };
  // Bent crown and generous brim: large contiguous shapes survive small icons.
  const rows = [[17,22],[14,23],[13,23],[12,20],[12,19],[11,19],
    [11,20],[10,20],[10,21],[9,21],[9,22],[8,22],[8,23],[7,24],
    [7,24],[6,25],[4,27],[2,29],[1,30],[2,29],[4,27],[7,24]];
  rows.forEach(([l,r], n) => {
    rect(l, n + 4, r-l+1, 1, R.black[0]);
    if (r-l > 2) rect(l+1, n+4, r-l-1, 1, R.deep[1]);
  });
  rect(15,5,6,1,R.deep[2]); rect(14,6,7,1,R.deep[2]);
  rect(13,7,5,2,R.deep[2]); rect(12,9,3,3,R.deep[2]);
  rect(11,12,3,3,R.deep[2]); rect(10,15,3,3,R.deep[2]);
  // Plum ribbon and brass buckle, with a small pink stitch.
  rect(8,18,15,2,R.plum[0]); rect(7,20,17,2,R.plum[1]);
  rect(8,20,7,1,R.plum[2]); rect(10,21,3,1,R.rose[1]);
  rect(15,18,5,4,R.yellow[0]); rect(16,18,3,1,R.yellow[2]);
  rect(16,19,3,2,R.black[1]); rect(18,19,1,2,R.yellow[1]);
  rect(4,23,22,1,R.deep[2]); rect(7,24,17,1,R.deep[1]);
  // Sage leaves and lavender tucked into the right side of the hatband.
  rect(22,10,1,10,R.grey[1]); rect(21,16,1,4,R.grey[1]);
  rect(24,12,1,5,R.grey[0]); rect(23,16,1,3,R.grey[1]);
  rect(19,13,2,2,R.sage[0]); rect(20,14,2,2,R.sage[1]);
  rect(23,17,3,2,R.sage[0]); rect(24,16,3,2,R.sage[1]);
  rect(25,16,2,1,R.sage[2]);
  rect(21,8,3,2,R.violet[0]); rect(21,10,3,2,R.violet[1]);
  rect(22,7,1,2,R.violet[2]); rect(21,9,1,2,R.violet[2]);
  rect(24,11,2,3,R.violet[1]); rect(24,10,1,2,R.violet[2]);
  return img;
}

export function preview() {
  const sheet = { width: 416, height: 288, px: [] };
  for (let y=0; y<sheet.height; y++) for (let x=0; x<sheet.width; x++)
    sheet.px.push((Math.floor(x/8)+Math.floor(y/8))%2 ? [226,222,213,255] : [242,239,232,255]);
  for (const [size,left] of [[256,8],[64,280],[32,368]]) {
    const img = upscale(logo(),size/32);
    const top = (sheet.height-size)/2;
    img.px.forEach((c,i) => { if(c[3]) sheet.px[(top+Math.floor(i/size))*sheet.width+left+i%size]=c; });
  }
  return sheet;
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const root = fileURLToPath(new URL('../../../', import.meta.url));
  const target = resolve(root, 'src/main/resources/herbalwitch-logo.png');
  const bytes = png(upscale(logo(),8));
  const args = process.argv.slice(2);
  if (args.includes('--check')) {
    if (!readFileSync(target).equals(bytes)) throw new Error('Logo is stale; run node tools/art/branding/logo.mjs');
    console.log('Logo is reproducible (256x256 PNG).');
  } else {
    mkdirSync(dirname(target), {recursive:true});
    writeFileSync(target,bytes);
  }
  const at = args.indexOf('--output');
  if(at !== -1) {
    if (!args[at+1]) throw new Error('--output requires a directory');
    const output = resolve(args[at+1]);
    mkdirSync(output,{recursive:true});
    writeFileSync(resolve(output,'herbalwitch-logo.png'),bytes);
    writeFileSync(resolve(output,'herbalwitch-preview.png'),png(preview()));
  }
}
