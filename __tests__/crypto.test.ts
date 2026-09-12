import {base64} from '@scure/base';

import {
  cifrar,
  cifrarConIv,
  descifrar,
  setClaveCanal,
} from '../src/shared/crypto';
import vector from '../test-vectors/aes-gcm.json';
import {claveCanalDePrueba, puente} from './setup/nativeModules';

describe('canal cifrado', () => {
  beforeEach(() => {
    setClaveCanal(claveCanalDePrueba);
  });

  it('cifra y descifra un payload con el IV que da el nativo', async () => {
    const payload = {
      celular: '3001234567',
      monto: 25000,
      descripcion: 'almuerzo',
    };

    const envelope = await cifrar(payload);

    expect(puente.randomBytes).toHaveBeenCalledWith(12);
    expect(Object.keys(JSON.parse(envelope))).toEqual(['iv', 'data']);
    expect(descifrar(envelope)).toEqual(payload);
  });

  it('usa tal cual el IV que devolvió randomBytes', async () => {
    puente.randomBytes.mockResolvedValueOnce('AAAAAAAAAAAAAAAA');

    const envelope = await cifrar({hola: 'mundo'});

    expect(JSON.parse(envelope).iv).toBe('AAAAAAAAAAAAAAAA');
  });

  it('un tag alterado no descifra y el error no trae el payload', () => {
    setClaveCanal(vector.clavePruebaB64);
    const sobre = JSON.parse(vector.envelope);
    const data = base64.decode(sobre.data);
    data[data.length - 1] = (data[data.length - 1] + 1) % 256;
    const alterado = JSON.stringify({iv: sobre.iv, data: base64.encode(data)});

    expect(() => descifrar(alterado)).toThrow(
      'No se pudo descifrar el mensaje',
    );
    expect(() => descifrar(alterado)).not.toThrow(/sessionId|u10001/);
  });

  it('un envelope que no es JSON tampoco pasa', () => {
    expect(() => descifrar('no soy un sobre')).toThrow('No se pudo descifrar');
  });
});

describe('vector compartido con Kotlin', () => {
  beforeEach(() => {
    setClaveCanal(vector.clavePruebaB64);
  });

  it('descifra el envelope del archivo', () => {
    expect(descifrar(vector.envelope)).toEqual(JSON.parse(vector.textoPlano));
  });

  it('con la misma llave y el mismo IV produce el mismo data', () => {
    const envelope = cifrarConIv(
      JSON.parse(vector.textoPlano),
      base64.decode(vector.ivPruebaB64),
    );

    expect(envelope).toBe(vector.envelope);
  });
});

describe('sin TextDecoder, como en Hermes', () => {
  const original = (globalThis as any).TextDecoder;

  beforeAll(() => {
    delete (globalThis as any).TextDecoder;
  });

  afterAll(() => {
    (globalThis as any).TextDecoder = original;
  });

  it('descifra tildes y caracteres de cuatro bytes', async () => {
    setClaveCanal(claveCanalDePrueba);
    const payload = {nombre: 'Alex Suárez', nota: 'ñandú y un símbolo raro: 𝄞'};

    expect(descifrar(await cifrar(payload))).toEqual(payload);
  });

  it('descifra el vector compartido', () => {
    setClaveCanal(vector.clavePruebaB64);

    expect(descifrar(vector.envelope)).toEqual(JSON.parse(vector.textoPlano));
  });
});
