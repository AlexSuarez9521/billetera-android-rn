module.exports = {
  preset: 'react-native',
  transformIgnorePatterns: [
    'node_modules/(?!((jest-)?react-native|@react-native(-community)?|@noble|@scure)/)',
  ],
  setupFiles: ['<rootDir>/tests/setup/nativeModules.ts'],
  testPathIgnorePatterns: [
    '<rootDir>/node_modules/',
    '<rootDir>/tests/setup/',
  ],
  // Los cinco segundos de Jest se quedan cortos para las pruebas de pantalla cuando
  // la máquina está ocupada: el primer render arrastra la transpilación de React Native.
  testTimeout: 15000,
};
