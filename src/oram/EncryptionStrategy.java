package oram;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 12-03-2019. <br>
 * Master Thesis 2019 </p>
 */

public class EncryptionStrategy {
    private final Logger logger = LogManager.getLogger("log");

    public SecretKey generateSecretKey(byte[] randomBytes) {
        SecretKey res;
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            randomBytes = sha.digest(randomBytes);
            randomBytes = Arrays.copyOf(randomBytes, Constants.AES_KEY_SIZE);
            res = new SecretKeySpec(randomBytes, "AES");
        } catch (NoSuchAlgorithmException e) {
            logger.error("Error happened generating a key");
            logger.error(e);
            logger.debug("Stacktrace", e);
            return null;
        }
        return res;
    }

    public byte[] encrypt(byte[] message, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = Util.getRandomByteArray(Constants.AES_BLOCK_SIZE);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] cipherText = cipher.doFinal(message);
            byte[] addAll = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, addAll, 0, iv.length);
            System.arraycopy(cipherText, 0, addAll, iv.length, cipherText.length);
            return addAll;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException | InvalidAlgorithmParameterException e) {
            logger.error("Error happened while encrypting");
            logger.error(e);
            logger.debug("Stacktrace", e);
        }
        return null;
    }
}
