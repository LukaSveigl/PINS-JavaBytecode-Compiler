package pins.data.btc;

import pins.data.btc.vars.BtcField;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * A bytecode class.
 */
public class BtcClass {

    /** The class name. */
    public final String name;

    /** The class magic number. */
    private final int magicNumber = 0xCAFEBABE;

    /** The class minor version number. */
    private final int minorVersion = 0;

    /** The class major version number. */
    private final int majorVersion = 61;

    /** The class constant pool. */
    private final BtcConstPool constPool = new BtcConstPool();

    /** The class access modifiers.*/
    private final int accessModifiers = 0x0001 | 0x0010 | 0x1000; // Public, Final, Synthetic

    /** The class constant pool index. */
    private final int constPoolClassIndex = 0;

    /** The superclass constant pool index. */
    private final int constPoolSuperclassIndex = 0;

    /** The number of interfaces. */
    private int intefaceCount = 0;

    /** The class interfaces. */
    private final Vector<String> interfaces = new Vector<>();

    /** The number of fields. */
    private int fieldCount = 0;

    /** The class fields. */
    private final Vector<BtcField> fields;

    /** The number of methods. */
    private int methodCount = 0;

    /** The class methods. */
    private final Vector<BtcMethod> methods;

    /** The number of attributes. */
    private int attributeCount = 0;

    /** The class attributes. */
    private final Vector<String> attributes = new Vector<>();

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
        /*Vector<Integer> hex = new Vector<>();
        hex.addAll(hexStringToVector(magicString));
        hex.addAll(hexStringToVector(minorVersion));
        hex.addAll(hexStringToVector(majorVersion));
        for (BtcMethod method : methods) {
            hex.addAll(method.getHexRepresentation());
        }
        return hex;*/

        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
        byteBuffer.putInt(this.magicNumber);
        byteBuffer.putShort((short) this.minorVersion);
        byteBuffer.putShort((short) this.majorVersion);
        //byteBuffer.put(this.constPool.toBytecode());
        byteBuffer.putShort((short) this.accessModifiers);
        byteBuffer.putShort((short) this.constPoolClassIndex);
        byteBuffer.putShort((short) this.constPoolSuperclassIndex);
        //byteBuffer.put(this.interfacePool.toBytecode());
        byteBuffer.putShort((short) this.fieldCount);
        for (BtcField field : this.fields) {
            //byteBuffer.put(field.toBytecode());
        }
        byteBuffer.putShort((short) this.methodCount);
        for (BtcMethod method : this.methods) {
            //byteBuffer.put(method.toBytecode());
        }
        //byteBuffer.putShort((short) this.attributeCount);
        //byteBuffer.put(this.attributePool.toBytecode());
        //return byteBuffer.array();

        return null;
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
