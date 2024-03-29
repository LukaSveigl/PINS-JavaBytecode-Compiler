package pins.data.btc.method.instr;

import pins.common.logger.Loggable;
import pins.common.report.Report;

import java.util.Map;

/**
 * JVM bytecode instruction.
 */
public abstract class BtcInstr implements Loggable {

    /** The instruction index. */
    public final int index;

    /** The instruction opcode. */
    protected int opcode;

    /** The JVM instruction set opcodes. */
    protected static Map<String, Integer> opcodes = Map.<String, Integer>ofEntries(
            Map.entry("NOP", 0x00),
            Map.entry("ACONST_NULL", 0x01),
            Map.entry("ICONST_M1", 0x02),
            Map.entry("ICONST_0", 0x03),
            Map.entry("ICONST_1", 0x04),
            Map.entry("ICONST_2", 0x05),
            Map.entry("ICONST_3", 0x06),
            Map.entry("ICONST_4", 0x07),
            Map.entry("ICONST_5", 0x08),
            Map.entry("LCONST_0", 0x09),
            Map.entry("LCONST_1", 0x0a),
            Map.entry("FCONST_0", 0x0b),
            Map.entry("FCONST_1", 0x0c),
            Map.entry("FCONST_2", 0x0d),
            Map.entry("DCONST_0", 0x0e),
            Map.entry("DCONST_1", 0x0f),
            Map.entry("BIPUSH", 0x10),
            Map.entry("SIPUSH", 0x11),
            Map.entry("LDC", 0x12),
            Map.entry("LDC_W", 0x13),
            Map.entry("LDC2_W", 0x14),
            Map.entry("ILOAD", 0x15),
            Map.entry("LLOAD", 0x16),
            Map.entry("FLOAD", 0x17),
            Map.entry("DLOAD", 0x18),
            Map.entry("ALOAD", 0x19),
            Map.entry("ILOAD_0", 0x1a),
            Map.entry("ILOAD_1", 0x1b),
            Map.entry("ILOAD_2", 0x1c),
            Map.entry("ILOAD_3", 0x1d),
            Map.entry("LLOAD_0", 0x1e),
            Map.entry("LLOAD_1", 0x1f),
            Map.entry("LLOAD_2", 0x20),
            Map.entry("LLOAD_3", 0x21),
            Map.entry("FLOAD_0", 0x22),
            Map.entry("FLOAD_1", 0x23),
            Map.entry("FLOAD_2", 0x24),
            Map.entry("FLOAD_3", 0x25),
            Map.entry("DLOAD_0", 0x26),
            Map.entry("DLOAD_1", 0x27),
            Map.entry("DLOAD_2", 0x28),
            Map.entry("DLOAD_3", 0x29),
            Map.entry("ALOAD_0", 0x2a),
            Map.entry("ALOAD_1", 0x2b),
            Map.entry("ALOAD_2", 0x2c),
            Map.entry("ALOAD_3", 0x2d),
            Map.entry("IALOAD", 0x2e),
            Map.entry("LALOAD", 0x2f),
            Map.entry("FALOAD", 0x30),
            Map.entry("DALOAD", 0x31),
            Map.entry("AALOAD", 0x32),
            Map.entry("BALOAD", 0x33),
            Map.entry("CALOAD", 0x34),
            Map.entry("SALOAD", 0x35),
            Map.entry("ISTORE", 0x36),
            Map.entry("LSTORE", 0x37),
            Map.entry("FSTORE", 0x38),
            Map.entry("DSTORE", 0x39),
            Map.entry("ASTORE", 0x3a),
            Map.entry("ISTORE_0", 0x3b),
            Map.entry("ISTORE_1", 0x3c),
            Map.entry("ISTORE_2", 0x3d),
            Map.entry("ISTORE_3", 0x3e),
            Map.entry("LSTORE_0", 0x3f),
            Map.entry("LSTORE_1", 0x40),
            Map.entry("LSTORE_2", 0x41),
            Map.entry("LSTORE_3", 0x42),
            Map.entry("FSTORE_0", 0x43),
            Map.entry("FSTORE_1", 0x44),
            Map.entry("FSTORE_2", 0x45),
            Map.entry("FSTORE_3", 0x46),
            Map.entry("DSTORE_0", 0x47),
            Map.entry("DSTORE_1", 0x48),
            Map.entry("DSTORE_2", 0x49),
            Map.entry("DSTORE_3", 0x4a),
            Map.entry("ASTORE_0", 0x4b),
            Map.entry("ASTORE_1", 0x4c),
            Map.entry("ASTORE_2", 0x4d),
            Map.entry("ASTORE_3", 0x4e),
            Map.entry("IASTORE", 0x4f),
            Map.entry("LASTORE", 0x50),
            Map.entry("FASTORE", 0x51),
            Map.entry("DASTORE", 0x52),
            Map.entry("AASTORE", 0x53),
            Map.entry("BASTORE", 0x54),
            Map.entry("CASTORE", 0x55),
            Map.entry("SASTORE", 0x56),
            Map.entry("POP", 0x57),
            Map.entry("POP2", 0x58),
            Map.entry("DUP", 0x59),
            Map.entry("DUP_X1", 0x5a),
            Map.entry("DUP_X2", 0x5b),
            Map.entry("DUP2", 0x5c),
            Map.entry("DUP2_X1", 0x5d),
            Map.entry("DUP2_X2", 0x5e),
            Map.entry("SWAP", 0x5f),
            Map.entry("IADD", 0x60),
            Map.entry("LADD", 0x61),
            Map.entry("FADD", 0x62),
            Map.entry("DADD", 0x63),
            Map.entry("ISUB", 0x64),
            Map.entry("LSUB", 0x65),
            Map.entry("FSUB", 0x66),
            Map.entry("DSUB", 0x67),
            Map.entry("IMUL", 0x68),
            Map.entry("LMUL", 0x69),
            Map.entry("FMUL", 0x6a),
            Map.entry("SMUL", 0x6b),
            Map.entry("IDIV", 0x6c),
            Map.entry("LDIV", 0x6d),
            Map.entry("FDIV", 0x6e),
            Map.entry("DDIV", 0x6f),
            Map.entry("IREM", 0x70),
            Map.entry("LREM", 0x71),
            Map.entry("FREM", 0x72),
            Map.entry("DREM", 0x73),
            Map.entry("INEG", 0x74),
            Map.entry("LNEG", 0x75),
            Map.entry("FNEG", 0x76),
            Map.entry("DNEG", 0x77),
            Map.entry("ISHL", 0x78),
            Map.entry("LSHL", 0x79),
            Map.entry("ISHR", 0x7a),
            Map.entry("LSHR", 0x7b),
            Map.entry("IUSHR", 0x7c),
            Map.entry("LUSHR", 0x7d),
            Map.entry("IAND", 0x7e),
            Map.entry("LAND", 0x7f),
            Map.entry("IOR", 0x80),
            Map.entry("LOR", 0x81),
            Map.entry("IXOR", 0x82),
            Map.entry("LXOR", 0x83),
            Map.entry("IINC", 0x84),
            Map.entry("I2L", 0x85),
            Map.entry("I2F", 0x86),
            Map.entry("I2D", 0x87),
            Map.entry("L2I", 0x88),
            Map.entry("L2F", 0x89),
            Map.entry("L2D", 0x8a),
            Map.entry("F2I", 0x8b),
            Map.entry("F2L", 0x8c),
            Map.entry("F2D", 0x8d),
            Map.entry("D2I", 0x8e),
            Map.entry("D2L", 0x8f),
            Map.entry("D2F", 0x90),
            Map.entry("I2B", 0x91),
            Map.entry("I2C", 0x92),
            Map.entry("I2S", 0x93),
            Map.entry("LCMP", 0x94),
            Map.entry("FCMPL", 0x95),
            Map.entry("FCMPG", 0x96),
            Map.entry("DCMPL", 0x97),
            Map.entry("DCMPG", 0x98),
            Map.entry("IFEQ", 0x99),
            Map.entry("IFNE", 0x9a),
            Map.entry("IFLT", 0x9b),
            Map.entry("IFGE", 0x9c),
            Map.entry("IFGT", 0x9d),
            Map.entry("IFLE", 0x9e),
            Map.entry("IF_ICMPEQ", 0x9f),
            Map.entry("IF_ICMPNE", 0xa0),
            Map.entry("IF_ICMPLT", 0xa1),
            Map.entry("IF_ICMPGE", 0xa2),
            Map.entry("IF_ICMPGT", 0xa3),
            Map.entry("IF_ICMPLE", 0xa4),
            Map.entry("IF_ACMPEQ", 0xa5),
            Map.entry("IF_ACMPNE", 0xa6),
            Map.entry("GOTO", 0xa7),
            Map.entry("JSR", 0xa8),
            Map.entry("RET", 0xa9),
            Map.entry("TABLESWITCH", 0xaa),
            Map.entry("LOOKUPSWITCH", 0xab),
            Map.entry("IRETURN", 0xac),
            Map.entry("LRETURN", 0xad),
            Map.entry("FRETURN", 0xae),
            Map.entry("DRETURN", 0xaf),
            Map.entry("ARETURN", 0xb0),
            Map.entry("RETURN", 0xb1),
            Map.entry("GETSTATIC", 0xb2),
            Map.entry("PUTSTATIC", 0xb3),
            Map.entry("GETFIELD", 0xb4),
            Map.entry("PUTFIELD", 0xb5),
            Map.entry("INVOKEVIRTUAL", 0xb6),
            Map.entry("INVOKESPECIAL", 0xb7),
            Map.entry("INVOKESTATIC", 0xb8),
            Map.entry("INVOKEINTERFACE", 0xb9),
            Map.entry("INVOKEDYNAMIC", 0xba),
            Map.entry("NEW", 0xbb),
            Map.entry("NEWARRAY", 0xbc),
            Map.entry("ANEWARRAY", 0xbd),
            Map.entry("ARRAYLENGTH", 0xbe),
            Map.entry("ATHROW", 0xbf),
            Map.entry("CHECKCAST", 0xc0),
            Map.entry("INSTANCEOF", 0xc1),
            Map.entry("MONITORENTER", 0xc2),
            Map.entry("MONITOREXIT", 0xc3),
            Map.entry("WIDE", 0xc4),
            Map.entry("MULTIANEWARRAY", 0xc5),
            Map.entry("IFNULL", 0xc6),
            Map.entry("IFNONNULL", 0xc7),
            Map.entry("GOTO_W", 0xc8),
            Map.entry("JSR_W", 0xc9)
    );

    protected BtcInstr(int index) {
        this.index = index;
    }

    /**
     * Gets the instruction name from the opcode.
     *
     * @param opcode The opcode of the instruction to find.
     * @return The instruction name.
     * @throws Report.InternalError
     */
    protected static String getInstructionFromOpcode(int opcode) {
        for (Map.Entry<String, Integer> entry : BtcInstr.opcodes.entrySet()) {
            if (entry.getValue().equals(opcode)) {
                return entry.getKey();
            }
        }
        throw new Report.InternalError();
    }

    /**
     * Returns the opcode of the instruction.
     *
     * @return The opcode of the instruction.
     */
    public int opcode() {
        return this.opcode;
    }

    /**
     * Gets the instruction size in bytes (instruction + optional arguments).
     *
     * @return The instruction size in bytes.
     */
    public abstract int size();

    @Override
    public abstract void log(String pfx);

}
