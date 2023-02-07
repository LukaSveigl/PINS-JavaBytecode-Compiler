package pins.phase.btcgen;

/**
 * Bytecode class generator.
 */
public class ClassGenerator {

    /** The destination class name. */
    private final String dstClassName;

    /**
     * Constructs a new bytecode class generator.
     */
    public ClassGenerator(String dstFileName) {
        this.dstClassName = dstFileName.substring(0, dstFileName.indexOf('.'));
    }

    /**
     * Generates the class bytecode.
     */
    public void generate() {

    }

    /**
     * Generates the class signature.
     */
    private void generateSignature() {

    }

}
