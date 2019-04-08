package oram;

import oram.blockcreator.BlockCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 20-02-2019. <br>
 * Master Thesis 2019 </p>
 */

public class Util {
    private static final Logger logger = LogManager.getLogger("log");

    public static byte[] getRandomByteArray(int length) {
        if (length <= 0) return new byte[0];

        SecureRandom random = new SecureRandom();
        byte[] res = new byte[length];
        random.nextBytes(res);

        return res;
    }

    //        TODO: Needs testing. All numbers from 0 to like 100.
    public static int byteArrayToLeInt(byte[] b) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getInt();
    }

    //        TODO: Needs testing. All numbers from 0 to like 100.
    public static byte[] leIntToByteArray(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

    //        TODO: Needs testing. All numbers from 0 to like 100.
    public static byte[] beIntToByteArray(int i) {
        final ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }


    public static String getShortDataString(byte[] data) {
        String dataString;
        if (data.length > 10) {
            String arrayString = Arrays.toString(Arrays.copyOf(data, 10));
            arrayString = arrayString.substring(0, arrayString.length() - 1);
            arrayString += ", ...";
            dataString = arrayString;
        } else
            dataString = Arrays.toString(data);
        return dataString;
    }

    public static boolean writeFile(byte[] bytesForFile, String fileName) {
        String filePath = Constants.FILES_DIR + fileName;
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(bytesForFile);
        } catch (IOException e) {
            logger.error("Error happened while writing file: " + filePath + ", " + e);
            logger.debug("Stacktrace", e);
            return false;
        }
        return true;
    }

    public static void logAndPrint(Logger logger, String string) {
        System.out.println(string);
        logger.info(string);
    }

    public static boolean createBlocks(int numberOfFiles, BlockCreator blockCreator) {
        File filesDir = new File(Constants.FILES_DIR);
        String[] files = filesDir.list();
        if (files == null) {
            Util.logAndPrint(logger, "Unable to get list of files");
            return false;
        }

        int numberOfFilesToDelete = files.length;
        Util.logAndPrint(logger, "Deleting " + numberOfFilesToDelete + " files");
        for (int i = 0; i < numberOfFilesToDelete; i++) {
            File f = new File(Constants.FILES_DIR + files[i]);
            if (!f.delete()) {
                Util.logAndPrint(logger, "Deleting files went wrong");
                return false;
            }

            double percent = ((double) (i + 1) / numberOfFilesToDelete) * 100;
            if (percent % 10 == 0)
                Util.logAndPrint(logger, "    Done with " + ((int) percent) + "% of the files");
        }

        List<String> addresses = new ArrayList<>();
        for (int i = 0; i < numberOfFiles; i++)
            addresses.add(String.valueOf(i));

        return blockCreator.createBlocks(addresses);
    }
}
