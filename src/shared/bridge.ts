import {DeviceEventEmitter, NativeModules} from 'react-native';

import {cifrar, descifrar, setClaveCanal} from './crypto';
import type {
  Credenciales,
  DatosRegistro,
  Destino,
  Movimiento,
  OrdenTransferencia,
  PropsIniciales,
  ResultadoTransferencia,
  SesionIniciada,
} from './tipos';

const nativo = NativeModules.BilleteraBridge;

const MENSAJES: {[codigo: string]: string} = {
  CAMPOS_OBLIGATORIOS: 'Completa todos los campos',
  CELULAR_INVALIDO: 'El celular debe tener 10 dígitos y empezar por 3',
  CREDENCIALES_INVALIDAS: 'Celular o clave incorrectos',
  USUARIO_INACTIVO: 'Tu cuenta está inactiva, comunícate con soporte',
  USUARIO_YA_EXISTE: 'Ese celular ya tiene una cuenta',
  CLAVE_DEBIL: 'La clave debe tener al menos 6 caracteres',
  SESION_INVALIDA: 'Tu sesión expiró',
  MONTO_INVALIDO: 'Ingresa un monto mayor a cero',
  SALDO_INSUFICIENTE: 'No tienes saldo suficiente',
  DESTINO_NO_EXISTE: 'Ese celular no tiene cuenta',
  DESTINO_INACTIVO: 'La cuenta destino está inactiva',
  MISMA_CUENTA: 'No puedes transferirte a ti mismo',
  PAYLOAD_INVALIDO: 'No pudimos procesar la solicitud',
  ERROR_INTERNO: 'Ocurrió un error, intenta de nuevo',
};

export class BridgeError extends Error {
  readonly codigo: string;

  constructor(codigo: string) {
    super(mensajeDe(codigo));
    this.name = 'BridgeError';
    this.codigo = codigo;
  }
}

export function mensajeDe(codigo: string): string {
  return MENSAJES[codigo] ?? MENSAJES.ERROR_INTERNO;
}

export function init(props: PropsIniciales): void {
  setClaveCanal(props.claveCanal);
}

export function login(datos: Credenciales): Promise<SesionIniciada> {
  return pedir('login', datos);
}

export function registrar(datos: DatosRegistro): Promise<SesionIniciada> {
  return pedir('registrar', datos);
}

export function buscarDestino(datos: {celular: string}): Promise<Destino> {
  return pedir('buscarDestino', datos);
}

export function transferir(
  datos: OrdenTransferencia,
): Promise<ResultadoTransferencia> {
  return pedir('transferir', datos);
}

export async function movimientos(): Promise<Movimiento[]> {
  try {
    const sobre = await nativo.movimientos();
    return (descifrar(sobre) as {movimientos: Movimiento[]}).movimientos;
  } catch (e) {
    throw comoBridgeError(e);
  }
}

export async function send(evento: string, payload?: unknown): Promise<void> {
  try {
    await nativo.send(
      evento,
      payload === undefined ? null : await cifrar(payload),
    );
  } catch {
    console.warn(`No se pudo entregar ${evento} al contenedor`);
  }
}

/** Escucha un evento del contenedor y devuelve la función para darse de baja. */
export function on<T = undefined>(
  evento: string,
  atender: (payload: T) => void,
): () => void {
  const sub = DeviceEventEmitter.addListener(
    evento,
    (envelope?: string | null) => {
      let datos: unknown;
      if (envelope != null) {
        try {
          datos = descifrar(envelope);
        } catch {
          console.warn(`Descarto ${evento}: el payload no descifra`);
          return;
        }
      }
      atender(datos as T);
    },
  );
  return () => sub.remove();
}

async function pedir<T>(metodo: string, datos: unknown): Promise<T> {
  try {
    return descifrar(await nativo[metodo](await cifrar(datos))) as T;
  } catch (e) {
    throw comoBridgeError(e);
  }
}

// El nativo rechaza con (codigo, mensaje) y en JS eso llega como un Error con .code.
// El texto que ve el usuario lo decide esta tabla, no el mensaje que venga de Android.
function comoBridgeError(e: unknown): BridgeError {
  if (e instanceof BridgeError) {
    return e;
  }
  const codigo = (e as {code?: unknown} | null)?.code;
  return new BridgeError(typeof codigo === 'string' ? codigo : 'ERROR_INTERNO');
}
