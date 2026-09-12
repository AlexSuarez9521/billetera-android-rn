import {useState} from 'react';

import {
  BridgeError,
  buscarDestino,
  mensajeDe,
  send,
  transferir,
} from '../../shared/bridge';
import type {ResultadoTransferencia} from '../../shared/tipos';
import {
  esCelularValido,
  esMontoValido,
  formatearMontoEntrada,
  parseMonto,
} from '../../shared/validaciones';

type Paso = 'destino' | 'monto' | 'resultado';

export function useTransferencia() {
  const [paso, setPaso] = useState<Paso>('destino');
  const [celular, setCelular] = useState('');
  const [destino, setDestino] = useState('');
  const [monto, setMonto] = useState('');
  const [descripcion, setDescripcion] = useState('');
  const [ocupado, setOcupado] = useState(false);
  const [confirmando, setConfirmando] = useState(false);
  const [resultado, setResultado] = useState<ResultadoTransferencia | null>(
    null,
  );
  // Se guarda el código y no el texto: el mensaje sale siempre de la misma tabla,
  // venga del nativo o de una validación de acá.
  const [codigo, setCodigo] = useState<string | null>(null);

  function cambiarCelular(texto: string) {
    setCelular(texto.replace(/\D/g, '').slice(0, 10));
    setCodigo(null);
  }

  function cambiarMonto(texto: string) {
    setMonto(formatearMontoEntrada(texto));
    setCodigo(null);
  }

  async function buscar() {
    if (ocupado) {
      return;
    }
    if (!esCelularValido(celular)) {
      setCodigo('CELULAR_INVALIDO');
      return;
    }
    setCodigo(null);
    setOcupado(true);
    try {
      const cuenta = await buscarDestino({celular});
      setDestino(cuenta.nombre);
      setPaso('monto');
    } catch (e) {
      setCodigo(codigoDe(e));
    } finally {
      setOcupado(false);
    }
  }

  function cambiarDestino() {
    setPaso('destino');
    setDestino('');
    setCodigo(null);
  }

  function revisar() {
    if (!esMontoValido(parseMonto(monto))) {
      setCodigo('MONTO_INVALIDO');
      return;
    }
    setCodigo(null);
    setConfirmando(true);
  }

  async function confirmar() {
    if (ocupado) {
      return;
    }
    setOcupado(true);
    try {
      const hecha = await transferir({
        celular,
        monto: parseMonto(monto),
        descripcion: descripcion.trim() || undefined,
      });
      // El aviso va antes de pintar el resultado; si el usuario se devuelve de
      // una, Home ya tiene que estar enterada.
      send('TRANSFER_SUCCESS', {movimientoId: hecha.movimientoId});
      setConfirmando(false);
      setResultado(hecha);
      setPaso('resultado');
    } catch (e) {
      setConfirmando(false);
      setCodigo(codigoDe(e));
    } finally {
      setOcupado(false);
    }
  }

  return {
    paso,
    celular,
    destino,
    monto,
    descripcion,
    resultado,
    ocupado,
    confirmando,
    error: codigo === null ? null : mensajeDe(codigo),
    cambiarCelular,
    cambiarMonto,
    cambiarDescripcion: setDescripcion,
    cambiarDestino,
    buscar,
    revisar,
    cancelar: () => setConfirmando(false),
    confirmar,
  };
}

function codigoDe(e: unknown): string {
  return e instanceof BridgeError ? e.codigo : 'ERROR_INTERNO';
}
