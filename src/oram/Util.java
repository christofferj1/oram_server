package oram;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 20-02-2019. <br>
 * Master Thesis 2019 </p>
 */

public class Util {
    private static final Logger logger = LogManager.getLogger("log");

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
    public static String getTimeString(long milliseconds) {
        int hours = (int) (milliseconds / 3600000);
        int minutes = (int) (milliseconds % 3600000) / 60000;
        int seconds = (int) (milliseconds % 60000) / 1000;
        int millisecondsMod = (int) (milliseconds % 1000);

        String millisecondsString = "" + millisecondsMod;
        if (millisecondsMod < 10) millisecondsString = "  " + millisecondsString;
        if (millisecondsMod < 100) millisecondsString = " " + millisecondsString;

        return String.format("%02d:%02d:%02d." + millisecondsString, hours, minutes, seconds);
    }


    public static void printPercentageDone(long startTime, double numberOfRounds, int roundNumber) {
        double percentDone = ((roundNumber + 1) / numberOfRounds) * 100;
        if ((percentDone % 1) == 0) {
            int percentDoneInt = (int) percentDone;
            long timeElapsed = (System.nanoTime() - startTime) / 1000000;
            long timeElapsedPerPercent = timeElapsed / percentDoneInt;
            long timeLeft = timeElapsedPerPercent * (100 - percentDoneInt);
            System.out.println("Done with " + percentDoneInt + "%, time spend: " + getTimeString(timeElapsed) +
                    ", estimated time left: " + getTimeString(timeLeft));
            logger.info("Done with " + percentDoneInt + "%, time spend: " + getTimeString(timeElapsed) +
                    ", estimated time left: " + getTimeString(timeLeft));
        }
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
}
