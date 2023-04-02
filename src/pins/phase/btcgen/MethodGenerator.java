package pins.phase.btcgen;

import pins.data.ast.*;
import pins.data.ast.visitor.AstVisitor;
import pins.data.btc.BtcMethod;
import pins.data.btc.instr.BtcInstr;
import pins.data.btc.instr.arithmetic.*;
import pins.data.btc.instr.flow.*;
import pins.data.btc.instr.object.*;
import pins.data.btc.instr.stack.*;
import pins.data.btc.vars.*;
import pins.data.imc.code.expr.ImcCONST;
import pins.data.mem.*;
import pins.data.typ.*;
import pins.phase.imcgen.ImcGen;
import pins.phase.memory.Memory;
import pins.phase.seman.SemAn;

import java.util.HashSet;

/**
 * Bytecode method generator.
 */
public class MethodGenerator implements AstVisitor<BtcInstr, BtcMethod> {

    private int lineCounter = 0;

    private int fieldCounter = 0;

    private int localCounter = 0;

    /**
     * Constructs a new bytecode method generator.
     */
    public MethodGenerator() {
    }

    /**
     * The general purpose visit method, that first resolves global variables and then everything else. That is because
     * the global variables represent the fields of the class, and they need to be resolved first, in case they are
     * accessed by the methods.
     *
     * @param asts      The ASTs to visit.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(ASTs<?> asts, BtcMethod btcMethod) {
        // First pass: resolve global variable declarations.
        for (AST ast : asts.asts()) {
            if (ast instanceof AstVarDecl) {
                MemAccess access = Memory.varAccesses.get(ast);
                if (access instanceof MemAbsAccess) {
                    ast.accept(this, btcMethod);
                }
            }
        }
        // Second pass: resolve everything else.
        for (AST ast : asts.asts()) {
            if (ast instanceof AstVarDecl) {
                MemAccess access = Memory.varAccesses.get(ast);
                if (access instanceof MemAbsAccess) {
                    continue;
                }
            }
            ast.accept(this, btcMethod);
        }
        return null;
    }

    // Declarations

    /**
     * Visits a function declaration. If no bytecode method currently exists, it creates one. When the function is
     * resolved it adds it to the list of class methods.
     *
     * @param funDecl   The function declaration.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstFunDecl funDecl, BtcMethod btcMethod) {
        if (btcMethod == null) {
            BtcMethod.Type type = null;
            if (SemAn.describesType.get(funDecl.type) instanceof SemVoid) {
                type = BtcMethod.Type.VOID;
            } else if (SemAn.describesType.get(funDecl.type) instanceof SemChar) {
                type = BtcMethod.Type.CHAR;
            } else if (SemAn.describesType.get(funDecl.type) instanceof SemInt) {
                type = BtcMethod.Type.LONG;
            } else if (SemAn.describesType.get(funDecl.type) instanceof SemPtr) {
                type = BtcMethod.Type.REF;
            } else if (SemAn.describesType.get(funDecl.type) instanceof SemArr) {
                type = BtcMethod.Type.REF;
            }
            btcMethod = new BtcMethod(funDecl.name, type);
        }
        localCounter = 0;
        lineCounter = 0;
        // TODO: Implement nested functions
        for (AstParDecl par : funDecl.pars.asts()) {
            par.accept(this, btcMethod);
        }
        funDecl.expr.accept(this, btcMethod);
        // If no return statement (for example, last stmt in void method is assign), artificially add one.
        // This is only needed for void methods, as the return for other types is enforced by the semantic analyzer.
        if (btcMethod.type == BtcMethod.Type.VOID) {
            if (btcMethod.instrs().size() == 0 || !(btcMethod.instrs().get(btcMethod.instrs().size() - 1) instanceof BtcRETURN)) {
                btcMethod.instrs().add(new BtcRETURN(BtcRETURN.Type.RETURN));
                lineCounter++;
            }
        }
        BtcGen.btcMethods.put(funDecl, btcMethod);
        return null;
    }

    /**
     * Visits a variable declaration. If the variable is global, it adds it to the list of class fields. If it is local,
     * it adds it to the list of local variables.
     *
     * @param varDecl   The variable declaration.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstVarDecl varDecl, BtcMethod btcMethod) {
        BtcVar.Type type = null;
        int offset = 0;

        if (SemAn.describesType.get(varDecl.type) instanceof SemVoid) {
            type = BtcVar.Type.REF;
            offset += 1;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemInt) {
            type = BtcVar.Type.LONG;
            offset += 2;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemChar) {
            type = BtcVar.Type.CHAR;
            offset += 1;
        }

        if (Memory.varAccesses.get(varDecl) instanceof MemAbsAccess) {
            BtcGen.btcFields.put((MemAbsAccess) Memory.varAccesses.get(varDecl),
                                 new _BtcField(varDecl.name, type, fieldCounter));
            // TODO: The counters should be refactored into the method.
            fieldCounter++;
        } else {
            BtcGen.btcLocals.put((MemRelAccess) Memory.varAccesses.get(varDecl), new BtcLocal(type, localCounter));
            BtcGen.methodLocals.computeIfAbsent(btcMethod, k -> new HashSet<BtcLocal>());
            BtcGen.methodLocals.get(btcMethod).add(BtcGen.btcLocals.get((MemRelAccess) Memory.varAccesses.get(varDecl)));
            localCounter += offset;

        }
        return null;
    }

    /**
     * Visits a parameter declaration. It adds it to the list of local variables.
     *
     * @param parDecl   The parameter declaration.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstParDecl parDecl, BtcMethod btcMethod) {
        BtcVar.Type type = null;
        int index = localCounter;
        if (SemAn.describesType.get(parDecl.type) instanceof SemInt) {
            type = BtcVar.Type.LONG;
            localCounter += 2;
        } else if (SemAn.describesType.get(parDecl.type) instanceof SemChar) {
            type = BtcVar.Type.CHAR;
            localCounter++;
        }
        BtcLocal btcLocal = new BtcLocal(type, index);
        BtcGen.btcLocals.put(Memory.parAccesses.get(parDecl), btcLocal);
        BtcGen.methodLocals.computeIfAbsent(btcMethod, k -> new HashSet<BtcLocal>());
        BtcGen.methodLocals.get(btcMethod).add(btcLocal);
        return null;
    }

    // Statements

    /**
     * Visits an if statement. First, it computes the condition, then it computes the operator, and finally it computes
     * the body statements.
     *
     * @param ifStmt    The if statement.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstIfStmt ifStmt, BtcMethod btcMethod) {
        ifStmt.condExpr.accept(this, btcMethod);

        BtcCJUMP.Oper oper = null;

        if (ifStmt.condExpr instanceof AstBinExpr) {
            AstBinExpr binExpr = (AstBinExpr) ifStmt.condExpr;
            // The comparison operator must be reversed due to how the JVM handles conditional jumps.
            switch (binExpr.oper) {
                case EQU -> oper = BtcCJUMP.Oper.NE;
                case NEQ -> oper = BtcCJUMP.Oper.EQ;
                case LTH -> oper = BtcCJUMP.Oper.GE;
                case GTH -> oper = BtcCJUMP.Oper.LE;
                case LEQ -> oper = BtcCJUMP.Oper.GT;
                case GEQ -> oper = BtcCJUMP.Oper.LT;
            }
        }
        // TODO: Make it work for no else body.
        // TODO: Fix the CJUMP expression, it currently works on integers not longs.
        BtcCJUMP cjumpInstr = new BtcCJUMP(oper, 0);
        btcMethod.addInstr(cjumpInstr);
        lineCounter++;
        ifStmt.elseBodyStmt.accept(this, btcMethod);
        BtcGOTO gotoInstr = new BtcGOTO(0);
        btcMethod.addInstr(gotoInstr);
        lineCounter++;
        cjumpInstr.target = lineCounter;
        ifStmt.thenBodyStmt.accept(this, btcMethod);
        gotoInstr.target = lineCounter;
        return cjumpInstr;
    }

    /**
     * Visits a while statement. First, it computes the condition, then it computes the operator, and finally it
     * computes the body statements.
     *
     * @param whileStmt The while statement.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstWhileStmt whileStmt, BtcMethod btcMethod) {
        int start = lineCounter;
        whileStmt.condExpr.accept(this, btcMethod);

        BtcCJUMP.Oper oper = null;

        if (whileStmt.condExpr instanceof AstBinExpr) {
            AstBinExpr binExpr = (AstBinExpr) whileStmt.condExpr;
            // The comparison operator must be reversed due to how the JVM handles conditional jumps.
            switch (binExpr.oper) {
                case EQU -> oper = BtcCJUMP.Oper.NE;
                case NEQ -> oper = BtcCJUMP.Oper.EQ;
                case LTH -> oper = BtcCJUMP.Oper.GE;
                case GTH -> oper = BtcCJUMP.Oper.LE;
                case LEQ -> oper = BtcCJUMP.Oper.GT;
                case GEQ -> oper = BtcCJUMP.Oper.LT;
            }
        }
        BtcCJUMP cjumpInstr = new BtcCJUMP(oper, 0);
        btcMethod.addInstr(cjumpInstr);
        lineCounter += 3;
        whileStmt.bodyStmt.accept(this, btcMethod);
        BtcGOTO gotoInstr = new BtcGOTO(start);
        btcMethod.addInstr(gotoInstr);
        lineCounter += 3;
        cjumpInstr.target = lineCounter;
        return null;
    }

    /**
     * Visits an expression statement.
     *
     * @param exprStmt  The expression statement.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstExprStmt exprStmt, BtcMethod btcMethod) {
        BtcInstr btcInstr = exprStmt.expr.accept(this, btcMethod);
        return btcInstr;
    }

    /**
     * Visits an assignment statement. It computes the value of the expression, and uses the appropriate store
     * instruction based on the type of the variable. Global variables use the putstatic instruction, and local
     * variables use the store instruction.
     *
     * @param assignStmt The assignment statement.
     * @param btcMethod  The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstAssignStmt assignStmt, BtcMethod btcMethod) {
        return null;
        /*BtcInstr btcInstr = null;
        if (assignStmt.fstSubExpr instanceof AstBinExpr) {
            assignStmt.fstSubExpr.accept(this, btcMethod);
        } else if (assignStmt.fstSubExpr instanceof AstNameExpr) {
            AstDecl decl = SemAn.declaredAt.get(assignStmt.fstSubExpr);
            if (decl instanceof AstVarDecl) {
                MemAccess access = Memory.varAccesses.get(decl);
                if (access instanceof MemAbsAccess) {
                    btcInstr = new BtcACCESS(BtcACCESS.Dir.PUT, BtcACCESS.Type.STATIC,
                                             BtcGen.btcFields.get(access).index);
                } else {
                    BtcSTORE.Kind kind = null;
                    if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemInt) {
                        kind = BtcSTORE.Kind.LSTORE;
                    } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemChar) {
                        kind = BtcSTORE.Kind.ISTORE;
                    }
                    btcInstr = new BtcSTORE(kind, BtcGen.btcLocals.get((MemRelAccess) access).index);
                }
            } else if (decl instanceof AstParDecl) {
                BtcSTORE.Kind kind = null;
                if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemInt) {
                    kind = BtcSTORE.Kind.LSTORE;
                } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemChar) {
                    kind = BtcSTORE.Kind.ISTORE;
                }
                MemRelAccess access = Memory.parAccesses.get(decl);
                btcInstr = new BtcSTORE(kind, BtcGen.btcLocals.get(access).index);
            }
        }
        assignStmt.sndSubExpr.accept(this, btcMethod);
        btcMethod.addInstr(btcInstr);
        lineCounter++;
        return btcInstr;*/
    }


    // Expressions

    /**
     * Visits a constant expression. It uses the appropriate load instruction based on the type of the constant.
     *
     * @param constExpr The constant expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstConstExpr constExpr, BtcMethod btcMethod) {
        BtcInstr btcInstr = null;
        if (SemAn.exprOfType.get(constExpr) instanceof SemInt) {
            //btcInstr = new BtcCONST(((ImcCONST) ImcGen.exprImc.get(constExpr)).value, BtcCONST.Type.LONG);
            btcInstr = new BtcCONST(1, BtcCONST.Type.LONG);
        } else if (SemAn.exprOfType.get(constExpr) instanceof SemChar) {
            btcInstr = new BtcPUSH(((ImcCONST) ImcGen.exprImc.get(constExpr)).value, BtcPUSH.Type.BYTE);
        }
        btcMethod.addInstr(btcInstr);
        lineCounter++;
        return btcInstr;
    }

    /**
     * Visits a name expression. It uses the appropriate load instruction based on the type of the variable. Global
     * variables use the getstatic instruction, and local variables use the load instruction.
     *
     * @param nameExpr  The name expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstNameExpr nameExpr, BtcMethod btcMethod) {
        AstDecl decl = SemAn.declaredAt.get(nameExpr);
        MemAccess access = null;
        if (decl instanceof AstVarDecl) {
            access = Memory.varAccesses.get(decl);
            if (access instanceof MemAbsAccess) {
                BtcInstr btcInstr = new BtcACCESS(BtcACCESS.Dir.GET, BtcACCESS.Type.STATIC,
                                                  BtcGen.btcFields.get(access).index);
                btcMethod.addInstr(btcInstr);
                lineCounter++;
                return btcInstr;
            }
        }
        if (decl instanceof AstParDecl) {
            access = Memory.parAccesses.get(decl);
        }

        BtcLOAD.Type type = null;

        if (SemAn.exprOfType.get(nameExpr) instanceof SemInt) {
            type = BtcLOAD.Type.LLOAD;
        } else if (SemAn.exprOfType.get(nameExpr) instanceof SemChar) {
            type = BtcLOAD.Type.ILOAD;
        }

        BtcInstr btcInstr = new BtcLOAD(BtcGen.btcLocals.get((MemRelAccess) access).index, type);
        btcMethod.addInstr(btcInstr);
        lineCounter++;
        return btcInstr;
    }

    /**
     * Visits a binary expression. It computes the value of the left and right subexpressions, and uses the appropriate
     * arithmetic instruction based on the type of the expression.
     *
     * @param binExpr   The binary expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstBinExpr binExpr, BtcMethod btcMethod) {
        binExpr.fstSubExpr.accept(this, btcMethod);
        binExpr.sndSubExpr.accept(this, btcMethod);

        BtcARITHM.Type atype = null;

        if (SemAn.exprOfType.get(binExpr) instanceof SemInt) {
            atype = BtcARITHM.Type.LONG;
        } else if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
            atype = BtcARITHM.Type.INT;
        }

        // Arithmetic expression.
        switch (binExpr.oper) {
            case ADD -> {
                BtcInstr btcInstr = new BtcARITHM(BtcARITHM.Oper.ADD, atype);
                btcMethod.addInstr(btcInstr);
                lineCounter++;
                return btcInstr;
            }
            case SUB -> {
                BtcInstr btcInstr = new BtcARITHM(BtcARITHM.Oper.SUB, atype);
                btcMethod.addInstr(btcInstr);
                lineCounter++;
                return btcInstr;
            }
            case MUL -> {
                BtcInstr btcInstr = new BtcARITHM(BtcARITHM.Oper.MUL, atype);
                btcMethod.addInstr(btcInstr);
                lineCounter++;
                return btcInstr;
            }
            case DIV -> {
                BtcInstr btcInstr = new BtcARITHM(BtcARITHM.Oper.DIV, atype);
                btcMethod.addInstr(btcInstr);
                lineCounter++;
                return btcInstr;
            }
            case MOD -> {
                BtcInstr btcInstr = new BtcARITHM(BtcARITHM.Oper.REM, atype);
                btcMethod.addInstr(btcInstr);
                lineCounter++;
                return btcInstr;
            }
        }

        // Logical expression.

        if (atype == BtcARITHM.Type.LONG) {
            BtcInstr btcInstr = new BtcCMP(BtcCMP.Oper.CMP, BtcCMP.Type.LONG);
            btcMethod.addInstr(btcInstr);
            lineCounter++;
            return btcInstr;
        }

        return null;
    }

    /**
     * Visits  a prefix expression. It computes the value of the subexpression, and uses the appropriate arithmetic
     * instruction based on the type of the expression.
     *
     * @param preExpr   The prefix expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstPreExpr preExpr, BtcMethod btcMethod) {
        BtcInstr subInstr = preExpr.subExpr.accept(this, btcMethod);
        BtcInstr btcInstr = null;
        switch (preExpr.oper) {
            case SUB -> {
                if (SemAn.exprOfType.get(preExpr) instanceof SemInt) {
                    btcInstr = new BtcARITHM(BtcARITHM.Oper.NEG, BtcARITHM.Type.LONG);
                } else if (SemAn.exprOfType.get(preExpr) instanceof SemChar) {
                    btcInstr = new BtcARITHM(BtcARITHM.Oper.NEG, BtcARITHM.Type.INT);
                }
            }
            case NOT -> {

            }
            case NEW -> {
                if (SemAn.exprOfType.get(preExpr) instanceof SemInt) {
                    btcInstr = new BtcNEWARRAY(BtcNEWARRAY.Type.T_LONG);
                } else if (SemAn.exprOfType.get(preExpr) instanceof SemChar) {
                    btcInstr = new BtcNEWARRAY(BtcNEWARRAY.Type.T_CHAR);
                }
            }
            case DEL -> {
                // Ignore.
            }
            case PTR -> {
                // TODO: Implement referencing.
            }
            // TODO: Implement other operators.
        }
        btcMethod.addInstr(btcInstr);
        lineCounter++;
        return btcInstr;
    }

    /**
     * Visits a postfix expression. It computes the value of the subexpression, and uses the appropriate arithmetic
     * instruction based on the type of the expression.
     *
     * @param pstExpr   The postfix expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstPstExpr pstExpr, BtcMethod btcMethod) {
        BtcInstr btcInstr = pstExpr.subExpr.accept(this, btcMethod);
        return btcInstr;
    }

    /**
     * Visits a call expression. It computes the value of the arguments, and uses the appropriate call instruction based
     * on the type of the expression.
     *
     * @param callExpr  The call expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstCallExpr callExpr, BtcMethod btcMethod) {
        // TODO: Implement call expression.
        callExpr.args.accept(this, btcMethod);
        return null;
    }

    /**
     * Visits a cast expression. It computes the value of the subexpression, and generates the cast instruction based on
     * the type of the subexpression (fromType) and the target type (toType).
     *
     * @param castExpr  The cast expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstCastExpr castExpr, BtcMethod btcMethod) {
        BtcCAST.Type from = null;
        BtcCAST.Type to = null;

        if (SemAn.exprOfType.get(castExpr.subExpr) instanceof SemInt) {
            from = BtcCAST.Type.LONG;
        } else if (SemAn.exprOfType.get(castExpr.subExpr) instanceof SemChar) {
            from = BtcCAST.Type.CHAR;
        }

        if (SemAn.describesType.get(castExpr.type) instanceof SemInt) {
            to = BtcCAST.Type.LONG;
        } else if (SemAn.describesType.get(castExpr.type) instanceof SemChar) {
            to = BtcCAST.Type.CHAR;
        }

        castExpr.subExpr.accept(this, btcMethod);

        BtcCAST btcInstr = new BtcCAST(from, to);
        btcMethod.addInstr(btcInstr);
        lineCounter++;
        return btcInstr;
    }

    /**
     * Visits a where expression. It first visits the where expression declarations, and then the subexpression.
     *
     * @param whereExpr The where expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstWhereExpr whereExpr, BtcMethod btcMethod) {
        whereExpr.decls.accept(this, btcMethod);
        whereExpr.subExpr.accept(this, btcMethod);
        return null;
    }

    /**
     * Visits a statement expression. It visits all the statements in the expression, and then generates the appropriate
     * return instruction if an expression statement is the last statement in the list.
     *
     * @param stmtExpr  The statement expression.
     * @param btcMethod The current bytecode method.
     * @return The bytecode instructions.
     */
    public BtcInstr visit(AstStmtExpr stmtExpr, BtcMethod btcMethod) {
        stmtExpr.stmts.asts().forEach(stmt -> stmt.accept(this, btcMethod));

        AstStmt lastStmt = stmtExpr.stmts.asts().get(stmtExpr.stmts.asts().size() - 1);

        if (lastStmt instanceof AstExprStmt) {
            BtcRETURN.Type type = null;
            if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemVoid) {
                type = BtcRETURN.Type.RETURN;
            } else if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemInt) {
                type = BtcRETURN.Type.LRETURN;
            } else if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemChar) {
                type = BtcRETURN.Type.IRETURN;
            }
            BtcInstr btcInstr = new BtcRETURN(type);
            btcMethod.addInstr(btcInstr);
            return btcInstr;
        }
        return null;
    }

}
