package oram.server;

import oram.block.BlockTrivial;

import java.util.List;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 11-03-2019. <br>
 * Master Thesis 2019 </p>
 */

interface ServerApplication {
    BlockTrivial read(String address);

    List<BlockTrivial> read(List<String> addresses);

    boolean write(String address, byte[] dataArray);

    boolean write(List<String> addresses, List<byte[]> dataArrays);
}
