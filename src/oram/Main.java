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

        Util.logAndPrint(logger, "Lookahead, Path or trivial blocks? [l/p/t]");
        answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("p") || answer.equals("t"))) {
            Util.logAndPrint(logger, "Answer either 'l', 'p', or 't'");
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
                if (Util.createBlocks(numberOfFiles, new TrivialBlockCreator()))
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Trivial files successfully");
                else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Trivial files");
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

        List<String> lookAddresses = new ArrayList<>();
        List<String> pathAddresses = new ArrayList<>();
        List<String> trivAddresses = new ArrayList<>();

        outer:
        for (int i = 0; i < numberOfLayers; i++) {
            Util.logAndPrint(logger, "Type of layer " + i + "? [l/p/t]");
            answer = scanner.nextLine();
            while (!(answer.equals("l") || answer.equals("p") || answer.equals("t"))) {
                Util.logAndPrint(logger, "Answer either 'l', 'p', or 't'");
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
        new LookaheadBlockCreator().createBlocks(lookAddresses);
        new PathBlockCreator().createBlocks(pathAddresses);
        new TrivialBlockCreator().createBlocks(trivAddresses);
    }

    private static void runServer(Scanner scanner) {
        String answer = "y";
//        String answer = Util.getYesNoAnswer(scanner, "Run server in layers? [y/n]");
        if (answer.equals("y")) {
            runLayeredServer(scanner);
            return;
        }

        Util.logAndPrint(logger, "Server with Lookahead or Trivial blocks? [l/p/t]");
        answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("p") || answer.equals("t"))) {
            Util.logAndPrint(logger, "Answer either 'l', 'p', or 't'");
            answer = scanner.nextLine();
        }

        int numberOfAddresses = Util.getInteger("max number of addresses");

        switch (answer) {
            case "l":
                new MainServer().runServer(Util.getAddressStrings(0, numberOfAddresses), new ArrayList<>(),
                        new ArrayList<>());
            case "p":
                new MainServer().runServer(new ArrayList<>(), Util.getAddressStrings(0, numberOfAddresses),
                        new ArrayList<>());
            default:
                new MainServer().runServer(new ArrayList<>(), new ArrayList<>(),
                        Util.getAddressStrings(0, numberOfAddresses));
        }
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
            Util.logAndPrint(logger, "Type of layer " + i + "? [l/p/t]");
            answer = scanner.nextLine();
            while (!(answer.equals("l") || answer.equals("p") || answer.equals("t"))) {
                Util.logAndPrint(logger, "Answer either 'l', 'p', or 't'");
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
        new MainServer().runServer(lookAddresses, pathAddresses, trivAddresses);
    }
}
