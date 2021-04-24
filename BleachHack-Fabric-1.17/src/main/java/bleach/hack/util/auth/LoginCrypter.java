/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.Hashing;

import bleach.hack.BleachHack;

public class LoginCrypter {

	private Cipher dcipher;
	private SecretKey key;

	public LoginCrypter(String passPhrase) {
		try {
			byte[] pass = passPhrase.getBytes(StandardCharsets.UTF_8);
			MessageDigest sha = MessageDigest.getInstance("SHA-256");
			pass = sha.digest(pass);
			pass = Arrays.copyOf(pass, 16); // use only first 128 bit
			key = new SecretKeySpec(pass, "AES");
			dcipher = Cipher.getInstance("AES");
		} catch (Exception e) {
			BleachHack.logger.error("Error initing login crypter");
		}
	}

	public static String getPassPhrase() {
		return Hashing.sha256().hashString(new StringBuilder()
				.append(System.getProperty("user.home"))
				.append(System.getProperty("os.name"))
				.append(System.getProperty("os.version"))
				.append(String.valueOf(Runtime.getRuntime().availableProcessors()))
				.append(System.getProperty("os.arch"))
				.append(System.getProperty("user.name"))
				.toString(), StandardCharsets.UTF_8).toString();
	}

	public String encrypt(String data) throws Exception {
		dcipher.init(Cipher.ENCRYPT_MODE, key);
		byte[] utf8EncryptedData = dcipher.doFinal(data.getBytes());
		return new String(Base64.getEncoder().encode(utf8EncryptedData));
	}

	public String decrypt(String base64EncryptedData) throws Exception {
		dcipher.init(Cipher.DECRYPT_MODE, key);
		byte[] decryptedData = Base64.getDecoder().decode(base64EncryptedData);
		return new String(dcipher.doFinal(decryptedData), StandardCharsets.UTF_8);
	}

}
