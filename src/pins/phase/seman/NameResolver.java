package pins.phase.seman;

import pins.common.report.Report;
import pins.data.ast.visitor.*;
import pins.data.ast.*;

/**
 * Name resolver.
 */
public class NameResolver extends AstFullVisitor<Object, NameResolver.Mode> {

    /**
     * Name resolver mode, used for resolving typ and function declarations.
     */
    public enum Mode {
        HEAD, BODY
    }

    /**
     * Symbol table.
     */
    private final SymbTable symbTable = new SymbTable();

    // GENERAL PURPOSE

    /**
     * General purpose visit method, which traverses the abstract syntax trees. It visits the trees in 5 separate loops:
     * 1. The first loop resolves the names of types.
     * 2. The second loop resolves the values of types.
     * 3. The third loop resolves the names of variables.
     * 4. The fourth loop resolves the names, parameter types and return types of functions.
     * 5. The fifth loop resolves the names of parameters and the expression of functions.
     *
     * @param trees The abstract syntax trees to traverse.
     * @param mode  The mode of the name resolver.
     * @return The result of the traversal.
     */
    @Override
    public Object visit(ASTs<?> trees, NameResolver.Mode mode) {
        // On first pass, resolve named type names.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstTypDecl) {
                tree.accept(this, Mode.HEAD);
            }
        }
        // On second pass, resolve named type values.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstTypDecl) {
                tree.accept(this, Mode.BODY);
            }
        }
        // On third pass, resolve variables.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstVarDecl) {
                tree.accept(this, mode);
            }
        }
        // On fourth pass, resolve function names, parameter types and return types.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstFunDecl) {
                tree.accept(this, Mode.HEAD);
            }
        }
        // On fifth pass, resolve function parameter names and expression.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstFunDecl) {
                tree.accept(this, Mode.BODY);
            }
        }

        return null;
    }

    // DECLARATIONS

    /**
     * Visit method for variable declaration.
     *
     * @param varDecl The variable declaration to visit.
     * @param mode    The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error if the variable is already defined.
     */
    @Override
    public Object visit(AstVarDecl varDecl, Mode mode) {
        try {
            // Add the connection variable name : variable declaration to the symbol table.
            symbTable.ins(varDecl.name, varDecl);
        } catch (SymbTable.CannotInsNameException __) {
            throw new Report.Error(varDecl.location, "Semantic error: Cannot redefine '" + varDecl.name + "' as a variable.");
        }
        // Visit the type of the variable.
        varDecl.type.accept(this, null);

        return null;
    }

    /**
     * Visit method for type declaration.
     *
     * @param typDecl The type declaration to visit.
     * @param mode    The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error if the type is already defined.
     */
    @Override
    public Object visit(AstTypDecl typDecl, Mode mode) {
        // If NameResolver is in HEAD mode, add the connection type name : type declaration to the symbol table.
        // This is needed so that types aren't resolved twice and are also resolved for function parameters.
        if (mode == Mode.HEAD) {
            try {
                symbTable.ins(typDecl.name, typDecl);
            } catch (SymbTable.CannotInsNameException __) {
                throw new Report.Error(typDecl.location, "Semantic error: Cannot redefine '" + typDecl.name + "' as a type.");
            }
        }
        if (mode == Mode.BODY) {
            // Visit the type of the type declaration.
            typDecl.type.accept(this, mode);
        }

        return null;
    }

    /**
     * Visit method for function declaration.
     *
     * @param funDecl The function declaration to visit.
     * @param mode    The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error if the function is already defined.
     */
    @Override
    public Object visit(AstFunDecl funDecl, Mode mode) {
        // If NameResolver is in HEAD mode, add the connection function name : function declaration to the symbol table.
        if (mode == Mode.HEAD) {
            try {
                symbTable.ins(funDecl.name, funDecl);
            } catch (SymbTable.CannotInsNameException __) {
                throw new Report.Error(funDecl.location, "Cannot redefine '" + funDecl.name + "' as a function.");
            }
            // Visit the type of the function.
            funDecl.type.accept(this, mode);

            // Visit the parameters of the function.
            for (AstParDecl parDecl : funDecl.pars.asts()) {
                parDecl.accept(this, mode);
            }
        }
        else {
            // Create new nested scope for the function.
            symbTable.newScope();

            // Visit the parameters of the function.
            for (AstParDecl parDecl : funDecl.pars.asts()) {
                parDecl.accept(this, mode);
            }

            // Visit the body of the function.
            funDecl.expr.accept(this, mode);

            // Return to the previous scope.
            symbTable.oldScope();
        }

        return null;
    }

    // USES OF NAMES

    /**
     * Visit method for named type.
     *
     * @param typeName The type name to visit.
     * @param mode     The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error if the type is not defined.
     */
    @Override
    public Object visit(AstTypeName typeName, Mode mode) {
        // When encountering a named type use, add the declaration to Semantic analyzer's use - declaration map.
        try {
            SemAn.declaredAt.put(typeName, symbTable.fnd(typeName.name));
        } catch (SymbTable.CannotFndNameException __) {
            throw new Report.Error(typeName.location, "Semantic Error: '" + typeName.name + "' is not declared.");
        }

        return null;
    }

    /**
     * Visit method for named expression.
     *
     * @param nameExpr The named expression to visit.
     * @param mode     The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error if the name is not defined.
     */
    @Override
    public Object visit(AstNameExpr nameExpr, Mode mode) {
        // When encountering a named expression use, add the declaration to Semantic analyzer's use - declaration map.
        try {

            if (!(symbTable.fnd(nameExpr.name) instanceof AstVarDecl) && !(symbTable.fnd(nameExpr.name) instanceof AstParDecl)) {
                throw new Report.Error(nameExpr.location, "Semantic Error: '" + nameExpr.name + "' is not declared.");
            }
            SemAn.declaredAt.put(nameExpr, symbTable.fnd(nameExpr.name));
        } catch (SymbTable.CannotFndNameException __) {
            throw new Report.Error(nameExpr.location, "Semantic Error: '" + nameExpr.name + "' is not declared.");
        }

        return null;
    }

    /**
     * Visit method for call expression.
     *
     * @param callExpr The call expression to visit.
     * @param mode     The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error if the function is not defined.
     */
    @Override
    public Object visit(AstCallExpr callExpr, Mode mode) {
        // When encountering a call expression use, add the declaration to Semantic analyzer's use - declaration map.
        try {
            SemAn.declaredAt.put(callExpr, symbTable.fnd(callExpr.name));
        } catch (SymbTable.CannotFndNameException __) {
            throw new Report.Error(callExpr.location, "Semantic Error: '" + callExpr.name + "' is not declared.");
        }
        // Visit the arguments of the call expression.
        for (AstExpr expr : callExpr.args.asts()) {
            expr.accept(this, mode);
        }

        return null;
    }

    // EXPRESSIONS

    /**
     * Visit method for statement expression.
     *
     * @param stmtExpr The statement expression to visit.
     * @param mode     The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public Object visit(AstStmtExpr stmtExpr, Mode mode) {
        // Visit all statements in the statement expression.
        for (AstStmt stmt : stmtExpr.stmts.asts()) {
            stmt.accept(this, mode);
        }

        return null;
    }

    /**
     * Visit method for where expression.
     *
     * @param whereExpr The where expression to visit.
     * @param mode      The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public Object visit(AstWhereExpr whereExpr, Mode mode) {
        // Create new nested scope for the where expression.
        symbTable.newScope();
        // Visit the where expression's declarations.
        whereExpr.decls.accept(this, null);
        // Visit the where expression's expression.
        whereExpr.subExpr.accept(this, null);
        // Return to the previous scope.
        symbTable.oldScope();

        return null;
    }

    // FUNCTION PARAMETERS

    /**
     * Visit method for parameter declaration.
     *
     * @param parDecl The parameter declaration to visit.
     * @param mode    The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error if the parameter is already defined.
     */
    @Override
    public Object visit(AstParDecl parDecl, Mode mode) {
        // If NameResolver is in HEAD mode, visit only the function parameter types.
        if (mode == Mode.HEAD) {
            parDecl.type.accept(this, mode);
        }
        // If NameResolver is in BODY mode, add the connection parameter name : parameter declaration to the symbol table.
        else {
            try {
                symbTable.ins(parDecl.name, parDecl);
            } catch (SymbTable.CannotInsNameException __) {
                throw new Report.Error(parDecl.location, "Semantic error: Cannot redefine '" + parDecl.name + "' as a parameter.");
            }
        }

        return null;
    }

}
