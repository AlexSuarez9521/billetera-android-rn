const {spawnSync} = require('child_process');
const fs = require('fs');
const path = require('path');

const raiz = path.resolve(__dirname, '..');
const rnDir = path.dirname(require.resolve('react-native/package.json'));
const cli = require.resolve('react-native/cli.js');

const NOMBRES = ['login', 'home', 'transferencia', 'movimientos'];

const pedidos = process.argv.slice(2);
const desconocido = pedidos.find(n => !NOMBRES.includes(n));
if (desconocido) {
  console.error(
    `No hay un bundle llamado "${desconocido}". Son: ${NOMBRES.join(', ')}`,
  );
  process.exit(1);
}
const bundles = pedidos.length > 0 ? pedidos : NOMBRES;

const binHermes =
  process.platform === 'win32'
    ? 'win64-bin/hermesc.exe'
    : process.platform === 'darwin'
    ? 'osx-bin/hermesc'
    : 'linux64-bin/hermesc';
const hermesc = path.join(rnDir, 'sdks/hermesc', binHermes);

const intermedios = path.join(raiz, 'build/bundles');
const assets = path.join(raiz, 'android/app/src/main/assets/bundles');
fs.mkdirSync(intermedios, {recursive: true});
fs.mkdirSync(assets, {recursive: true});

function correr(comando, args) {
  const r = spawnSync(comando, args, {cwd: raiz, stdio: 'inherit'});
  if (r.error) {
    console.error(r.error.message);
    process.exit(1);
  }
  if (r.status !== 0) {
    process.exit(r.status === null ? 1 : r.status);
  }
}

for (const nombre of bundles) {
  const js = path.join(intermedios, `${nombre}.jsbundle`);
  const salida = path.join(assets, `${nombre}.android.bundle`);

  correr(process.execPath, [
    cli,
    'bundle',
    '--platform',
    'android',
    '--dev',
    'false',
    '--minify',
    'false',
    '--reset-cache',
    '--entry-file',
    `entries/${nombre}.js`,
    '--bundle-output',
    js,
  ]);

  // Hermes hace el trabajo de minificar: lo que va en assets es bytecode, no texto.
  correr(hermesc, ['-O', '-emit-binary', '-w', '-out', salida, js]);

  const kb = Math.round(fs.statSync(salida).size / 1024);
  console.log(`${nombre}.android.bundle  ${kb} KB`);
}
