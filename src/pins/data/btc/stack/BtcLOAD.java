package pins.data.btc.stack;

import pins.data.btc.BtcInstr;

import java.util.Vector;

/**
 * The LOAD instruction.
 */
public class BtcLOAD extends BtcInstr {

    /** The LOAD instruction type. */
    public enum Type {
        ILOAD, LLOAD, FLOAD, DLOAD, ALOAD
    }

    /** The LOAD instruction index (points to local). */
    public final int index;

    /** The LOAD instruction type. */
    public final Type type;

    /**
     * Constructs a new LOAD instruction.
     *
     * @param index The LOAD instruction index.
     * @param type  The LOAD instruction type.
     */
    public BtcLOAD(int index, Type type) {
        this.index = index;
        this.type = type;
    }

    @Override
    public Vector<Integer> getHexRepresentation() {
        // TODO: Implement

        return null;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + type + " " + index);
        /*switch (type) {
            case ILOAD -> System.out.println(pfx + "ILOAD " + index);
            case LLOAD -> System.out.println(pfx + "LLOAD " + index);
            case FLOAD -> System.out.println(pfx + "FLOAD " + index);
            case DLOAD -> System.out.println(pfx + "DLOAD " + index);
            case ALOAD -> System.out.println(pfx + "ALOAD " + index);
        }*/
    }

    @Override
    public String toString() {
        return type.toString() + " " + index;
    }

}
