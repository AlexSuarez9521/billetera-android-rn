import React from 'react';
import {Modal, Pressable, StyleSheet, Text, View} from 'react-native';

import Boton from '../../shared/components/Boton';
import {formatoCOP} from '../../shared/formato';
import {colores, espacio, margen, radio, texto} from '../../shared/theme';

const estilos = StyleSheet.create({
  fondo: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  telon: {
    ...StyleSheet.absoluteFillObject,
    backgroundColor: 'rgba(31, 29, 26, 0.4)',
  },
  hoja: {
    backgroundColor: colores.crema,
    borderTopLeftRadius: radio.tarjeta + 6,
    borderTopRightRadius: radio.tarjeta + 6,
    padding: margen,
    // El Modal se dibuja en su propia ventana y no hereda el inset que la Activity
    // le pone al contenedor, así que "Cancelar" tiene que librar la barra de tres
    // botones por su cuenta.
    paddingBottom: espacio.l + 32,
  },
  agarre: {
    width: 40,
    height: 4,
    borderRadius: 2,
    backgroundColor: colores.linea,
    alignSelf: 'center',
    marginBottom: espacio.m,
  },
  titulo: {
    fontSize: texto.titulo,
    fontWeight: '700',
    color: colores.tinta,
    marginBottom: espacio.m,
  },
  fila: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-start',
    paddingVertical: espacio.s,
  },
  etiqueta: {
    fontSize: texto.normal,
    color: colores.gris,
  },
  valor: {
    fontSize: texto.normal,
    color: colores.tinta,
    flexShrink: 1,
    textAlign: 'right',
  },
  total: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: colores.linea,
    marginTop: espacio.s,
    paddingTop: espacio.m,
    marginBottom: espacio.l,
  },
  totalValor: {
    fontSize: texto.titulo,
    fontWeight: '700',
    color: colores.tinta,
  },
});

type Props = {
  visible: boolean;
  destino: string;
  celular: string;
  monto: number;
  descripcion: string;
  enviando: boolean;
  alConfirmar: () => void;
  alCerrar: () => void;
};

export default function HojaConfirmacion({
  visible,
  destino,
  celular,
  monto,
  descripcion,
  enviando,
  alConfirmar,
  alCerrar,
}: Props) {
  return (
    <Modal
      visible={visible}
      transparent
      animationType="slide"
      onRequestClose={alCerrar}>
      <View style={estilos.fondo}>
        <Pressable
          style={estilos.telon}
          onPress={enviando ? undefined : alCerrar}
        />
        <View style={estilos.hoja}>
          <View style={estilos.agarre} />
          <Text style={estilos.titulo}>Revisa antes de enviar</Text>
          <Dato etiqueta="Para" valor={destino} />
          <Dato etiqueta="Celular" valor={celular} />
          {descripcion ? (
            <Dato etiqueta="Descripción" valor={descripcion} />
          ) : null}
          <View style={estilos.total}>
            <Text style={estilos.etiqueta}>Monto</Text>
            <Text style={estilos.totalValor}>{formatoCOP(monto)}</Text>
          </View>
          <Boton titulo="Confirmar" onPress={alConfirmar} cargando={enviando} />
          <Boton
            titulo="Cancelar"
            variante="texto"
            onPress={alCerrar}
            deshabilitado={enviando}
          />
        </View>
      </View>
    </Modal>
  );
}

function Dato({etiqueta, valor}: {etiqueta: string; valor: string}) {
  return (
    <View style={estilos.fila}>
      <Text style={estilos.etiqueta}>{etiqueta}</Text>
      <Text style={estilos.valor}>{valor}</Text>
    </View>
  );
}
