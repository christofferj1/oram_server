package oram.server;

import oram.OperationType;
import oram.Util;
import oram.blockcreator.BlockCreator;
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
    Set<String> filesWritten;

    public ServerCommunicationLayer(ServerApplication application) {
        this.application = application;
        filesWritten = new HashSet<>();
    }

    public void run(Socket socket, BlockCreator blockCreator) {
        this.socket = socket;

        if (!initializeStreams())
            System.exit(-2);

        outer:
        while (true) {
            AccessEvent accessEvent = receiveRequests();
            OperationType operationType = (accessEvent != null) ? accessEvent.getOperationType() : null;
            if (accessEvent == null || operationType == null) break;

            List<String> addresses = accessEvent.getAddresses();

            logger.info("Received access event of type: " + operationType + ", to addresses: " +
                    (operationType.equals(OperationType.END) ? Arrays.toString(addresses.toArray()) : null));

            switch (operationType) {
                case READ: { // Handle a read event
                    if (!sendBlocks(application.read(addresses)))
                        break outer;
                    break;
                }
                case WRITE: { // Handle a write event
                    List<byte[]> dataArrays = accessEvent.getDataArrays();
                    boolean statusBit = application.write(addresses, dataArrays);
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
                    if (sendWritingStatusBit(blockCreator.createBlocks(fileWrittenList))) {
                        System.out.println("Successfully send writing status bit");
                        logger.info("Successfully send writing status bit");
                    } else{
                        System.out.println("Failed to send writing status bit");
                        logger.error("Failed to send writing status bit");
                    }

                    break outer;
                }
                default:
                    logger.error("There seems to be missing a operation type");
                    break outer;
            }
        }
        try {
            dataOutputStream.close();
            dataInputStream.close();
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
                    dataArrays.add(data);
                }
                return new AccessEvent(addresses, dataArrays, OperationType.WRITE);
            }
            case 2: {
                return new AccessEvent(null, null, OperationType.END);
            }
            default:
                logger.error("What kind op operation type did you mean?");
                return null;
        }
    }

    private boolean sendBlocks(List<BlockStandard> blocks) {
        try {
            for (BlockStandard block : blocks) {
                int length = block.getData().length;
                dataOutputStream.write(Util.beIntToByteArray(length));
                dataOutputStream.write(block.getData());
            }
            dataOutputStream.flush();
        } catch (IOException e) {
            logger.error("Error happened while sending block: " + e);
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
}
