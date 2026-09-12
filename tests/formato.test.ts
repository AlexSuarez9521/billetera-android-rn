import {formatoCOP, formatoDia, formatoFecha} from '../src/shared/formato';

describe('formatoCOP', () => {
  it('pone el separador de miles', () => {
    expect(formatoCOP(1250000)).toBe('$ 1.250.000');
    expect(formatoCOP(480000)).toBe('$ 480.000');
    expect(formatoCOP(1000)).toBe('$ 1.000');
    expect(formatoCOP(950)).toBe('$ 950');
    expect(formatoCOP(0)).toBe('$ 0');
  });

  it('el signo va antes del peso', () => {
    expect(formatoCOP(-25000)).toBe('-$ 25.000');
  });
});

describe('formatoFecha', () => {
  it('usa el mes abreviado y la hora de 24', () => {
    expect(formatoFecha('2026-05-07T10:30:00')).toBe('7 may 2026, 10:30');
    expect(formatoFecha('2026-09-08T17:40:00')).toBe('8 sep 2026, 17:40');
    expect(formatoFecha('2026-01-31T08:05:00')).toBe('31 ene 2026, 08:05');
  });

  it('no se corre de día con horas tempranas', () => {
    expect(formatoFecha('2026-09-05T00:15:00')).toBe('5 sep 2026, 00:15');
  });
});

describe('formatoDia', () => {
  beforeEach(() => {
    jest.useFakeTimers().setSystemTime(new Date(2026, 8, 11, 9, 0, 0));
  });

  afterEach(() => {
    jest.useRealTimers();
  });

  it('resuelve hoy y ayer contra el reloj', () => {
    expect(formatoDia('2026-09-11T08:12:00')).toBe('Hoy');
    expect(formatoDia('2026-09-10T23:59:00')).toBe('Ayer');
  });

  it('para el resto escribe el día y el mes completo', () => {
    expect(formatoDia('2026-09-05T08:12:00')).toBe('5 de septiembre');
    expect(formatoDia('2026-08-30T10:00:00')).toBe('30 de agosto');
  });
});
