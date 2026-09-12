import React from 'react';
import {StyleSheet, Text, TextInput, View} from 'react-native';
import type {TextInputProps} from 'react-native';

import {colores, espacio, radio, texto} from '../theme';

type Props = TextInputProps & {
  etiqueta: string;
  valor: string;
  alCambiar: (valor: string) => void;
  error?: string | null;
};

export default function Campo({
  etiqueta,
  valor,
  alCambiar,
  error,
  ...resto
}: Props) {
  return (
    <View style={estilos.campo}>
      <Text style={estilos.etiqueta}>{etiqueta}</Text>
      <TextInput
        {...resto}
        value={valor}
        onChangeText={alCambiar}
        placeholderTextColor={colores.gris}
        style={[estilos.entrada, error ? estilos.entradaConError : null]}
      />
      {error ? <Text style={estilos.error}>{error}</Text> : null}
    </View>
  );
}

const estilos = StyleSheet.create({
  campo: {
    marginBottom: espacio.m,
  },
  etiqueta: {
    fontSize: texto.menor,
    color: colores.gris,
    marginBottom: espacio.xs,
  },
  entrada: {
    borderWidth: 1,
    borderColor: colores.linea,
    borderRadius: radio.boton,
    backgroundColor: colores.blanco,
    paddingHorizontal: espacio.m,
    paddingVertical: 12,
    fontSize: texto.medio,
    color: colores.tinta,
  },
  entradaConError: {
    borderColor: colores.rojo,
  },
  error: {
    fontSize: texto.menor,
    color: colores.rojo,
    marginTop: espacio.xs,
  },
});
