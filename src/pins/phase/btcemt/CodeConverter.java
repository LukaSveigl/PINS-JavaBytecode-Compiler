package pins.phase.btcemt;

import pins.common.report.Report;
import pins.data.btc.BtcCLASS;
import pins.data.btc.method.BtcMETHOD;
import pins.data.btc.method.instr.BtcInstr;
import pins.data.btc.method.instr.stack.*;
import pins.data.btc.method.instr.object.*;
import pins.data.btc.method.instr.ctrl.*;
import pins.data.btc.method.instr.arithm.*;
import pins.data.btc.var.BtcFIELD;
import pins.data.btc.var.BtcLOCAL;
import pins.data.emt.EmtClassFile;
import pins.data.emt.attr.EmtAttributeInfo;
import pins.data.emt.constp.*;
import pins.data.emt.field.EmtFieldInfo;
import pins.data.emt.method.EmtMethodInfo;
import pins.data.mem.MemRelAccess;
import pins.phase.btcgen.BtcGen;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Vector;

public class CodeConverter {

    private EmtClassFile currentClassFile;

    public void convert() {
        for (BtcCLASS btcCLASS : BtcGen.btcClasses) {
            convertClass(btcCLASS);
        }
    }

    private void convertClass(BtcCLASS btcCLASS) {
        currentClassFile = new EmtClassFile(btcCLASS.name, btcCLASS.dstName);
        BtcEmt.classFiles.put(btcCLASS, currentClassFile);
        // Convert class fields.
        // Generate const pool etc.
        for (BtcFIELD field : btcCLASS.fields().values()) {
            convertField(field);
        }
        for (BtcMETHOD method : btcCLASS.methods()) {
            convertMethod(method);
        }

        // Maybe minimize const pool.
    }

    private void convertField(BtcFIELD field) {
        // Generate the field flags.
        Vector<EmtFieldInfo.AccessFlag> flags = new Vector<>();
        for (BtcFIELD.Flags flag : field.flags()) {
            switch (flag) {
                case PUBLIC -> flags.add(EmtFieldInfo.AccessFlag.PUBLIC);
                case PRIVATE -> flags.add(EmtFieldInfo.AccessFlag.PRIVATE);
                case PROTECTED -> flags.add(EmtFieldInfo.AccessFlag.PROTECTED);
                case STATIC -> flags.add(EmtFieldInfo.AccessFlag.STATIC);
                case FINAL -> flags.add(EmtFieldInfo.AccessFlag.FINAL);
                case VOLATILE -> flags.add(EmtFieldInfo.AccessFlag.VOLATILE);
                case TRANSIENT -> flags.add(EmtFieldInfo.AccessFlag.TRANSIENT);
                case SYNTHETIC -> flags.add(EmtFieldInfo.AccessFlag.SYNTHETIC);
                case ENUM -> flags.add(EmtFieldInfo.AccessFlag.ENUM);
            }
        }

        // Generate the field name.
        int nameIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(field.name));

        // Generate the field descriptor.
        String typeDescriptor = "";
        switch (field.type) {
            case INT -> typeDescriptor = "I";
            case FLOAT -> typeDescriptor = "F";
            case LONG -> typeDescriptor = "J";
            case DOUBLE -> typeDescriptor = "D";
            case BOOL -> typeDescriptor = "Z";
            case STRING -> typeDescriptor = "Ljava/lang/String;";
            // TODO: Fix arrays to include multiple dimensions.
            case ARRAY -> typeDescriptor = "[" + field.subType;
            case OBJECT -> typeDescriptor = "L" + field.subType + ";";
        }

        int descriptorIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(typeDescriptor));
        currentClassFile.addFieldInfo(new EmtFieldInfo(flags, nameIndex, descriptorIndex, 0, null));
        EmtNameAndTypeInfo nameAndTypeInfo = new EmtNameAndTypeInfo(nameIndex, descriptorIndex);
        int nameAndTypeIndex = currentClassFile.addConstPoolInfo(nameAndTypeInfo);
        EmtFieldrefInfo fieldrefInfo = new EmtFieldrefInfo(currentClassFile.thisClassIndex, nameAndTypeIndex);
        currentClassFile.addConstPoolInfo(fieldrefInfo);
        BtcEmt.fieldRefs.put(field, fieldrefInfo);
    }

    private void convertMethod(BtcMETHOD method) {
        // Convert method.

        Vector<EmtMethodInfo.AccessFlag> flags = new Vector<>();
        for (BtcMETHOD.Flags flag : method.flags()) {
            switch (flag) {
                case PUBLIC -> flags.add(EmtMethodInfo.AccessFlag.PUBLIC);
                case PRIVATE -> flags.add(EmtMethodInfo.AccessFlag.PRIVATE);
                case PROTECTED -> flags.add(EmtMethodInfo.AccessFlag.PROTECTED);
                case STATIC -> flags.add(EmtMethodInfo.AccessFlag.STATIC);
                case FINAL -> flags.add(EmtMethodInfo.AccessFlag.FINAL);
                case SYNCHRONIZED -> flags.add(EmtMethodInfo.AccessFlag.SYNCHRONIZED);
                case NATIVE -> flags.add(EmtMethodInfo.AccessFlag.NATIVE);
                case ABSTRACT -> flags.add(EmtMethodInfo.AccessFlag.ABSTRACT);
                case STRICT -> flags.add(EmtMethodInfo.AccessFlag.STRICT);
                case SYNTHETIC -> flags.add(EmtMethodInfo.AccessFlag.SYNTHETIC);
            }
        }

        // Generate the method name.
        int nameIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(method.name));

        String typeDescriptor = "(";
        for (BtcMETHOD.Type parType : method.parTypes()) {
            switch (parType) {
                case INT -> typeDescriptor += "I";
                case FLOAT -> typeDescriptor += "F";
                case LONG -> typeDescriptor += "J";
                case DOUBLE -> typeDescriptor += "D";
                case BOOL -> typeDescriptor += "Z";
                case STRING -> typeDescriptor += "Ljava/lang/String;";
            }
        }

        typeDescriptor += ")" + switch (method.ret) {
            case INT -> "I";
            case FLOAT -> "F";
            case LONG -> "J";
            case DOUBLE -> "D";
            case BOOL -> "Z";
            case STRING -> "Ljava/lang/String;";
            // TODO: Fix arrays and pointers.
            //case ARRAY -> "[" + method.retSubType;
            //case OBJECT -> "L" + method.retSubType + ";";
            case VOID -> "V";
            default -> throw new Report.InternalError();
        };

        int descriptorIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info(typeDescriptor));

        EmtNameAndTypeInfo nameAndTypeInfo = new EmtNameAndTypeInfo(nameIndex, descriptorIndex);

        int nameAndTypeIndex = currentClassFile.addConstPoolInfo(nameAndTypeInfo);

        EmtMethodrefInfo methodrefInfo = new EmtMethodrefInfo(currentClassFile.thisClassIndex, nameAndTypeIndex);

        currentClassFile.addConstPoolInfo(methodrefInfo);

        BtcEmt.methodRefs.put(method, methodrefInfo);

        // Generate the method code.

        int codeAttributeIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("Code"));

        int instrSize = 0;
        for (BtcInstr instr : method.instrs()) {
            instrSize += instr.size();
        }

        System.out.println("Method " + method.name + " has " + instrSize + " instructions.");

        ByteBuffer code = ByteBuffer.allocate(instrSize + 2 + 4 + 2 + 2 + 2);

        code.putShort((short) 256); // Max stack size.
        code.putShort((short) 256); // Max locals.
        code.putInt(instrSize); // Code length.

        int codeSize = 0;

        for (BtcInstr instr : method.instrs()) { // Code.
            byte[] instrCode = convertInstruction(instr);
            //code.put(convertInstruction(instr));
            code.put(instrCode);
            codeSize += instrCode.length;
        }

        System.out.println("Code size: " + codeSize);

        code.putShort((short) 0); // Exception table length.
        code.putShort((short) 0); // Attributes count.

        System.out.println("Method name: " + method.name);
        int i = 0;
        for (byte b : code.array()) {
            System.out.print(Byte.toUnsignedInt(b) + " ");
            if (i % 10 == 0 && i != 0) {
                System.out.println();
            }
            i++;
        }
        System.out.println();

        EmtAttributeInfo codeAttribute = new EmtAttributeInfo(codeAttributeIndex, code.array().length, code.array());

        Vector<EmtAttributeInfo> attributes = new Vector<>();
        attributes.add(codeAttribute);

        // Generate the stackmaptable attribute.
        EmtAttributeInfo stackMapTable = generateStackMapTable(method);
        if (stackMapTable != null) {
            attributes.add(stackMapTable);
        }

        System.out.println("Method: " + method.name);
        System.out.println("Instruction size: " + method.instrCount());

        currentClassFile.addMethodInfo(new EmtMethodInfo(flags, nameIndex, descriptorIndex, attributes.size(), attributes));
    }

    private byte[] convertInstruction(BtcInstr instr) {
        ByteBuffer code = ByteBuffer.allocate(instr.size());
        // Convert instruction.
        // Stack instructions.
        if (instr instanceof BtcCONST) {
            code.put((byte) instr.opcode());
        } else if (instr instanceof BtcDUP) {
            code.put((byte) instr.opcode());
        } else if (instr instanceof BtcLDC) {
            if (((BtcLDC) instr).type == BtcLDC.Type.DEFAULT) {
                int index = currentClassFile.addConstPoolInfo(new EmtIntegerInfo((int) ((BtcLDC) instr).value));
                if (index < 0xff) {
                    code = ByteBuffer.allocate(2);
                    code.put((byte) 0x12);
                    code.put((byte) index);
                    System.out.println("LDC " + index);
                } else {
                    code = ByteBuffer.allocate(3);
                    code.put((byte) 0x13);
                    code.putShort((short) index);
                    System.out.println("LDC_W " + index);
                }
            } else {
                code = ByteBuffer.allocate(3);
                int index = currentClassFile.addConstPoolInfo(new EmtLongInfo(((BtcLDC) instr).value));
                code.put((byte) 0x14);
                code.putShort((short) index);
                System.out.println("LDC2_W " + index);
            }
        } else if (instr instanceof BtcLOAD) {
            code.put((byte) instr.opcode());
            code.put((byte) ((BtcLOAD) instr).index);
        } else if (instr instanceof BtcPOP) {
            code.put((byte) instr.opcode());
        } else if (instr instanceof BtcPUSH) {
            if (((BtcPUSH) instr).type == BtcPUSH.Type.BYTE) {
                code.put((byte) instr.opcode());
                code.put((byte) ((BtcPUSH) instr).value);
            } else {
                code.put((byte) instr.opcode());
                code.putShort((short) ((BtcPUSH) instr).value);
            }
        } else if (instr instanceof BtcSTORE) {
            code.put((byte) instr.opcode());
            code.put((byte) ((BtcSTORE) instr).index);
        } else if (instr instanceof BtcSWAP) {
            code.put((byte) instr.opcode());
        }
        // Object instructions.
        else if (instr instanceof BtcACCESS) {

        } else if (instr instanceof BtcALOAD) {

        } else if (instr instanceof BtcASTORE) {

        } else if (instr instanceof BtcINVOKE) {

        } else if (instr instanceof BtcNEWARRAY) {

        } else if (instr instanceof BtcMULTIANEWARRAY) {

        }
        // Control flow instructions.
        else if (instr instanceof BtcCJUMP) {
            code.put((byte) instr.opcode());
            code.putShort((short) (((BtcCJUMP) instr).target - instr.index));
            System.out.println("Generating CJUMP " + instr.opcode() + " to " + ((BtcCJUMP) instr).target);
            System.out.println("Instr index: " + instr.index);
            System.out.println("Instr size: " + instr.size());
            System.out.println("Short target: " + (short)(((BtcCJUMP) instr).target));
            for (byte b : code.array()) {
                System.out.print(b + " ");
            }
            System.out.println();
        } else if (instr instanceof BtcGOTO) {
            code.put((byte) instr.opcode());
            if (((BtcGOTO) instr).target < 0xffff) {
                code.putShort((short) (((BtcGOTO) instr).target - instr.index));
            } else {
                code.putInt(((BtcGOTO) instr).target - instr.index);
            }
        } else if (instr instanceof BtcRETURN) {
            code.put((byte) instr.opcode());
        }
        // Arithmetic instructions.
        else if (instr instanceof BtcARITHM) {
            code.put((byte) instr.opcode());
        } else if (instr instanceof BtcBITWISE) {
            code.put((byte) instr.opcode());
        } else if (instr instanceof BtcCMP) {
            code.put((byte) instr.opcode());
        } else if (instr instanceof BtcCAST) {
            code.put((byte) instr.opcode());
        }
        return code.array();
    }

    private EmtAttributeInfo generateStackMapTable(BtcMETHOD method) {
        final int stackMapTableIndex = currentClassFile.addConstPoolInfo(new EmtUTF8Info("StackMapTable"));

        int stackMapTableLength = 0;
        int stackMapFrameLength = 0;

        Vector<ByteBuffer> stackMapFrames = new Vector<>();

        boolean methodHasLocals = method.locals().size() != method.parTypes().size();
        boolean firstFrame = true;

        int currentOffset = 0;

        for (BtcInstr instr : method.instrs()) {
            if (instr instanceof BtcCJUMP) {
                stackMapFrameLength++;
                if (firstFrame) {
                    firstFrame = false;
                    if (methodHasLocals) {
                        Vector<BtcMETHOD.Type> localTypes = method.locTypes();

                        // Generate append_frame
                        if (localTypes.size() < 4) {
                            stackMapFrames.add(ByteBuffer.allocate(1 + // frame_type
                                                                   2 + // offset_delta
                                                                   localTypes.size() // locals
                            ));

                            System.out.println("Generating append_frame " + ((BtcCJUMP) instr).target);

                            stackMapFrames.lastElement().put((byte) (251 + localTypes.size()));
                            stackMapFrames.lastElement().putShort((short) ((BtcCJUMP) instr).target);
                            for (BtcMETHOD.Type type : localTypes) {
                                switch (type) {
                                    case INT -> stackMapFrames.lastElement().put((byte) 1);
                                    case LONG -> stackMapFrames.lastElement().put((byte) 4);
                                    case FLOAT -> stackMapFrames.lastElement().put((byte) 2);
                                    case DOUBLE -> stackMapFrames.lastElement().put((byte) 3);
                                }
                            }

                            currentOffset = ((BtcCJUMP) instr).target;
                        }
                        // Generate full_frame
                        else {
                            stackMapFrames.add(ByteBuffer.allocate(1 +  // frame_type
                                                                   2 +  // offset_delta
                                                                   2 +  // number_of_locals
                                                                   localTypes.size() +  // locals
                                                                   2 // number_of_stack_items
                                                                   // stack
                            ));

                            System.out.println("Generating full_frame " + ((BtcCJUMP) instr).target);

                            stackMapFrames.lastElement().put((byte) 255);
                            stackMapFrames.lastElement().putShort((short) ((BtcCJUMP) instr).target);
                            stackMapFrames.lastElement().putShort((short) localTypes.size());
                            for (BtcMETHOD.Type type : localTypes) {
                                switch (type) {
                                    case INT -> stackMapFrames.lastElement().put((byte) 1);
                                    case LONG -> stackMapFrames.lastElement().put((byte) 4);
                                    case FLOAT -> stackMapFrames.lastElement().put((byte) 2);
                                    case DOUBLE -> stackMapFrames.lastElement().put((byte) 3);
                                }
                            }
                            stackMapFrames.lastElement().putShort((short) 0);

                        }
                    } else {
                        stackMapFrames.add(ByteBuffer.allocate(1));
                        stackMapFrames.lastElement().put((byte) ((BtcCJUMP) instr).target);
                    }
                } else {
                    stackMapFrames.add(ByteBuffer.allocate(1));
                    stackMapFrames.lastElement().put((byte) ((BtcCJUMP) instr).target);
                }
            } else if (instr instanceof BtcGOTO) {
                stackMapFrameLength++;
                if (firstFrame) {
                    firstFrame = false;
                    if (methodHasLocals) {
                        Vector<BtcMETHOD.Type> localTypes = method.locTypes();

                        // Generate append_frame
                        if (localTypes.size() < 4) {
                            stackMapFrames.add(ByteBuffer.allocate(1 + // frame_type
                                                                   2 + // offset_delta
                                                                   localTypes.size() // locals
                            ));
                            stackMapFrames.lastElement().put((byte) (251 + localTypes.size()));
                            stackMapFrames.lastElement().putShort((short) ((BtcGOTO) instr).target);
                            for (BtcMETHOD.Type type : localTypes) {
                                switch (type) {
                                    case INT -> stackMapFrames.lastElement().put((byte) 1);
                                    case LONG -> stackMapFrames.lastElement().put((byte) 4);
                                    case FLOAT -> stackMapFrames.lastElement().put((byte) 2);
                                    case DOUBLE -> stackMapFrames.lastElement().put((byte) 3);
                                }
                            }
                        }
                        // Generate full_frame
                        else {
                            stackMapFrames.add(ByteBuffer.allocate(1 +  // frame_type
                                                                   2 +  // offset_delta
                                                                   2 +  // number_of_locals
                                                                   localTypes.size() +  // locals
                                                                   2 // number_of_stack_items
                                                                   // stack
                            ));
                            stackMapFrames.lastElement().put((byte) 255);
                            stackMapFrames.lastElement().putShort((short) ((BtcGOTO) instr).target);
                            stackMapFrames.lastElement().putShort((short) localTypes.size());
                            for (BtcMETHOD.Type type : localTypes) {
                                switch (type) {
                                    case INT -> stackMapFrames.lastElement().put((byte) 1);
                                    case LONG -> stackMapFrames.lastElement().put((byte) 4);
                                    case FLOAT -> stackMapFrames.lastElement().put((byte) 2);
                                    case DOUBLE -> stackMapFrames.lastElement().put((byte) 3);
                                }
                            }
                            stackMapFrames.lastElement().putShort((short) 0);
                        }
                    } else {
                        stackMapFrames.add(ByteBuffer.allocate(1));
                        stackMapFrames.lastElement().put((byte) ((BtcGOTO) instr).target);
                    }
                } else {
                    stackMapFrames.add(ByteBuffer.allocate(1));
                    stackMapFrames.lastElement().put((byte) ((BtcGOTO) instr).target);
                }
            }
        }

        if (stackMapFrames.size() == 0) {
            return null;
        }

        int length = 0;
        for (ByteBuffer stackMapFrame : stackMapFrames) {
            length += stackMapFrame.array().length;
        }

        ByteBuffer stackMapTableBuffer = ByteBuffer.allocate(2 + // number_of_entries
                                                             length // entries
        );

        stackMapTableBuffer.putShort((short) stackMapFrameLength);
        for (ByteBuffer stackMapFrame : stackMapFrames) {
            stackMapTableBuffer.put(stackMapFrame.array());
        }

        System.out.println("Method: " + method.name);
        System.out.println("StackMapTable: " + stackMapTableBuffer.array().length);
        System.out.println("Frames: " + stackMapFrames.size());
        System.out.println("Length: " + length);

        EmtAttributeInfo stackMapTable = new EmtAttributeInfo(stackMapTableIndex, stackMapTableBuffer.array().length, stackMapTableBuffer.array());
        return stackMapTable;
    }

}
