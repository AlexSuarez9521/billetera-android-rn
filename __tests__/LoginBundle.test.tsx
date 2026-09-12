import React from 'react';
import {act, fireEvent, render, screen} from '@testing-library/react-native';

import LoginBundle from '../src/bundles/login/LoginBundle';
import type {PropsIniciales} from '../src/shared/tipos';
import {
  claveCanalDePrueba,
  puente,
  rechaza,
  reiniciarPuente,
  responde,
} from './setup/nativeModules';

const props: PropsIniciales = {
  claveCanal: claveCanalDePrueba,
  bundle: 'login',
  esDebug: true,
  version: '1.0',
};

beforeEach(reiniciarPuente);

test('con los campos vacíos avisa y ni siquiera cruza el puente', async () => {
  await render(<LoginBundle {...props} />);

  await fireEvent.press(screen.getByText('Ingresar'));

  expect(screen.getByText('Completa todos los campos')).toBeOnTheScreen();
  expect(puente.login).not.toHaveBeenCalled();
});

test('muestra el mensaje del código que rechaza el nativo', async () => {
  rechaza('login', 'CREDENCIALES_INVALIDAS');
  await render(<LoginBundle {...props} />);

  await fireEvent.changeText(screen.getByTestId('celular'), '3001234567');
  await fireEvent.changeText(screen.getByTestId('clave'), 'Clave123');
  await fireEvent.press(screen.getByText('Ingresar'));

  expect(
    await screen.findByText('Celular o clave incorrectos'),
  ).toBeOnTheScreen();
  expect(puente.send).not.toHaveBeenCalled();
});

test('si el contenedor no navega, el botón deja de girar y avisa', async () => {
  responde('login', {
    sessionId: 'a6b1',
    nombre: 'Alex Suárez',
    celular: '3001234567',
    expiraEn: '2026-09-11T09:32:00',
  });
  jest.useFakeTimers();
  await render(<LoginBundle {...props} />);

  await fireEvent.changeText(screen.getByTestId('celular'), '3001234567');
  await fireEvent.changeText(screen.getByTestId('clave'), 'Clave123');
  await act(async () => {
    fireEvent.press(screen.getByText('Ingresar'));
  });
  expect(screen.queryByText('Ingresar')).toBeNull();

  await act(async () => {
    jest.advanceTimersByTime(5000);
  });
  jest.useRealTimers();

  expect(screen.getByText('Ingresar')).toBeOnTheScreen();
  expect(
    screen.getByText('Ocurrió un error, intenta de nuevo'),
  ).toBeOnTheScreen();
});
