import {act, renderHook} from '@testing-library/react-native';

import {useTransferencia} from '../src/bundles/transferencia/useTransferencia';
import {descifrar, setClaveCanal} from '../src/shared/crypto';
import {
  claveCanalDePrueba,
  puente,
  rechaza,
  reiniciarPuente,
  responde,
} from './setup/nativeModules';

type Maquina = ReturnType<typeof useTransferencia>;

const LAURA = '3109876543';

beforeEach(() => {
  reiniciarPuente();
  setClaveCanal(claveCanalDePrueba);
});

async function llegarAlMonto(result: {current: Maquina}) {
  responde('buscarDestino', {nombre: 'Laura P.'});
  await act(async () => result.current.cambiarCelular(LAURA));
  await act(async () => result.current.buscar());
}

it('va del celular al resultado y le avisa al contenedor', async () => {
  const {result} = await renderHook(() => useTransferencia());
  await llegarAlMonto(result);

  expect(result.current.paso).toBe('monto');
  expect(result.current.destino).toBe('Laura P.');

  await act(async () => result.current.cambiarMonto('25000'));
  await act(async () => result.current.cambiarDescripcion('almuerzo'));
  expect(result.current.monto).toBe('25.000');

  await act(async () => result.current.revisar());
  expect(result.current.confirmando).toBe(true);

  responde('transferir', {
    movimientoId: 'm90012',
    nuevoSaldo: 1225000,
    destinoNombre: 'Laura P.',
    fecha: '2026-09-11T10:30:00',
  });
  await act(async () => result.current.confirmar());

  expect(result.current.paso).toBe('resultado');
  expect(result.current.confirmando).toBe(false);
  expect(result.current.resultado?.nuevoSaldo).toBe(1225000);
  expect(descifrar(puente.transferir.mock.calls[0][0])).toEqual({
    celular: LAURA,
    monto: 25000,
    descripcion: 'almuerzo',
  });

  const [evento, sobre] = puente.send.mock.calls[0] as unknown[];
  expect(evento).toBe('TRANSFER_SUCCESS');
  expect(descifrar(sobre as string)).toEqual({movimientoId: 'm90012'});
});

it('no consulta el puente si el celular está incompleto', async () => {
  const {result} = await renderHook(() => useTransferencia());

  await act(async () => result.current.cambiarCelular('31098'));
  await act(async () => result.current.buscar());

  expect(puente.buscarDestino).not.toHaveBeenCalled();
  expect(result.current.paso).toBe('destino');
  expect(result.current.error).toBe(
    'El celular debe tener 10 dígitos y empezar por 3',
  );
});

it('un celular sin cuenta deja el paso donde estaba', async () => {
  rechaza('buscarDestino', 'DESTINO_NO_EXISTE');
  const {result} = await renderHook(() => useTransferencia());

  await act(async () => result.current.cambiarCelular('3001111111'));
  await act(async () => result.current.buscar());

  expect(result.current.paso).toBe('destino');
  expect(result.current.destino).toBe('');
  expect(result.current.error).toBe('Ese celular no tiene cuenta');
  expect(result.current.ocupado).toBe(false);
});

it('un monto en cero no abre la hoja de confirmación', async () => {
  const {result} = await renderHook(() => useTransferencia());
  await llegarAlMonto(result);

  await act(async () => result.current.cambiarMonto('0'));
  await act(async () => result.current.revisar());

  expect(result.current.confirmando).toBe(false);
  expect(result.current.error).toBe('Ingresa un monto mayor a cero');
});

it('con saldo insuficiente cierra la hoja y deja corregir el monto', async () => {
  const {result} = await renderHook(() => useTransferencia());
  await llegarAlMonto(result);

  await act(async () => result.current.cambiarMonto('9000000'));
  await act(async () => result.current.revisar());

  rechaza('transferir', 'SALDO_INSUFICIENTE');
  await act(async () => result.current.confirmar());

  expect(result.current.paso).toBe('monto');
  expect(result.current.confirmando).toBe(false);
  expect(result.current.resultado).toBeNull();
  expect(result.current.error).toBe('No tienes saldo suficiente');
  expect(puente.send).not.toHaveBeenCalled();

  await act(async () => result.current.cambiarMonto('90000'));
  expect(result.current.error).toBeNull();
});
