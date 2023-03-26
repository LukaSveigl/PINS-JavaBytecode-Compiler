package pins.data.btc.ipool;

import pins.common.logger.Loggable;
import pins.data.btc.BtcComp;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * The JVM class interface pool.
 */
public class BtcInterfacePool implements Loggable, BtcComp {

    /** The interface pool entries. */
    private final Vector<Short> interfaces = new Vector<>();

    /**
     * Constructs a new interface pool.
     */
    public BtcInterfacePool() {

    }

    /**
     * Adds an interface to the interface pool.
     *
     * @param interfaceIndex The interface index in the constant pool.
     */
    public int addInterface(short interfaceIndex) {
        interfaces.add(interfaceIndex);
        return interfaces.size() - 1;
    }

    /**
     * Returns the interface at the specified index.
     *
     * @param index The index of the interface.
     * @return The interface at the specified index.
     */
    public short getInterface(int index) {
        return interfaces.get(index);
    }

    /**
     * Returns the interface pool entries.
     *
     * @return The interface pool entries.
     */
    public Vector<Short> interfaces() {
        return interfaces;
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.putShort((short) interfaces.size());
        for (short interfaceIndex : interfaces) {
            byteBuffer.putShort(interfaceIndex);
        }
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 2 + interfaces.size() * 2;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "BtcInterfacePool:");
        for (int i = 0; i < interfaces.size(); i++) {
            System.out.println(pfx + "  " + i + ": " + interfaces.get(i));
        }
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("BtcInterfacePool:\n");
        for (int i = 0; i < interfaces.size(); i++) {
            stringBuilder.append("  ").append(i).append(": ").append(interfaces.get(i)).append("\n");
        }
        return stringBuilder.toString();
    }

}
