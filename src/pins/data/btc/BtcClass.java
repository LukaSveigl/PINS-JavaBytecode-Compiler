package pins.data.btc;

import pins.data.btc.vars.BtcField;

import java.util.Vector;

/**
 * A bytecode class.
 */
public class BtcClass {

    /** The class name. */
    public final String name;

    /** The class methods. */
    private final Vector<BtcMethod> methods;

    private final Vector<BtcField> fields;

    /** The JVM class magic string. */
    public final String magicString = "CAFEBABE";

    /** The JVM class minor version. */
    public final String minorVersion = "0000";

    /** The JVM class major version. */
    public final String majorVersion = "0003C"; // 60

    // TODO: Add more fields

    /**
     * Constructs a new bytecode class.
     *
     * @param name The class name.
     */
    public BtcClass(String name) {
        this.name = name;
        this.methods = new Vector<>();
        this.fields = new Vector<>();
    }

    /**
     * Adds a method to the class.
     *
     * @param method The method to add.
     */
    public void addMethod(BtcMethod method) {
        methods.add(method);
    }

    /**
     * Returns the class methods.
     *
     * @return The class methods.
     */
    public Vector<BtcMethod> methods() {
        return methods;
    }

    /**
     * Adds a field to the class.
     *
     * @param field The field to add.
     */
    public void addField(BtcField field) {
        fields.add(field);
    }

    /**
     * Returns the class fields.
     *
     * @return The class fields.
     */
    public Vector<BtcField> fields() {
        return fields;
    }

    /**
     * Returns the class as a hex representation.
     *
     * @return The class as a hex representation.
     */
    public Vector<Integer> getHexRepresentation() {
        Vector<Integer> hex = new Vector<>();
        hex.addAll(hexStringToVector(magicString));
        hex.addAll(hexStringToVector(minorVersion));
        hex.addAll(hexStringToVector(majorVersion));
        for (BtcMethod method : methods) {
            hex.addAll(method.getHexRepresentation());
        }
        return hex;
    }

    /**
     * Logs the class.
     */
    public void log(String pfx) {
        System.out.println(pfx + "Class " + name);
        for (BtcMethod method : methods) {
            method.log(pfx + "  ");
        }
    }

    /**
     * Converts a hex string to a vector of integers.
     *
     * @param hexString The hex string to convert.
     * @return The vector of integers.
     */
    private Vector<Integer> hexStringToVector(String hexString) {
        Vector<Integer> hex = new Vector<>();
        for (int i = 0; i < hexString.length(); i += 2) {
            String hexByte = hexString.substring(i, i + 2);
            hex.add(Integer.parseInt(hexByte, 16));
        }
        return hex;
    }

}
