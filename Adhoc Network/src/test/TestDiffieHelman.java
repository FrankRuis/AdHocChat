package test;

import encryption.DiffieHelman;
import encryption.Encryption;

/**
 * @author Frank
 */
public class TestDiffieHelman {
    public static void main(String[] args) {
        DiffieHelman diffieHelman = new DiffieHelman(true);
        String pubKey = diffieHelman.publicKeyToString();
        String generatedKey = Encryption.generateKey();
        String encryptedSymKey = Encryption.base64Encode(DiffieHelman.encrypt(generatedKey.getBytes(), DiffieHelman.stringToPublicKey(pubKey)));
        String decryptedSymKey = new String(DiffieHelman.decrypt(Encryption.base64Decode(encryptedSymKey), diffieHelman.getPrivateKey()));
        System.out.println(decryptedSymKey + " " + generatedKey);
    }
}
