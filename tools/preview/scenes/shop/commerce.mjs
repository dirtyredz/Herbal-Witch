// Commerce review scenes (Milestone 11): the register closed and open on actual apothecary counters,
// the stock jar on a counter, and the wall shelf and order board against proxy walls, each in every
// horizontal facing. Display merchandise is a block-entity projection drawn in game, not in previews.
const HW = 'herbalwitch:';
const FACINGS = ['north', 'east', 'south', 'west'];
const BEHIND = { north: [0, 0, 1], east: [-1, 0, 0], south: [0, 0, -1], west: [1, 0, 0] };
const COUNTER = `${HW}decor/surfaces/apothecary_counter`;

const onCounter = (name, block, props) => ({
  name, view: 'iso', scale: 14,
  entries: FACINGS.flatMap((facing, i) => [
    { at: [i * 3, 0, 0], block: COUNTER, props: { facing } },
    { at: [i * 3, 1, 0], block, props: { facing, ...props } },
  ]),
});

const onWall = (name, block) => ({
  name, view: 'iso', scale: 14,
  entries: FACINGS.flatMap((facing, i) => {
    const [dx, dy, dz] = BEHIND[facing];
    return [
      { at: [i * 3 + dx, 1 + dy, 1 + dz], proxy: 'stone' },
      { at: [i * 3, 1, 1], block, props: { facing } },
    ];
  }),
});

export const SHOP_SCENES = [
  onCounter('shop-register-closed', `${HW}shop/register`, { open: 'false' }),
  onCounter('shop-register-open', `${HW}shop/register`, { open: 'true' }),
  onCounter('shop-stock-jar', `${HW}shop/stock_jar`, {}),
  onWall('shop-stock-shelf', `${HW}shop/stock_shelf`),
  onWall('shop-order-board', `${HW}shop/order_board`),
];
