const MES_CORTO = 'ene feb mar abr may jun jul ago sep oct nov dic'.split(' ');

const MESES = [
  'enero',
  'febrero',
  'marzo',
  'abril',
  'mayo',
  'junio',
  'julio',
  'agosto',
  'septiembre',
  'octubre',
  'noviembre',
  'diciembre',
];

export function formatoCOP(valor: number): string {
  const entero = Math.round(Math.abs(valor)).toString();
  const conPuntos = entero.replace(/\B(?=(\d{3})+(?!\d))/g, '.');
  return `${valor < 0 ? '-' : ''}$ ${conPuntos}`;
}

export function formatoFecha(iso: string): string {
  const f = fechaLocal(iso);
  const mes = MES_CORTO[f.getMonth()];
  const hora = `${dos(f.getHours())}:${dos(f.getMinutes())}`;
  return `${f.getDate()} ${mes} ${f.getFullYear()}, ${hora}`;
}

export function formatoDia(iso: string): string {
  const hoy = new Date();
  if (iso.slice(0, 10) === diaIso(hoy)) {
    return 'Hoy';
  }
  const ayer = new Date(hoy.getFullYear(), hoy.getMonth(), hoy.getDate() - 1);
  if (iso.slice(0, 10) === diaIso(ayer)) {
    return 'Ayer';
  }
  const f = fechaLocal(iso);
  return `${f.getDate()} de ${MESES[f.getMonth()]}`;
}

// Android manda "2026-09-11T08:12:00" sin zona. Lo parto a mano porque hay motores
// que ese formato lo leen como UTC y los días se corrían una posición.
function fechaLocal(iso: string): Date {
  const [fecha, hora = '00:00:00'] = iso.split('T');
  const [a, m, d] = fecha.split('-').map(Number);
  const [hh, mm, ss] = hora.split(':').map(Number);
  return new Date(a, m - 1, d, hh || 0, mm || 0, ss || 0);
}

function diaIso(f: Date): string {
  return `${f.getFullYear()}-${dos(f.getMonth() + 1)}-${dos(f.getDate())}`;
}

function dos(n: number): string {
  return n < 10 ? `0${n}` : `${n}`;
}
