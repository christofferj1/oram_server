package oram;

import oram.blockcreator.BlockCreator;
import oram.blockcreator.LookaheadBlockCreator;
import oram.blockcreator.PathBlockCreator;
import oram.blockcreator.StandardBlockCreator;
import oram.server.MainServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
        String answer;
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
