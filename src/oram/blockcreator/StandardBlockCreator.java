package oram.blockcreator;

import oram.BlockEncrypted;
import oram.Constants;
import oram.EncryptionStrategyImpl;
import oram.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static oram.Util.writeFile;

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
        for (int i = 0; i < numberOfFiles; i++) {

            BlockEncrypted block = getEncryptedDummy(secretKey, encryptionStrategy);
            byte[] data = block.getData();
            byte[] address = block.getAddress();
            byte[] bytesToWrite = new byte[data.length + address.length];
            System.arraycopy(address, 0, bytesToWrite, 0, address.length);
            System.arraycopy(data, 0, bytesToWrite, address.length, data.length);

            if (!writeFile(bytesToWrite, addresses.get(i))) {
                logger.error("Unable to write file: " + i);
                return false;
            }

            double percent = ((double) (i + 1) / numberOfFiles) * 100;
            if (percent % 1 == 0)
                System.out.println("    Done with " + ((int) percent) + "% of the files");
        }

        return true;
    }

    public boolean createBlocks(int numberOfFiles) {
        File filesDir = new File(Constants.FILES_DIR);
        String[] files = filesDir.list();
        if (files == null) {
            System.out.println("Unable to get list of files");
            return false;
        }

        for (String s : files) {
            File f = new File(Constants.FILES_DIR + s);
            if (!f.delete()) {
                System.out.println("Deleting files went wrong");
                return false;
            }
        }

        List<String> addresses = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++)
            addresses.add(String.valueOf(i));

        return createBlocks(addresses);
    }

    private BlockEncrypted getEncryptedDummy(SecretKey key, EncryptionStrategyImpl encryptionStrategy) {
        byte[] encryptedAddress = encryptionStrategy.encrypt(Util.leIntToByteArray(0), key);
        byte[] encryptedData = encryptionStrategy.encrypt(new byte[Constants.BLOCK_SIZE], key);
        return new BlockEncrypted(encryptedAddress, encryptedData);
    }
}
