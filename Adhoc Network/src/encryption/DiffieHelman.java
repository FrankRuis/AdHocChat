package encryption;

import java.security.*;

/**
 * Diffie Helman key exchange class
 *
 * @author Frank
 */
public class DiffieHelman {
    protected PrivateKey priKey;
    protected PublicKey pubKey;

    public DiffieHelman() {
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

    public PrivateKey getPrivateKey() {
        return priKey;
    }

    public PublicKey getPublicKey() {
        return pubKey;
    }

    public static void main(String[] args) {
        DiffieHelman diffieHelman = new DiffieHelman();

        System.out.println(diffieHelman.getPublicKey().toString());
    }
}
