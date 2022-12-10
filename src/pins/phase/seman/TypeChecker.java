package pins.phase.seman;

import pins.common.report.Report;
import pins.data.ast.visitor.AstFullVisitor;
import pins.data.ast.*;
import pins.data.typ.*;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Type checker.
 */
public class TypeChecker extends AstFullVisitor<SemType, TypeChecker.Mode> {

    /**
     * Type checker mode, used for resolving typ and function declarations.
     */
    public enum Mode {
        HEAD, BODY
    }

    /**
     * Set of already used names, used for cycle protection.
     */
    HashSet<String> usedNames = new HashSet<String>();

    // GENERAL PURPOSE

    /**
     * General purpose visit method, which traverses the abstract syntax trees.
     * It visits the trees in 5 separate loops:
     * <ul> <li>1. The first loop maps declarations to semantic types.</li>
     *      <li>2. The second loop sets declared type definitions.</li>
     *      <li>3. The third loop resolves variable types.</li>
     *      <li>4. The fourth loop resolves function return and parameter types.</li>
     *      <li>5. The fifth loop resolves function body types.</li>
     * </ul>
     *
     * @param trees The abstract syntax trees to traverse.
     * @param mode The mode of the name resolver.
     * @return The result of the traversal.
     */
    @Override
    public SemType visit(ASTs<?> trees, Mode mode) {
        // On first pass, map declarations to semantic types.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstTypDecl) {
                tree.accept(this, Mode.HEAD);
            }
        }
        // On second pass, set declared type definitions.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstTypDecl) {
                tree.accept(this, Mode.BODY);
            }
        }
        // On third pass, resolve variable types.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstVarDecl) {
                tree.accept(this, mode);
            }
        }
        // On fourth pass, resolve function return and parameter types.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstFunDecl) {
                tree.accept(this, Mode.HEAD);
            }
        }
        // On fifth pass, resolve function body types.
        for (AST tree : trees.asts()) {
            if (tree instanceof AstFunDecl) {
                tree.accept(this, Mode.BODY);
            }
        }
        return null;
    }

    // DECLARATIONS

    /**
     * Visit method for type declarations.
     *
     * @param typDecl The type declaration to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the type declaration is cyclical.
     */
    @Override
    public SemType visit(AstTypDecl typDecl, Mode mode) {
        // If typeChecker is in HEAD mode, add the connection type declaration : semantic name to SemAn class.
        // Otherwise, set declared type's definition and check if it is cyclically declared.
        if (mode == Mode.HEAD) {
            SemAn.declaresType.put(typDecl, new SemName(typDecl.name));

            // First visit type names that define primitive types.
            if (!(typDecl.type instanceof AstTypeName)) {
                SemType type = typDecl.type.accept(this, mode);
                SemAn.declaresType.get(typDecl).define(type.actualType());
            }
        }
        else if (mode == Mode.BODY) {
            // Visit type names that define other type names.
            if (typDecl.type instanceof AstTypeName) {
                SemType type = typDecl.type.accept(this, mode);
                SemAn.declaresType.get(typDecl).define(type.actualType());
            }

            if (isDeclaredCyclically(SemAn.declaresType.get(typDecl), null)) {
                throw new Report.Error(typDecl.type.location,
                        "Semantic error: Types cannot be declared cyclically.");
            }
        }

        return null;
    }

    /**
     * Visit method for variable declarations.
     *
     * @param varDecl The variable declaration to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstVarDecl varDecl, Mode mode) {
        return varDecl.type.accept(this, mode);
    }

    /**
     * Visit method for function declarations.
     *
     * @param funDecl The function declaration to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the function return type is invalid or the actual and expected return type do not match.
     */
    @Override
    public SemType visit(AstFunDecl funDecl, Mode mode) {
        if (mode == Mode.HEAD) {
            // Visit and resolve function parameter return types.
            for (AstParDecl parDecl : funDecl.pars.asts()) {
                parDecl.accept(this, mode);
            }

            // Visit and resolve function return type.
            SemType returnType = funDecl.type.accept(this, mode);

            if (returnType.actualType() instanceof SemArr) {
                throw new Report.Error(funDecl.type.location,
                        "Semantic error: Function return type cannot be of type '" +
                                getTypeString(returnType) +  "'.");
            }

            return returnType;
        }
        else if (mode == Mode.BODY) {
            // Visit and resolve function body type, which is also it's return type.
            SemType actualReturnType = funDecl.expr.accept(this, mode);
            // Get expected return type from function declaration.
            SemType expectedReturnType = funDecl.type.accept(this, mode);

            // Check if return type is compatible with expected return type.
            if (!equal(actualReturnType, expectedReturnType, null)) {
                throw new Report.Error(funDecl.type.location,
                        "Semantic error: Actual return type (" + getTypeString(actualReturnType) + ") " +
                                "does not match expected return type (" + getTypeString(expectedReturnType) + ").");
            }
        }
        return null;
    }

    // FUNCTION PARAMETERS

    /**
     * Visit method for parameter declarations.
     *
     * @param parDecl The parameter declaration to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the parameter type is invalid.
     */
    @Override
    public SemType visit(AstParDecl parDecl, Mode mode) {
        // Visit and resolve parameter type.
        SemType parType = parDecl.type.accept(this, mode);

        // Parameter cannot be of type void.
        if (parType instanceof SemVoid || parType instanceof SemArr) {
            throw new Report.Error(parDecl.type.location,
                    "Semantic error: Parameter (" + parDecl.name + ") cannot be of type '" + getTypeString(parType) + "'.");
        }
        return parType;
    }

    // TYPES

    /**
     * Visit method for atomic types.
     *
     * @param atomType The atomic type to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstAtomType atomType, Mode mode) {
        // Map atomic type to corresponding semantic type.
        switch (atomType.kind) {
            case INT -> {
                SemAn.describesType.put(atomType, new SemInt());
                return new SemInt();
            }
            case CHAR -> {
                SemAn.describesType.put(atomType, new SemChar());
                return new SemChar();
            }
            case VOID -> {
                SemAn.describesType.put(atomType, new SemVoid());
                return new SemVoid();
            }
        }
        return null;
    }

    /**
     * Visti method for pointer types.
     *
     * @param ptrType The pointer type to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstPtrType ptrType, Mode mode) {
        // Visit and resolve pointer's subtype.
        SemType type = ptrType.subType.accept(this, mode);

        SemPtr semPtr = new SemPtr(type.actualType());

        // Map pointer type to corresponding semantic type.
        SemAn.describesType.put(ptrType, semPtr);
        return semPtr;
    }

    /**
     * Visit method for array types.
     *
     * @param arrType The array type to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the array size type is invalid.
     */
    @Override
    public SemType visit(AstArrType arrType, Mode mode) {
        // Visit and resolve array's element type.
        SemType elemType = arrType.elemType.accept(this, mode);

        // Parse type of array's size.
        SemType sizeType = arrType.size.accept(this, mode);

        if (!(sizeType instanceof SemInt)) {
            throw new Report.Error(arrType.size.location, "Semantic error: Array size must be of type int.");
        }

        // Try to parse array's size. This should fail in the case that array's size is not a constant integer
        // atomic expression.
        long size = -1;
        try {
            // Check if array size is signed.
            if (arrType.size instanceof AstPreExpr) {
                if (((AstPreExpr) arrType.size).oper == AstPreExpr.Oper.ADD) {
                    size = Long.parseLong(((AstConstExpr)((AstPreExpr) arrType.size).subExpr).name);
                }
                else {
                    throw new Report.Error(arrType.size.location,
                            "Semantic error: Array size must be a positive constant integer.");
                }
            }
            else {
                size = Long.parseLong(((AstConstExpr) arrType.size).name);
            }
        } catch (ClassCastException __) {
            throw new Report.Error(arrType.size.location,
                    "Semantic error: Array size must be a positive constant integer.");
        }

        // Check if array's size is within bounds.
        if (size <= 0) {
            throw new Report.Error(arrType.size.location,
                    "Semantic error: Array size must be higher than 0.");
        }

        SemArr semArr = new SemArr(elemType.actualType(), size);

        // Map array type to corresponding semantic type.
        SemAn.describesType.put(arrType, semArr);
        return semArr;
    }

    /**
     * Visit method for named types.
     *
     * @param typeName The named type to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the named type is declared cyclically.
     */
    @Override
    public SemType visit(AstTypeName typeName, Mode mode) {
        AstTypDecl typDecl = (AstTypDecl) SemAn.declaredAt.get(typeName);
        SemType type = SemAn.declaresType.get(typDecl);

        // Cycle protection.
        if (usedNames.contains(typeName.name)) {
            throw new Report.Error(typDecl.type.location,
                    "Semantic error: Types cannot be declared cyclically.");
        }
        usedNames.add(typeName.name);

        // If the type of typeName is typeName, get it's base type.
        if (typDecl.type instanceof AstTypeName) {
            type = typDecl.type.accept(this, mode);
        }

        // Map named type to corresponding semantic type.
        SemAn.describesType.put(typeName, type.actualType());
        // Clear set of used names, to reset the cycle protection.
        usedNames.clear();
        return type;
    }

    // EXPRESSIONS

    /**
     * Visit method for atomic expressions.
     *
     * @param constExpr The atomic expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstConstExpr constExpr, Mode mode) {
        // Map atomic expression to corresponding semantic type.
        switch (constExpr.kind) {
            case INT -> {
                // Map expression to result type.
                SemAn.exprOfType.put(constExpr, new SemInt());
                return new SemInt();
            }
            case CHAR -> {
                // Map expression to result type.
                SemAn.exprOfType.put(constExpr, new SemChar());
                return new SemChar();
            }
            case VOID -> {
                // Map expression to result type.
                SemAn.exprOfType.put(constExpr, new SemVoid());
                return new SemVoid();
            }
            case PTR -> {
                // Map expression to result type.
                SemAn.exprOfType.put(constExpr, new SemPtr(new SemVoid()));
                return new SemPtr(new SemVoid());
            }
        }
        return null;
    }

    /**
     * Visit method for prefix expressions.
     *
     * @param preExpr The prefix expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the prefix expression type is invalid.
     */
    @Override
    public SemType visit(AstPreExpr preExpr, Mode mode) {
        // Resolve prefix expression's operand and return type.
        switch (preExpr.oper) {
            case ADD, SUB, NOT -> {
                // Visit and resolve operand type.
                SemType type = preExpr.subExpr.accept(this, mode);

                // Check if operand type is correct (integer).
                if (!(type.actualType() instanceof SemInt)) {
                    throw new Report.Error(preExpr.subExpr.location,
                            "Semantic error: Prefix operator '" + preExpr.oper +
                                    "' cannot be applied to type '" + getTypeString(type) + "'.");
                }

                // Map expression to result type.
                SemAn.exprOfType.put(preExpr, type.actualType());
                return type;
            }
            case PTR -> {
                // Visit and resolve operand type.
                SemType type = preExpr.subExpr.accept(this, mode);
                SemPtr ptr = new SemPtr(type.actualType());

                // Map expression to result type.
                SemAn.exprOfType.put(preExpr, ptr);
                return ptr;
            }
            case NEW -> {
                // Visit and resolve operand type.
                SemType type = preExpr.subExpr.accept(this, mode);

                if (!(type.actualType() instanceof SemInt)) {
                    throw new Report.Error(preExpr.subExpr.location,
                            "Semantic error: Prefix operator '" + preExpr.oper +
                                    "' cannot be applied to type '" + getTypeString(type) + "'.");
                }

                SemPtr ptr = new SemPtr(new SemVoid());

                // Map expression to result type.
                SemAn.exprOfType.put(preExpr, ptr);
                return ptr;
            }
            case DEL -> {
                // Visit and resolve operand type.
                SemType type = preExpr.subExpr.accept(this, mode);

                // Check if operand type is correct (pointer).
                if (!(type.actualType() instanceof SemPtr)) {
                    throw new Report.Error(preExpr.subExpr.location,
                            "Semantic error: Prefix operator '" + preExpr.oper +
                                    "' cannot be applied to type '" + getTypeString(type) + "'.");
                }

                // Map expression to result type.
                SemAn.exprOfType.put(preExpr, new SemVoid());
                return new SemVoid();
            }
        }
        return null;
    }

    /**
     * Visit method for postfix expressions.
     *
     * @param postExpr The postfix expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the postfix expression type is invalid.
     */
    @Override
    public SemType visit(AstPstExpr postExpr, Mode mode) {
        // Visit and resolve operand type.
        SemType type = postExpr.subExpr.accept(this, mode);

        // Check if operand type is correct (pointer).
        if (!(type.actualType() instanceof SemPtr)) {
            throw new Report.Error(postExpr.subExpr.location,
                    "Semantic error: Postfix operator '" + postExpr.oper +
                            "' cannot be applied to type '" + getTypeString(type) + "'.");
        }

        // Get pointer's base type.
        SemType base = ((SemPtr) type).baseType;

        // Map expression to result type.
        SemAn.exprOfType.put(postExpr, base.actualType());
        return base;
    }

    /**
     * Visit method for binary expressions.
     *
     * @param binExpr The binary expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the binary expression's subexpressions' types are invalid.
     */
    @Override
    public SemType visit(AstBinExpr binExpr, Mode mode) {
        // Resolve binary expression's left and right operand types and return type.
        switch (binExpr.oper) {
            case ADD, SUB, MUL, DIV, MOD, AND, OR -> {
                // Visit and resolve left and right operand types.
                SemType type1 = binExpr.fstSubExpr.accept(this, mode);
                SemType type2 = binExpr.sndSubExpr.accept(this, mode);

                // Check if operand types are correct (integer).
                if (!(type1.actualType() instanceof SemInt) || !(type2.actualType() instanceof SemInt)) {
                    throw new Report.Error(binExpr.fstSubExpr.location,
                            "Semantic error: Binary operator '" + binExpr.oper +
                                    "' cannot be applied to types '" + getTypeString(type1) +
                                    "', '" + getTypeString(type2) +"'.");
                }

                // Map expression to result type.
                SemAn.exprOfType.put(binExpr, new SemInt());
                return new SemInt();
            }
            case EQU, NEQ, LTH, GTH, LEQ, GEQ  -> {
                // Visit and resolve left and right operand types.
                SemType type1 = binExpr.fstSubExpr.accept(this, mode);
                SemType type2 = binExpr.sndSubExpr.accept(this, mode);

                // Check if operand types are correct (not void or array).
                if (type1.actualType() instanceof SemVoid || type1.actualType() instanceof SemArr
                        || type2.actualType() instanceof SemArr || type2.actualType() instanceof SemArr) {
                    throw new Report.Error(binExpr.fstSubExpr.location,
                            "Semantic error: Binary operator '" + binExpr.oper +
                                    "' cannot be applied to types '" + getTypeString(type1) +
                                    "', '" + getTypeString(type2) +"'.");
                }

                // Check if operand types are equal.
                if (!equal(type1, type2, null)) {
                    throw new Report.Error(binExpr.sndSubExpr.location,
                            "Semantic error: Type mismatch: '" + getTypeString(type1) +
                                    "', '" + getTypeString(type2) + "'.");
                }

                // Map expression to result type.
                SemAn.exprOfType.put(binExpr, new SemInt());
                return new SemInt();
            }
            case ARR -> {
                // Visit and resolve left and right operand types.
                SemType type1 = binExpr.fstSubExpr.accept(this, mode);
                SemType type2 = binExpr.sndSubExpr.accept(this, mode);

                // Check if operand types are wrong (not array).
                if (!(type1.actualType() instanceof SemArr)) {
                    throw new Report.Error(binExpr.fstSubExpr.location,
                            "Semantic error: Array indexing operator cannot be applied to type '" +
                                    getTypeString(type1) + "'.");
                }
                if (!(type2.actualType() instanceof SemInt)) {
                    throw new Report.Error(binExpr.sndSubExpr.location,
                            "Semantic error: Array index cannot be applied to type '" +
                                    getTypeString(type2) + "'.");
                }

                // Map expression to result type.
                SemAn.exprOfType.put(binExpr, ((SemArr) type1).elemType.actualType());
                return ((SemArr) type1).elemType.actualType();
            }
        }
        return null;
    }

    /**
     * Visit method for named expressions.
     *
     * @param nameExpr The named expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstNameExpr nameExpr, Mode mode) {
        // Visit and resolve the name expression's type.
        AstType aType = SemAn.declaredAt.get(nameExpr).type;
        SemType type = aType.accept(this, mode);

        // Map expression to result type.
        SemAn.exprOfType.put(nameExpr, type.actualType());
        return type.actualType();
    }

    /**
     * Visit method for call expressions.
     *
     * @param callExpr The call expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the arguments and parameter types do not match.
     */
    @Override
    public SemType visit(AstCallExpr callExpr, Mode mode) {
        AstFunDecl funDecl = (AstFunDecl) SemAn.declaredAt.get(callExpr);

        if (funDecl.pars.asts().size() != callExpr.args.asts().size()) {
            throw new Report.Error(callExpr.location,
                    "Semantic error: Function '" + funDecl.name + "' expects " +
                            funDecl.pars.asts().size() + " arguments, but " +
                            callExpr.args.asts().size() + " were given.");
        }

        // Visit and resolve the call expression's argument types.
        for (int i = 0; i < callExpr.args.asts().size(); i++) {
            // Visit and resolve argument and parameter type.
            SemType argType = callExpr.args.asts().get(i).accept(this, mode);
            SemType parType = funDecl.pars.asts().get(i).type.accept(this, mode);

            // Check if argument type is equal to parameter type.
            if (!equal(argType, parType, null)) {
                throw new Report.Error(callExpr.args.asts().get(i).location,
                        "Semantic error: Argument type mismatch: '" +
                                getTypeString(argType) + "', '" +
                                funDecl.pars.asts().get(i).name + ": " + getTypeString(parType) + "'.");
            }
        }

        // Visit and resolve the call expression's return type.
        SemType type = funDecl.type.accept(this, mode);
        // Map expression to result type.
        SemAn.exprOfType.put(callExpr, type.actualType());
        return type.actualType();
    }

    /**
     * Visit method for cast expressions.
     *
     * @param castExpr The cast expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the cast expression's type is invalid.
     */
    @Override
    public SemType visit(AstCastExpr castExpr, Mode mode) {
        // Visit and resolve the cast expression's source and target type.
        SemType type1 = castExpr.subExpr.accept(this, mode);
        SemType type2 = castExpr.type.accept(this, mode);

        // Check if source and destination types are correct (not void or array).
        if ((type1.actualType() instanceof SemVoid) || (type1.actualType() instanceof SemArr)) {
            throw new Report.Error(castExpr.subExpr.location, "Semantic error: Cannot cast from type '" +
                    getTypeString(type1) +  "'.");
        }
        if ((type2.actualType() instanceof SemVoid) || (type2.actualType() instanceof SemArr)) {
            throw new Report.Error(castExpr.type.location, "Semantic error: Cannot cast to type '" +
                    getTypeString(type2) + "'.");
        }

        // Map expression to result type.
        SemAn.exprOfType.put(castExpr, type2.actualType());
        return type2.actualType();
    }

    /**
     * Visit method for where expressions.
     *
     * @param whereExpr The where expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstWhereExpr whereExpr, Mode mode) {
        // Visit and resolve the where expression's body declarations.
        for (AstDecl decl : whereExpr.decls.asts()) {
            // As function and type declarations must be visited multiple times, each visit doing different
            // we need to accept them with both modes, instead of only the mode passed to this function.
            if (decl instanceof AstFunDecl || decl instanceof AstTypDecl) {
                decl.accept(this, Mode.HEAD);
                decl.accept(this, Mode.BODY);
            }
            else {
                decl.accept(this, mode);
            }
        }

        // Visit and resolve the where expression's expression.
        SemType type = whereExpr.subExpr.accept(this, mode);

        // Map expression to result type.
        SemAn.exprOfType.put(whereExpr, type.actualType());
        return type.actualType();
    }

    /**
     * Visit method for statement expressions.
     *
     * @param stmtExpr The statement expression to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstStmtExpr stmtExpr, Mode mode) {
        SemType type = null;

        // Get type of last statement in expression, that is the result type.
        for (AstStmt stmt : stmtExpr.stmts.asts()) {
            type = stmt.accept(this, mode);
        }

        // Map expression to result type.
        SemAn.exprOfType.put(stmtExpr, type.actualType());
        return type.actualType();
    }

    // STATEMENTS

    /**
     * Visit method for expression statements.
     *
     * @param exprStmt The expression statement to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     */
    @Override
    public SemType visit(AstExprStmt exprStmt, Mode mode) {
        SemType type = exprStmt.expr.accept(this, mode);
        // Map statement to result type.
        SemAn.stmtOfType.put(exprStmt, type.actualType());
        return type.actualType();
    }

    /**
     * Visit method for assignment statements.
     *
     * @param assignStmt The assignment statement to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the assignment types are invalid or not matching.
     */
    @Override
    public SemType visit(AstAssignStmt assignStmt, Mode mode) {
        // Visit and resolve the assignment statement's left and right expressions.
        SemType type1 = assignStmt.fstSubExpr.accept(this, mode);
        SemType type2 = assignStmt.sndSubExpr.accept(this, mode);

        // Check if left and right expressions' types are correct (not void or array).
        if ((type1.actualType() instanceof SemVoid) || (type1.actualType() instanceof SemArr)) {
            throw new Report.Error(assignStmt.fstSubExpr.location,
                    "Semantic error: Cannot assign to type '" +
                            getTypeString(type1) + "'.");
        }
        if ((type2.actualType() instanceof SemVoid) || (type2.actualType() instanceof SemArr)) {
            throw new Report.Error(assignStmt.sndSubExpr.location,
                    "Semantic error: Cannot assign type '" +
                            getTypeString(type2) + "'.");
        }

        // Check if left and right expressions are of the same type.
        if (!equal(type1, type2, null)) {
            throw new Report.Error(assignStmt.sndSubExpr.location,
                    "Semantic error: Type mismatch: '" +
                            getTypeString(type1) + "', '" +
                            getTypeString(type2) + "'.");
        }

        // Map statement to result type.
        SemAn.stmtOfType.put(assignStmt, new SemVoid());
        return new SemVoid();
    }

    /**
     * Visit method for if statements.
     *
     * @param ifStmt The if statement to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the if statement's condition is invalid or if then and else body types are not void.
     */
    @Override
    public SemType visit(AstIfStmt ifStmt, Mode mode) {
        // Visit and resolve the if statement's condition expression.
        SemType exprType = ifStmt.condExpr.accept(this, mode);
        // Check if condition expression's type is correct (integer).
        if (!(exprType.actualType() instanceof SemInt)) {
            throw new Report.Error(ifStmt.condExpr.location,
                    "Semantic error: Condition must be an integer expression.");
        }

        // Visit and resolve the if statement's body statements.
        SemType thenType = ifStmt.thenBodyStmt.accept(this, mode);
        SemType elseType = null;

        // If there is an else body statement, visit and resolve it.
        if (ifStmt.elseBodyStmt != null) {
             elseType = ifStmt.elseBodyStmt.accept(this, mode);
        }

        // Check if then and else body statements' types are correct (not void).
        if (!(thenType.actualType() instanceof SemVoid)) {
            throw new Report.Error(ifStmt.thenBodyStmt.location,
                    "Semantic error: Then body must be a void statement.");
        }
        if (elseType != null && !(elseType.actualType() instanceof SemVoid)) {
            throw new Report.Error(ifStmt.elseBodyStmt.location,
                    "Semantic error: Else body must be a void statement.");
        }

        // Map statement to result type.
        SemAn.stmtOfType.put(ifStmt, new SemVoid());
        return new SemVoid();
    }

    /**
     * Visit method for while statements.
     *
     * @param whileStmt The while statement to visit.
     * @param mode The mode of the name resolver.
     * @return The result of the visit.
     * @throws Report.Error If the while statement's condition is invalid or if body type is not void.
     */
    @Override
    public SemType visit(AstWhileStmt whileStmt, Mode mode) {
        // Visit and resolve the while statement's condition expression.
        SemType exprType = whileStmt.condExpr.accept(this, mode);
        // Check if condition expression's type is correct (integer).
        if (!(exprType.actualType() instanceof SemInt)) {
            throw new Report.Error(whileStmt.condExpr.location,
                    "Semantic error: Condition must be an integer expression.");
        }

        // Visit and resolve the while statement's body statements.
        SemType bodyType = whileStmt.bodyStmt.accept(this, mode);
        // Check if body statement's type is correct (not void).
        if (!(bodyType.actualType() instanceof SemVoid)) {
            throw new Report.Error(whileStmt.bodyStmt.location,
                    "Semantic error: Body must be a void statement.");
        }

        // Map statement to result type.
        SemAn.stmtOfType.put(whileStmt, new SemVoid());
        return new SemVoid();
    }

    // UTILS

    /**
     * Checks if two types are STRUCTURALLY equal.
     *
     * @param type1 The first type.
     * @param type2 The second type.
     * @param equivTypes The map of semantic names and their equivalent types.
     * @return True if the types are STRUCTURALLY equal, false otherwise.
     */
    private boolean equal(SemType type1, SemType type2, HashMap<SemType, HashSet<SemType>> equivTypes) {
        // Check if both types are named types.
        if ((type1 instanceof SemName) && (type2 instanceof SemName)) {
            if (equivTypes == null) {
                equivTypes = new HashMap<SemType, HashSet<SemType>>();
            }

            if (!equivTypes.isEmpty()) {
                // If named types' equivalent types contain each other, they are structurally equal.
                if (equivTypes.get(type1).contains(type2) && equivTypes.get(type2).contains(type1)) {
                    return true;
                } else {
                    // Create set of equivalent types for the first type.
                    HashSet<SemType> types = equivTypes.get(type1);
                    // Add the second type to the set.
                    types.add(type2);
                    // Add the set to the map.
                    equivTypes.put(type1, types);

                    // Create set of equivalent types for the second type.
                    types = equivTypes.get(type2);
                    // Add the first type to the set.
                    types.add(type1);
                    // Add the set to the map.
                    equivTypes.put(type2, types);
                }
            }
        }

        // Get both types' actual types (their atomic representations).
        type1 = type1.actualType();
        type2 = type2.actualType();

        // Check if both types are the same.
        if (type1 instanceof SemVoid) {
            return (type2 instanceof SemVoid);
        }
        if (type1 instanceof SemInt) {
            return (type2 instanceof SemInt);
        }
        if (type1 instanceof SemChar) {
            return (type2 instanceof SemChar);
        }

        // Check if both types are pointer types.
        if (type1 instanceof SemPtr) {
            if (!(type2 instanceof SemPtr)) {
                return false;
            }
            SemPtr ptr1 = (SemPtr) type1;
            SemPtr ptr2 = (SemPtr) type2;

            // If one pointer's type is of type void, they are structurally equal.
            if ((ptr1.baseType.actualType() instanceof SemVoid) || (ptr2.baseType.actualType() instanceof SemVoid)) {
                return true;
            }

            // Check if pointer's base types are structurally equal.
            return equal(ptr1.baseType, ptr2.baseType, equivTypes);
        }

        // Check if both types are array types.
        if (type1 instanceof SemArr) {
            if (!(type2 instanceof SemArr)) {
                return false;
            }
            SemArr arr1 = (SemArr) type1;
            SemArr arr2 = (SemArr) type2;

            // Check if array's sizes are equal.
            if (arr1.numElems != arr2.numElems) {
                return false;
            }

            // Check if array's base types are structurally equal.
            return equal(arr1.elemType, arr2.elemType, equivTypes);
        }

        throw new Report.InternalError();
    }

    /**
     * Checks if two types are declared cyclically.
     *
     * @param type The type to check.
     * @param namedTypes The set of named types.
     * @return True if the type is declared cyclically, false otherwise.
     */
    private boolean isDeclaredCyclically(SemType type, HashSet<SemName> namedTypes) {
        if (namedTypes == null) {
            namedTypes = new HashSet<SemName>();
        }

        // Check if the type is a named type.
        if (!(type instanceof SemName)) {
            return false;
        }

        // Check if the type is already in the set of named types. If not, add it and continue the search.
        SemName name = (SemName) type;
        if (namedTypes.contains(name)) {
            return true;
        }
        else {
            namedTypes.add(name);
            return isDeclaredCyclically(name.type(), namedTypes);
        }
    }

    /**
     * Get String representation of semantic type.
     *
     * @param type The semantic type.
     * @return String representation of semantic type.
     * @throws Report.InternalError If the semantic type is not supported.
     */
    private String getTypeString(SemType type) {
        if (type instanceof SemName) {
            return ((SemName) type).name;
        }
        else if (type instanceof SemVoid) {
            return "void";
        }
        else if (type instanceof SemInt) {
            return "int";
        }
        else if (type instanceof SemChar) {
            return "char";
        }
        else if (type instanceof SemPtr) {
            return "^" + getTypeString(((SemPtr) type).baseType);
        }
        else if (type instanceof SemArr) {
            return "[" + ((SemArr) type).numElems +  "]" + getTypeString(((SemArr) type).elemType);
        }
        else {
            throw new Report.InternalError();
        }
    }

}
