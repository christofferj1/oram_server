package oram.blockcreator;

import oram.BlockEncrypted;
import oram.Constants;
import oram.EncryptionStrategy;
import oram.Util;
import oram.block.BlockPath;
import oram.blockenc.BlockEncryptionStrategyPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 09-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class PathBlockCreator implements BlockCreator {
    private static final Logger logger = LogManager.getLogger("log");
    private BlockEncryptionStrategyPath blockEncStrategy;

    public PathBlockCreator(EncryptionStrategy encryptionStrategy) {
        blockEncStrategy = new BlockEncryptionStrategyPath(encryptionStrategy);
    }

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

            EncryptionStrategy encryptionStrategy = new EncryptionStrategy();
            List<BlockEncrypted> encryptedList = blockEncStrategy.encryptBlocks(blocks,
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

    private BlockPath getPathDummyBlock() {
        return new BlockPath(0, new byte[Constants.BLOCK_SIZE], 0);
    }
}
