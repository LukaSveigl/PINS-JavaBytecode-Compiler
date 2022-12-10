package pins.phase.imcgen;

import java.util.*;

import pins.data.ast.*;
import pins.data.ast.visitor.*;
import pins.data.imc.code.expr.ImcExpr;
import pins.data.imc.code.expr.ImcTEMP;
import pins.data.imc.code.stmt.ImcMOVE;
import pins.data.mem.*;
import pins.phase.memory.*;

/**
 * Intermediate code generator.
 */
public class CodeGenerator extends AstFullVisitor<Object, Stack<MemFrame>> {

    /**
     * The general purpose visit method, which traverses the abstract syntax trees.
     *
     * @param trees The abstract syntax trees to traverse.
     * @param frames The stack of memory frames.
     * @return The abstract syntax trees.
     */
	@Override
    public Object visit(ASTs<?> trees, Stack<MemFrame> frames) {
        if (frames == null) {
            frames = new Stack<MemFrame>();
        }

        for (AST tree : trees.asts()) {
            tree.accept(this, frames);
        }
        return trees;
    }

    /**
     * The visit method for function declarations, which serve as entry points for the code generator.
     *
     * @param funDecl The function declaration to visit.
     * @param frames The stack of memory frames.
     * @return The function declaration.
     */
    @Override
    public Object visit(AstFunDecl funDecl, Stack<MemFrame> frames) {
        // Add current function frame to the stack of memory frames.
        frames.push(Memory.frames.get(funDecl));
        if (funDecl.expr != null) {
            funDecl.expr.accept(new ExprGenerator(), frames);
        }
        // After the function body has been generated, remove the current function frame from the stack of memory frames.
        frames.pop();
        return funDecl;
    }
}
