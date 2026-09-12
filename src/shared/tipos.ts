export type NombreBundle = 'login' | 'home' | 'transferencia' | 'movimientos';

export type MotivoLogin = 'logout' | 'expirada';

export type PropsIniciales = {
  claveCanal: string;
  bundle: NombreBundle;
  esDebug: boolean;
  version: string;
  motivo?: MotivoLogin;
};

export type Credenciales = {
  celular: string;
  clave: string;
};

export type DatosRegistro = {
  nombre: string;
  celular: string;
  clave: string;
};

export type OrdenTransferencia = {
  celular: string;
  monto: number;
  descripcion?: string;
};

export type SesionIniciada = {
  sessionId: string;
  nombre: string;
  celular: string;
  expiraEn: string;
};

export type DatosHome = {
  nombre: string;
  celular: string;
  saldo: number;
  expiraEn: string;
};

export type Destino = {
  nombre: string;
};

export type ResultadoTransferencia = {
  movimientoId: string;
  nuevoSaldo: number;
  destinoNombre: string;
  fecha: string;
};

export type Movimiento = {
  id: string;
  fecha: string;
  tipo: 'DEBITO' | 'CREDITO';
  valor: number;
  descripcion: string;
  estado: 'EXITOSA' | 'RECHAZADA';
};
