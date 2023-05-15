package pins.data.emt;

import pins.data.emt.attr.EmtAttributeInfo;
import pins.data.emt.constp.*;
import pins.data.emt.field.EmtFieldInfo;
import pins.data.emt.method.EmtMethodInfo;

import java.nio.ByteBuffer;
import java.util.Vector;

/**
 * JVM class file struct as specified by the JVM spec (section 4.1).
 */
public class EmtClassFile implements EmtInfo {

    /** The class name. */
    public final String name;

    /** The class destination file. */
    public final String dstName;

    /** The class file magic number field. */
    private final int magicNumber = 0xCAFEBABE;

    /** The class file minor_version field. */
    private final int minorVersion = 0;

    /** The class file major_version field. */
    private final int majorVersion = 60;

    // Constant pool
    /** The class file constant_pool_count field. */
    private int constantPoolCount = 1;

    /** The class file constant_pool field. */
    private final Vector<EmtConstPoolInfo> constantPool = new Vector<>();

    /** The class file access_flags field. */
    private final int accessFlags = 0x0001 | 0x0010 | 0x1000; // ACC_PUBLIC | ACC_SUPER | ACC_FINAL

    /** The class file this_class field. */
    private int constPoolClassIndex;

    /** The class file super_class field. */
    private int constPoolSuperClassIndex;

    // Interfaces
    /** The class file interfaces_count field. */
    private int interfacesCount = 0;

    /** The class file interfaces field. */
    private final Vector<Short> interfaces = new Vector<Short>();

    // Fields
    /** The class file fields_count field. */
    private int fieldsCount = 0;

    /** The class file fields field. */
    private final Vector<EmtFieldInfo> fields = new Vector<EmtFieldInfo>();

    // Methods
    /** The class file methods_count field. */
    private int methodsCount = 0;

    /** The class file methods field. */
    private final Vector<EmtMethodInfo> methods = new Vector<EmtMethodInfo>();

    // Attributes
    /** The class file attributes_count field. */
    private int attributesCount = 0;

    /** The class file attributes field. */
    private final Vector<EmtAttributeInfo> attributes = new Vector<EmtAttributeInfo>();

    public int thisClassIndex;

    /**
     * Constructs a new class file.
     *
     * @param name The class name.
     */
    public EmtClassFile(String name, String dstFileName) {
        this.name = name;
        this.dstName = dstFileName;
        init();
    }

    private void init() {
        int classNameIndex = addConstPoolInfo(new EmtUTF8Info(name));
        int objectClassNameIndex = addConstPoolInfo(new EmtUTF8Info("java/lang/Object"));
        int constructorNameIndex = addConstPoolInfo(new EmtUTF8Info("<init>"));
        int constructorDescriptorIndex = addConstPoolInfo(new EmtUTF8Info("()V"));
        int codeNameIndex = addConstPoolInfo(new EmtUTF8Info("Code"));
        int lineNumberNameIndex = addConstPoolInfo(new EmtUTF8Info("main"));

        int objectClassIndex = addConstPoolInfo(new EmtClassInfo(objectClassNameIndex));
        int thisClassIndex = addConstPoolInfo(new EmtClassInfo(classNameIndex));

        this.thisClassIndex = thisClassIndex;

        int constructorNameAndTypeIndex = addConstPoolInfo(new EmtNameAndTypeInfo(constructorNameIndex, constructorDescriptorIndex));
        int constructorMethodIndex = addConstPoolInfo(new EmtMethodrefInfo(objectClassIndex, constructorNameAndTypeIndex));

        constPoolClassIndex = thisClassIndex;
        constPoolSuperClassIndex = objectClassIndex;
    }

    public int addConstPoolInfo(EmtConstPoolInfo info) {
        constantPool.add(info);
        int returnIndex = constantPoolCount;
        constantPoolCount += 1;
        if (info.tag == EmtConstPoolInfo.Tag.LONG || info.tag == EmtConstPoolInfo.Tag.DOUBLE) {
            constantPoolCount += 1;
        }
        return returnIndex;
    }

    public Vector<EmtConstPoolInfo> getConstPool() {
        return constantPool;
    }

    public Vector<EmtConstPoolInfo> getConstPoolInfoWithTag(EmtConstPoolInfo.Tag tag) {
        Vector<EmtConstPoolInfo> result = new Vector<>();
        for (EmtConstPoolInfo info : constantPool) {
            if (info.tag == tag) {
                result.add(info);
            }
        }
        return result;
    }

    public int addInterfaceInfo(short info) {
        interfaces.add(info);
        interfacesCount++;
        return interfaces.size();
    }

    public Vector<Short> getInterfaces() {
        return interfaces;
    }

    public int addFieldInfo(EmtFieldInfo info) {
        fields.add(info);
        fieldsCount++;
        return fields.size();
    }

    public Vector<EmtFieldInfo> getFields() {
        return fields;
    }

    public int addMethodInfo(EmtMethodInfo info) {
        methods.add(info);
        methodsCount++;
        return methods.size();
    }

    public Vector<EmtMethodInfo> getMethods() {
        return methods;
    }

    public int addAttributeInfo(EmtAttributeInfo info) {
        attributes.add(info);
        attributesCount++;
        return attributes.size();
    }

    public Vector<EmtAttributeInfo> getAttributes() {
        return attributes;
    }

    @Override
    public byte[] bytecode() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(size());
        byteBuffer.putInt(magicNumber);
        byteBuffer.putShort((short) minorVersion);
        byteBuffer.putShort((short) majorVersion);

        byteBuffer.putShort((short) constantPoolCount);
        for (EmtConstPoolInfo constPoolInfo : constantPool) {
            byteBuffer.put(constPoolInfo.bytecode());
        }

        byteBuffer.putShort((short) accessFlags);
        byteBuffer.putShort((short) constPoolClassIndex);
        byteBuffer.putShort((short) constPoolSuperClassIndex);

        byteBuffer.putShort((short) interfacesCount);
        for (Short i : interfaces) {
            byteBuffer.putShort(i);
        }

        byteBuffer.putShort((short) fieldsCount);
        for (EmtFieldInfo f : fields) {
            byteBuffer.put(f.bytecode());
        }

        byteBuffer.putShort((short) methodsCount);
        for (EmtMethodInfo m : methods) {
            byteBuffer.put(m.bytecode());
        }

        byteBuffer.putShort((short) attributesCount);
        for (EmtAttributeInfo a : attributes) {
            byteBuffer.put(a.bytecode());
        }

        return byteBuffer.array();
    }

    @Override
    public int size() {
        int size = 4 + // Magic number
                   2 + // Minor version
                   2 + // Major version
                   2 + // Access flags
                   2 + // This class
                   2;  // Super class

        size += 2; // Constant pool count
        for (EmtConstPoolInfo constPoolInfo : constantPool) {
            size += constPoolInfo.size();
        }

        size += 2; // Interfaces count
        size += interfacesCount * 2; // Interfaces

        size += 2; // Fields count
        for (EmtFieldInfo field : fields) {
            size += field.size();
        }

        size += 2; // Methods count
        for (EmtMethodInfo method : methods) {
            size += method.size();
        }

        size += 2; // Attributes count
        for (EmtAttributeInfo attribute : attributes) {
            size += attribute.size();
        }

        return size;
    }

    @Override
    public void log(String pfx) {
        System.out.println(pfx + "ClassFile[" + name + "]");
        System.out.println(pfx + "  magicNumber: " + magicNumber);
        System.out.println(pfx + "  minorVersion: " + minorVersion);
        System.out.println(pfx + "  majorVersion: " + majorVersion);
        System.out.println(pfx + "  accessFlags: " + accessFlags);
        System.out.println(pfx + "  constPoolCount: " + constantPoolCount);
        for (EmtConstPoolInfo constPoolInfo : constantPool) {
            constPoolInfo.log(pfx + "  ");
        }
        System.out.println(pfx + "  constPoolClassIndex: " + constPoolClassIndex);
        System.out.println(pfx + "  constPoolSuperClassIndex: " + constPoolSuperClassIndex);
        System.out.println(pfx + "  interfacesCount: " + interfacesCount);
        for (Short i : interfaces) {
            System.out.println(pfx + "    " + i);
        }
        System.out.println(pfx + "  fieldsCount: " + fieldsCount);
        for (EmtFieldInfo f : fields) {
            f.log(pfx + "    ");
        }
        System.out.println(pfx + "  methodsCount: " + methodsCount);
        for (EmtMethodInfo m : methods) {
            m.log(pfx + "    ");
        }
        System.out.println(pfx + "  attributesCount: " + attributesCount);
        for (EmtAttributeInfo a : attributes) {
            a.log(pfx + "    ");
        }
    }

}
