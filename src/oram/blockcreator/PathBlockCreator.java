package oram.blockcreator;

import oram.BlockEncrypted;
import oram.Constants;
import oram.EncryptionStrategyImpl;
import oram.Util;
import oram.block.BlockPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 09-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class PathBlockCreator implements BlockCreator {
    private static final Logger logger = LogManager.getLogger("log");

    @Override
    public boolean createBlocks(List<String> addresses) {
        if (addresses == null) {
            Util.logAndPrint(logger, "Addresses were null");
            return false;
        }
        if (addresses.isEmpty()) {
            Util.logAndPrint(logger, "Addresses were empty");
            return true;
        }

        Util.logAndPrint(logger, " --- Overwriting blocks in total: " + addresses.size());
        List<List<String>> addressLists = Util.getListsOfAddresses(addresses);

        for (int j = 0; j < addressLists.size(); j++) {
            List<String> currentAddresses = addressLists.get(j);
            List<BlockPath> blocks = new ArrayList<>();
            int numberOfFiles = currentAddresses.size();
            Util.logAndPrint(logger, "Overwriting " + numberOfFiles + " Path files, from: " + currentAddresses.get(0) + ", to: " + currentAddresses.get(currentAddresses.size() - 1) + ", part " + (j + 1) + "/" + addressLists.size());
            for (String ignored : currentAddresses)
                blocks.add(getPathDummyBlock());

            Util.logAndPrint(logger, "    " + numberOfFiles + " dummy blocks created");

            EncryptionStrategyImpl encryptionStrategy = new EncryptionStrategyImpl();
            List<BlockEncrypted> encryptedList = encryptBlocks(blocks, encryptionStrategy,
                    encryptionStrategy.generateSecretKey(Constants.KEY_BYTES));

            if (encryptedList.isEmpty()) return false;

            Util.logAndPrint(logger, "    Data encrypted");

            for (int i = 0; i < numberOfFiles; i++) {
                byte[] data = encryptedList.get(i).getData();
                byte[] address = encryptedList.get(i).getAddress();
                byte[] bytesToWrite = new byte[data.length + address.length];
                System.arraycopy(address, 0, bytesToWrite, 0, address.length);
                System.arraycopy(data, 0, bytesToWrite, address.length, data.length);

                if (Util.writeFileFailed(bytesToWrite, currentAddresses.get(i))) {
                    logger.error("Unable to write file: " + i);
                    return false;
                }

                double percent = ((double) (i + 1) / numberOfFiles) * 100;
                if (percent % 10 == 0)
                    Util.logAndPrint(logger, "    Done with " + ((int) percent) + "% of the files of size: "
                            + Constants.BLOCK_SIZE);
            }
        }

        return true;

    }

    private List<BlockEncrypted> encryptBlocks(List<BlockPath> blockPaths, EncryptionStrategyImpl encryptionStrategy,
                                               SecretKey secretKey) {
        List<BlockEncrypted> res = new ArrayList<>();
        for (BlockPath block : blockPaths) {
            if (block == null) {
                res.add(null);
                continue;
            }

            byte[] addressCipher = encryptionStrategy.encrypt(Util.leIntToByteArray(block.getAddress()), secretKey);
            byte[] indexCipher = encryptionStrategy.encrypt(Util.leIntToByteArray(block.getIndex()), secretKey);
            byte[] dataCipher = encryptionStrategy.encrypt(block.getData(), secretKey);
            if (addressCipher == null || indexCipher == null || dataCipher == null) {
                logger.error("Unable to encrypt address: " + block.getAddress() + " or data");
                return new ArrayList<>();
            }

            byte[] encryptedDataPlus = new byte[indexCipher.length + dataCipher.length];
            System.arraycopy(dataCipher, 0, encryptedDataPlus, 0, dataCipher.length);
            System.arraycopy(indexCipher, 0, encryptedDataPlus, dataCipher.length, indexCipher.length);

            res.add(new BlockEncrypted(addressCipher, encryptedDataPlus));
        }
        return res;
    }

    private BlockPath getPathDummyBlock() {
        return new BlockPath(0, new byte[Constants.BLOCK_SIZE], 0);
    }
}
