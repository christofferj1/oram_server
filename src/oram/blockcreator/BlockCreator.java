package oram.blockcreator;

import java.util.List;

/**
 * <p> oram_server <br>
 * Created by Christoffer S. Jensen on 04-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public interface BlockCreator {
    boolean createBlocks(List<String> addresses);
}
