import {render, screen} from '@testing-library/react-native';
import React from 'react';

import MovimientosBundle from '../src/bundles/movimientos/MovimientosBundle';
import type {PropsIniciales} from '../src/shared/tipos';
import {claveCanalDePrueba, responde} from './setup/nativeModules';

const props: PropsIniciales = {
  claveCanal: claveCanalDePrueba,
  bundle: 'movimientos',
  esDebug: true,
  version: '1.0',
};

// Las cabeceras dicen "Hoy" y "Ayer" contra el reloj real, así que las fechas
// del mock se arman desde la de hoy.
function isoHace(dias: number, hora: string): string {
  const f = new Date();
  f.setDate(f.getDate() - dias);
  const mes = `${f.getMonth() + 1}`.padStart(2, '0');
  const dia = `${f.getDate()}`.padStart(2, '0');
  return `${f.getFullYear()}-${mes}-${dia}T${hora}`;
}

test('separa los movimientos de hoy de los de ayer', async () => {
  responde('movimientos', {
    movimientos: [
      {
        id: 'm1',
        fecha: isoHace(0, '08:12:00'),
        tipo: 'CREDITO',
        valor: 1500000,
        descripcion: 'Abono de nómina',
        estado: 'EXITOSA',
      },
      {
        id: 'm2',
        fecha: isoHace(1, '17:40:00'),
        tipo: 'DEBITO',
        valor: 250000,
        descripcion: 'Pago servicios públicos',
        estado: 'EXITOSA',
      },
      {
        id: 'm3',
        fecha: isoHace(1, '09:15:00'),
        tipo: 'DEBITO',
        valor: 900000,
        descripcion: 'Transferencia rechazada: saldo insuficiente',
        estado: 'RECHAZADA',
      },
    ],
  });

  await render(<MovimientosBundle {...props} />);

  expect(await screen.findByText('Hoy')).toBeTruthy();
  expect(screen.getByText('Ayer')).toBeTruthy();
  expect(screen.getByText('+$ 1.500.000')).toBeTruthy();
  expect(screen.getByText('-$ 250.000')).toBeTruthy();
  expect(screen.getByText('08:12 · Crédito')).toBeTruthy();
  expect(screen.getByText('Rechazada')).toBeTruthy();
  expect(screen.getAllByText('Exitosa')).toHaveLength(2);
});

test('con la cuenta sin movimientos muestra el aviso y ninguna fila', async () => {
  responde('movimientos', {movimientos: []});

  await render(<MovimientosBundle {...props} />);

  expect(await screen.findByText('Aún no tienes movimientos')).toBeTruthy();
  expect(screen.queryByText('Hoy')).toBeNull();
});
