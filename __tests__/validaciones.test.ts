import {
  esCelularValido,
  esMontoValido,
  formatearMontoEntrada,
  parseMonto,
} from '../src/shared/validaciones';

test.each(['3001234567', '3109876543', '3999999999'])(
  'acepta el celular %s',
  celular => {
    expect(esCelularValido(celular)).toBe(true);
  },
);

test.each([
  ['300123456', 'nueve dígitos'],
  ['30012345678', 'once dígitos'],
  ['2001234567', 'no empieza por 3'],
  ['300 123 4567', 'con espacios en medio'],
  ['300123456a', 'con una letra'],
  ['', 'vacío'],
])('rechaza %s (%s)', celular => {
  expect(esCelularValido(celular)).toBe(false);
});

test('ignora los espacios de los extremos', () => {
  expect(esCelularValido('  3001234567 ')).toBe(true);
});

test('el monto tiene que ser un entero positivo', () => {
  expect(esMontoValido(1)).toBe(true);
  expect(esMontoValido(1250000)).toBe(true);
  expect(esMontoValido(0)).toBe(false);
  expect(esMontoValido(-5000)).toBe(false);
  expect(esMontoValido(1000.5)).toBe(false);
  expect(esMontoValido(Number.NaN)).toBe(false);
});

test('parseMonto se queda solo con los dígitos', () => {
  expect(parseMonto('1.250.000')).toBe(1250000);
  expect(parseMonto('$ 25.000')).toBe(25000);
  expect(parseMonto('50000')).toBe(50000);
  expect(parseMonto('')).toBe(0);
  expect(parseMonto('abc')).toBe(0);
});

test('formatearMontoEntrada agrupa de a tres mientras se escribe', () => {
  expect(formatearMontoEntrada('1')).toBe('1');
  expect(formatearMontoEntrada('12')).toBe('12');
  expect(formatearMontoEntrada('1234')).toBe('1.234');
  expect(formatearMontoEntrada('1250000')).toBe('1.250.000');
  expect(formatearMontoEntrada('1.250.00')).toBe('125.000');
  expect(formatearMontoEntrada('')).toBe('');
});
