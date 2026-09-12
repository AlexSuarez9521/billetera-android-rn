import {formatoDia} from '../../shared/formato';
import type {Movimiento} from '../../shared/tipos';

export type SeccionDia = {
  title: string;
  data: Movimiento[];
};

export function agruparPorDia(movimientos: Movimiento[]): SeccionDia[] {
  const porDia = new Map<string, Movimiento[]>();

  for (const mov of [...movimientos].sort(delMasNuevo)) {
    const dia = mov.fecha.slice(0, 10);
    const acumulados = porDia.get(dia);
    if (acumulados) {
      acumulados.push(mov);
    } else {
      porDia.set(dia, [mov]);
    }
  }

  return [...porDia.values()].map(movs => ({
    title: formatoDia(movs[0].fecha),
    data: movs,
  }));
}

// La fecha siempre viene como "yyyy-MM-ddTHH:mm:ss", así que comparar los textos
// ordena bien y me ahorro construir un Date por movimiento.
function delMasNuevo(a: Movimiento, b: Movimiento): number {
  return b.fecha.localeCompare(a.fecha);
}
