package oram.blockenc;

import oram.BlockEncrypted;
import oram.EncryptionStrategy;
import oram.Util;
import oram.block.BlockLookahead;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 30-05-2019. <br>
 * Master Thesis 2019 </p>
 */

public class BlockEncryptionStrategyLookahead {
    private static final Logger logger = LogManager.getLogger("log");
    private EncryptionStrategy encryptionStrategy;

    public BlockEncryptionStrategyLookahead(EncryptionStrategy encryptionStrategy) {
        this.encryptionStrategy = encryptionStrategy;
    }

    public List<BlockEncrypted> encryptBlocks(List<BlockLookahead> blocks, SecretKey secretKey) {
        List<BlockEncrypted> res = new ArrayList<>();
        for (BlockLookahead block : blocks) {
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
            byte[] encryptedIndex = encryptionStrategy.encrypt(indexBytes,
                    secretKey);

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

    public BlockEncrypted encryptBlock(BlockLookahead block, SecretKey secretKey) {
        List<BlockEncrypted> encrypted = encryptBlocks(Collections.singletonList(block), secretKey);
        if (encrypted.isEmpty())
            return null;
        else
            return encrypted.get(0);
    }
}
