package oram.blockenc;

import oram.BlockEncrypted;
import oram.EncryptionStrategy;
import oram.Util;
import oram.block.BlockPath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.List;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 30-05-2019. <br>
 * Master Thesis 2019 </p>
 */

public class BlockEncryptionStrategyPath {
    private static final Logger logger = LogManager.getLogger("log");
    private EncryptionStrategy encryptionStrategy;

    public BlockEncryptionStrategyPath(EncryptionStrategy encryptionStrategy) {
        this.encryptionStrategy = encryptionStrategy;
    }

    public List<BlockEncrypted> encryptBlocks(List<BlockPath> blocks, SecretKey secretKey) {
        List<BlockEncrypted> encryptedBlocksToWrite = new ArrayList<>();

        for (BlockPath block : blocks) {
            byte[] addressCipher = encryptionStrategy.encrypt(Util.leIntToByteArray(block.getAddress()), secretKey);
            byte[] indexCipher = encryptionStrategy.encrypt(Util.leIntToByteArray(block.getIndex()), secretKey);
            byte[] dataCipher = encryptionStrategy.encrypt(block.getData(), secretKey);
            if (addressCipher == null || indexCipher == null || dataCipher == null) {
                logger.error("Unable to encrypt address: " + block.getAddress() + " or data");
                return null;
            }

            byte[] encryptedDataPlus = new byte[indexCipher.length + dataCipher.length];
            System.arraycopy(dataCipher, 0, encryptedDataPlus, 0, dataCipher.length);
            System.arraycopy(indexCipher, 0, encryptedDataPlus, dataCipher.length, indexCipher.length);

            encryptedBlocksToWrite.add(new BlockEncrypted(addressCipher, encryptedDataPlus));
        }

        return encryptedBlocksToWrite;
    }
}
