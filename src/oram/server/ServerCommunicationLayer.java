package oram.server;

import oram.OperationType;
import oram.Util;
import oram.block.BlockTrivial;
import oram.blockcreator.LookaheadBlockCreator;
import oram.blockcreator.PathBlockCreator;
import oram.blockcreator.TrivialBlockCreator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 11-03-2019. <br>
 * Master Thesis 2019 </p>
 */

public class ServerCommunicationLayer {
    private final Logger logger = LogManager.getLogger("log");
    private Socket socket;
    private ServerApplication application;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private Set<String> filesWritten;

    public ServerCommunicationLayer(ServerApplication application) {
        this.application = application;
        filesWritten = new HashSet<>();
    }

    public void run(Socket socket, List<String> lookAddresses, List<String> pathAddresses, List<String> trivAddresses,
                    boolean continueVersion) {
        this.socket = socket;

        if (!initializeStreams())
            System.exit(-2);

        outer:
        while (true) {
            AccessEvent accessEvent = receiveRequests();
            OperationType opType = (accessEvent != null) ? accessEvent.getOperationType() : null;
            if (accessEvent == null || opType == null) break;

            List<String> addresses = accessEvent.getAddresses();

            boolean hasAddresses = opType.equals(OperationType.READ) || opType.equals(OperationType.WRITE);
            logger.info("Received access event of type: " + opType + ", to addresses: " +
                    (hasAddresses ? Arrays.toString(addresses.toArray()) : null));

            switch (opType) {
                case READ: { // Handle a read event
                    if (!sendBlocks(application.read(addresses)))
                        break outer;
                    break;
                }
                case WRITE: { // Handle a write event
//                    List<byte[]> dataArrays = accessEvent.getDataArrays();
//                    boolean statusBit = application.write(addresses, dataArrays);
                    boolean statusBit = accessEvent.getDataArrays() != null;
                    boolean sendStatusBit = sendWritingStatusBit(statusBit);

                    if (!(statusBit && sendStatusBit)) {
                        logger.error("Status bit: " + statusBit + ", send status bit: " + sendStatusBit);
                        break outer;
                    }
                    filesWritten.addAll(addresses);
                    break;
                }
                case END: {
                    ArrayList<String> fileWrittenList = new ArrayList<>(filesWritten);
                    Collections.sort(fileWrittenList);

                    boolean writingStatus;
                    if (continueVersion)
                        writingStatus = generateFiles(lookAddresses, pathAddresses, trivAddresses);
                    else
                        writingStatus = Util.recreateBlocks(fileWrittenList, lookAddresses, pathAddresses,
                                trivAddresses);

                    if (sendWritingStatusBit(writingStatus))
                        Util.logAndPrint(logger, "Successfully send writing status bit");
                    else
                        Util.logAndPrint(logger, "Failed to send writing status bit");

                    break outer;
                }
                case SPEED_TEST: {
                    boolean write = sendData(accessEvent.getDataArrays().get(0));
                    if (!write) {
                        Util.logAndPrint(logger, "Unable to do speed test");
                        break outer;
                    }
                    break;
                }
                default:
                    logger.error("There seems to be missing a operation type");
                    break outer;
            }
        }
        try {
            dataOutputStream.close();
            dataInputStream.close();
//            if (!continueVersion)
            socket.close();
        } catch (IOException e) {
            logger.error("Error happened while closing streams/socket: " + e);
            logger.debug("Stacktrace", e);
        }
    }

    private boolean initializeStreams() {
        try {
            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataInputStream = new DataInputStream(socket.getInputStream());
        } catch (IOException e) {
            logger.error("Error happened while initializing streams: " + e);
            logger.debug("Stacktrace", e);
        }
        return true;
    }

    private AccessEvent receiveRequests() {
        byte[] operationTypeBytes = readBytes();
        if (operationTypeBytes == null) return null;
        byte[] numberOfRequestsBytes = readBytes();
        if (numberOfRequestsBytes == null) return null;

        int operationTypeInt = Util.byteArrayToLeInt(operationTypeBytes);
        int numberOfRequests = Util.byteArrayToLeInt(numberOfRequestsBytes);
        switch (operationTypeInt) {
            case 0: {
                List<String> addresses = new ArrayList<>();
                for (int i = 0; i < numberOfRequests; i++) {
                    byte[] addressBytes = readBytes();
                    if (addressBytes == null) return null;
                    addresses.add(Integer.toString(Util.byteArrayToLeInt(addressBytes)));
                }
                return new AccessEvent(addresses, null, OperationType.READ);
            }
            case 1: {
                List<String> addresses = new ArrayList<>();
                List<byte[]> dataArrays = new ArrayList<>();
                for (int i = 0; i < numberOfRequests; i++) {
                    byte[] addressBytes = readBytes();
                    if (addressBytes == null) return null;
                    addresses.add(Integer.toString(Util.byteArrayToLeInt(addressBytes)));

                    byte[] data = readBytes();
                    if (data == null) return null;

                    boolean write = application.write(Integer.toString(Util.byteArrayToLeInt(addressBytes)), data);
                    if (!write)
                        return new AccessEvent(addresses, null, OperationType.WRITE);
//                    dataArrays.add(data);

                }
                return new AccessEvent(addresses, new ArrayList<>(), OperationType.WRITE);
            }
            case 2: {
                return new AccessEvent(null, null, OperationType.END);
            }
            case 3: {
                byte[] data = readBytes();
                if (data == null)
                    return null;
                return new AccessEvent(null, Collections.singletonList(data), OperationType.SPEED_TEST);
            }
            default:
                logger.error("What kind op operation type did you mean?");
                return null;
        }
    }

    private boolean sendBlocks(List<BlockTrivial> blocks) {
        if (blocks == null) {
            logger.error("Blocks were null");
            return false;
        }
        try {
            for (BlockTrivial block : blocks) {
                if (!sendData(block.getData()))
                    return false;
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            logger.error("Error happened while sending block: " + e);
            logger.debug("Stacktrace", e);
            return false;
        }
        return true;
    }

    private boolean sendData(byte[] data) {
        try {
            int length = data.length;
            dataOutputStream.write(Util.beIntToByteArray(length));
            dataOutputStream.write(data);

        } catch (IOException e) {
            logger.error("Error happened while sending data: " + e);
            logger.debug("Stacktrace", e);
            return false;
        }
        return true;
    }

    private boolean sendWritingStatusBit(boolean status) {
        try {
            byte[] bytes = Util.leIntToByteArray(status ? 1 : 0);
            int length = bytes.length;
            dataOutputStream.write(Util.beIntToByteArray(length));
            dataOutputStream.write(bytes);

            dataOutputStream.flush();
        } catch (IOException e) {
            logger.error("Error happened while sending status bit: " + e);
            logger.debug("Stacktrace", e);
            return false;
        }
        return true;
    }

    private byte[] readBytes() {
        byte[] res = null;
        try {
            int length = dataInputStream.readInt();
            if (length > 0) {
                res = new byte[length];
                dataInputStream.readFully(res, 0, length);
            }
        } catch (IOException e) {
            logger.error("Error happened while receiving requests: " + e);
            logger.debug("Stacktrace", e);
            return null;
        }
        return res;
    }

    private boolean generateFiles(List<String> lookAddresses, List<String> pathAddresses, List<String> trivAddresses) {
        boolean res = Util.deleteFiles();
        int numberOfFiles;
        if (!lookAddresses.isEmpty()) {
            numberOfFiles = lookAddresses.size();
            if (new LookaheadBlockCreator().createBlocks(lookAddresses)) {
                Util.logAndPrint(logger, "Created " + numberOfFiles + " Lookahead files successfully");
                res = true;
            } else {
                Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Lookahead files");
                res = false;
            }
        }
        if (res && !pathAddresses.isEmpty()) {
            numberOfFiles = pathAddresses.size();
            if (new PathBlockCreator().createBlocks(pathAddresses)) {
                Util.logAndPrint(logger, "Created " + numberOfFiles + " Path files successfully");
                res = true;
            } else {
                Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Path files");
                res = false;
            }
        }
        if (res && !trivAddresses.isEmpty()) {
            numberOfFiles = trivAddresses.size();
            if (new TrivialBlockCreator().createBlocks(trivAddresses)) {
                Util.logAndPrint(logger, "Created " + numberOfFiles + " Trivial files successfully");
                res = true;
            } else {
                Util.logAndPrint(logger, "Unable to create " + numberOfFiles + " Trivial files");
                res = false;
            }
        }
        return res;
    }
}
