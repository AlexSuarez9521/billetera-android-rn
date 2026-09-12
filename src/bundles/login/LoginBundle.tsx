import React, {useEffect, useRef, useState} from 'react';
import {StyleSheet, Text, View} from 'react-native';

import {BridgeError, init, login, mensajeDe, send} from '../../shared/bridge';
import Aviso from '../../shared/components/Aviso';
import Boton from '../../shared/components/Boton';
import Campo from '../../shared/components/Campo';
import Pantalla from '../../shared/components/Pantalla';
import Wordmark from '../../shared/components/Wordmark';
import {colores, espacio, texto} from '../../shared/theme';
import type {PropsIniciales} from '../../shared/tipos';
import {esCelularValido} from '../../shared/validaciones';
import FormularioRegistro from './FormularioRegistro';

export default function LoginBundle(props: PropsIniciales) {
  const puenteListo = useRef(false);
  if (!puenteListo.current) {
    init(props);
    puenteListo.current = true;
  }

  const [registrando, setRegistrando] = useState(false);
  const [celular, setCelular] = useState('');
  const [clave, setClave] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);
  const rescate = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  useEffect(() => {
    return () => clearTimeout(rescate.current);
  }, []);

  async function entrar() {
    const falla = revisar(celular, clave);
    if (falla) {
      setError(falla);
      return;
    }
    setError(null);
    setCargando(true);
    try {
      const sesion = await login({celular: celular.trim(), clave});
      // El botón se queda cargando a propósito: Android navega a Home al recibir
      // LOGIN_SUCCESS y apagarlo aquí solo alcanza a parpadear.
      await send('LOGIN_SUCCESS', {sessionId: sesion.sessionId});
      // Pero si no navega (el sessionId que anuncio no cuadra con el suyo, o el
      // evento no llegó a la Activity) la pantalla quedaría girando para siempre.
      rescate.current = setTimeout(() => {
        setCargando(false);
        setError(mensajeDe('ERROR_INTERNO'));
      }, 5000);
    } catch (e) {
      setCargando(false);
      setError(
        e instanceof BridgeError ? e.message : mensajeDe('ERROR_INTERNO'),
      );
    }
  }

  const aviso = error ? (
    <Aviso tipo="error">{error}</Aviso>
  ) : props.motivo === 'expirada' ? (
    <Aviso tipo="alerta">Tu sesión expiró, ingresa de nuevo</Aviso>
  ) : null;

  return (
    <Pantalla>
      <View style={estilos.marca}>
        <Wordmark />
      </View>
      {registrando ? (
        <FormularioRegistro alVolver={() => setRegistrando(false)} />
      ) : (
        <>
          {aviso}
          <Text style={estilos.intro}>Ingresa tu celular y tu clave</Text>
          <Campo
            etiqueta="Celular"
            valor={celular}
            alCambiar={setCelular}
            testID="celular"
            keyboardType="number-pad"
            maxLength={10}
            autoComplete="tel"
          />
          <Campo
            etiqueta="Clave"
            valor={clave}
            alCambiar={setClave}
            testID="clave"
            secureTextEntry
            autoCapitalize="none"
          />
          <Boton titulo="Ingresar" onPress={entrar} cargando={cargando} />
          <Boton
            titulo="Crear cuenta"
            variante="texto"
            style={estilos.alterna}
            onPress={() => {
              setError(null);
              setRegistrando(true);
            }}
          />
        </>
      )}
    </Pantalla>
  );
}

// Repito aquí lo que ya valida Android para no cruzar el puente por algo que se
// ve en pantalla; la autoridad sigue siendo el lado nativo.
function revisar(celular: string, clave: string): string | null {
  if (!celular.trim() || !clave) {
    return mensajeDe('CAMPOS_OBLIGATORIOS');
  }
  if (!esCelularValido(celular)) {
    return mensajeDe('CELULAR_INVALIDO');
  }
  if (clave.length < 6) {
    return mensajeDe('CLAVE_DEBIL');
  }
  return null;
}

const estilos = StyleSheet.create({
  marca: {
    marginBottom: espacio.xl,
  },
  intro: {
    fontSize: texto.normal,
    color: colores.gris,
    marginBottom: espacio.l,
  },
  alterna: {
    marginTop: espacio.s,
  },
});
