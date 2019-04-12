package oram;

import oram.blockcreator.BlockCreator;
import oram.blockcreator.LookaheadBlockCreator;
import oram.blockcreator.PathBlockCreator;
import oram.blockcreator.StandardBlockCreator;
import oram.server.MainServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Scanner;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 04-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class Main {
    private static final Logger logger = LogManager.getLogger("log");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Util.logAndPrint(logger, "Create files or run server? [f/s]");
        String answer = scanner.nextLine();
        while (!(answer.equals("f") || answer.equals("s"))) {
            Util.logAndPrint(logger, "Answer either 'f' or 's'");
            answer = scanner.nextLine();
        }

        if (answer.equals("f")) {
            generateFiles(scanner);
        } else {
            runServer(scanner);
        }
    }

    private static void generateFiles(Scanner scanner) {
        String answer = Util.getYesNoAnswer(scanner, "Create files in layers? [y/n]");
        if (answer.equals("y")) {
            createFilesInLayers();
            return;
        }

        Util.logAndPrint(logger, "How many files to create?");
        answer = scanner.nextLine();
        while (!answer.matches("\\d+")) {
            Util.logAndPrint(logger, "Put in an integer");
            answer = scanner.nextLine();
        }
        int numberOfFiles = Integer.parseInt(answer);

        Util.logAndPrint(logger, "Lookahead, Path or standard blocks? [l/p/s]");
        answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("p") || answer.equals("s"))) {
            Util.logAndPrint(logger, "Answer either 'l', 'p', or 's'");
            answer = scanner.nextLine();
        }

        switch (answer) {
            case "l":
                if (Util.createBlocks(numberOfFiles, new LookaheadBlockCreator()))
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Lookahead files successfully");
                else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Lookahead files");
                break;
            case "p":
                if (Util.createBlocks(numberOfFiles, new PathBlockCreator()))
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Path files successfully");
                else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Path files");
                break;
            default:
                if (Util.createBlocks(numberOfFiles, new StandardBlockCreator()))
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Standard files successfully");
                else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Standard files");
                break;
        }
    }

    private static void createFilesInLayers() {
        Util.logAndPrint(logger, "Delete files");
        Util.deleteFiles();

        int numberOfLayers = Util.getInteger("How many layers of ORAM are going to be used?");
        if (numberOfLayers > 5) {
            Util.logAndPrint(logger, "Can't do more than 5 layers");
            return;
        } else if (numberOfLayers < 1) {
            Util.logAndPrint(logger, "Number og layers must be a positive number");
            return;
        }

        String answer;
        Scanner scanner = new Scanner(System.in);
        int offset = 0;
        int newOffset;
        List<String> addresses;

        for (int i = 0; i < numberOfLayers; i++) {
            Util.logAndPrint(logger, "Type of layer " + i + "? [l/p/s]");
            answer = scanner.nextLine();
            while (!(answer.equals("l") || answer.equals("p") || answer.equals("s"))) {
                Util.logAndPrint(logger, "Answer either 'l', 'p', or 's'");
                answer = scanner.nextLine();
            }
            int levelSize = (int) Math.pow(2, (((numberOfLayers - 1) - i) * 4) + 6);
            switch (answer) {
                case "l":
                    newOffset = offset + levelSize + (int) (2 * Math.sqrt(levelSize));
                    addresses = Util.getAddressStrings(offset, newOffset);
                    offset = newOffset;
                    new LookaheadBlockCreator().createBlocks(addresses);
                    break;
                case "p":
                    newOffset = offset + (levelSize - 1) * Constants.DEFAULT_BUCKET_SIZE;
                    addresses = Util.getAddressStrings(offset, newOffset);
                    offset = newOffset;
                    new PathBlockCreator().createBlocks(addresses);
                    break;
                default:
                    newOffset = offset + levelSize + 1; // TODO: if this is chosen, the rest should not be there (we can return from here)
                    addresses = Util.getAddressStrings(offset, newOffset);
                    offset = newOffset;
                    new StandardBlockCreator().createBlocks(addresses);
            }

        }
    }

    private static void runServer(Scanner scanner) {
        String answer;
        Util.logAndPrint(logger, "Server with Lookahead or Standard blocks? [l/p/s]");
        answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("p") || answer.equals("s"))) {
            Util.logAndPrint(logger, "Answer either 'l', 'p', or 's'");
            answer = scanner.nextLine();
        }

        BlockCreator blockCreator;
        switch (answer) {
            case "l":
                blockCreator = new LookaheadBlockCreator();
                break;
            case "p":
                blockCreator = new PathBlockCreator();
                break;
            default:
                blockCreator = new StandardBlockCreator();
                break;
        }

        new MainServer().runServer(blockCreator);
    }
}
