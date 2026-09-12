const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

const archivo = path.resolve(__dirname, '..', 'test-vectors', 'aes-gcm.json');

const sesion = {
  sessionId: '8c2f7c1e-4d3a-4b6e-9d2f-0a1b2c3d4e5f',
  userId: 'u10001',
  name: 'Alex Suárez',
  phone: '3001234567',
  expiresAt: '2026-05-07T10:30:00',
};

// La llave y el IV se reutilizan si el vector ya existe: si cambiaran habría que
// tocar las pruebas de Kotlin y de Jest que comparan contra este archivo.
function fijos() {
  if (!fs.existsSync(archivo)) {
    return {
      clavePruebaB64: crypto.randomBytes(32).toString('base64'),
      ivPruebaB64: crypto.randomBytes(12).toString('base64'),
    };
  }
  const previo = JSON.parse(fs.readFileSync(archivo, 'utf8'));
  return {
    clavePruebaB64: previo.clavePruebaB64,
    ivPruebaB64: previo.ivPruebaB64,
  };
}

async function main() {
  // @noble/ciphers solo publica ESM; desde un script CommonJS toca importarlo así.
  const {gcm} = await import('@noble/ciphers/aes.js');
  const {utf8ToBytes} = await import('@noble/ciphers/utils.js');
  const {base64} = await import('@scure/base');

  const {clavePruebaB64, ivPruebaB64} = fijos();
  const llave = base64.decode(clavePruebaB64);
  const iv = base64.decode(ivPruebaB64);
  const textoPlano = JSON.stringify(sesion);
  const data = gcm(llave, iv).encrypt(utf8ToBytes(textoPlano));

  const vector = {
    nota: 'Vector solo para pruebas de interoperabilidad entre Kotlin (javax.crypto) y JS (@noble/ciphers). La app nunca usa esta llave; la de verdad se genera al azar en cada arranque. Se regenera con npm run vector.',
    algoritmo:
      'AES-256-GCM, IV de 12 bytes, tag de 128 bits al final de data, sin AAD, texto plano en UTF-8',
    clavePruebaB64,
    ivPruebaB64,
    textoPlano,
    envelope: JSON.stringify({
      iv: base64.encode(iv),
      data: base64.encode(data),
    }),
  };

  fs.writeFileSync(archivo, JSON.stringify(vector, null, 2) + '\n');
  console.log(`Escrito ${path.relative(process.cwd(), archivo)}`);
}

main().catch(e => {
  console.error(e.message);
  process.exit(1);
});
