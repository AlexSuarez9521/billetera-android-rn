import React, {useEffect, useRef, useState} from 'react';
import {StyleSheet, Text} from 'react-native';

import {BridgeError, mensajeDe, registrar, send} from '../../shared/bridge';
import Aviso from '../../shared/components/Aviso';
import Boton from '../../shared/components/Boton';
import Campo from '../../shared/components/Campo';
import {colores, espacio, texto} from '../../shared/theme';
import {esCelularValido} from '../../shared/validaciones';

const estilos = StyleSheet.create({
  titulo: {
    fontSize: texto.titulo,
    fontWeight: '600',
    color: colores.tinta,
    marginBottom: espacio.l,
  },
  volver: {
    marginTop: espacio.s,
  },
});

type Errores = Partial<
  Record<'nombre' | 'celular' | 'clave' | 'confirmacion', string>
>;

type Props = {
  alVolver: () => void;
};

export default function FormularioRegistro({alVolver}: Props) {
  const [nombre, setNombre] = useState('');
  const [celular, setCelular] = useState('');
  const [clave, setClave] = useState('');
  const [confirmacion, setConfirmacion] = useState('');
  const [errores, setErrores] = useState<Errores>({});
  const [error, setError] = useState<string | null>(null);
  const [cargando, setCargando] = useState(false);
  const espera = useRef<ReturnType<typeof setTimeout> | undefined>(undefined);

  useEffect(() => () => clearTimeout(espera.current), []);

  async function crearCuenta() {
    const fallos: Errores = {};
    if (!nombre.trim()) {
      fallos.nombre = 'Escribe tu nombre y tu apellido';
    }
    if (!celular.trim()) {
      fallos.celular = 'Escribe tu celular';
    } else if (!esCelularValido(celular)) {
      fallos.celular = mensajeDe('CELULAR_INVALIDO');
    }
    if (clave.length < 6) {
      fallos.clave = mensajeDe('CLAVE_DEBIL');
    } else if (confirmacion !== clave) {
      fallos.confirmacion = 'Las dos claves tienen que ser iguales';
    }
    setErrores(fallos);
    if (Object.keys(fallos).length > 0) {
      return;
    }

    setError(null);
    setCargando(true);
    try {
      const sesion = await registrar({
        nombre: nombre.trim(),
        celular: celular.trim(),
        clave,
      });
      await send('LOGIN_SUCCESS', {sessionId: sesion.sessionId});
      // Misma red que en el ingreso: si Android no navega, suelto el botón.
      espera.current = setTimeout(() => {
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

  return (
    <>
      <Text style={estilos.titulo}>Crea tu cuenta</Text>
      {error ? <Aviso tipo="error">{error}</Aviso> : null}
      <Campo
        etiqueta="Nombre y apellido"
        valor={nombre}
        alCambiar={setNombre}
        error={errores.nombre}
        testID="nombre"
        autoCapitalize="words"
      />
      <Campo
        etiqueta="Celular"
        valor={celular}
        alCambiar={setCelular}
        error={errores.celular}
        testID="celular"
        keyboardType="number-pad"
        maxLength={10}
      />
      <Campo
        etiqueta="Clave"
        valor={clave}
        alCambiar={setClave}
        error={errores.clave}
        testID="clave"
        placeholder="Mínimo 6 caracteres"
        secureTextEntry
        autoCapitalize="none"
      />
      <Campo
        etiqueta="Confirma la clave"
        valor={confirmacion}
        alCambiar={setConfirmacion}
        error={errores.confirmacion}
        testID="confirmacion"
        secureTextEntry
        autoCapitalize="none"
      />
      <Boton titulo="Crear cuenta" onPress={crearCuenta} cargando={cargando} />
      <Boton
        titulo="Ya tengo cuenta"
        variante="texto"
        style={estilos.volver}
        onPress={alVolver}
      />
    </>
  );
}
