// Fixed orthographic cameras. Models face north (their front is the -z side), so "front" looks at
// the north face from the north, and the isometric views look down from the north-east or the
// north-west at 30 degrees. "gui" is the item GUI camera: it looks along -z at the model after its
// gui display transform.

const DEG = Math.PI / 180;

function basis(forward) {
  const len = Math.hypot(...forward);
  const f = forward.map((v) => v / len);
  // right = forward x worldUp, up = right x forward
  let r = [f[1] * 0 - f[2] * 1, f[2] * 0 - f[0] * 0, f[0] * 1 - f[1] * 0];
  const rl = Math.hypot(...r) || 1;
  r = r.map((v) => v / rl);
  const u = [r[1] * f[2] - r[2] * f[1], r[2] * f[0] - r[0] * f[2], r[0] * f[1] - r[1] * f[0]];
  return { forward: f, right: r, up: u };
}

/** A view looking from compass yaw (0 = from the north, 90 = from the east) down at pitch degrees. */
export function view(name, yawDeg, pitchDeg) {
  const yaw = yawDeg * DEG;
  const pitch = pitchDeg * DEG;
  // camera sits at direction (sin yaw, ., -cos yaw) from the subject; it looks the opposite way
  const from = [Math.sin(yaw) * Math.cos(pitch), Math.sin(pitch), -Math.cos(yaw) * Math.cos(pitch)];
  return { name, ...basis(from.map((v) => -v)) };
}

export const VIEWS = {
  front: view('front', 0, 0),
  iso: view('iso', 45, 30), // from the north-east, above
  isoWest: view('iso-west', -45, 30), // from the north-west, above
  back: view('back', 180, 25), // from the south, above
  top: view('top', 0, 89.9),
  gui: { name: 'gui', forward: [0, 0, -1], right: [1, 0, 0], up: [0, 1, 0] },
};

/** Screen projection of a model-space point: [x right, y down, depth away from the camera]. */
export function project(cam, p) {
  return [
    p[0] * cam.right[0] + p[1] * cam.right[1] + p[2] * cam.right[2],
    -(p[0] * cam.up[0] + p[1] * cam.up[1] + p[2] * cam.up[2]),
    p[0] * cam.forward[0] + p[1] * cam.forward[1] + p[2] * cam.forward[2],
  ];
}
