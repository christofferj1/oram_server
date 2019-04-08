package oram;

import oram.blockcreator.BlockCreator;
import oram.blockcreator.LookaheadBlockCreator;
import oram.blockcreator.StandardBlockCreator;
import oram.server.MainServer;

import java.util.Scanner;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 04-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Create files or run server? [f/s]");
        String answer = scanner.nextLine();
        while (!(answer.equals("f") || answer.equals("s"))) {
            System.out.println("Answer either 'f' or 's'");
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
        System.out.println("How many files to create?");
        answer = scanner.nextLine();
        while (!answer.matches("\\d+")) {
            System.out.println("Put in an integer");
            answer = scanner.nextLine();
        }
        int numberOfFiles = Integer.parseInt(answer);

        System.out.println("Lookahead or standard blocks? [l/s]");
        answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("s"))) {
            System.out.println("Answer either 'l' or 's'");
            answer = scanner.nextLine();
        }

        if (answer.equals("l")) {
            if (Util.createBlocks(numberOfFiles, new LookaheadBlockCreator()))
                System.out.println("Created " + numberOfFiles + " Lookahead files successfully");
            else
                System.out.println("Unable to create " + numberOfFiles + " Lookahead files");
        } else {
            if (Util.createBlocks(numberOfFiles, new StandardBlockCreator()))
                System.out.println("Created " + numberOfFiles + " Standard files successfully");
            else
                System.out.println("Unable to create " + numberOfFiles + " Standard files");
        }
    }

    private static void runServer(Scanner scanner) {
        String answer;
        System.out.println("Lookahead or Standard blocks? [l/s]");
        answer = scanner.nextLine();
        while (!(answer.equals("l") || answer.equals("s"))) {
            System.out.println("Answer either 'l' or 's'");
            answer = scanner.nextLine();
        }

        BlockCreator blockCreator;
        if (answer.equals("l"))
            blockCreator = new LookaheadBlockCreator();
        else
            blockCreator = new StandardBlockCreator();

        new MainServer().runServer(blockCreator);
    }
}
