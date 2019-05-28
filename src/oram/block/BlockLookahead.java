package oram.block;

import oram.Util;
import oram.lookahead.Index;

import java.util.Arrays;
import java.util.Objects;

/**
 * <p> ORAM <br>
 * Created by Christoffer S. Jensen on 04-03-2019. <br>
 * Master Thesis 2019 </p>
 */

public class BlockLookahead {
    private int address;
    private byte[] data;
    private int colIndex;
    private int rowIndex;

    public BlockLookahead(int address, byte[] data) {
        this.address = address;
        this.data = data;
    }

    public void setIndex(Index index) {
        rowIndex = index.getRowIndex();
        colIndex = index.getColIndex();
    }

    public int getAddress() {
        return address;
    }

    public byte[] getData() {
        return data;
    }

    public int getColIndex() {
        return colIndex;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    @Override
    public String toString() {
        return "Lookahead{" +
                "add=" + (address < 10 ? " " + address : address) +
                ", data=" + Arrays.toString(data) +
                ", row=" + rowIndex +
                ", col=" + colIndex +
                '}';
    }

    public String toStringShort() {
        String dataString = Util.getShortDataString(data);
        return "Lookahead{" +
                "add=" + (address < 10 ? " " + address : address) +
                ", (" + rowIndex + ", " + colIndex + ")" +
                ", data=" + dataString +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockLookahead that = (BlockLookahead) o;
        return getAddress() == that.getAddress() &&
                getColIndex() == that.getColIndex() &&
                getRowIndex() == that.getRowIndex() &&
                Arrays.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(getAddress(), getColIndex(), getRowIndex());
        result = 31 * result + Arrays.hashCode(getData());
        return result;
    }
}
