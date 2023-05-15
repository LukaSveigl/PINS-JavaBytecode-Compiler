package pins.phase.btcgen;

import pins.data.ast.AST;
import pins.data.ast.ASTs;
import pins.data.ast.AstFunDecl;
import pins.data.ast.AstVarDecl;
import pins.data.ast.visitor.AstVisitor;
import pins.data.btc.BtcCLASS;
import pins.data.mem.MemAbsAccess;
import pins.data.mem.MemAccess;
import pins.phase.memory.Memory;

/**
 * Bytecode class generator.
 */
public class ClassGenerator implements AstVisitor<BtcCLASS, Void> {

    /** The destination class name. */
    private final String dstClassName;

    /** The current bytecode class. */
    private final BtcCLASS currClass;

    /**
     * Constructs a new bytecode class generator.
     *
     * @param dstFileName The destination file name.
     */
    public ClassGenerator(String dstFileName) {
        this.dstClassName = dstFileName.substring(dstFileName.lastIndexOf("/") + 1, dstFileName.lastIndexOf("."));
        this.currClass = BtcGen.btcClasses.push(new BtcCLASS(dstClassName, dstFileName.substring(dstFileName.indexOf(
                "="), dstFileName.lastIndexOf("."))));
    }

    /**
     * General purpose visit method that traverses the ASTs. It generates the class fields and methods.
     *
     * @param asts   The ASTs to visit.
     * @param visArg The visitor argument.
     * @return The result of the visit.
     */
    @Override
    public BtcCLASS visit(ASTs<?> asts, Void visArg) {
        // First: Generate the class fields.
        for (AST ast : asts.asts()) {
            if (ast instanceof AstVarDecl) {
                MemAccess access = Memory.varAccesses.get(ast);
                if (access instanceof MemAbsAccess) {
                    ast.accept(new FieldGenerator(), this.currClass);
                }
            }
        }
        // Second: Generate the class methods.
        for (AST ast : asts.asts()) {
            if (ast instanceof AstFunDecl) {
                ast.accept(new MethodGenerator(), null);
            }
        }
        return this.currClass;
    }

}
