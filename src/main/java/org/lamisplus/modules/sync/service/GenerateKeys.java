package org.lamisplus.modules.sync.service;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException;
import org.lamisplus.modules.sync.domain.entity.RemoteAccessToken;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@Component
@RequiredArgsConstructor
public class GenerateKeys {

    private KeyPairGenerator keyGen;
    private PrivateKey privateKey;
    private PublicKey publicKey;
    private static final String PUBLICKEY_PREFIX    = "-----BEGIN PUBLIC KEY-----";
    private static final String PUBLICKEY_POSTFIX   = "-----END PUBLIC KEY-----";
    private static final String PRIVATEKEY_PREFIX   = "-----BEGIN RSA PRIVATE KEY-----";
    private static final String PRIVATEKEY_POSTFIX  = "-----END RSA PRIVATE KEY-----";


    private void generateSecureKeys() throws NoSuchAlgorithmException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        this.keyGen.initialize(2048);
    }

    private void createKeys() {
        KeyPair pair = this.keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    private PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    private PublicKey getPublicKey() {
        return this.publicKey;
    }

    public RemoteAccessToken keyGenerateAndReturnKey(RemoteAccessToken remoteAccessToken) {
        String publicKeyPEM = null;
        String privateKeyPEM;
        System.out.println("main method of generator");
        try {
            this.generateSecureKeys();
            this.createKeys();

            // THIS IS PEM:
            publicKeyPEM = /*PUBLICKEY_PREFIX + "\n" + */DatatypeConverter.printBase64Binary(this.getPublicKey().getEncoded())/*.replaceAll("(.{64})", "$1\n") + "\n" + PUBLICKEY_POSTFIX*/;
            privateKeyPEM = /*PRIVATEKEY_PREFIX + "\n" + */DatatypeConverter.printBase64Binary(this.getPrivateKey().getEncoded())/*.replaceAll("(.{64})", "$1\n") + "\n" + PRIVATEKEY_POSTFIX*/;
            remoteAccessToken.setPrKey(privateKeyPEM);
            remoteAccessToken.setPubKey(publicKeyPEM);

        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return remoteAccessToken;
    }

    public PrivateKey readPrivateKey(RemoteAccessToken remoteAccessToken)
            throws IOException, GeneralSecurityException {
        PrivateKey key;
        String fileString =  remoteAccessToken.getPrKey();
        if(StringUtils.isBlank(fileString)) throw new EntityNotFoundException(RemoteAccessToken.class, "Private key", "Private key");

       /* fileString = fileString.replace("-----BEGIN RSA PRIVATE KEY-----\n", "")
                .replace("-----END RSA PRIVATE KEY-----", "");*/
        byte[] keyBytes = DatatypeConverter.parseBase64Binary(fileString);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        key = kf.generatePrivate(spec);
        return key;
    }

    public PublicKey readPublicKey(RemoteAccessToken remoteAccessToken) throws GeneralSecurityException {
        PublicKey key;
        String fileString =  remoteAccessToken.getPubKey();
        if(StringUtils.isBlank(fileString)) throw new EntityNotFoundException(RemoteAccessToken.class, "Public key", "Public key");

        /*fileString = fileString.replace("-----BEGIN PUBLIC KEY-----\n", "").replace("-----END PUBLIC KEY-----", "");*/
        byte[] keyBytes = DatatypeConverter.parseBase64Binary(fileString);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        key = kf.generatePublic(publicKeySpec);
        return key;
    }

    public byte[] encrypt(byte[] bytes, RemoteAccessToken remoteAccessToken) throws GeneralSecurityException, IOException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, this.readPublicKey(remoteAccessToken));
        byte[] encryptedMessageBytes = encryptCipher.doFinal(bytes);
        return encryptedMessageBytes;
    }

}
