package pins.data.btc;

import pins.common.logger.Loggable;
import pins.data.btc.apool.BtcAttributeInfo;
import pins.data.btc.apool.BtcAttributePool;
import pins.data.btc.cpool.BtcConstPool;
import pins.data.btc.cpool.entry.*;
import pins.data.btc.fpool.BtcField;
import pins.data.btc.fpool.BtcFieldPool;
import pins.data.btc.ipool.BtcInterfacePool;
import pins.data.btc.vars._BtcField;

import java.nio.ByteBuffer;
import java.util.HashMap;
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
    private final int majorVersion = 60;

    /** The class constant pool. */
    private final BtcConstPool constPool = new BtcConstPool();

    /** The class access modifiers.*/
    private final int accessModifiers = 0x0001 | 0x0010 | 0x1000; // Public, Final, Synthetic

    /** The class constant pool index. */
    private final int constPoolClassIndex;

    /** The superclass constant pool index. */
    private final int constPoolSuperclassIndex;

    /** The class interface pool. */
    private BtcInterfacePool interfacePool = new BtcInterfacePool();

    /** The number of fields. */
    //private int fieldCount = 0;

    /** The class fields. */
    //private final Vector<_BtcField> fields;

    /** The class field pool. */
    private final BtcFieldPool fieldPool = new BtcFieldPool();

    public final HashMap<String, Integer> fieldToFieldref = new HashMap<>();

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

        // Initialize the correct class data, such as the constructor, names, etc.
        // First initialize all UTF8 entries.
        int classNameIndex = constPool.addEntry(new BtcUtf8Info(name));
        int objectClassNameIndex = constPool.addEntry(new BtcUtf8Info("java/lang/Object"));
        int constructorNameIndex = constPool.addEntry(new BtcUtf8Info("<init>"));
        int constructorDescriptorIndex = constPool.addEntry(new BtcUtf8Info("()V"));
        int codeAttributeNameIndex = constPool.addEntry(new BtcUtf8Info("Code"));
        int mainMethodNameIndex = constPool.addEntry(new BtcUtf8Info("main"));

        // Then initialize the class entries.
        int objectClassIndex = constPool.addEntry(new BtcClassInfo(objectClassNameIndex));
        int thisClassIndex = constPool.addEntry(new BtcClassInfo(classNameIndex));

        // Then initialize the name and type entries.
        int constructorNameAndTypeIndex = constPool.addEntry(new BtcNameAndTypeInfo(constructorNameIndex, constructorDescriptorIndex));

        // Then initialize the method entries.
        int constructorMethodIndex = constPool.addEntry(new BtcMethodrefInfo(objectClassIndex, constructorNameAndTypeIndex));

        // Link the class constant pool index to the class entry.
        constPoolClassIndex = thisClassIndex;

        // Link the superclass constant pool index to the object class entry.
        constPoolSuperclassIndex = objectClassIndex;

        // Add field descriptors to the const pool.
        int booleanDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.BOOLEAN.descriptor));
        int byteDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.BYTE.descriptor));
        int charDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.CHAR.descriptor));
        int shortDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.SHORT.descriptor));
        int intDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.INT.descriptor));
        int longDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.LONG.descriptor));
        int floatDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.FLOAT.descriptor));
        int doubleDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.DOUBLE.descriptor));
        int referenceDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.REFERENCE.descriptor));
        int arrayDescriptorIndex = constPool.addEntry(new BtcUtf8Info(BtcFieldPool.Descriptor.ARRAY.descriptor));
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
     * Adds a field to the class field pool.
     *
     * @param field The field to add.
     * @return The index of the field in the field pool.
     */
    public int addField(BtcField field) {
        return fieldPool.addField(field);
    }

    /**
     * Adds a field to the class field pool.
     *
     * @param nameIndex  The name index in the constant pool.
     * @param descriptor The descriptor of the field.
     * @param attributes The attributes of the field.
     * @return The index of the field in the field pool.
     */
    public int addField(int nameIndex, String descriptor, BtcAttributeInfo[] attributes) {
        int descriptorIndex = 0;
        for (int i = 0; i < constPool.entries().size(); i++) {
            if (constPool.entries().get(i) instanceof BtcUtf8Info) {
                if (((BtcUtf8Info) constPool.entries().get(i)).value().equals(descriptor)) {
                    descriptorIndex = i + 1;
                    break;
                }
            }
        }

        if (descriptorIndex == 0) {
            descriptorIndex = constPool.addEntry(new BtcUtf8Info(descriptor));
        }

        // Add the NameAndType to the constant pool.
        int nameAndTypeIndex = constPool.addEntry(new BtcNameAndTypeInfo(nameIndex, descriptorIndex));
        // Add the Fieldref to the constant pool.
        int fieldrefIndex = constPool.addEntry(new BtcFieldrefInfo(constPoolClassIndex, nameAndTypeIndex));

        this.fieldToFieldref.put(constPool.getEntry(nameIndex - 1).value(), fieldrefIndex);

        return fieldPool.addField(new BtcField(nameIndex, descriptorIndex, attributes));
    }

    public int addField(String name, String descriptor, BtcAttributeInfo[] attributes) {
        int nameIndex = 0;
        for (int i = 0; i < constPool.entries().size(); i++) {
            if (constPool.entries().get(i) instanceof BtcUtf8Info) {
                if (((BtcUtf8Info) constPool.entries().get(i)).value().equals(name)) {
                    nameIndex = i + 1;
                    break;
                }
            }
        }

        if (nameIndex == 0) {
            nameIndex = constPool.addEntry(new BtcUtf8Info(name));
        }

        return addField(nameIndex, descriptor, attributes);
    }

    /**
     * Returns the field at the specified index in the class field pool.
     *
     * @param index The index of the field in the field pool.
     * @return The field at the specified index in the class field pool.
     */
    public BtcField getField(int index) {
        return fieldPool.getField(index);
    }

    /**
     * Returns the field with the specified name in the class field pool.
     *
     * @param name The name of the field in the field pool.
     * @return The field with the specified name in the class field pool.
     */
    public BtcField getField(String name) {
        int index = 0;
        for (int i = 0; i < constPool.entries().size(); i++) {
            if (constPool.entries().get(i).value().equals(name)) {
                index = i;
                break;
            }
        }

        if (index == 0) {
            return null;
        }

        return fieldPool.getFieldByNameIndex(index);
    }

    /**
     * Adds an attribute to the class attribute pool.
     *
     * @param attribute The attribute to add.
     * @return The index of the attribute in the attribute pool.
     */
    public int addAttribute(BtcAttributeInfo attribute) {
        return attributePool.addAttribute(attribute);
    }

    /**
     * Returns the attribute at the specified index in the class attribute pool.
     *
     * @param index The index of the attribute in the attribute pool.
     * @return The attribute at the specified index in the class attribute pool.
     */
    public BtcAttributeInfo getAttribute(int index) {
        return attributePool.getAttribute(index);
    }

    @Override
    public byte[] toBytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(getBytecodeLength());
        byteBuffer.putInt(this.magicNumber);
        byteBuffer.putShort((short) this.minorVersion);
        byteBuffer.putShort((short) this.majorVersion);

        byteBuffer.put(this.constPool.toBytecode());

        byteBuffer.putShort((short) this.accessModifiers);
        byteBuffer.putShort((short) this.constPoolClassIndex);
        byteBuffer.putShort((short) this.constPoolSuperclassIndex);

        byteBuffer.put(this.interfacePool.toBytecode());
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
        int size = 4 + // Magic number.
                   2 + // Minor version.
                   2 + // Major version.
                   2 + // Constant pool count.
                   2 + // Access modifiers.
                   2 + // Class index.
                   2;  // Superclass index.

        size += constPool.getBytecodeLength();
        for (BtcMethod method : methods) {
            size += method.getBytecodeLength();
        }
        size += fieldPool.getBytecodeLength();
        size += interfacePool.getBytecodeLength();
        size += attributePool.getBytecodeLength();

        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "Class " + name);
        for (BtcMethod method : methods) {
            method.log(pfx + "  ");
        }
    }

}
