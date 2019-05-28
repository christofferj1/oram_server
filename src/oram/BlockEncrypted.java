package oram;

import java.io.Serializable;
import java.util.Arrays;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 23-02-2019. <br>
 * Master Thesis 2019 </p>
 */

public class BlockEncrypted implements Serializable {
    private byte[] address;
    private byte[] data;

    public BlockEncrypted(byte[] address, byte[] data) {
        this.address = address;
        this.data = data;
    }

    public byte[] getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "BlockEncrypted{" +
                "address=" + Arrays.toString(address) +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockEncrypted that = (BlockEncrypted) o;
        return Arrays.equals(getAddress(), that.getAddress()) &&
                Arrays.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {

        int result = Arrays.hashCode(getAddress());
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }
}
