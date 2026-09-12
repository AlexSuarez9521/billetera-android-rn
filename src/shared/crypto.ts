import {gcm} from '@noble/ciphers/aes.js';
import {utf8ToBytes} from '@noble/ciphers/utils.js';
import {base64} from '@scure/base';
import {NativeModules} from 'react-native';

let clave: Uint8Array | null = null;

export function setClaveCanal(b64: string): void {
  clave = base64.decode(b64);
}

export async function cifrar(obj: unknown): Promise<string> {
  const iv = await NativeModules.BilleteraBridge.randomBytes(12);
  return cifrarConIv(obj, base64.decode(iv));
}

// Solo la usan las pruebas del vector compartido. En la app el IV siempre sale
// del SecureRandom de Android, nunca de aquí.
export function cifrarConIv(obj: unknown, iv: Uint8Array): string {
  const data = gcm(llaveActual(), iv).encrypt(utf8ToBytes(JSON.stringify(obj)));
  return JSON.stringify({iv: base64.encode(iv), data: base64.encode(data)});
}

export function descifrar(envelope: string): unknown {
  const llave = llaveActual();
  try {
    const sobre = JSON.parse(envelope);
    const plano = gcm(llave, base64.decode(sobre.iv)).decrypt(
      base64.decode(sobre.data),
    );
    return JSON.parse(bytesAUtf8(plano));
  } catch {
    // Un tag alterado, un base64 roto y un JSON incompleto acaban igual a propósito:
    // el contenido no puede salir en el mensaje del error.
    throw new Error('No se pudo descifrar el mensaje');
  }
}

function llaveActual(): Uint8Array {
  if (!clave) {
    throw new Error('La llave del canal no llegó en las props iniciales');
  }
  return clave;
}

/* eslint-disable no-bitwise */
// Hermes 0.81 no trae TextDecoder, así que bytesToUtf8 de noble no sirve aquí.
function bytesAUtf8(bytes: Uint8Array): string {
  let texto = '';
  let i = 0;
  while (i < bytes.length) {
    const b = bytes[i++];
    let punto: number;
    if (b < 0x80) {
      punto = b;
    } else if (b < 0xe0) {
      punto = ((b & 0x1f) << 6) | (bytes[i++] & 0x3f);
    } else if (b < 0xf0) {
      punto =
        ((b & 0x0f) << 12) | ((bytes[i++] & 0x3f) << 6) | (bytes[i++] & 0x3f);
    } else {
      punto =
        ((b & 0x07) << 18) |
        ((bytes[i++] & 0x3f) << 12) |
        ((bytes[i++] & 0x3f) << 6) |
        (bytes[i++] & 0x3f);
    }
    texto += String.fromCodePoint(punto);
  }
  return texto;
}
