package oram;

import oram.blockcreator.LookaheadBlockCreator;
import oram.blockcreator.PathBlockCreator;
import oram.blockcreator.TrivialBlockCreator;
import oram.server.MainServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 04-04-2019. <br>
 * Master Thesis 2019 </p>
 */

class Main {
    private static final Logger logger = LogManager.getLogger("log");

    public static void main(String[] args) {
        if (!Util.makeSureFilesFolderExists()) {
            Util.logAndPrint(logger, "Unable to make sure folder for file exists");
            System.exit(-1);
        }

        Util.logAndPrint(logger, "Delete files");
        Util.deleteFilesFails();

        int numberOfORAMS = Util.getInteger("number of layers of ORAMs (between 1 and 5, both included)");
        if (numberOfORAMS > 5) {
            Util.logAndPrint(logger, "Can't do more than 5 layers");
            return;
        } else if (numberOfORAMS < 1) {
            Util.logAndPrint(logger, "Number og layers must be a positive number");
            return;
        }

        if (numberOfORAMS == 1) {
            if (!generateFiles())
                return;
        } else {
            List<String> orams = createFilesInLayers(numberOfORAMS);
            if (orams == null)
                return;
        }

        new MainServer().runServer();
    }

    private static boolean generateFiles() {
        Scanner scanner = new Scanner(System.in);
        Util.logAndPrint(logger, "Use Lookahead, Path, or Trivial blocks? [l/p/t]");
        String answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("p") || answer.equals("t"))) {
            Util.logAndPrint(logger, "Answer either 'l', 'p', or 't'");
            answer = scanner.nextLine();
        }

        boolean res = false;
        switch (answer) {
            case "l": {
                int oramSize = Util.getInteger("size, must be a square number");
                int numberOfFiles = (int) (oramSize + 2 * Math.sqrt(oramSize));
                if (Util.createBlocks(numberOfFiles, new LookaheadBlockCreator(new EncryptionStrategy()))) {
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Lookahead files successfully");
                    res = true;
                } else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Lookahead files");
                break;
            }
            case "p": {
                int oramSize = Util.getInteger("size, must be a power of 2");
                int bucketSize = Util.getInteger("bucket size");
                int numberOfFiles = (oramSize * bucketSize) - 1;
                if (Util.createBlocks(numberOfFiles, new PathBlockCreator(new EncryptionStrategy()))) {
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Path files successfully");
                    res = true;
                } else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Path files");
                break;
            }
            default:
                int oramSize = Util.getInteger("size");
                int numberOfFiles = oramSize + 1;
                if (Util.createBlocks(numberOfFiles, new TrivialBlockCreator())) {
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Trivial files successfully");
                    res = true;
                } else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Trivial files");
                break;
        }
        return res;
    }

    private static List<String> createFilesInLayers(int numberOfORAMs) {
        String answer;
        Scanner scanner = new Scanner(System.in);
        int offset = 0;
        int newOffset;
        List<String> addresses;

        List<String> lookAddresses = new ArrayList<>();
        List<String> pathAddresses = new ArrayList<>();
        List<String> trivAddresses = new ArrayList<>();

        List<String> res = new ArrayList<>();

        outer:
        for (int i = 0; i < numberOfORAMs; i++) {
            Util.logAndPrint(logger, "ORAM number " + i + ", choose between Lookahead, Path, Trivial, or Lookahead (using Trivial specialised for Lookahead) [l/lt/p/t]");
            answer = scanner.nextLine();
            while (!(answer.equals("l") || answer.equals("p") || answer.equals("t") || answer.equals("lt"))) {
                Util.logAndPrint(logger, "Answer either 'l', 'lt', 'p', or 't'");
                answer = scanner.nextLine();
            }
            res.add(answer);
            int levelSize = (int) Math.pow(2, (((numberOfORAMs - 1) - i) * 4) + 6);
            switch (answer) {
                case "l":
                    newOffset = offset + levelSize + (int) (2 * Math.sqrt(levelSize));
                    addresses = Util.getAddressStrings(offset, newOffset);
                    offset = newOffset;
                    lookAddresses.addAll(addresses);
                    break;
                case "lt":
                    newOffset = offset + levelSize + (int) (2 * Math.sqrt(levelSize));
                    newOffset += Math.ceil((double) levelSize / Constants.POSITION_BLOCK_SIZE);
                    addresses = Util.getAddressStrings(offset, newOffset);

                    lookAddresses.addAll(addresses);
                    break outer;
                case "p":
                    newOffset = offset + (levelSize - 1) * Constants.DEFAULT_BUCKET_SIZE;
                    addresses = Util.getAddressStrings(offset, newOffset);
                    offset = newOffset;
                    pathAddresses.addAll(addresses);
                    break;
                default:
                    newOffset = offset + levelSize + 1;
                    addresses = Util.getAddressStrings(offset, newOffset);
                    trivAddresses.addAll(addresses);
                    break outer;
            }
        }
        boolean lookahead = new LookaheadBlockCreator(new EncryptionStrategy()).createBlocks(lookAddresses);
        boolean path = new PathBlockCreator(new EncryptionStrategy()).createBlocks(pathAddresses);
        boolean trivial = new TrivialBlockCreator().createBlocks(trivAddresses);

        return lookahead && path && trivial ? res : null;
    }
}
