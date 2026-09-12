import React, {useEffect, useRef, useState} from 'react';
import {
  ActivityIndicator,
  Modal,
  Pressable,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import Pantalla from '../../shared/components/Pantalla';
import Tarjeta from '../../shared/components/Tarjeta';
import Boton from '../../shared/components/Boton';
import {init, on, send} from '../../shared/bridge';
import {formatoCOP} from '../../shared/formato';
import {colores, espacio, margen, radio, texto} from '../../shared/theme';
import type {DatosHome, PropsIniciales} from '../../shared/tipos';

export default function HomeBundle(props: PropsIniciales) {
  // La llave del canal tiene que estar puesta antes de que el efecto mande HOME_READY.
  const arrancado = useRef(false);
  if (!arrancado.current) {
    init(props);
    arrancado.current = true;
  }

  const [datos, setDatos] = useState<DatosHome | null>(null);
  const [expirada, setExpirada] = useState(false);
  // Abrir otra pantalla se toma su rato y esta sigue recibiendo toques; el botón
  // se queda inactivo hasta que Android reenvía LOAD_HOME al volver.
  const [saliendo, setSaliendo] = useState(false);

  useEffect(() => {
    // Android reenvía LOAD_HOME en cada onResume, así que al volver de una
    // transferencia el saldo se refresca sin pedirlo.
    const bajas = [
      on<DatosHome>('LOAD_HOME', llegada => {
        setDatos(llegada);
        setSaliendo(false);
      }),
      on('SESSION_EXPIRED', () => setExpirada(true)),
    ];
    send('HOME_READY');
    return () => bajas.forEach(baja => baja());
  }, []);

  function salir(evento: string) {
    if (saliendo) {
      return;
    }
    setSaliendo(true);
    send(evento);
  }

  if (!datos) {
    return (
      <Pantalla desplazable={false}>
        <View style={estilos.espera}>
          <ActivityIndicator color={colores.verde} />
          <Text style={estilos.cargando}>Cargando</Text>
        </View>
      </Pantalla>
    );
  }

  return (
    <>
      <Pantalla>
        <Text style={estilos.saludo}>Hola, {datos.nombre.split(' ')[0]}</Text>
        <Text style={estilos.celular}>{datos.celular}</Text>

        <Tarjeta style={estilos.saldo}>
          <Text style={estilos.disponible}>Disponible</Text>
          <Text style={estilos.monto}>{formatoCOP(datos.saldo)}</Text>
        </Tarjeta>

        <View style={estilos.acciones}>
          <Fila
            titulo="Transferir"
            deshabilitado={saliendo}
            onPress={() => salir('OPEN_TRANSFER')}
          />
          <View style={estilos.separador} />
          <Fila
            titulo="Movimientos"
            deshabilitado={saliendo}
            onPress={() => salir('OPEN_MOVEMENTS')}
          />
        </View>

        <Boton
          titulo="Cerrar sesión"
          variante="texto"
          style={estilos.salir}
          deshabilitado={saliendo}
          onPress={() => salir('LOGOUT')}
        />
      </Pantalla>

      {/* El back no lo cierra: Android lleva a Login dos segundos después. */}
      <Modal
        visible={expirada}
        transparent
        animationType="fade"
        onRequestClose={() => {}}>
        <View style={estilos.velo}>
          <Tarjeta>
            <Text style={estilos.avisoTitulo}>Tu sesión expiró</Text>
            <Text style={estilos.avisoTexto}>
              En un momento te llevamos al ingreso.
            </Text>
          </Tarjeta>
        </View>
      </Modal>
    </>
  );
}

function Fila({
  titulo,
  deshabilitado,
  onPress,
}: {
  titulo: string;
  deshabilitado: boolean;
  onPress: () => void;
}) {
  return (
    <Pressable
      accessibilityRole="button"
      disabled={deshabilitado}
      onPress={onPress}
      style={({pressed}) => [
        estilos.fila,
        pressed ? {backgroundColor: colores.verdeSuave} : null,
        deshabilitado ? estilos.filaInactiva : null,
      ]}>
      <Text style={estilos.filaTitulo}>{titulo}</Text>
      <Text style={estilos.chevron}>›</Text>
    </Pressable>
  );
}

const estilos = StyleSheet.create({
  espera: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  cargando: {
    marginTop: espacio.s,
    fontSize: texto.normal,
    color: colores.gris,
  },
  saludo: {
    fontSize: texto.titulo,
    fontWeight: '700',
    color: colores.tinta,
  },
  celular: {
    fontSize: texto.normal,
    color: colores.gris,
    marginTop: 2,
  },
  saldo: {
    backgroundColor: colores.verde,
    borderColor: colores.verde,
    paddingVertical: espacio.l,
    marginTop: espacio.l,
  },
  disponible: {
    fontSize: texto.menor,
    color: colores.verdeSuave,
    letterSpacing: 0.4,
  },
  monto: {
    fontSize: texto.saldo,
    fontWeight: '700',
    color: colores.blanco,
    marginTop: espacio.xs,
  },
  acciones: {
    marginTop: espacio.l,
    backgroundColor: colores.blanco,
    borderWidth: 1,
    borderColor: colores.linea,
    borderRadius: radio.tarjeta,
    // Sin esto el fondo del Pressable se sale por las esquinas redondeadas.
    overflow: 'hidden',
  },
  fila: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingHorizontal: espacio.m,
    paddingVertical: 18,
  },
  filaInactiva: {
    opacity: 0.5,
  },
  filaTitulo: {
    fontSize: texto.medio,
    color: colores.tinta,
  },
  chevron: {
    fontSize: texto.titulo,
    color: colores.gris,
  },
  separador: {
    height: 1,
    backgroundColor: colores.linea,
    marginLeft: espacio.m,
  },
  salir: {
    marginTop: espacio.l,
    alignSelf: 'flex-start',
  },
  velo: {
    flex: 1,
    justifyContent: 'center',
    padding: margen,
    backgroundColor: 'rgba(31, 29, 26, 0.45)',
  },
  avisoTitulo: {
    fontSize: texto.medio,
    fontWeight: '600',
    color: colores.tinta,
  },
  avisoTexto: {
    fontSize: texto.normal,
    color: colores.gris,
    marginTop: espacio.s,
    lineHeight: 21,
  },
});
