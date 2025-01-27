package oram.block;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 09-04-2019. <br>
 * Master Thesis 2019 </p>
 */

public class BlockPath {
    private int address;
    private byte[] data;
    private int index;

    public BlockPath(int address, byte[] data, int index) {
        this.address = address;
        this.data = data;
        this.index = index;
    }

    public int getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "Lookahead{" +
                "add=" + (address < 10 ? " " + address : address) +
                ", data=" + Arrays.toString(data) +
                ", index=" + index +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockPath blockPath = (BlockPath) o;
        return getAddress() == blockPath.getAddress() &&
                getIndex() == blockPath.getIndex() &&
                Arrays.equals(getData(), blockPath.getData());
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(getAddress(), getIndex());
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }
}
