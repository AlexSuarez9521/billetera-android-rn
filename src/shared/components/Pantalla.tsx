import React from 'react';
import type {ReactNode} from 'react';
import {
  KeyboardAvoidingView,
  ScrollView,
  StatusBar,
  StyleSheet,
  View,
} from 'react-native';

import {colores, margen} from '../theme';

type Props = {
  children: ReactNode;
  desplazable?: boolean;
};

export default function Pantalla({children, desplazable = true}: Props) {
  // La altura del teclado llega en coordenadas de pantalla, pero esta vista arranca
  // debajo de la barra de estado porque la Activity le mete ese inset al contenedor;
  // sin compensarlo, el teclado tapa el final del formulario.
  return (
    <KeyboardAvoidingView
      style={estilos.fondo}
      behavior="height"
      keyboardVerticalOffset={StatusBar.currentHeight ?? 0}>
      {desplazable ? (
        <ScrollView
          contentContainerStyle={estilos.contenido}
          keyboardShouldPersistTaps="handled">
          {children}
        </ScrollView>
      ) : (
        <View style={[estilos.contenido, estilos.fondo]}>{children}</View>
      )}
    </KeyboardAvoidingView>
  );
}

const estilos = StyleSheet.create({
  fondo: {
    flex: 1,
    backgroundColor: colores.crema,
  },
  contenido: {
    padding: margen,
    flexGrow: 1,
  },
});
