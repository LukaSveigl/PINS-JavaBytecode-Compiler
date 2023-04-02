package pins.phase.btcgen;

import pins.data.ast.AST;
import pins.data.ast.ASTs;
import pins.data.ast.AstFunDecl;
import pins.data.ast.AstVarDecl;
import pins.data.ast.visitor.AstVisitor;
import pins.data.btc.BtcClass;
import pins.data.mem.MemAbsAccess;
import pins.data.mem.MemAccess;
import pins.phase.memory.Memory;

/**
 * Bytecode class generator.
 */
public class ClassGenerator implements AstVisitor<BtcClass, Void> {

    /** The destination class name. */
    private final String dstClassName;

    /** The current bytecode class. */
    private final BtcClass currClass;

    /**
     * Constructs a new bytecode class generator.
     */
    public ClassGenerator(String dstFileName) {
        //this.dstClassName = dstFileName.substring(0, dstFileName.indexOf('.'));
        this.dstClassName = dstFileName.substring(dstFileName.lastIndexOf("/") + 1, dstFileName.lastIndexOf("."));
        System.out.println("Generating bytecode for class " + this.dstClassName);
        this.currClass = BtcGen.btcClasses.push(new BtcClass(dstClassName));
    }

    /**
     * General purpose visit method that traverses the ASTs. It generates the class fields and methods.
     *
     * @param asts   The ASTs to visit.
     * @param visArg The visitor argument.
     * @return The result of the visit.
     */
    @Override
    public BtcClass visit(ASTs<?> asts, Void visArg) {
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
