package encryption;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Encryption class for encrypting packet payload
 *
 * @author Frank
 */
public class Encryption {
    private static final String standardKey = "262b285e295e2d3c";
    protected static final String iv = "65502a5c62515b64";

    /**
     * Encrypt the given byte array
     * @param toEncrypt The array to encrypt
     * @return The encrypted array
     */
    public static byte[] encrypt(byte[] toEncrypt, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey aesKey = new SecretKeySpec(key != null ? key.getBytes() : standardKey.getBytes(), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.ENCRYPT_MODE, aesKey, ivParameterSpec);

            return cipher.doFinal(toEncrypt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Decrypt the given byte array
     * @param toDecrypt The encrypted array
     * @return The decrypted array
     */
    public static byte[] decrypt(byte[] toDecrypt, String key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKey aesKey = new SecretKeySpec(key != null ? key.getBytes() : standardKey.getBytes(), "AES");
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv.getBytes());

            cipher.init(Cipher.DECRYPT_MODE, aesKey, ivParameterSpec);

            return cipher.doFinal(toDecrypt);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Generate a symmetric key
     * @return The generated key
     */
    public static String generateKey() {
        char[] VALID_CHARACTERS = "abcdefghijklmnopqrstuvwxyz0123456879".toCharArray();
        SecureRandom random = new SecureRandom();

        char[] buffer = new char[16];

        for (int i = 0; i < 16; ++i) {
            buffer[i] = VALID_CHARACTERS[random.nextInt(VALID_CHARACTERS.length)];
        }

        return new String(buffer);
    }

    /**
     * Encode the given byte array to base64
     * @param toEncode The byte array to encode
     * @return The base64 encoded string
     */
    public static String base64Encode(byte[] toEncode) {
        BASE64Encoder encoder = new BASE64Encoder();

        return encoder.encode(toEncode);
    }

    /**
     * Decode the given base64 string
     * @param toDecode The string to decode
     * @return The decoded byte array
     */
    public static byte[] base64Decode(String toDecode) {
        BASE64Decoder decoder = new BASE64Decoder();

        try {
            return decoder.decodeBuffer(toDecode);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
