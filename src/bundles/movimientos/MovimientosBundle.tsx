import React, {useEffect, useMemo, useRef, useState} from 'react';
import {
  ActivityIndicator,
  SectionList,
  StyleSheet,
  Text,
  View,
} from 'react-native';

import * as puente from '../../shared/bridge';
import Aviso from '../../shared/components/Aviso';
import {formatoCOP} from '../../shared/formato';
import {colores, espacio, margen, radio, texto} from '../../shared/theme';
import type {Movimiento, PropsIniciales} from '../../shared/tipos';
import {agruparPorDia} from './agruparPorDia';

export default function MovimientosBundle(props: PropsIniciales) {
  const [lista, setLista] = useState<Movimiento[]>([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [expirada, setExpirada] = useState(false);
  const arrancado = useRef(false);

  if (!arrancado.current) {
    puente.init(props);
    arrancado.current = true;
  }

  useEffect(() => {
    let montado = true;

    async function traerMovimientos() {
      try {
        // TODO: paginar si el mock pasa de unos 200 movimientos
        const movs = await puente.movimientos();
        if (montado) {
          setLista(movs);
        }
      } catch (e) {
        const codigo =
          e instanceof puente.BridgeError ? e.codigo : 'ERROR_INTERNO';
        if (montado) {
          setError(puente.mensajeDe(codigo));
        }
      } finally {
        if (montado) {
          setCargando(false);
        }
      }
    }

    traerMovimientos();
    const dejarDeEscuchar = puente.on('SESSION_EXPIRED', () =>
      setExpirada(true),
    );

    return () => {
      montado = false;
      dejarDeEscuchar();
    };
  }, []);

  const secciones = useMemo(() => agruparPorDia(lista), [lista]);

  let cuerpo;
  if (expirada) {
    // Con la sesión caída no vale la pena pintar la lista: Android manda a Login
    // dos segundos después y el aviso de arriba ya dice lo que pasó.
    cuerpo = null;
  } else if (cargando) {
    cuerpo = <ActivityIndicator color={colores.verde} style={estilos.espera} />;
  } else if (error) {
    cuerpo = (
      <View style={estilos.aLosLados}>
        <Aviso tipo="error">{error}</Aviso>
      </View>
    );
  } else {
    cuerpo = (
      <SectionList
        sections={secciones}
        keyExtractor={mov => mov.id}
        stickySectionHeadersEnabled
        renderSectionHeader={({section}) => (
          <Text style={estilos.dia}>{section.title}</Text>
        )}
        renderItem={({item}) => <Fila mov={item} />}
        ListEmptyComponent={
          <Text style={estilos.vacio}>Aún no tienes movimientos</Text>
        }
        contentContainerStyle={estilos.contenido}
      />
    );
  }

  return (
    <View style={estilos.pantalla}>
      <Text style={estilos.titulo}>Movimientos</Text>
      {expirada ? (
        <View style={estilos.aLosLados}>
          <Aviso tipo="alerta">Tu sesión expiró, ingresa de nuevo</Aviso>
        </View>
      ) : null}
      {cuerpo}
    </View>
  );
}

function Fila({mov}: {mov: Movimiento}) {
  const esCredito = mov.tipo === 'CREDITO';
  const rechazada = mov.estado === 'RECHAZADA';
  const valor = `${esCredito ? '+' : '-'}${formatoCOP(mov.valor)}`;

  return (
    <View style={estilos.fila}>
      <View style={estilos.detalle}>
        <Text style={estilos.descripcion} numberOfLines={2}>
          {mov.descripcion}
        </Text>
        <Text style={estilos.hora}>
          {horaDe(mov.fecha)} · {esCredito ? 'Crédito' : 'Débito'}
        </Text>
      </View>
      <View style={estilos.montos}>
        <Text style={[estilos.valor, esCredito && estilos.valorCredito]}>
          {valor}
        </Text>
        {rechazada ? (
          <Text style={estilos.chip}>Rechazada</Text>
        ) : (
          <Text style={estilos.exitosa}>Exitosa</Text>
        )}
      </View>
    </View>
  );
}

// La fecha viene en hora local y sin zona, así que la hora sale del propio texto.
function horaDe(iso: string): string {
  return iso.slice(11, 16);
}

const estilos = StyleSheet.create({
  pantalla: {
    flex: 1,
    backgroundColor: colores.crema,
  },
  titulo: {
    fontSize: texto.titulo,
    fontWeight: '700',
    color: colores.tinta,
    paddingHorizontal: margen,
    paddingTop: margen,
    paddingBottom: espacio.m,
  },
  aLosLados: {
    paddingHorizontal: margen,
  },
  espera: {
    marginTop: espacio.xl,
  },
  contenido: {
    paddingHorizontal: margen,
    paddingBottom: espacio.xl,
    flexGrow: 1,
  },
  // La cabecera se queda pegada arriba, así que necesita fondo propio o las filas
  // se ven pasar por debajo.
  dia: {
    fontSize: texto.menor,
    color: colores.gris,
    backgroundColor: colores.crema,
    paddingTop: espacio.m,
    paddingBottom: espacio.s,
    letterSpacing: 0.4,
  },
  fila: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    backgroundColor: colores.blanco,
    borderWidth: 1,
    borderColor: colores.linea,
    borderRadius: radio.tarjeta,
    padding: espacio.m,
    marginBottom: espacio.s,
  },
  detalle: {
    flex: 1,
    paddingRight: espacio.m,
  },
  descripcion: {
    fontSize: texto.normal,
    color: colores.tinta,
  },
  hora: {
    fontSize: texto.menor,
    color: colores.gris,
    marginTop: 2,
  },
  montos: {
    alignItems: 'flex-end',
  },
  valor: {
    fontSize: texto.medio,
    fontWeight: '600',
    color: colores.tinta,
  },
  valorCredito: {
    color: colores.verde,
  },
  exitosa: {
    marginTop: espacio.xs,
    fontSize: texto.menor,
    color: colores.gris,
  },
  chip: {
    marginTop: espacio.xs,
    paddingHorizontal: espacio.s,
    paddingVertical: 2,
    borderRadius: 6,
    overflow: 'hidden',
    backgroundColor: colores.rojoSuave,
    color: colores.rojo,
    fontSize: texto.menor,
  },
  vacio: {
    fontSize: texto.normal,
    color: colores.gris,
    textAlign: 'center',
    marginTop: espacio.xl,
  },
});
