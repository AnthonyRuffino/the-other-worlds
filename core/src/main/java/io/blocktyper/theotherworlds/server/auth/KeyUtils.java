package io.blocktyper.theotherworlds.server.auth;

import io.blocktyper.theotherworlds.plugin.utils.FileUtils;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class KeyUtils {

    private static final String GENERATION_ALGORITHM = "RSA";
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";

    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance(GENERATION_ALGORITHM);
            kpg.initialize(1024);
            return kpg.genKeyPair();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new RuntimeException("unable to create public/private keys: " + ex.getMessage());
        }
    }

    public static PrivateKey decodePrivateKey(byte[] privateKey) {
        try {
            return KeyFactory.getInstance(GENERATION_ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKey));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new RuntimeException("unable decode private key: " + ex.getMessage());
        }
    }

    public static PublicKey decodePublicKey(byte[] publicKey) {
        try {
            return KeyFactory.getInstance(GENERATION_ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKey));
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            throw new RuntimeException("unable decode public key: " + ex.getMessage());
        }
    }

    public static String sign(String plainText, PrivateKey privateKey) {
        try {
            Signature privateSignature = Signature.getInstance(SIGNING_ALGORITHM);
            privateSignature.initSign(privateKey);
            privateSignature.update(plainText.getBytes(UTF_8));

            byte[] signature = privateSignature.sign();

            return Base64.getEncoder().encodeToString(signature);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to sign message " + plainText);
        }
    }

    public static boolean verify(String plainText, String signature, PublicKey publicKey) {
        try {
            Signature publicSignature = Signature.getInstance(SIGNING_ALGORITHM);
            publicSignature.initVerify(publicKey);
            publicSignature.update(plainText.getBytes(UTF_8));

            byte[] signatureBytes = Base64.getDecoder().decode(signature);

            return publicSignature.verify(signatureBytes);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to verify message " + plainText);
        }
    }

    public static KeyPair loadKeyPair(String privateFilePath, String publicFilePath) {

        byte[] privateKeyBytes = FileUtils.getLocalFileBytes(privateFilePath);

        if (privateKeyBytes == null || privateKeyBytes.length == 0) {
            KeyPair keyPair = KeyUtils.generateKeyPair();
            FileUtils.writeFile(privateFilePath, keyPair.getPrivate().getEncoded());
            FileUtils.writeFile(publicFilePath, keyPair.getPublic().getEncoded());
        }

        byte[] publicKeyBytes = FileUtils.getLocalFileBytes(publicFilePath);
        if (publicKeyBytes == null || publicKeyBytes.length == 0) {
            throw new RuntimeException("could not load public key");
        }
        PublicKey publicKey = KeyUtils.decodePublicKey(publicKeyBytes);

        privateKeyBytes = FileUtils.getLocalFileBytes(privateFilePath);
        if (privateKeyBytes == null || privateKeyBytes.length == 0) {
            throw new RuntimeException("could not load private key");
        }
        PrivateKey privateKey = KeyUtils.decodePrivateKey(privateKeyBytes);


        return new KeyPair(publicKey, privateKey);
    }
}
