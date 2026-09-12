export function esCelularValido(celular: string): boolean {
  return /^3\d{9}$/.test(celular.trim());
}

export function esMontoValido(monto: number): boolean {
  return Number.isInteger(monto) && monto > 0;
}

export function parseMonto(entrada: string): number {
  const digitos = entrada.replace(/\D/g, '');
  return digitos === '' ? 0 : parseInt(digitos, 10);
}

export function formatearMontoEntrada(entrada: string): string {
  const monto = parseMonto(entrada);
  return monto === 0
    ? ''
    : monto.toString().replace(/\B(?=(\d{3})+(?!\d))/g, '.');
}
