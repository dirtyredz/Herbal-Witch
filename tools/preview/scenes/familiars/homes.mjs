/** Three real home blockstates on floor proxies, in all horizontal orientations. */
export const FAMILIAR_HOME_SCENES = ['bed', 'nest', 'perch'].map(key => ({
  name: `familiar-home-${key}`, view: 'iso', scale: 14,
  entries: ['north', 'east', 'south', 'west'].flatMap((facing, i) => [
    {at: [i * 2, 0, 0], proxy: 'stone'},
    {at: [i * 2, 1, 0], block: `herbalwitch:familiars/home/${key}`, props: {facing}},
  ]),
}));
