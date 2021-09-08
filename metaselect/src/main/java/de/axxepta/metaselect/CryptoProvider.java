package de.axxepta.metaselect;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;

class CryptoProvider {
    private byte[] pwd;

    private Cipher encCipher = null;
    private Cipher decCipher = null;

    CryptoProvider(String pwd) {
        setPwd(pwd);
    }

    void setPwd(String pwd) {
        try {
            byte[] key = (pwd).getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            this.pwd = key;
        } catch (Exception ex) {
            ex.printStackTrace();
            this.pwd = new byte[16];
        }
    }

    private Cipher getEncryptCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        if (encCipher == null) {
            encCipher = getCipher(Cipher.ENCRYPT_MODE);
        }
        return encCipher;
    }

    private Cipher getDecryptCipher() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        if (decCipher == null) {
            decCipher = getCipher(Cipher.DECRYPT_MODE);
        }
        return decCipher;
    }

    private Cipher getCipher(int mode) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        Key k = new SecretKeySpec(pwd, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(pwd);
        cipher.init(mode, k, ivParameterSpec);
        return cipher;
    }

    String encrypt(String plainText) {
        try {
            Cipher cipher = getEncryptCipher();
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            Base64.Encoder encoder = Base64.getEncoder();
            return encoder.encodeToString(cipherText);
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }

    String decrypt(String encrypted) {
        try {
            Cipher cipher = getDecryptCipher();
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] cipherText = decoder.decode(encrypted.getBytes(StandardCharsets.UTF_8));
            return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
}
