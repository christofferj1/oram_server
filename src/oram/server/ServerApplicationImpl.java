package oram.server;

import oram.Util;
import oram.block.BlockTrivial;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 11-03-2019. <br>
 * Master Thesis 2019 </p>
 */

public class ServerApplicationImpl implements ServerApplication {
    private final Logger logger = LogManager.getLogger("log");

    @Override
    public BlockTrivial read(String address) {
        List<BlockTrivial> read = read(Collections.singletonList(address));
        if (read == null)
            return null;
        return read.get(0);
    }

    @Override
    public List<BlockTrivial> read(List<String> addresses) {
        List<BlockTrivial> res = new ArrayList<>();
        for (String address : addresses) {
            byte[] data = readFile(address);
            if (data == null) {
                logger.error("Reading file failed");
                return null;
            }

            res.add(new BlockTrivial(Integer.parseInt(address), data));
        }
        return res;
    }

    @Override
    public boolean write(String address, byte[] dataArray) {
        return write(Collections.singletonList(address), Collections.singletonList(dataArray));
    }

    @Override
    public boolean write(List<String> addresses, List<byte[]> dataArrays) {
        if (addresses.size() != dataArrays.size()) {
            logger.error("Not same number of addresses (" + addresses.size() + ") as data arrays (" + dataArrays.size()
                    + ")");
            return false;
        }
        for (int i = 0; i < addresses.size(); i++)
            if (!Util.writeFile(dataArrays.get(i), addresses.get(i))) return false;

        return true;
    }

    private byte[] readFile(String fileName) {
        byte[] res;
        try {
            fileName = System.getProperty("user.dir") + "/files/" + fileName;
            res = Files.readAllBytes(Paths.get(fileName));
        } catch (IOException e) {
            logger.error("Error happened while reading file: " + fileName + ", " + e);
            logger.debug("Stacktrace", e);
            return null;
        }
        return res;
    }
}
