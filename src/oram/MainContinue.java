package oram;

import oram.blockcreator.LookaheadBlockCreator;
import oram.blockcreator.PathBlockCreator;
import oram.blockcreator.TrivialBlockCreator;
import oram.server.MainServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 04-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class MainContinue {
    private static final Logger logger = LogManager.getLogger("log");

    public static void main(String[] args) {
        int size = 64;
        MainServer mainServer = new MainServer();

        generateFiles("l", size);
        runServer("l", size, mainServer);
        runServer("p", size, mainServer);
        runServer("t", size, mainServer);
    }

    private static void generateFiles(String type, int size) {
        int numberOfFiles;
        switch (type) {
            case "l":
                numberOfFiles = (int) (size + 2 * Math.sqrt(size));
                if (Util.createBlocks(numberOfFiles, new LookaheadBlockCreator()))
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Lookahead files successfully");
                else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Lookahead files");
                break;
            case "p":
                numberOfFiles = Constants.DEFAULT_BUCKET_SIZE * (size - 1);
                if (Util.createBlocks(numberOfFiles, new PathBlockCreator()))
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Path files successfully");
                else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Path files");
                break;
            default:
                numberOfFiles = size + 1;
                if (Util.createBlocks(numberOfFiles, new TrivialBlockCreator()))
                    Util.logAndPrint(logger, "Created " + numberOfFiles + " Trivial files successfully");
                else
                    Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Trivial files");
                break;
        }
    }

    private static void runServer(String type, int size, MainServer mainServer) {
        int numberOfAddresses;
        switch (type) {
            case "l":
                numberOfAddresses = Constants.DEFAULT_BUCKET_SIZE * (size - 1);
                mainServer.runServer(new ArrayList<>(), Util.getAddressStrings(0, numberOfAddresses),
                        new ArrayList<>(), true);
                break;
            case "p":
                numberOfAddresses = size + 1;
                mainServer.runServerAgain(new ArrayList<>(), new ArrayList<>(),
                        Util.getAddressStrings(0, numberOfAddresses), true);
                break;
            default:
                mainServer.runServerAgain(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), true);
        }
    }
}
