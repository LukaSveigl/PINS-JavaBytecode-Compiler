package pins.data._btc.vars;

/**
 * A field of a bytecode class.
 */
public class _BtcField extends BtcVar {

    /** The field name. */
    public final String name;

    /** The field type. */
    public final Type type;

    /** The field index in the constant pool. */
    public final int index;

    public int init = 0;

    public _BtcField(String name, Type type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    @Override
    public byte[] toBytecode() {
        return new byte[0];
    }

    @Override
    public int getBytecodeLength() {
        return 0;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "BtcField: " + name + " " + type + " " + index + " " + init);
    }

    public String toString() {
        return name + ": " + type + " " + index + " " + init;
    }


}
