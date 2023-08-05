package pins.phase.refan;

import pins.data.ast.*;
import pins.data.ast.visitor.AstFullVisitor;
import pins.data.mem.MemAbsAccess;
import pins.data.mem.MemAccess;
import pins.data.mem.MemRelAccess;
import pins.phase.memory.Memory;
import pins.phase.seman.SemAn;

import java.util.Stack;

/**
 * Reference evaluator.
 */
public class RefEvaluator extends AstFullVisitor<Object, RefEvaluator.RefDepth> {

    /**
     * Reference depth.
     */
    public class RefDepth {
        /** The depth that represents how many nested pointers point to a variable. */
        public int depth = 0;

    }

    /** Stack of function declarations. */
    public Stack<AstFunDecl> funStack = new Stack<>();

    /**
     * General purpose visit method, which traverses the abstract syntax trees.
     *
     * @param trees The abstract syntax trees to traverse.
     * @param refDepth The current reference depth.
     * @return The result of the traversal.
     */
    @Override
    public Object visit(ASTs<?> trees, RefDepth refDepth) {
        for (AST tree : trees.asts()) {

            refDepth = new RefDepth();

            tree.accept(this, refDepth);
        }
        return trees;
    }

    // DECLARATIONS

    /**
     * Visit method for function declarations.
     *
     * @param funDecl The function declaration.
     * @param refDepth The current reference depth.
     * @return The result of the traversal.
     */
    @Override
    public Object visit(AstFunDecl funDecl, RefDepth refDepth) {

        funStack.push(funDecl);

        funDecl.pars.accept(this, refDepth);
        funDecl.expr.accept(this, refDepth);

        funStack.pop();

        return null;
    }

    // EXPRESSIONS

    /**
     * Visit method for name expressions.
     *
     * @param nameExpr The name expression.
     * @param refDepth The current reference depth.
     * @return The result of the traversal.
     */
    @Override
    public Object visit(AstNameExpr nameExpr, RefDepth refDepth) {

        MemAccess access;

        if (SemAn.declaredAt.get(nameExpr) instanceof AstVarDecl) {
            access = Memory.varAccesses.get((AstVarDecl) SemAn.declaredAt.get(nameExpr));
        } else if (SemAn.declaredAt.get(nameExpr) instanceof AstParDecl) {
            access = Memory.parAccesses.get((AstParDecl) SemAn.declaredAt.get(nameExpr));
        } else {
            return null;
        }

        if (RefAn.referenceCandidates.containsKey(access)) {
            if (RefAn.referenceCandidates.get(access) < refDepth.depth) {
                RefAn.referenceCandidates.put(access, refDepth.depth);
            }
        } else {
            RefAn.referenceCandidates.put(access, refDepth.depth);
        }

        refDepth = new RefDepth();

        // If the name expression is not a global variable, check if it is a closure candidate.
        if (access instanceof MemAbsAccess) {
            return null;
        }

        int funDepth = Memory.frames.get(funStack.peek()).depth;

        // If the variable is declared in a function that is above the current function in the call stack,
        // it is a closure candidate.
        if (((MemRelAccess) access).depth <= funDepth) {
            RefAn.closureCandidates.add((MemRelAccess) access);
        }

        return null;

    }

    /**
     * Visit method for prefix expressions.
     *
     * @param preExpr The prefix expression.
     * @param refDepth The current reference depth.
     * @return The result of the traversal.
     */
    @Override
    public Object visit(AstPreExpr preExpr, RefDepth refDepth) {

        // If the prefix expression is a pointer, increase the reference depth.
        if (preExpr.oper == AstPreExpr.Oper.PTR) {
            refDepth.depth++;
        }

        preExpr.subExpr.accept(this, refDepth);

        return null;

    }

}
