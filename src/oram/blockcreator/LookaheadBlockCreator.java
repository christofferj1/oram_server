package oram.blockcreator;

import oram.BlockEncrypted;
import oram.Constants;
import oram.EncryptionStrategyImpl;
import oram.Util;
import oram.lookahead.BlockLookahead;
import oram.lookahead.Index;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 04-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class LookaheadBlockCreator implements BlockCreator {
    private static final Logger logger = LogManager.getLogger("log");

    @Override
    public boolean createBlocks(List<String> addresses) {
        List<BlockLookahead> blocks = new ArrayList<>();
        int numberOfFiles = addresses.size();
        for (String ignored : addresses)
            blocks.add(getLookaheadDummyBlock());

        System.out.println("    " + numberOfFiles + " dummy blocks created");

        EncryptionStrategyImpl encryptionStrategy = new EncryptionStrategyImpl();
        List<BlockEncrypted> encryptedList = encryptBlocks(blocks, encryptionStrategy,
                encryptionStrategy.generateSecretKey(Constants.KEY_BYTES));

        if (encryptedList.isEmpty()) return false;

        System.out.println("    Data encrypted");

        for (int i = 0; i < numberOfFiles; i++) {
            byte[] data = encryptedList.get(i).getData();
            byte[] address = encryptedList.get(i).getAddress();
            byte[] bytesToWrite = new byte[data.length + address.length];
            System.arraycopy(address, 0, bytesToWrite, 0, address.length);
            System.arraycopy(data, 0, bytesToWrite, address.length, data.length);

            if (!Util.writeFile(bytesToWrite, addresses.get(i))) {
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

    private List<BlockEncrypted> encryptBlocks(List<BlockLookahead> blockLookaheads,
                                               EncryptionStrategyImpl encryptionStrategy, SecretKey secretKey) {
        List<BlockEncrypted> res = new ArrayList<>();
        for (BlockLookahead block : blockLookaheads) {
            if (block == null) {
                res.add(null);
                continue;
            }
            byte[] rowIndexBytes = Util.leIntToByteArray(block.getRowIndex());
            byte[] colIndexBytes = Util.leIntToByteArray(block.getColIndex());
            byte[] addressBytes = Util.leIntToByteArray(block.getAddress());
            byte[] encryptedAddress = encryptionStrategy.encrypt(addressBytes, secretKey);
            byte[] encryptedData = encryptionStrategy.encrypt(block.getData(), secretKey);

            byte[] indexBytes = new byte[rowIndexBytes.length + colIndexBytes.length];
            System.arraycopy(rowIndexBytes, 0, indexBytes, 0, rowIndexBytes.length);
            System.arraycopy(colIndexBytes, 0, indexBytes, rowIndexBytes.length, colIndexBytes.length);
            byte[] encryptedIndex = encryptionStrategy.encrypt(indexBytes, secretKey);

            if (encryptedAddress == null || encryptedData == null || encryptedIndex == null) {
                logger.error("Unable to encrypt block: " + block.toStringShort());
                return new ArrayList<>();
            }

            byte[] encryptedDataPlus = new byte[encryptedData.length + encryptedIndex.length];
            System.arraycopy(encryptedData, 0, encryptedDataPlus, 0, encryptedData.length);
            System.arraycopy(encryptedIndex, 0, encryptedDataPlus, encryptedData.length, encryptedIndex.length);

            res.add(new BlockEncrypted(encryptedAddress, encryptedDataPlus));
        }
        return res;
    }

    private BlockLookahead getLookaheadDummyBlock() {
        BlockLookahead blockLookahead = new BlockLookahead(0, new byte[Constants.BLOCK_SIZE]);
        blockLookahead.setIndex(new Index(0, 0));
        return blockLookahead;
    }
}