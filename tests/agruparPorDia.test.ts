import {agruparPorDia} from '../src/bundles/movimientos/agruparPorDia';
import type {Movimiento} from '../src/shared/tipos';

beforeAll(() => {
  jest.useFakeTimers().setSystemTime(new Date(2026, 8, 11, 12, 0, 0));
});

afterAll(() => {
  jest.useRealTimers();
});

function mov(id: string, fecha: string): Movimiento {
  return {
    id,
    fecha,
    tipo: 'DEBITO',
    valor: 25000,
    descripcion: 'Pago servicios públicos',
    estado: 'EXITOSA',
  };
}

test('arma una sección por día, de la más reciente a la más vieja', () => {
  const secciones = agruparPorDia([
    mov('m1', '2026-09-11T08:12:00'),
    mov('m2', '2026-09-10T19:05:00'),
    mov('m3', '2026-09-10T07:30:00'),
    mov('m4', '2026-09-07T17:40:00'),
  ]);

  expect(secciones.map(s => s.title)).toEqual([
    'Hoy',
    'Ayer',
    '7 de septiembre',
  ]);
  expect(secciones[1].data.map(m => m.id)).toEqual(['m2', 'm3']);
});

test('ordena aunque los movimientos lleguen revueltos', () => {
  const secciones = agruparPorDia([
    mov('viejo', '2026-09-07T17:40:00'),
    mov('nuevo', '2026-09-11T08:12:00'),
    mov('medio', '2026-09-10T19:05:00'),
  ]);

  expect(secciones.map(s => s.data.map(m => m.id))).toEqual([
    ['nuevo'],
    ['medio'],
    ['viejo'],
  ]);
});

test('deja intacto el arreglo que recibe', () => {
  const entrada = [
    mov('m1', '2026-09-07T17:40:00'),
    mov('m2', '2026-09-11T08:12:00'),
  ];

  agruparPorDia(entrada);

  expect(entrada.map(m => m.id)).toEqual(['m1', 'm2']);
});

test('sin movimientos no hay secciones', () => {
  expect(agruparPorDia([])).toEqual([]);
});
