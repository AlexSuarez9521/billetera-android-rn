import React from 'react';
import {ActivityIndicator, Pressable, StyleSheet, Text} from 'react-native';
import type {StyleProp, ViewStyle} from 'react-native';

import {colores, espacio, radio, texto} from '../theme';

type Variante = 'primario' | 'texto';

type Props = {
  titulo: string;
  onPress: () => void;
  variante?: Variante;
  cargando?: boolean;
  deshabilitado?: boolean;
  style?: StyleProp<ViewStyle>;
};

export default function Boton({
  titulo,
  onPress,
  variante = 'primario',
  cargando = false,
  deshabilitado = false,
  style,
}: Props) {
  const inactivo = deshabilitado || cargando;
  const colorTexto = variante === 'primario' ? colores.blanco : colores.verde;

  return (
    <Pressable
      accessibilityRole="button"
      disabled={inactivo}
      onPress={onPress}
      style={({pressed}) => [
        estilos.base,
        estilos[variante],
        pressed && !inactivo ? estilos.presionado : null,
        inactivo ? estilos.inactivo : null,
        style,
      ]}>
      {cargando ? (
        <ActivityIndicator color={colorTexto} />
      ) : (
        <Text style={[estilos.titulo, {color: colorTexto}]}>{titulo}</Text>
      )}
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  base: {
    borderRadius: radio.boton,
    paddingVertical: 14,
    paddingHorizontal: espacio.m,
    alignItems: 'center',
    justifyContent: 'center',
    minHeight: 48,
  },
  primario: {
    backgroundColor: colores.verde,
  },
  texto: {
    backgroundColor: 'transparent',
    paddingVertical: espacio.s,
    minHeight: 0,
  },
  presionado: {
    opacity: 0.85,
  },
  inactivo: {
    opacity: 0.5,
  },
  titulo: {
    fontSize: texto.medio,
    fontWeight: '600',
  },
});
