import {gcm} from '@noble/ciphers/aes.js';
import {utf8ToBytes} from '@noble/ciphers/utils.js';
import {base64} from '@scure/base';
import {DeviceEventEmitter, NativeModules} from 'react-native';

export const claveCanalDePrueba =
  'qZw0smCDHVYSwrlA2+SsowQjY2x7IE4pOMVKobR29xk=';

const llave = base64.decode(claveCanalDePrueba);

let contador = 0;

function ivDePrueba(n = 12): Uint8Array {
  const iv = new Uint8Array(n);
  contador += 1;
  iv[0] = contador % 256;
  iv[1] = Math.floor(contador / 256) % 256;
  return iv;
}

function sobre(datos: unknown): string {
  const iv = ivDePrueba();
  const data = gcm(llave, iv).encrypt(utf8ToBytes(JSON.stringify(datos)));
  return JSON.stringify({iv: base64.encode(iv), data: base64.encode(data)});
}

export const puente = {
  randomBytes: jest.fn(async (n: number) => base64.encode(ivDePrueba(n))),
  login: jest.fn(),
  registrar: jest.fn(),
  buscarDestino: jest.fn(),
  transferir: jest.fn(),
  movimientos: jest.fn(),
  send: jest.fn(async () => undefined),
};

NativeModules.BilleteraBridge = puente;

type Metodo = keyof typeof puente;

export function responde(metodo: Metodo, datos: unknown): void {
  (puente[metodo] as jest.Mock).mockResolvedValue(sobre(datos));
}

export function rechaza(
  metodo: Metodo,
  codigo: string,
  mensaje = 'rechazado',
): void {
  // Así llega en JS un promise.reject(codigo, mensaje) del módulo nativo.
  const error = Object.assign(new Error(mensaje), {code: codigo});
  (puente[metodo] as jest.Mock).mockRejectedValue(error);
}

export function emiteEvento(evento: string, datos?: unknown): void {
  DeviceEventEmitter.emit(evento, datos === undefined ? null : sobre(datos));
}

export function reiniciarPuente(): void {
  for (const metodo of Object.keys(puente) as Metodo[]) {
    (puente[metodo] as jest.Mock).mockReset();
  }
  puente.randomBytes.mockImplementation(async (n: number) =>
    base64.encode(ivDePrueba(n)),
  );
  puente.send.mockImplementation(async () => undefined);
}
