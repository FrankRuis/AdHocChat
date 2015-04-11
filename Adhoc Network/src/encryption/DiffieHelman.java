package encryption;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * Diffie Helman class for key exchanges
 *
 * @author Frank
 */
public class DiffieHelman {
    protected PrivateKey priKey;
    protected PublicKey pubKey;

    protected String symmetricKey;

    private boolean exchangeSuccesful;

    /**
     * Constructor, generates a public and a private key
     */
    public DiffieHelman() {
        exchangeSuccesful = false;

        try {
            KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
            keyGenerator.initialize(1024);

            KeyPair kPair = keyGenerator.genKeyPair();

            priKey = kPair.getPrivate();
            pubKey = kPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    /**
     * Encrypt the given byte array
     * @param toEncrypt The byte array to encrypt
     * @param key The public key
     * @return The encrypted byte array
     */
    public static byte[] encrypt(byte[] toEncrypt, PublicKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            return cipher.doFinal(toEncrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Decrypt the given byte array
     * @param toDecrypt The byte array to decrypt
     * @param key The private key
     * @return The decrypted byte array
     */
    public static byte[] decrypt(byte[] toDecrypt, PrivateKey key) {
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);

            return cipher.doFinal(toDecrypt);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @return The generated private key
     */
    public PrivateKey getPrivateKey() {
        return priKey;
    }

    /**
     * Convert the public key to a base64 string
     * @return The public key as string
     */
    public String publicKeyToString() {
        try {
            KeyFactory fact = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec spec = fact.getKeySpec(pubKey, X509EncodedKeySpec.class);
            return Encryption.base64Encode(spec.getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Convert a base64 string to a public key
     * @param key The base64 string
     * @return The public key
     */
    public static PublicKey stringToPublicKey(String key) {
        try {
            byte[] data = Encryption.base64Decode(key);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(data);
            KeyFactory fact = KeyFactory.getInstance("RSA");

            return fact.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @return The generated public key
     */
    public PublicKey getPublicKey() {
        return pubKey;
    }

    /**
     * Set the symmetric key
     * @param key The key to set
     */
    public void setSymmetricKey(String key) {
        symmetricKey = key;
    }

    /**
     * @return The symmetric key
     */
    public String getSymmetricKey() {
        return symmetricKey;
    }

    /**
     * Set the exchange status
     * @param exchangeSuccesful The boolean to set
     */
    public void setExchangeSuccesful(boolean exchangeSuccesful) {
        this.exchangeSuccesful = exchangeSuccesful;
    }

    /**
     * @return Whether or not the exchange was completed successfully
     */
    public boolean isExchangeSuccesful() {
        return exchangeSuccesful;
    }
}
