import React from 'react';
import type {ReactNode} from 'react';
import {StyleSheet, Text, View} from 'react-native';
import type {TextStyle, ViewStyle} from 'react-native';

import {colores, espacio, radio, texto} from '../theme';

type Tipo = 'alerta' | 'error';

type Props = {
  children: ReactNode;
  tipo: Tipo;
};

const caja: Record<Tipo, ViewStyle> = {
  alerta: {borderWidth: 1, borderColor: colores.ambar},
  error: {backgroundColor: colores.rojoSuave},
};

const letra: Record<Tipo, TextStyle> = {
  alerta: {color: colores.ambar},
  error: {color: colores.rojo},
};

export default function Aviso({children, tipo}: Props) {
  return (
    <View style={[estilos.caja, caja[tipo]]}>
      <Text style={[estilos.texto, letra[tipo]]}>{children}</Text>
    </View>
  );
}

const estilos = StyleSheet.create({
  caja: {
    borderRadius: radio.boton,
    paddingVertical: 12,
    paddingHorizontal: espacio.m,
    marginBottom: espacio.m,
  },
  texto: {
    fontSize: texto.normal,
    lineHeight: 21,
  },
});
