import React, {type ReactNode} from 'react';
import {StyleSheet, View} from 'react-native';
import type {StyleProp, ViewStyle} from 'react-native';

import {colores, espacio, radio} from '../theme';

const estilos = StyleSheet.create({
  tarjeta: {
    backgroundColor: colores.blanco,
    borderColor: colores.linea,
    borderWidth: 1,
    borderRadius: radio.tarjeta,
    padding: espacio.m,
  },
});

type Props = {
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
};

export default function Tarjeta({children, style}: Props) {
  return <View style={[estilos.tarjeta, style]}>{children}</View>;
}
