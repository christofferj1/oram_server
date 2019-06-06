package oram;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 19-02-2019. <br>
 * Master Thesis 2019 </p>
 */

public class Constants {
    public static final int PORT = 59595;

    public static final byte[] KEY_BYTES = "$ Hello World! $".getBytes();

    public static final int BLOCK_SIZE = 512;
    static final int AES_BLOCK_SIZE = 16;
    static final int AES_KEY_SIZE = 16;
    static final int POSITION_BLOCK_SIZE = 16;

    static final double BLOCKS_CREATED_AT_A_TIME = 1000d; // This reduce the max heap size when creating blocks

    static final String FILES_DIR = System.getProperty("user.dir") + "/files/";

    static final int DEFAULT_BUCKET_SIZE = 4;
}
