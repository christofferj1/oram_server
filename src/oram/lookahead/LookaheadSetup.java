package oram.lookahead;

import oram.BlockEncrypted;
import oram.Constants;
import oram.EncryptionStrategyImpl;
import oram.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 03-04-2019. <br>
 */

public class LookaheadSetup {
    private static final Logger logger = LogManager.getLogger("log");

    public static void main(String[] args) {
        if (args.length < 1)
            System.out.println("Put in number of files as first argument");
        int numberOfFiles = Integer.parseInt(args[0]);

        List<BlockLookahead> blocks = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++)
            blocks.add(getLookaheadDummyBlock());

        EncryptionStrategyImpl encryptionStrategy = new EncryptionStrategyImpl();
        List<BlockEncrypted> encryptedList = encryptBlocks(blocks, encryptionStrategy,
                encryptionStrategy.generateSecretKey(Constants.KEY_BYTES));

        for (int i = 0; i < numberOfFiles; i++) {
            if (!writeFile(encryptedList.get(i).getData(), String.valueOf(i))) {
                logger.error("Unable to write file: " + i);
                System.exit(-1);
            }
        }
    }

    private static BlockLookahead getLookaheadDummyBlock() {
        BlockLookahead blockLookahead = new BlockLookahead(0, new byte[Constants.BLOCK_SIZE]);
        blockLookahead.setIndex(new Index(0, 0));
        return blockLookahead;
    }

    private static List<BlockEncrypted> encryptBlocks(List<BlockLookahead> blockLookaheads,
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

            byte[] encryptedIndex = new byte[rowIndexBytes.length + colIndexBytes.length];
            System.arraycopy(rowIndexBytes, 0, encryptedIndex, 0, rowIndexBytes.length);
            System.arraycopy(colIndexBytes, 0, encryptedIndex, rowIndexBytes.length, colIndexBytes.length);

//            byte[] encryptedIndex = encryptionStrategy.encrypt(ArrayUtils.addAll(rowIndexBytes, colIndexBytes),
//                    secretKey);

            if (encryptedAddress == null || encryptedData == null || encryptedIndex == null) {
                logger.error("Unable to encrypt block: " + block.toStringShort());
                return new ArrayList<>();
            }

            byte[] encryptedDataPlus = new byte[encryptedData.length + encryptedIndex.length];
            System.arraycopy(encryptedData, 0, encryptedDataPlus, 0, encryptedData.length);
            System.arraycopy(encryptedIndex, 0, encryptedDataPlus, encryptedData.length, encryptedData.length);

//            byte[] encryptedDataPlus = ArrayUtils.addAll(encryptedData, encryptedIndex);

            res.add(new BlockEncrypted(encryptedAddress, encryptedDataPlus));
        }
        return res;
    }

    private static boolean writeFile(byte[] bytesForFile, String fileName) {
        fileName = System.getProperty("user.dir") + "/files/" + fileName;
        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(bytesForFile);
        } catch (IOException e) {
            logger.error("Error happened while writing file: " + fileName + ", " + e);
            logger.debug("Stacktrace", e);
            return false;
        }
        return true;
    }
}
