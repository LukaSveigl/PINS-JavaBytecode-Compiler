package pins.data.btc.var;

import pins.common.logger.Loggable;

/**
 * JVM bytecode variable.
 */
public abstract class BtcVar implements Loggable {

    public enum Type {
        INT, FLOAT, LONG, DOUBLE, BOOL, STRING, ARRAY, OBJECT, VOID, CHAR
    }

    /** The type of the variable. */
    public final Type type;

    /**
     * Constructs a new variable.
     *
     * @param type The type of the variable.
     */
    protected BtcVar(Type type) {
        this.type = type;
    }

    @Override
    public abstract void log(String pfx);

}
