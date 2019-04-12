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
    public static final int AES_KEY_SIZE = 16;
    public static final int AES_BLOCK_SIZE = 16;

    public static final String FILES_DIR = System.getProperty("user.dir") + "/files/";
//    public static final String FILES_DIR = System.getProperty("user.home") + "/files/";

    public static final int SIZE_1 = 64;
    public static final int SIZE_2 = 1024;
    public static final int SIZE_3 = 16384;
    public static final int SIZE_4 = 262144;
    public static final int SIZE_5 = 4194304;
    public static final int DEFAULT_BUCKET_SIZE = 4;
}
