package bleach.hack.utils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class Decrypter {
    private static String passPhrase = null;
    private final Cipher dcipher;
    private final SecretKey key;

    public Decrypter(String passPhrase) throws Exception {
        byte[] pass = passPhrase.getBytes(StandardCharsets.UTF_8);
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        pass = sha.digest(pass);
        pass = Arrays.copyOf(pass, 16); // use only first 128 bit
        key = new SecretKeySpec(pass, "AES");
        dcipher = Cipher.getInstance("AES");
    }

    public static String getPassPhrase() {
        if (passPhrase == null) {
            String pass = "";
            pass += System.getProperty("user.dir");
            pass += System.getProperty("os.name");
            pass += System.getProperty("os.version");
            pass += String.valueOf(Runtime.getRuntime().availableProcessors());
            pass += System.getProperty("os.arch");
            pass += System.getProperty("user.name");
            passPhrase = pass;
        }
        return passPhrase;
    }

    public String encrypt(String data) throws Exception {
        dcipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] utf8EncryptedData = dcipher.doFinal(data.getBytes());
        String base64EncryptedData = new String(Base64.getEncoder().encode(utf8EncryptedData));
        return base64EncryptedData;
    }

    public String decrypt(String base64EncryptedData) throws Exception {
        dcipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedData = Base64.getDecoder().decode(base64EncryptedData);
        byte[] utf8 = dcipher.doFinal(decryptedData);
        return new String(utf8, StandardCharsets.UTF_8);
    }

}
