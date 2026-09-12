import React from 'react';
import {act, fireEvent, render, screen} from '@testing-library/react-native';

import HomeBundle from '../src/bundles/home/HomeBundle';
import type {PropsIniciales} from '../src/shared/tipos';
import {
  claveCanalDePrueba,
  emiteEvento,
  puente,
  reiniciarPuente,
} from './setup/nativeModules';

const props: PropsIniciales = {
  claveCanal: claveCanalDePrueba,
  bundle: 'home',
  esDebug: true,
  version: '1.0',
};

const alex = {
  nombre: 'Alex Suárez',
  celular: '3001234567',
  saldo: 1250000,
  expiraEn: '2026-09-11T09:32:00',
};

beforeEach(reiniciarPuente);

test('avisa que ya escucha y pinta lo que llega en LOAD_HOME', async () => {
  await render(<HomeBundle {...props} />);

  expect(screen.getByText('Cargando')).toBeTruthy();
  expect(puente.send).toHaveBeenCalledWith('HOME_READY', null);

  await act(() => emiteEvento('LOAD_HOME', alex));

  expect(screen.getByText('Hola, Alex')).toBeTruthy();
  expect(screen.getByText('$ 1.250.000')).toBeTruthy();
  expect(screen.queryByText('Cargando')).toBeNull();
});

test('cerrar sesión se lo deja al contenedor', async () => {
  await render(<HomeBundle {...props} />);
  await act(() => emiteEvento('LOAD_HOME', alex));

  await fireEvent.press(screen.getByText('Cerrar sesión'));

  expect(puente.send).toHaveBeenCalledWith('LOGOUT', null);
});
