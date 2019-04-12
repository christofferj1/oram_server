package oram.blockcreator;

import oram.BlockEncrypted;
import oram.Constants;
import oram.EncryptionStrategyImpl;
import oram.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.util.List;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 04-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class StandardBlockCreator implements BlockCreator {
    private static final Logger logger = LogManager.getLogger("log");

    @Override
    public boolean createBlocks(List<String> addresses) {
        EncryptionStrategyImpl encryptionStrategy = new EncryptionStrategyImpl();
        SecretKey secretKey = encryptionStrategy.generateSecretKey(Constants.KEY_BYTES);
        int numberOfFiles = addresses.size();
        Util.logAndPrint(logger, "Overwriting " + numberOfFiles + " Standard files, from: " + addresses.get(0) + ", to: " + addresses.get(addresses.size() - 1));
        for (int i = 0; i < numberOfFiles; i++) {

            BlockEncrypted block = getEncryptedDummy(secretKey, encryptionStrategy);
            byte[] data = block.getData();
            byte[] address = block.getAddress();
            byte[] bytesToWrite = new byte[data.length + address.length];
            System.arraycopy(address, 0, bytesToWrite, 0, address.length);
            System.arraycopy(data, 0, bytesToWrite, address.length, data.length);

            if (!Util.writeFile(bytesToWrite, addresses.get(i))) {
                logger.error("Unable to write file: " + i);
                return false;
            }

            double percent = ((double) (i + 1) / numberOfFiles) * 100;
            if (percent % 1 == 0)
                Util.logAndPrint(logger, "    Done with " + ((int) percent) + "% of the files");
        }

        return true;
    }

    private BlockEncrypted getEncryptedDummy(SecretKey key, EncryptionStrategyImpl encryptionStrategy) {
        byte[] encryptedAddress = encryptionStrategy.encrypt(Util.leIntToByteArray(0), key);
        byte[] encryptedData = encryptionStrategy.encrypt(new byte[Constants.BLOCK_SIZE], key);
        return new BlockEncrypted(encryptedAddress, encryptedData);
    }
}
