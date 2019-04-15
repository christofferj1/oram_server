package oram;

import oram.blockcreator.BlockCreator;
import oram.blockcreator.LookaheadBlockCreator;
import oram.blockcreator.PathBlockCreator;
import oram.blockcreator.StandardBlockCreator;
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
import java.util.Scanner;

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
        if (deleteFiles()) return false;

        int from = 0;
        int to = numberOfFiles + from;
        List<String> addresses = getAddressStrings(from, to);

        return blockCreator.createBlocks(addresses);
    }

    public static List<String> getAddressStrings(int from, int to) {
        List<String> addresses = new ArrayList<>();
        for (int i = from; i < to; i++)
            addresses.add(String.valueOf(i));
        return addresses;
    }

    public static boolean deleteFiles() {
        File filesDir = new File(Constants.FILES_DIR);
        String[] files = filesDir.list();
        if (files == null) {
            Util.logAndPrint(logger, "Unable to get list of files");
            return true;
        }

        int numberOfFilesToDelete = files.length;
        Util.logAndPrint(logger, "Deleting " + numberOfFilesToDelete + " files");
        for (int i = 0; i < numberOfFilesToDelete; i++) {
            File f = new File(Constants.FILES_DIR + files[i]);
            if (!f.delete()) {
                Util.logAndPrint(logger, "Deleting files went wrong");
                return true;
            }

            double percent = ((double) (i + 1) / numberOfFilesToDelete) * 100;
            if (percent % 10 == 0)
                Util.logAndPrint(logger, "    Done with " + ((int) percent) + "% of the files");
        }
        return false;
    }

    public static String getYesNoAnswer(Scanner scanner, String string) {
        logAndPrint(logger, string);
        String answer = scanner.nextLine();
        logger.info("a");
        while (!(answer.equals("y") || answer.equals("n"))) {
            logAndPrint(logger, "Provide a yes or no answer [y/n]");
            answer = scanner.nextLine();
            logger.info(answer);
        }
        return answer;
    }

    public static int getInteger(String name) {
        Scanner scanner = new Scanner(System.in);
        logAndPrint(logger, "Enter integer for '" + name + "'");
        String answer = scanner.nextLine();
        logger.info(answer);
        while (!answer.matches("\\d+")) {
            logAndPrint(logger, "Enter integer for '" + name + "'");
            answer = scanner.nextLine();
            logger.info(answer);
        }
        return Integer.parseInt(answer);
    }

    public static boolean recreateBlocks(List<String> filesToWrite, List<String> lookAddresses,
                                         List<String> pathAddresses, List<String> trivAddresses) {
        if (!lookAddresses.isEmpty()) {
            for (int i = lookAddresses.size() - 1; i >= 0; i--) {
                String string = lookAddresses.get(i);
                if (filesToWrite.contains(string)) {filesToWrite.remove(string);} else {lookAddresses.remove(i);}
            }

            boolean res = new LookaheadBlockCreator().createBlocks(lookAddresses);
            if (!res) return false;
        }

        if (!pathAddresses.isEmpty()) {
            for (int i = pathAddresses.size() - 1; i >= 0; i--) {
                String string = pathAddresses.get(i);
                if (filesToWrite.contains(string))
                    filesToWrite.remove(string);
                else
                    pathAddresses.remove(i);
            }

            boolean res = new PathBlockCreator().createBlocks(pathAddresses);
            if (!res) return false;
        }

        if (!trivAddresses.isEmpty()) {
            for (int i = trivAddresses.size() - 1; i >= 0; i--) {
                if (!filesToWrite.contains(trivAddresses.get(i)))
                    trivAddresses.remove(i);
            }

            return new StandardBlockCreator().createBlocks(trivAddresses);
        }
        return true;
    }
}
