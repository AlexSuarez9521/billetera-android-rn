import React from 'react';
import {StyleSheet, Text, View} from 'react-native';

import {colores, espacio, texto} from '../theme';

const estilos = StyleSheet.create({
  fila: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  simbolo: {
    width: 22,
    height: 16,
    borderRadius: 5,
    backgroundColor: colores.verde,
    transform: [{rotate: '12deg'}],
    marginRight: espacio.s + 2,
  },
  nombre: {
    fontSize: texto.titulo,
    fontWeight: '700',
    letterSpacing: 0.3,
    color: colores.tinta,
  },
});

export default function Wordmark() {
  return (
    <View style={estilos.fila}>
      <View style={estilos.simbolo} />
      <Text style={estilos.nombre}>Billetera</Text>
    </View>
  );
}
