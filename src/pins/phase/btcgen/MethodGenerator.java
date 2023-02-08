package pins.phase.btcgen;

import pins.data.lin.LinCodeChunk;
import pins.phase.imclin.ImcLin;

/**
 * Bytecode method generator.
 */
public class MethodGenerator {

    /**
     * Constructs a new bytecode method generator.
     */
    public MethodGenerator() {

    }

    /**
     * Generates bytecode for all code chunks.
     */
    public void generate() {
        for (LinCodeChunk chunk : ImcLin.codeChunks()) {
            generateMethod(chunk);
        }
    }

    /**
     * Generates the bytecode for a method.
     * @param chunk The linearized code chunk.
     */
    private void generateMethod(LinCodeChunk chunk) {

    }

}
