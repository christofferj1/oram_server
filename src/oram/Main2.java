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

public class Main2 {
    private static final Logger logger = LogManager.getLogger("log");

    public static void main(String[] args) {
        Util.logAndPrint(logger, "Delete files");
        Util.deleteFiles();

        int numberOfORAMS = Util.getInteger("number of layers of ORAMs (between 1 and 5, both included)");
        if (numberOfORAMS > 5) {
            Util.logAndPrint(logger, "Can't do more than 5 layers");
            return;
        } else if (numberOfORAMS < 1) {
            Util.logAndPrint(logger, "Number og layers must be a positive number");
            return;
        }

        if (numberOfORAMS == 1)
            if (!generateFiles())
                return;

        List<String> orams = createFilesInLayers(numberOfORAMS);
        if (orams == null)
            return;

        new MainServer().runServer();
    }

    private static boolean generateFiles() {
        Scanner scanner = new Scanner(System.in);

        int numberOfFiles = Util.getInteger("number of blocks");

        Util.logAndPrint(logger, "Lookahead, Path or trivial blocks? [l/p/t]");
        String answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("p") || answer.equals("t"))) {
            Util.logAndPrint(logger, "Answer either 'l', 'p', or 't'");
            answer = scanner.nextLine();
        }

        boolean res = false;
        switch (answer) {
            case "l":
                if (Util.createBlocks(numberOfFiles, new LookaheadBlockCreator())) {
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Lookahead files successfully");
                    res = true;
                } else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Lookahead files");
                break;
            case "p":
                if (Util.createBlocks(numberOfFiles, new PathBlockCreator())) {
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Path files successfully");
                    res = true;
                } else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Path files");
                break;
            default:
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
        boolean lookahead = new LookaheadBlockCreator().createBlocks(lookAddresses);
        boolean path = new PathBlockCreator().createBlocks(pathAddresses);
        boolean trivial = new TrivialBlockCreator().createBlocks(trivAddresses);

        return lookahead && path && trivial ? res : null;
    }

    private static void runServer(List<String> orams) {
        int numberOfAddresses = Util.getInteger("max number of addresses");

//        switch (answer) {
//            case "l":
//                new MainServer().runServer(Util.getAddressStrings(0, numberOfAddresses), new ArrayList<>(),
//                        new ArrayList<>());
//            case "p":
//                new MainServer().runServer(Util.getAddressStrings(0, numberOfAddresses),
//                        new ArrayList<>());
//            default:
//                new MainServer().runServer(
//                        Util.getAddressStrings(0, numberOfAddresses));
//        }
    }

    private static void runLayeredServer(Scanner scanner) {
        int numberOfLayers = Util.getInteger("How many layers of ORAM are going to be used?");
        if (numberOfLayers > 5) {
            Util.logAndPrint(logger, "Can't do more than 5 layers");
            return;
        } else if (numberOfLayers < 1) {
            Util.logAndPrint(logger, "Number og layers must be a positive number");
            return;
        }

        String answer;
        int offset = 0;
        int newOffset;
        List<String> addresses;

        List<String> lookAddresses = new ArrayList<>();
        List<String> pathAddresses = new ArrayList<>();
        List<String> trivAddresses = new ArrayList<>();

        outer:
        for (int i = 0; i < numberOfLayers; i++) {
            Util.logAndPrint(logger, "Type of layer " + i + "? [l/lt/p/t]");
            answer = scanner.nextLine();
            while (!(answer.equals("l") || answer.equals("p") || answer.equals("t") || answer.equals("lt"))) {
                Util.logAndPrint(logger, "Answer either 'l', 'lt', 'p', or 't'");
                answer = scanner.nextLine();
            }
            int levelSize = (int) Math.pow(2, (((numberOfLayers - 1) - i) * 4) + 6);
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
        new MainServer().runServer();
    }
}
