import React, {useEffect, useRef, useState} from 'react';
import {BackHandler, StyleSheet, Text, View} from 'react-native';

import Aviso from '../../shared/components/Aviso';
import Boton from '../../shared/components/Boton';
import Campo from '../../shared/components/Campo';
import Pantalla from '../../shared/components/Pantalla';
import Tarjeta from '../../shared/components/Tarjeta';
import {formatoCOP, formatoFecha} from '../../shared/formato';
import {colores, espacio, radio, texto} from '../../shared/theme';
import type {PropsIniciales} from '../../shared/tipos';
import {parseMonto} from '../../shared/validaciones';
import HojaConfirmacion from './HojaConfirmacion';
import {useTransferencia} from './useTransferencia';
import {init, on} from '../../shared/bridge';

export default function TransferenciaBundle(props: PropsIniciales) {
  const arrancado = useRef(false);
  if (!arrancado.current) {
    init(props);
    arrancado.current = true;
  }

  const envio = useTransferencia();
  const [sesionExpiro, setSesionExpiro] = useState(false);

  useEffect(() => on('SESSION_EXPIRED', () => setSesionExpiro(true)), []);

  const {resultado} = envio;
  if (resultado) {
    return (
      <Pantalla desplazable={false}>
        {sesionExpiro ? (
          <Aviso tipo="alerta">Tu sesión expiró, ingresa de nuevo</Aviso>
        ) : null}
        <View style={estilos.resultado}>
          <View style={estilos.circulo}>
            <Text style={estilos.check}>✓</Text>
          </View>
          <Text style={estilos.listo}>Listo</Text>
          <Text style={estilos.enviado}>
            {formatoCOP(parseMonto(envio.monto))}
          </Text>
          <Text style={estilos.para}>para {resultado.destinoNombre}</Text>
          <Text style={estilos.cuando}>{formatoFecha(resultado.fecha)}</Text>
          <Tarjeta style={estilos.saldo}>
            <Text style={estilos.etiqueta}>Nuevo saldo</Text>
            <Text style={estilos.saldoValor}>
              {formatoCOP(resultado.nuevoSaldo)}
            </Text>
          </Tarjeta>
        </View>
        <Boton
          titulo="Volver al inicio"
          onPress={() => BackHandler.exitApp()}
        />
      </Pantalla>
    );
  }

  return (
    <Pantalla>
      {sesionExpiro ? (
        <Aviso tipo="alerta">Tu sesión expiró, ingresa de nuevo</Aviso>
      ) : null}

      <Text style={estilos.paso}>
        {envio.paso === 'destino' ? 'Paso 1 de 2' : 'Paso 2 de 2'}
      </Text>

      {envio.paso === 'destino' ? (
        <>
          <Text style={estilos.titulo}>¿A quién le envías?</Text>
          <Campo
            etiqueta="Celular"
            valor={envio.celular}
            alCambiar={envio.cambiarCelular}
            keyboardType="number-pad"
            maxLength={10}
            placeholder="3001234567"
            autoFocus
          />
          {envio.error ? <Aviso tipo="error">{envio.error}</Aviso> : null}
          <Boton
            titulo="Continuar"
            onPress={envio.buscar}
            cargando={envio.ocupado}
          />
        </>
      ) : (
        <>
          <Text style={estilos.titulo}>¿Cuánto le envías?</Text>
          <Tarjeta style={estilos.destino}>
            <View>
              <Text style={estilos.destinoNombre}>{envio.destino}</Text>
              <Text style={estilos.etiqueta}>{envio.celular}</Text>
            </View>
            <Boton
              titulo="Cambiar"
              variante="texto"
              onPress={envio.cambiarDestino}
            />
          </Tarjeta>
          <Campo
            etiqueta="Monto en pesos"
            valor={envio.monto}
            alCambiar={envio.cambiarMonto}
            keyboardType="number-pad"
            placeholder="0"
            autoFocus
          />
          <Campo
            etiqueta="Descripción (opcional)"
            valor={envio.descripcion}
            alCambiar={envio.cambiarDescripcion}
            maxLength={40}
            placeholder="Almuerzo"
          />
          {envio.error ? <Aviso tipo="error">{envio.error}</Aviso> : null}
          <Boton titulo="Continuar" onPress={envio.revisar} />
          <HojaConfirmacion
            visible={envio.confirmando}
            destino={envio.destino}
            celular={envio.celular}
            monto={parseMonto(envio.monto)}
            descripcion={envio.descripcion.trim()}
            enviando={envio.ocupado}
            alConfirmar={envio.confirmar}
            alCerrar={envio.cancelar}
          />
        </>
      )}
    </Pantalla>
  );
}

const estilos = StyleSheet.create({
  paso: {
    fontSize: texto.menor,
    color: colores.gris,
    letterSpacing: 0.6,
    marginBottom: espacio.xs,
  },
  titulo: {
    fontSize: texto.titulo,
    fontWeight: '700',
    color: colores.tinta,
    marginBottom: espacio.l,
  },
  destino: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: espacio.m,
  },
  destinoNombre: {
    fontSize: texto.medio,
    fontWeight: '600',
    color: colores.tinta,
  },
  etiqueta: {
    fontSize: texto.menor,
    color: colores.gris,
  },
  resultado: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  circulo: {
    width: 72,
    height: 72,
    borderRadius: 36,
    backgroundColor: colores.verdeSuave,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: espacio.l,
  },
  check: {
    fontSize: 34,
    lineHeight: 40,
    color: colores.verde,
  },
  listo: {
    fontSize: texto.titulo,
    fontWeight: '700',
    color: colores.tinta,
  },
  enviado: {
    fontSize: texto.saldo,
    fontWeight: '700',
    color: colores.tinta,
    marginTop: espacio.s,
  },
  para: {
    fontSize: texto.medio,
    color: colores.gris,
    marginTop: espacio.xs,
  },
  cuando: {
    fontSize: texto.menor,
    color: colores.gris,
    marginTop: espacio.s,
  },
  saldo: {
    alignSelf: 'stretch',
    marginTop: espacio.xl,
    borderRadius: radio.tarjeta,
  },
  saldoValor: {
    fontSize: texto.medio,
    fontWeight: '600',
    color: colores.verdeOscuro,
    marginTop: 2,
  },
});
