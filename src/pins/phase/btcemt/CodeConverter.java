package pins.phase.btcemt;

import pins.common.report.Report;
import pins.data.btc.BtcCLASS;
import pins.data.btc.method.BtcMETHOD;
import pins.data.btc.method.instr.BtcInstr;
import pins.data.btc.method.instr.stack.*;
import pins.data.btc.method.instr.object.*;
import pins.data.btc.method.instr.ctrl.*;
import pins.data.btc.var.BtcFIELD;
import pins.data.emt.EmtClassFile;
import pins.data.emt.attr.EmtAttributeInfo;
import pins.data.emt.constp.*;
import pins.data.emt.field.EmtFieldInfo;
import pins.data.emt.method.EmtMethodInfo;
import pins.phase.btcgen.BtcGen;

import java.nio.ByteBuffer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Bytecode converter. Converts the {@link BtcCLASS}es to {@link EmtClassFile}s.
 */
public class CodeConverter {

    /** The current class file. */
    private EmtClassFile currentClassFile;

    /** The Code attribute max stack value size in bytes. */
    private static final int MAX_STACK_BYTE_SIZE = 2;

    /** The Code attribute code length value size in bytes. */
    private static final int CODE_LENGTH_BYTE_SIZE = 4;

    /** The Code attribute exception table length value size in bytes. */
    private static final int EXCEPTION_TABLE_LENGTH_BYTE_SIZE = 2;

    /** The Code attribute method attribute count value size in bytes. */
    private static final int METHOD_ATTRIBUTE_COUNT_BYTE_SIZE = 2;

    /** The Code attribute max locals value size in bytes. */
    private static final int MAX_LOCALS_BYTE_SIZE = 2;


    /** The Code attribute maximum stack size. */
    private static final int MAX_STACK = 256;

    /** The Code attribute maximum number of locals. */
    private static final int MAX_LOCALS = 256;


    /** The Code attribute exception table length. */
    private static final int EXCEPTION_TABLE_LENGTH = 0;

    /** The Code attribute method attribute count. */
    private static final int METHOD_ATTRIBUTE_COUNT = 0;


    /** The Java bytecode representation of a BYTE. */
    private static final String TYPE_BYTE = "B";

    /** The Java bytecode representation of a SHORT. */
    private static final String TYPE_SHORT = "S";

    /** The Java bytecode representation of an INT. */
    private static final String TYPE_INT = "I";

    /** The Java bytecode representation of a FLOAT. */
    private static final String TYPE_FLOAT = "F";

    /** The Java bytecode representation of a LONG. */
    private static final String TYPE_LONG = "J";

    /** The Java bytecode representation of a DOUBLE. */
    private static final String TYPE_DOUBLE = "D";

    /** The Java bytecode representation of a VOID. */
    private static final String TYPE_VOID = "V";

    /** The Java bytecode representation of a BOOLEAN. */
    private static final String TYPE_BOOLEAN = "Z";

    /** The Java bytecode representation of a CHAR. */
    private static final String TYPE_CHAR = "C";

    /** The Java bytecode representation of a STRING. */
    private static final String TYPE_STRING = "Ljava/lang/String;";

    /** The Java bytecode representation of an OBJECT (Array or reference. In this case only array of LONG). */
    private static final String TYPE_OBJECT = "[J";

    /** The Java bytecode representation of an ARRAY. */
    private static final String TYPE_ARRAY = "[";

    /**
     * Initiates the conversion of the intermediate bytecode representation to the executable bytecode representation.
     */
    public void convert() {
        for (BtcCLASS btcCLASS : BtcGen.btcClasses) {
            convertClass(btcCLASS);
        }
    }

    /**
     * Converts a class from the intermediate bytecode representation to the executable bytecode representation.
     *
     * @param btcCLASS The class to convert.
     */
    private void convertClass(BtcCLASS btcCLASS) {
        currentClassFile = new EmtClassFile(btcCLASS.name, btcCLASS.dstName);
        BtcEmt.classFiles.put(btcCLASS, currentClassFile);
        for (BtcFIELD field : btcCLASS.fields().values()) {
            convertField(field);
        }
        for (BtcMETHOD method : btcCLASS.methods()) {
            convertMethod(method);
        }
    }

    /**
     * Converts a field from the intermediate bytecode representation to the executable bytecode representation.
     *
     * @param field The field to convert.
     */
    private void convertField(BtcFIELD field) {
        // Generate the field flags.
        Map<BtcFIELD.Flags, EmtFieldInfo.AccessFlag> flagMap = new HashMap<>();
        flagMap.put(BtcFIELD.Flags.PUBLIC, EmtFieldInfo.AccessFlag.PUBLIC);
        flagMap.put(BtcFIELD.Flags.PRIVATE, EmtFieldInfo.AccessFlag.PRIVATE);
        flagMap.put(BtcFIELD.Flags.PROTECTED, EmtFieldInfo.AccessFlag.PROTECTED);
        flagMap.put(BtcFIELD.Flags.STATIC, EmtFieldInfo.AccessFlag.STATIC);
        flagMap.put(BtcFIELD.Flags.FINAL, EmtFieldInfo.AccessFlag.FINAL);
        flagMap.put(BtcFIELD.Flags.VOLATILE, EmtFieldInfo.AccessFlag.VOLATILE);
        flagMap.put(BtcFIELD.Flags.TRANSIENT, EmtFieldInfo.AccessFlag.TRANSIENT);
        flagMap.put(BtcFIELD.Flags.SYNTHETIC, EmtFieldInfo.AccessFlag.SYNTHETIC);
        flagMap.put(BtcFIELD.Flags.ENUM, EmtFieldInfo.AccessFlag.ENUM);

        Vector<EmtFieldInfo.AccessFlag> flags = field.flags().stream()
                .map(flagMap::get)
                .collect(Collectors.toCollection(Vector::new));

        // Generate the field name.
        int nameIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(field.name));

        // Generate the field descriptor.
        StringBuilder typeDescriptor = new StringBuilder();
        switch (field.type) {
            case INT -> typeDescriptor = new StringBuilder(TYPE_INT);
            case FLOAT -> typeDescriptor = new StringBuilder(TYPE_FLOAT);
            case LONG -> typeDescriptor = new StringBuilder(TYPE_LONG);
            case DOUBLE -> typeDescriptor = new StringBuilder(TYPE_DOUBLE);
            case BOOL -> typeDescriptor = new StringBuilder(TYPE_BOOLEAN);
            case CHAR -> typeDescriptor = new StringBuilder(TYPE_CHAR);
            case STRING -> typeDescriptor = new StringBuilder(TYPE_STRING);
            // TODO: Fix arrays to include multiple dimensions.
            case ARRAY -> {
                typeDescriptor.append("[");
                for (BtcFIELD.Type type : field.subTypes) {
                    switch (type) {
                        case ARRAY -> typeDescriptor.append("[");
                        case BOOL -> typeDescriptor.append(TYPE_BOOLEAN);
                        case DOUBLE -> typeDescriptor.append(TYPE_DOUBLE);
                        case FLOAT -> typeDescriptor.append(TYPE_FLOAT);
                        case INT -> typeDescriptor.append(TYPE_INT);
                        case LONG -> typeDescriptor.append(TYPE_LONG);
                        case CHAR -> typeDescriptor.append(TYPE_CHAR);
                    }
                }
            }
        }

        EmtFieldrefInfo emtFieldrefInfo;
        if (field.name.equals("PrintStream")) {
            int systemUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("java/lang/System"));
            int outUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("out"));
            int printUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("Ljava/io/PrintStream;"));

            int nameAndTypeIndex = currentClassFile.addConstPoolInfo(new EmtNameAndTypeInfo(outUtfIndex, printUtfIndex));
            int classIndex = currentClassFile.addConstPoolInfo(new EmtClassInfo(systemUtfIndex));
            emtFieldrefInfo = new EmtFieldrefInfo(classIndex, nameAndTypeIndex);
        } else {
            int descriptorIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(typeDescriptor.toString()));
            currentClassFile.addFieldInfo(new EmtFieldInfo(flags, nameIndex, descriptorIndex, 0, null));
            EmtNameAndTypeInfo nameAndTypeInfo = new EmtNameAndTypeInfo(nameIndex, descriptorIndex);
            int nameAndTypeIndex = currentClassFile.addConstPoolInfo(nameAndTypeInfo);
            emtFieldrefInfo = new EmtFieldrefInfo(currentClassFile.thisClassIndex, nameAndTypeIndex);
        }

        int index = currentClassFile.addConstPoolInfo(emtFieldrefInfo);
        BtcEmt.fieldRefs.put(field, emtFieldrefInfo);
        BtcEmt.fieldRefConstPoolIndices.put(emtFieldrefInfo, index);
    }

    /**
     * Converts a method from the intermediate bytecode representation to the executable bytecode representation.
     *
     * @param method The method to convert.
     */
    private void convertMethod(BtcMETHOD method) {
        // Generate the method flags.
        Vector<EmtMethodInfo.AccessFlag> flags = convertMethodFlags(method.flags());

        // Generate the method name.
        int nameIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(method.name));
        String typeDescriptor = convertTypeDescriptor(method);
        int typeDescriptorIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(typeDescriptor));
        EmtNameAndTypeInfo nameAndTypeInfo = new EmtNameAndTypeInfo(nameIndex, typeDescriptorIndex);
        int nameAndTypeIndex = currentClassFile.addConstPoolInfo(nameAndTypeInfo);

        // Generate the method reference info.
        EmtMethodrefInfo methodRefInfo = new EmtMethodrefInfo(currentClassFile.thisClassIndex, nameAndTypeIndex);
        currentClassFile.addConstPoolInfo(methodRefInfo);

        // Generate the method code attribute.
        int codeAttributeIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("Code"));
        ByteBuffer code = convertMethodCode(method);
        EmtAttributeInfo codeAttribute = new EmtAttributeInfo(codeAttributeIndex, code.array().length, code.array());

        // Generate the method attributes.
        Vector<EmtAttributeInfo> attributes = new Vector<>();
        attributes.add(codeAttribute);

        currentClassFile.addMethodInfo(new EmtMethodInfo(flags, nameIndex, typeDescriptorIndex, attributes.size(), attributes));
    }

    /**
     * Converts a single instruction from the intermediate bytecode representation to the executable bytecode representation.
     *
     * @param instr The instruction to convert.
     * @return The converted instruction.
     */
    private byte[] convertInstruction(BtcInstr instr) {
        // Stack instructions.
        if (instr instanceof BtcLDC)
            return handleInstr((BtcLDC) instr);
        if (instr instanceof BtcLOAD)
            return handleInstr((BtcLOAD) instr);
        if (instr instanceof BtcPUSH)
            return handleInstr((BtcPUSH) instr);
        if (instr instanceof BtcSTORE)
            return handleInstr((BtcSTORE) instr);

        // Object instructions.
        if (instr instanceof BtcACCESS)
            return handleInstr((BtcACCESS) instr);
        if (instr instanceof BtcINVOKE)
            return handleInstr((BtcINVOKE) instr);
        if (instr instanceof BtcNEWARRAY)
            return handleInstr((BtcNEWARRAY) instr);
        if (instr instanceof BtcMULTIANEWARRAY)
            return handleInstr((BtcMULTIANEWARRAY) instr);
        if (instr instanceof BtcNEW)
            return handleInstr((BtcNEW) instr);

        // Flow control instructions.
        if (instr instanceof BtcCJUMP)
            return handleInstr((BtcCJUMP) instr);
        if (instr instanceof BtcGOTO)
            return handleInstr((BtcGOTO) instr);

        // Generic handler.
        return handleInstr(instr);
    }


    // Utilities

    /**
     * Converts the flags of a method.
     *
     * @param flags The flags to convert.
     * @return The vector of converted flags.
     */
    private Vector<EmtMethodInfo.AccessFlag> convertMethodFlags(Vector<BtcMETHOD.Flags> flags) {
        Vector<EmtMethodInfo.AccessFlag> convertedFlags = new Vector<>();

        for (BtcMETHOD.Flags flag : flags) {
            switch (flag) {
                case PUBLIC -> convertedFlags.add(EmtMethodInfo.AccessFlag.PUBLIC);
                case PRIVATE -> convertedFlags.add(EmtMethodInfo.AccessFlag.PRIVATE);
                case PROTECTED -> convertedFlags.add(EmtMethodInfo.AccessFlag.PROTECTED);
                case STATIC -> convertedFlags.add(EmtMethodInfo.AccessFlag.STATIC);
                case FINAL -> convertedFlags.add(EmtMethodInfo.AccessFlag.FINAL);
                case SYNCHRONIZED -> convertedFlags.add(EmtMethodInfo.AccessFlag.SYNCHRONIZED);
                case NATIVE -> convertedFlags.add(EmtMethodInfo.AccessFlag.NATIVE);
                case ABSTRACT -> convertedFlags.add(EmtMethodInfo.AccessFlag.ABSTRACT);
                case STRICT -> convertedFlags.add(EmtMethodInfo.AccessFlag.STRICT);
                case SYNTHETIC -> convertedFlags.add(EmtMethodInfo.AccessFlag.SYNTHETIC);
            }
        }

        return convertedFlags;
    }

    /**
     * Converts the type descriptor of a method.
     *
     * @param method The method to convert.
     * @return The converted type descriptor.
     * @throws Report.InternalError in case the return type does not conform to the PINS'22 specifications.
     */
    private String convertTypeDescriptor(BtcMETHOD method) {
        StringBuilder typeDescriptorBuilder = new StringBuilder("(");

        if (method.name.equals("main")) {
            typeDescriptorBuilder.append("[Ljava/lang/String;");
            typeDescriptorBuilder.append(")");
            typeDescriptorBuilder.append(TYPE_VOID);
        } else {
            // Generate the type descriptor for the parameters.
            for (BtcMETHOD.Type parType : method.parTypes()) {
                switch (parType) {
                    case INT -> typeDescriptorBuilder.append(TYPE_INT);
                    case FLOAT -> typeDescriptorBuilder.append(TYPE_FLOAT);
                    case LONG -> typeDescriptorBuilder.append(TYPE_LONG);
                    case DOUBLE -> typeDescriptorBuilder.append(TYPE_DOUBLE);
                    case BOOL -> typeDescriptorBuilder.append(TYPE_BOOLEAN);
                    case STRING -> typeDescriptorBuilder.append(TYPE_STRING);
                    case ARRAY, OBJECT -> typeDescriptorBuilder.append(TYPE_OBJECT);
                }
            }

            // Generate the type descriptor for the return type.
            typeDescriptorBuilder.append(")").append(switch (method.ret) {
                case INT -> TYPE_INT;
                case FLOAT -> TYPE_FLOAT;
                case LONG -> TYPE_LONG;
                case DOUBLE -> TYPE_DOUBLE;
                case BOOL -> TYPE_BOOLEAN;
                case STRING -> TYPE_STRING;
                // As per the specification, the return type of function cannot be an array.
                case OBJECT -> TYPE_OBJECT;
                case VOID -> TYPE_VOID;
                default -> throw new Report.InternalError();
            });
        }

        return typeDescriptorBuilder.toString();
    }

    /**
     * Converts the code of a method.
     *
     * @param method The method to convert.
     * @return The converted code.
     */
    private ByteBuffer convertMethodCode(BtcMETHOD method) {
        int codeLength = method.instrs().stream().mapToInt(BtcInstr::size).sum();

        ByteBuffer code = ByteBuffer.allocate(codeLength * 2
                + MAX_STACK_BYTE_SIZE
                + CODE_LENGTH_BYTE_SIZE
                + EXCEPTION_TABLE_LENGTH_BYTE_SIZE
                + METHOD_ATTRIBUTE_COUNT_BYTE_SIZE
                + MAX_LOCALS_BYTE_SIZE
        );

        code.putShort((short) MAX_STACK);
        code.putShort((short) MAX_LOCALS);
        code.putInt(codeLength); // Code length.

        // Convert the instructions and calculate the actual code length.
        int actualCodeLength = 0;
        for (BtcInstr instr : method.instrs()) {
            byte [] convertedInstr = convertInstruction(instr);
            code.put(convertedInstr);
            actualCodeLength += convertedInstr.length;
        }

        code.putShort((short) EXCEPTION_TABLE_LENGTH);
        code.putShort((short) METHOD_ATTRIBUTE_COUNT);

        // Correct the code length in the ByteBuffer. (Offset is 2 bytes for max stack size and 2 bytes for max locals.)
        code.putInt(2 + 2, actualCodeLength);

        // Correct the ByteBuffer. Remove the extra empty bytes. (This is a hack.)
        ByteBuffer correctedCode = ByteBuffer.allocate(code.position());
        correctedCode.put(code.array(), 0, correctedCode.capacity());

        return correctedCode;
    }

    // Instruction handlers

    // Stack instructions.

    /**
     * Handles the conversion of the {@link BtcLDC} instruction.
     *
     * @param btcLDC The instruction to convert.
     * @return The converted instruction.
     */
    private byte[] handleInstr(BtcLDC btcLDC) {
        ByteBuffer code;

        final int WIDE_THRESHOLD = 0xff;
        final int LDC_OPCODE = 0x12;
        final int LDC_W_OPCODE = 0x13;
        final int LDC2_W_OPCODE = 0x14;

        if (btcLDC.type == BtcLDC.Type.DEFAULT) {
            int index = currentClassFile.addConstPoolInfo(new EmtIntegerInfo((int) btcLDC.value));
            if (index < WIDE_THRESHOLD) {
                code = ByteBuffer.allocate(2);
                code.put((byte) LDC_OPCODE);
                code.put((byte) index);
            } else {
                code = ByteBuffer.allocate(3);
                code.put((byte) LDC_W_OPCODE);
                code.putShort((short) index);
            }
        } else {
            code = ByteBuffer.allocate(3);
            int index = currentClassFile.addConstPoolInfo(new EmtLongInfo(btcLDC.value));
            code.put((byte) LDC2_W_OPCODE);
            code.putShort((short) index);
        }

        return code.array();
    }

    /**
     * Handles the conversion of the {@link BtcLOAD} instruction.
     *
     * @param btcLOAD The instruction to convert.
     * @return The converted instruction.
     */
    private byte[] handleInstr(BtcLOAD btcLOAD) {
        ByteBuffer code = ByteBuffer.allocate(btcLOAD.size());

        code.put((byte) btcLOAD.opcode());
        code.put((byte) btcLOAD.index);

        return code.array();
    }

    /**
     * Handles the conversion of the {@link BtcPUSH} instruction.
     * @param btcPUSH The instruction to convert.
     * @return The converted instruction.
     */
    private byte[] handleInstr(BtcPUSH btcPUSH) {
        ByteBuffer code = ByteBuffer.allocate(btcPUSH.size());

        code.put((byte) btcPUSH.opcode());
        if (btcPUSH.type == BtcPUSH.Type.BYTE) {
            code.put((byte) btcPUSH.value);
        } else {
            code.putShort((short) btcPUSH.value);
        }

        return code.array();
    }

    /**
     * Handles the conversion of the {@link BtcSTORE} instruction.
     *
     * @param btcSTORE The instruction to convert.
     * @return The converted instruction.
     */
    private byte[] handleInstr(BtcSTORE btcSTORE) {
        ByteBuffer code = ByteBuffer.allocate(btcSTORE.size());

        code.put((byte) btcSTORE.opcode());
        code.put((byte) btcSTORE.index);

        return code.array();
    }

    // Object instructions.

    /**
     * Handles the conversion of the {@link BtcACCESS} instruction.
     *
     * @param btcACCESS The instruction to convert.
     * @return The converted instruction.
     */
    private byte[] handleInstr(BtcACCESS btcACCESS) {
        ByteBuffer code = ByteBuffer.allocate(btcACCESS.size());

        code.put((byte) btcACCESS.opcode());
        EmtFieldrefInfo emtFieldrefInfo = BtcEmt.fieldRefs.get(btcACCESS.field);
        int index = currentClassFile.addConstPoolInfo(emtFieldrefInfo);
        code.putShort((short) index);

        return code.array();
    }

    /**
     * Handles the conversion of the {@link BtcINVOKE} instruction.
     *
     * @param btcINVOKE The instruction to convert.
     * @return The converted instruction.
     * @throws Report.InternalError If the return type of the method is invalid (should never happen).
     */
    private byte[] handleInstr(BtcINVOKE btcINVOKE) {
        ByteBuffer code = ByteBuffer.allocate(btcINVOKE.size());

        code.put((byte) btcINVOKE.opcode());

        switch (btcINVOKE.type) {
            case SPECIAL, STATIC, VIRTUAL -> {
                if (btcINVOKE.name.equals("putInt") || btcINVOKE.name.equals("putChar")) {
                    int printStreamIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("java/io/PrintStream"));
                    int printIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("print"));
                    int descriptorIndex;

                    if (btcINVOKE.name.equals("putInt")) {
                        descriptorIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("(J)V"));
                    } else {
                        descriptorIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("(C)V"));
                    }

                    int nameAndTypeIndex = currentClassFile.addConstPoolInfo(new EmtNameAndTypeInfo(printIndex, descriptorIndex));
                    int classIndex = currentClassFile.addConstPoolInfo(new EmtClassInfo(printStreamIndex));
                    int methodRefIndex = currentClassFile.addConstPoolInfo(new EmtMethodrefInfo(classIndex, nameAndTypeIndex));
                    code.putShort((short) methodRefIndex);
                } else if (btcINVOKE.name.contains(":")) {
                    String classUtf = btcINVOKE.name.split("\\.")[0];
                    String methodUtf = btcINVOKE.name.split("\\.")[1].split(":")[0];
                    String descriptorUtf = btcINVOKE.name.split(":")[1];

                    int classUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(classUtf));
                    int methodUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(methodUtf));
                    int descriptorUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(descriptorUtf));

                    int nameAndTypeIndex = currentClassFile.addConstPoolInfo(new EmtNameAndTypeInfo(methodUtfIndex, descriptorUtfIndex));
                    int classIndex = currentClassFile.addConstPoolInfo(new EmtClassInfo(classUtfIndex));
                    int methodRefIndex = currentClassFile.addConstPoolInfo(new EmtMethodrefInfo(classIndex, nameAndTypeIndex));
                    code.putShort((short) methodRefIndex);
                } else {
                    int classUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(currentClassFile.name));
                    int methodUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(btcINVOKE.name));

                    BtcMETHOD callee = BtcGen.btcClasses.peek().getMethod(btcINVOKE.name);

                    StringBuilder typeDescriptor = new StringBuilder("(");

                    for (BtcMETHOD.Type parType : callee.parTypes()) {
                        switch (parType) {
                            case INT -> typeDescriptor.append(TYPE_INT);
                            case FLOAT -> typeDescriptor.append(TYPE_FLOAT);
                            case LONG -> typeDescriptor.append(TYPE_LONG);
                            case DOUBLE -> typeDescriptor.append(TYPE_DOUBLE);
                            case BOOL -> typeDescriptor.append(TYPE_BOOLEAN);
                            case STRING -> typeDescriptor.append(TYPE_STRING);
                            // TODO: Fix for characters.
                            case ARRAY -> typeDescriptor.append(TYPE_OBJECT);
                            case OBJECT -> typeDescriptor.append(TYPE_OBJECT);
                        }
                    }

                    typeDescriptor.append(")");
                    typeDescriptor.append(switch (callee.ret) {
                        case INT -> TYPE_INT;
                        case FLOAT -> TYPE_FLOAT;
                        case LONG -> TYPE_LONG;
                        case DOUBLE -> TYPE_DOUBLE;
                        case BOOL -> TYPE_BOOLEAN;
                        case STRING -> TYPE_STRING;
                        // As per the PINS'22 specifications, arrays are not a valid return type.
                        case ARRAY -> throw new Report.InternalError();
                        case OBJECT -> TYPE_OBJECT;
                        case VOID -> TYPE_VOID;
                    });

                    int descriptorIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(typeDescriptor.toString()));
                    int nameAndTypeIndex = currentClassFile.addConstPoolInfo(new EmtNameAndTypeInfo(methodUtfIndex, descriptorIndex));
                    int classIndex = currentClassFile.addConstPoolInfo(new EmtClassInfo(classUtfIndex));
                    int methodRefIndex = currentClassFile.addConstPoolInfo(new EmtMethodrefInfo(classIndex, nameAndTypeIndex));
                    code.putShort((short) methodRefIndex);
                }
            }
            case DYNAMIC -> {
                // As per the JVM dynamic invoke specification, the instruction must be followed by 2 zero bytes.
                // JVM invokedynamic: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokedynamic
                code.put((byte) 0);
                code.put((byte) 0);
            }
            case INTERFACE -> {
                // As per the JVM interface invoke specification, the instruction must be followed by 1 zero byte.
                // JVM invokeinterface: https://docs.oracle.com/javase/specs/jvms/se7/html/jvms-6.html#jvms-6.5.invokeinterface
                code.put((byte) 0);
            }
        }
        return code.array();
    }

    /**
     * Handles the conversion of the {@link BtcNEWARRAY} instruction.
     *
     * @param btcNEWARRAY The instruction to handle.
     * @return The bytecode of the instruction.
     */
    private byte[] handleInstr(BtcNEWARRAY btcNEWARRAY) {
        ByteBuffer code = ByteBuffer.allocate(btcNEWARRAY.size());

        code.put((byte) btcNEWARRAY.opcode());

        int arrType = switch (btcNEWARRAY.type) {
            case BOOLEAN -> 4;
            case CHAR -> 5;
            case FLOAT -> 6;
            case DOUBLE -> 7;
            case BYTE -> 8;
            case SHORT -> 9;
            case INT -> 10;
            case LONG -> 11;
            case REF -> throw new Report.InternalError();
        };

        code.put((byte) arrType);

        return code.array();
    }

    /**
     * Handles the conversion of the {@link BtcMULTIANEWARRAY} instruction.
     *
     * @param btcMULTIANEWARRAY The instruction to handle.
     * @return The bytecode of the instruction.
     */
    private byte[] handleInstr(BtcMULTIANEWARRAY btcMULTIANEWARRAY) {
        ByteBuffer code = ByteBuffer.allocate(btcMULTIANEWARRAY.size());

        code.put((byte) btcMULTIANEWARRAY.opcode());

        StringBuilder classValue = new StringBuilder();

        classValue.append(TYPE_ARRAY.repeat(Math.max(0, btcMULTIANEWARRAY.dimensions)));

        switch (btcMULTIANEWARRAY.type) {
            case BOOLEAN -> classValue.append(TYPE_BOOLEAN);
            case CHAR -> classValue.append(TYPE_CHAR);
            case FLOAT -> classValue.append(TYPE_FLOAT);
            case DOUBLE -> classValue.append(TYPE_DOUBLE);
            case BYTE -> classValue.append(TYPE_BYTE);
            case SHORT -> classValue.append(TYPE_SHORT);
            case INT -> classValue.append(TYPE_INT);
            case LONG -> classValue.append(TYPE_LONG);
        }

        int classUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(classValue.toString()));
        int classInfoIndex = currentClassFile.addConstPoolInfo(new EmtClassInfo(classUtfIndex));

        code.putShort((short) classInfoIndex);
        code.put((byte) btcMULTIANEWARRAY.dimensions);

        return code.array();
    }

    private byte[] handleInstr(BtcNEW btcNEW) {
        ByteBuffer code = ByteBuffer.allocate(btcNEW.size());

        code.put((byte) btcNEW.opcode());

        int classUtfIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(btcNEW.className));
        int classInfoIndex = currentClassFile.addConstPoolInfo(new EmtClassInfo(classUtfIndex));

        code.putShort((short) classInfoIndex);

        return code.array();
    }

    // Flow control instructions.

    /**
     * Handles the conversion of the {@link BtcCJUMP} instruction.
     * @param btcCJUMP The instruction to handle.
     * @return The bytecode of the instruction.
     */
    private byte[] handleInstr(BtcCJUMP btcCJUMP) {
        ByteBuffer code = ByteBuffer.allocate(btcCJUMP.size());

        code.put((byte) btcCJUMP.opcode());
        code.putShort((short) (btcCJUMP.target - btcCJUMP.index));

        return code.array();
    }

    /**
     * Handles the conversion of the {@link BtcGOTO} instruction.
     *
     * @param btcGOTO The instruction to handle.
     * @return The bytecode of the instruction.
     */
    private byte[] handleInstr(BtcGOTO btcGOTO) {
        ByteBuffer code = ByteBuffer.allocate(btcGOTO.size());

        final int WIDE_THRESHOLD = 0xffff;

        code.put((byte) btcGOTO.opcode());

        if (btcGOTO.target < WIDE_THRESHOLD) {
            code.putShort((short) (btcGOTO.target - btcGOTO.index));
        } else {
            code.putInt(btcGOTO.target - btcGOTO.index);
        }

        return code.array();
    }

    // Generic handler.

    /**
     * The generic handler for instructions that do not require any special handling.
     *
     * @param btcInstr The instruction to handle.
     * @return The bytecode of the instruction.
     */
    private byte[] handleInstr(BtcInstr btcInstr) {
        ByteBuffer code = ByteBuffer.allocate(btcInstr.size());

        code.put((byte) btcInstr.opcode());

        return code.array();
    }

}
