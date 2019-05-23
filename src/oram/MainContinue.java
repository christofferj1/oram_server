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
        MainServer mainServer = new MainServer();

        Constants.BLOCK_SIZE = 512;
        generateFiles("t", 65);

        Constants.BLOCK_SIZE = 65536;
        int numberOfAddresses = (int) (1024 + 2 * Math.sqrt(1024));
        mainServer.runServer(Util.getAddressStrings(0, numberOfAddresses), new ArrayList<>(), new ArrayList<>(), true);

        Constants.BLOCK_SIZE = 65536;
        numberOfAddresses = (4 * 1024) - 1;
        mainServer.runServerAgain(new ArrayList<>(),Util.getAddressStrings(0, numberOfAddresses),  Util.getAddressStrings(4092, 4157), true);

        Constants.BLOCK_SIZE = 65536;
        numberOfAddresses = (int) (1024 + 2 * Math.sqrt(1024));
        mainServer.runServerAgain(Util.getAddressStrings(0,numberOfAddresses),  new ArrayList<>() , Util.getAddressStrings(1088, 1153), true);

        Constants.BLOCK_SIZE = 65536;
        numberOfAddresses = (int) (1024 + 2 * Math.sqrt(1024));
        mainServer.runServerAgain(Util.getAddressStrings(0,numberOfAddresses),  new ArrayList<>() , Util.getAddressStrings(1088, 1153), true);

        mainServer.runServerAgain(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), true);

//        numberOfAddresses = size + 1;
//        mainServer.runServerAgain(new ArrayList<>(), new ArrayList<>(),
//                Util.getAddressStrings(0, numberOfAddresses), true);
//
//
//        numberOfAddresses = (int) (size2 + 2 * Math.sqrt(size2));
//        mainServer.runServerAgain(Util.getAddressStrings(0, numberOfAddresses), new ArrayList<>(), new ArrayList<>(),
//                true);
//
//        numberOfAddresses = Constants.DEFAULT_BUCKET_SIZE * (size2 - 1);
//        mainServer.runServerAgain(new ArrayList<>(), Util.getAddressStrings(0, numberOfAddresses),
//                new ArrayList<>(), true);
//
//        numberOfAddresses = size2 + 1;
//        mainServer.runServerAgain(new ArrayList<>(), new ArrayList<>(),
//                Util.getAddressStrings(0, numberOfAddresses), true);
//
//        numberOfAddresses = (int) (size3 + 2 * Math.sqrt(size3));
//        mainServer.runServerAgain(Util.getAddressStrings(0, numberOfAddresses), new ArrayList<>(), new ArrayList<>(),
//                true);
//
//        numberOfAddresses = Constants.DEFAULT_BUCKET_SIZE * (size3 - 1);
//        mainServer.runServerAgain(new ArrayList<>(), Util.getAddressStrings(0, numberOfAddresses),
//                new ArrayList<>(), true);
//
//        numberOfAddresses = size3 + 1;
//        mainServer.runServerAgain(new ArrayList<>(), new ArrayList<>(),
//                Util.getAddressStrings(0, numberOfAddresses), true);

//        mainServer.runServerAgain(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
//                true);
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
