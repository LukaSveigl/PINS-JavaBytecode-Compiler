package pins.data.btc;

import pins.common.logger.Loggable;
import pins.data.btc.apool.BtcAttributeInfo;
import pins.data.btc.apool.BtcAttributePool;
import pins.data.btc.cpool.BtcConstPool;
import pins.data.btc.cpool.entry.BtcCpInfo;
import pins.data.btc.fpool.BtcField;
import pins.data.btc.fpool.BtcFieldPool;
import pins.data.btc.ipool.BtcInterfacePool;
import pins.data.btc.vars._BtcField;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * A bytecode class.
 */
public class BtcClass implements Loggable, BtcComp {

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

    /** The class interface pool. */
    private BtcInterfacePool interfacePool = new BtcInterfacePool();

    /** The number of fields. */
    //private int fieldCount = 0;

    /** The class fields. */
    //private final Vector<_BtcField> fields;

    /** The class field pool. */
    private final BtcFieldPool fieldPool = new BtcFieldPool();

    /** The number of methods. */
    private int methodCount = 0;

    /** The class methods. */
    private final Vector<BtcMethod> methods;

    /** The class attribute pool. */
    private final BtcAttributePool attributePool = new BtcAttributePool();

    /**
     * Constructs a new bytecode class.
     *
     * @param name The class name.
     */
    public BtcClass(String name) {
        this.name = name;
        this.methods = new Vector<>();
        //this.fields = new Vector<>();
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
    /*public void addField(_BtcField field) {
        fields.add(field);
    }*/

    public void addField(BtcField field) {
        fieldPool.addEntry(field);
    }

    /**
     * Returns the class fields.
     *
     * @return The class fields.
     */
    /*public Vector<_BtcField> fields() {
        return fields;
    }*/
    public Vector<BtcField> fields() {
        return fieldPool.entries();
    }

    /**
     * Adds a constant pool entry to the class.
     *
     * @param entry The constant pool entry to add.
     */
    public int addConstPoolEntry(BtcCpInfo entry) {
        return constPool.addEntry(entry);
    }

    /**
     * Returns the entry in the class constant pool at the specified index.
     *
     * @param index The index of the entry.
     * @return The entry in the class constant pool at the specified index.
     */
    public BtcCpInfo getConstPoolEntry(int index) {
        return constPool.getEntry(index);
    }

    /**
     * Returns the entry in the class constant pool with the specified value.
     *
     * @param value The value of the entry.
     * @return The entry in the class constant pool with the specified value.
     */
    public BtcCpInfo getConstPoolEntry(String value) {
        return constPool.getEntry(value);
    }

    /**
     * Adds an interface to the class interface pool.
     *
     * @param interfaceIndex The interface index in the constant pool.
     * @return The index of the interface in the interface pool.
     */
    public int addInterface(short interfaceIndex) {
        return interfacePool.addInterface(interfaceIndex);
    }

    /**
     * Returns the interface at the specified index in the class interface pool.
     *
     * @param index The index of the interface in the interface pool.
     * @return The interface at the specified index in the class interface pool.
     */
    public short getInterface(int index) {
        return interfacePool.getInterface(index);
    }

    /**
     * Adds an attribute to the class attribute pool.
     *
     * @param attribute The attribute to add.
     */
    public int addAttribute(BtcAttributeInfo attribute) {
        return attributePool.addEntry(attribute);
    }

    /**
     * Returns the attribute at the specified index in the class attribute pool.
     *
     * @param index The index of the attribute in the attribute pool.
     * @return The attribute at the specified index in the class attribute pool.
     */
    public BtcAttributeInfo getAttribute(int index) {
        return attributePool.getEntry(index);
    }

    @Override
    public byte[] toBytecode() {
        int size = 4 + 2 + 2 + 2 + 2 + 2 + 2 + 2 + 2;

        size += constPool.getBytecodeLength();

        for (BtcMethod method : methods) {
            size += method.getBytecodeLength();
        }

        /*for (_BtcField field : fields) {
            size += field.getBytecodeLength();
        }*/
        size += fieldPool.getBytecodeLength();

        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        byteBuffer.putInt(this.magicNumber);
        byteBuffer.putShort((short) this.minorVersion);
        byteBuffer.putShort((short) this.majorVersion);

        byteBuffer.put(this.constPool.toBytecode());

        byteBuffer.putShort((short) this.accessModifiers);
        byteBuffer.putShort((short) this.constPoolClassIndex);
        byteBuffer.putShort((short) this.constPoolSuperclassIndex);

        byteBuffer.put(this.interfacePool.toBytecode());

        /*byteBuffer.putShort((short) this.fieldCount);
        for (_BtcField field : this.fields) {
            byteBuffer.put(field.toBytecode());
        }*/
        byteBuffer.put(this.fieldPool.toBytecode());

        byteBuffer.putShort((short) this.methodCount);
        for (BtcMethod method : this.methods) {
            byteBuffer.put(method.toBytecode());
        }

        byteBuffer.put(this.attributePool.toBytecode());
        return byteBuffer.array();
    }

    @Override
    public int getBytecodeLength() {
        return 0;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "Class " + name);
        for (BtcMethod method : methods) {
            method.log(pfx + "  ");
        }
    }

}
