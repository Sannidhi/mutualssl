package com.example.mutualssl.keystore;

public class EnvKeyStoreFactory implements KeyStoreFactory {
    @Override
    public KeyStoreConfig createKeyStoreConfig() {
        KeyStoreConfig keyStoreConfig = new KeyStoreConfig();
        keyStoreConfig.setKeyStore(System.getenv(LoadKeyStore.KEY_STORE));
        keyStoreConfig.setKeyStorePass(System.getenv(LoadKeyStore.KEY_STORE_PASSWORD));
        keyStoreConfig.setTrustStore(System.getenv(LoadKeyStore.TRUST_STORE));
        keyStoreConfig.setTrustStorePass(System.getenv(LoadKeyStore.TRUST_STORE_PASSWORD));
        return keyStoreConfig;
    }
}
