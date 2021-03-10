package bleach.hack.util.auth;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class LoginCrypter {

	private static String passPhrase = null;
	private Cipher dcipher;
	private SecretKey key;

	public LoginCrypter(String passPhrase) throws Exception {
		byte[] pass = passPhrase.getBytes("UTF-8");
		MessageDigest sha = MessageDigest.getInstance("SHA-1");
		pass = sha.digest(pass);
		pass = Arrays.copyOf(pass, 16); // use only first 128 bit
		key = new SecretKeySpec(pass, "AES");
		dcipher = Cipher.getInstance("AES");
	}

	public static String getPassPhrase() {
		if (passPhrase == null) {
			passPhrase = new StringBuilder()
					.append(System.getProperty("user.dir"))
					.append(System.getProperty("os.name"))
					.append(System.getProperty("os.version"))
					.append(String.valueOf(Runtime.getRuntime().availableProcessors()))
					.append(System.getProperty("os.arch"))
					.append(System.getProperty("user.name"))
					.toString();
		}

		return passPhrase;
	}

	public String encrypt(String data) throws Exception {
		dcipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] utf8EncryptedData = dcipher.doFinal(data.getBytes());
		return new String(Base64.getEncoder().encode(utf8EncryptedData));
	}

	public String decrypt(String base64EncryptedData) throws Exception {
		dcipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedData = Base64.getDecoder().decode(base64EncryptedData);
		return new String(dcipher.doFinal(decryptedData), "UTF8");
	}

}
