package pins.phase.btcgen;

import pins.common.report.Report;
import pins.data.ast.*;
import pins.data.ast.visitor.AstVisitor;
import pins.data.btc.method.BtcMETHOD;
import pins.data.btc.method.instr.BtcInstr;
import pins.data.btc.method.instr.arithm.BtcARITHM;
import pins.data.btc.method.instr.arithm.BtcCAST;
import pins.data.btc.method.instr.arithm.BtcCMP;
import pins.data.btc.method.instr.ctrl.BtcCJUMP;
import pins.data.btc.method.instr.ctrl.BtcGOTO;
import pins.data.btc.method.instr.ctrl.BtcRETURN;
import pins.data.btc.method.instr.object.BtcACCESS;
import pins.data.btc.method.instr.object.BtcASTORE;
import pins.data.btc.method.instr.object.BtcINVOKE;
import pins.data.btc.method.instr.stack.BtcCONST;
import pins.data.btc.method.instr.stack.BtcLDC;
import pins.data.btc.method.instr.stack.BtcLOAD;
import pins.data.btc.method.instr.stack.BtcSTORE;
import pins.data.btc.var.BtcFIELD;
import pins.data.btc.var.BtcLOCAL;
import pins.data.mem.MemAbsAccess;
import pins.data.mem.MemAccess;
import pins.data.mem.MemRelAccess;
import pins.data.typ.*;
import pins.phase.memory.Memory;
import pins.phase.seman.SemAn;

/**
 * Bytecode method generator.
 */
public class MethodGenerator implements AstVisitor<BtcInstr, BtcMETHOD> {

    @Override
    public BtcInstr visit(ASTs<?> asts, BtcMETHOD btcMethod) {
        for (AST ast : asts.asts()) {
            ast.accept(this, btcMethod);
        }
        return null;
    }

    // DECLARATIONS

    @Override
    public BtcInstr visit(AstFunDecl funDecl, BtcMETHOD btcMethod) {
        BtcMETHOD.Type type = null;
        if (SemAn.describesType.get(funDecl.type) instanceof SemInt) {
            type = BtcMETHOD.Type.LONG;
        } else if (SemAn.describesType.get(funDecl.type) instanceof SemChar) {
            type = BtcMETHOD.Type.INT;
        } else if (SemAn.describesType.get(funDecl.type) instanceof SemArr) {
            type = BtcMETHOD.Type.ARRAY;
        } else if (SemAn.describesType.get(funDecl.type) instanceof SemPtr) {
            type = BtcMETHOD.Type.OBJECT;
        } else if (SemAn.describesType.get(funDecl.type) instanceof SemVoid) {
            type = BtcMETHOD.Type.VOID;
        } else {
            throw new Report.InternalError();
        }

        System.out.println("Generating bytecode for method " + funDecl.name + " of type " + type);
        System.out.println("Frame depth is: " + Memory.frames.get(funDecl).depth);

        // Top level function.
        if (Memory.frames.get(funDecl).depth == 1) {
            btcMethod = new BtcMETHOD(funDecl.name, type);
            BtcGen.btcClasses.peek().addMethod(btcMethod);

            for (AstParDecl parDecl : funDecl.pars.asts()) {
                parDecl.accept(this, btcMethod);
            }

            funDecl.expr.accept(this, btcMethod);

            if (btcMethod.instrs().size() == 0) {
                // TODO: Remove this once everything is implemented.
                return null;
            }

            // If the last instruction of a method is not a return instruction, artificially add it.
            if (!(btcMethod.instrs().get(btcMethod.instrs().size() - 1) instanceof BtcRETURN)) {
                BtcRETURN.Type returnInstrType = null;
                switch (type) {
                    case INT -> returnInstrType = BtcRETURN.Type.LONG;
                    case FLOAT -> returnInstrType = BtcRETURN.Type.FLOAT;
                    case LONG -> returnInstrType = BtcRETURN.Type.LONG;
                    case DOUBLE -> returnInstrType = BtcRETURN.Type.DOUBLE;
                    case ARRAY, OBJECT -> returnInstrType = BtcRETURN.Type.REFERENCE;
                    case VOID -> returnInstrType = BtcRETURN.Type.VOID;
                }
                BtcRETURN btcReturn = new BtcRETURN(btcMethod.instrCount(), returnInstrType);
                btcMethod.addInstr(btcReturn);
            }
        }
        // Nested function.
        else {
            // TODO: Implement nested function generation.
        }
        return null;
    }

    @Override
    public BtcInstr visit(AstParDecl parDecl, BtcMETHOD btcMethod) {
        BtcLOCAL.Type type = null;
        if (SemAn.describesType.get(parDecl.type) instanceof SemInt) {
            type = BtcLOCAL.Type.LONG;
        } else if (SemAn.describesType.get(parDecl.type) instanceof SemChar) {
            type = BtcLOCAL.Type.INT;
        } else if (SemAn.describesType.get(parDecl.type) instanceof SemArr) {
            type = BtcLOCAL.Type.ARRAY;
        } else if (SemAn.describesType.get(parDecl.type) instanceof SemPtr) {
            type = BtcLOCAL.Type.OBJECT;
        } else {
            throw new Report.InternalError();
        }

        MemRelAccess access = Memory.parAccesses.get(parDecl);

        BtcLOCAL btcLocal = new BtcLOCAL(btcMethod.localCount(), type);
        btcMethod.addPar(access, btcLocal);
        return null;
    }

    @Override
    public BtcInstr visit(AstVarDecl varDecl, BtcMETHOD btcMethod) {
        BtcLOCAL.Type type = null;
        if (SemAn.describesType.get(varDecl.type) instanceof SemInt) {
            type = BtcLOCAL.Type.LONG;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemChar) {
            type = BtcLOCAL.Type.INT;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemArr) {
            type = BtcLOCAL.Type.ARRAY;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemPtr) {
            type = BtcLOCAL.Type.OBJECT;
        } else {
            throw new Report.InternalError();
        }

        MemRelAccess access = (MemRelAccess) Memory.varAccesses.get(varDecl);

        BtcLOCAL btcLocal = new BtcLOCAL(btcMethod.localCount(), type);
        btcMethod.addLocal(access, btcLocal);
        return null;
    }

    // STATEMENTS

    @Override
    public BtcInstr visit(AstIfStmt ifStmt, BtcMETHOD btcMethod) {
        ifStmt.condExpr.accept(this, btcMethod);
        BtcCJUMP.Oper oper = null;
        if (ifStmt.condExpr instanceof AstBinExpr) {
            AstBinExpr binExpr = (AstBinExpr) ifStmt.condExpr;
            switch (binExpr.oper) {
                case EQU -> oper = BtcCJUMP.Oper.NE;
                case NEQ -> oper = BtcCJUMP.Oper.EQ;
                case LTH -> oper = BtcCJUMP.Oper.GE;
                case LEQ -> oper = BtcCJUMP.Oper.GT;
                case GTH -> oper = BtcCJUMP.Oper.LE;
                case GEQ -> oper = BtcCJUMP.Oper.LT;
            }
        }

        if (ifStmt.elseBodyStmt == null) {
            BtcCJUMP btcCjump = new BtcCJUMP(btcMethod.instrCount(), oper);
            btcMethod.addInstr(btcCjump);
            ifStmt.thenBodyStmt.accept(this, btcMethod);
            btcCjump.setTarget(btcMethod.instrCount());
            return btcCjump;
        } else {
            BtcCJUMP btcCjump = new BtcCJUMP(btcMethod.instrCount(), oper);
            btcMethod.addInstr(btcCjump);
            ifStmt.thenBodyStmt.accept(this, btcMethod);
            BtcGOTO btcJump = new BtcGOTO(btcMethod.instrCount());
            btcMethod.addInstr(btcJump);
            btcCjump.setTarget(btcMethod.instrCount());
            ifStmt.elseBodyStmt.accept(this, btcMethod);
            btcJump.setTarget(btcMethod.instrCount());
            return btcJump;
        }
    }

    @Override
    public BtcInstr visit(AstWhileStmt whileStmt, BtcMETHOD btcMethod) {
        BtcCJUMP.Oper oper = null;
        if (whileStmt.condExpr instanceof AstBinExpr) {
            AstBinExpr binExpr = (AstBinExpr) whileStmt.condExpr;
            switch (binExpr.oper) {
                case EQU -> oper = BtcCJUMP.Oper.EQ;
                case NEQ -> oper = BtcCJUMP.Oper.NE;
                case LTH -> oper = BtcCJUMP.Oper.LT;
                case LEQ -> oper = BtcCJUMP.Oper.LE;
                case GTH -> oper = BtcCJUMP.Oper.GT;
                case GEQ -> oper = BtcCJUMP.Oper.GE;
            }
        }

        // TODO: Fix this.
        int target = btcMethod.instrCount();
        whileStmt.bodyStmt.accept(this, btcMethod);

        whileStmt.condExpr.accept(this, btcMethod);
        BtcCJUMP btcCjump = new BtcCJUMP(btcMethod.instrCount(), oper);
        btcCjump.setTarget(target);
        btcMethod.addInstr(btcCjump);
        return btcCjump;
    }

    @Override
    public BtcInstr visit(AstExprStmt exprStmt, BtcMETHOD btcMethod) {
        return exprStmt.expr.accept(this, btcMethod);
    }

    @Override
    public BtcInstr visit(AstAssignStmt assignStmt, BtcMETHOD btcMethod) {
        BtcInstr sndSubExpr = assignStmt.sndSubExpr.accept(this, btcMethod);

        // Storing into array.
        if (assignStmt.fstSubExpr instanceof AstBinExpr) {
            AstBinExpr binExpr = (AstBinExpr) assignStmt.fstSubExpr;
            if (binExpr.oper == AstBinExpr.Oper.ARR) {
                BtcASTORE.Type type = null;

                if (SemAn.exprOfType.get(binExpr) instanceof SemInt) {
                    type = BtcASTORE.Type.LONG;
                } else if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
                    type = BtcASTORE.Type.INT;
                } else {
                    throw new Report.InternalError();
                }

                BtcInstr btcInstr = new BtcASTORE(btcMethod.instrCount(), type);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
        }
        // Storing into variable.
        else if (assignStmt.fstSubExpr instanceof AstNameExpr) {
            AstDecl decl = SemAn.declaredAt.get(assignStmt.fstSubExpr);
            MemAccess access = null;
            if (decl instanceof AstVarDecl) {
                access = Memory.varAccesses.get(decl);
            } else if (decl instanceof AstParDecl) {
                access = Memory.parAccesses.get(decl);
            }

            if (access instanceof MemAbsAccess) {
                BtcFIELD btcField = BtcGen.btcClasses.peek().getField((AstVarDecl) decl);
                BtcInstr btcInstr = new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.PUT, btcField);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            } else if (access instanceof MemRelAccess) {
                BtcSTORE.Type type = null;
                if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemInt) {
                    type = BtcSTORE.Type.LONG;
                } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemChar) {
                    type = BtcSTORE.Type.INT;
                } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemArr) {
                    type = BtcSTORE.Type.ARR;
                } else {
                    throw new Report.InternalError();
                }
                BtcLOCAL btcLocal = btcMethod.getLocal((MemRelAccess) access);
                BtcInstr btcInstr = new BtcSTORE(btcMethod.instrCount(), btcLocal.index, type);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
        }
        else {
            throw new Report.InternalError();
        }
        return null;
    }

    // EXPRESSIONS

    @Override
    public BtcInstr visit(AstConstExpr constExpr, BtcMETHOD btcMethod) {
        BtcInstr btcInstr = null;

        if (SemAn.exprOfType.get(constExpr) instanceof  SemInt) {
            final long value = Long.parseLong(constExpr.name);

            if (value == 0 || value == 1) {
                btcInstr = new BtcCONST(btcMethod.instrCount(), value, BtcCONST.Type.LONG);
            } else {
                btcInstr = new BtcLDC(btcMethod.instrCount(), value, BtcLDC.Type.LONG);
            }

        } else if (SemAn.exprOfType.get(constExpr) instanceof SemChar) {
            final int value = constExpr.name.length() == 3? constExpr.name.charAt(1) : constExpr.name.charAt(2);

            if (value == 0 || value == 1) {
                btcInstr = new BtcCONST(btcMethod.instrCount(), value, BtcCONST.Type.INT);
            } else {
                btcInstr = new BtcLDC(btcMethod.instrCount(), value, BtcLDC.Type.DEFAULT);
            }

        } else {
            throw new Report.InternalError();
        }
        btcMethod.addInstr(btcInstr);
        return btcInstr;
    }

    @Override
    public BtcInstr visit(AstNameExpr nameExpr, BtcMETHOD btcMethod) {
        AstDecl decl = SemAn.declaredAt.get(nameExpr);
        MemAccess memAccess = null;
        if (decl instanceof AstVarDecl) {
            if (Memory.varAccesses.get(decl) instanceof MemAbsAccess) {
                BtcFIELD field = BtcGen.btcClasses.peek().getField((AstVarDecl) decl);
                BtcInstr btcInstr = new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, field);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            } else {
                memAccess = Memory.varAccesses.get(decl);
            }
        } else {
            memAccess = Memory.parAccesses.get(decl);
        }

        BtcLOAD.Type type = null;

        if (SemAn.exprOfType.get(nameExpr) instanceof SemInt) {
            type = BtcLOAD.Type.LONG;
        } else if (SemAn.exprOfType.get(nameExpr) instanceof SemChar) {
            type = BtcLOAD.Type.INT;
        } else if (SemAn.exprOfType.get(nameExpr) instanceof SemArr) {
            type = BtcLOAD.Type.ARR;
        } else if (SemAn.exprOfType.get(nameExpr) instanceof SemPtr) {
            type = BtcLOAD.Type.ARR;
        } else {
            throw new Report.InternalError();
        }

        BtcLOCAL btcLocal = btcMethod.getLocal((MemRelAccess) memAccess);
        BtcInstr btcInstr = new BtcLOAD(btcMethod.instrCount(), btcLocal.index, type);
        btcMethod.addInstr(btcInstr);
        return btcInstr;
    }

    @Override
    public BtcInstr visit(AstBinExpr binExpr, BtcMETHOD btcMethod) {
        BtcInstr fstSubExpr = binExpr.fstSubExpr.accept(this, btcMethod);
        BtcInstr sndSubExpr = binExpr.sndSubExpr.accept(this, btcMethod);

        BtcARITHM.Type operType = null;

        if (SemAn.exprOfType.get(binExpr) instanceof SemInt) {
            operType = BtcARITHM.Type.LONG;
        } else if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
            operType = BtcARITHM.Type.INT;
        } else {
            throw new Report.InternalError();
        }

        // Arithmetic expression.
        switch (binExpr.oper) {
            case ADD -> {
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.ADD, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case SUB -> {
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.SUB, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case MUL -> {
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.MUL, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case DIV -> {
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.DIV, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case MOD -> {
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.REM, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
        }

        if (binExpr.oper == AstBinExpr.Oper.ARR) {
            // TODO: Implement array.
            return null;
        }

        // Logical expression.
        if (operType == BtcARITHM.Type.LONG) {
            BtcInstr btcInstr = new BtcCMP(btcMethod.instrCount(), BtcCMP.Oper.CMP, BtcCMP.Type.LONG);
            btcMethod.addInstr(btcInstr);
            return btcInstr;
        } else {
            // TODO: Implement char.
            BtcInstr btcInstr = new BtcCMP(btcMethod.instrCount(), BtcCMP.Oper.CMP, BtcCMP.Type.LONG);
            btcMethod.addInstr(btcInstr);
            return btcInstr;
        }

    }

    @Override
    public BtcInstr visit(AstPreExpr preExpr, BtcMETHOD btcMethod) {
        BtcInstr subInstr = preExpr.subExpr.accept(this, btcMethod);
        BtcInstr btcInstr = null;
        switch (preExpr.oper) {
            case SUB -> {
                if (SemAn.exprOfType.get(preExpr) instanceof SemInt) {
                    btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.NEG, BtcARITHM.Type.LONG);
                } else {
                    throw new Report.InternalError();
                }
            }
            case NOT -> {

            }
            case NEW -> {
                // TODO: Implement arrays.
            }
            case DEL -> {
                // TODO: Nullify the pointer.
            }
            case PTR -> {
                // TODO: Implement referencing.
            }
            case ADD -> {
                // Ignore, it's just a unary plus (identity).
            }
        }
        btcMethod.addInstr(btcInstr);
        return btcInstr;
    }

    @Override
    public BtcInstr visit(AstPstExpr pstExpr, BtcMETHOD btcMethod) {
        BtcInstr btcInstr = pstExpr.subExpr.accept(this, btcMethod);
        // TODO: Implement de-referencing.
        return btcInstr;
    }

    @Override
    public BtcInstr visit(AstCallExpr callExpr, BtcMETHOD btcMethod) {
        callExpr.args.accept(this, btcMethod);

        BtcINVOKE.Type type = null;
        for (BtcMETHOD.Flags flag : btcMethod.flags()) {
            if (flag == BtcMETHOD.Flags.STATIC) {
                type = BtcINVOKE.Type.STATIC;
                break;
            }
        }
        if (type == null) {
            type = BtcINVOKE.Type.DYNAMIC;
        }

        BtcInstr btcInstr = new BtcINVOKE(btcMethod.instrCount(), type, callExpr.name);
        btcMethod.addInstr(btcInstr);
        return btcInstr;
    }

    @Override
    public BtcInstr visit(AstCastExpr castExpr, BtcMETHOD btcMethod) {
        BtcCAST.Type from = null;
        BtcCAST.Type to = null;

        if (SemAn.exprOfType.get(castExpr.subExpr) instanceof SemInt) {
            from = BtcCAST.Type.LONG;
        } else if (SemAn.exprOfType.get(castExpr.subExpr) instanceof SemChar) {
            from = BtcCAST.Type.INT;
        } else {
            throw new Report.InternalError();
        }

        if (SemAn.describesType.get(castExpr.type) instanceof SemInt) {
            to = BtcCAST.Type.LONG;
        } else if (SemAn.describesType.get(castExpr.type) instanceof SemChar) {
            to = BtcCAST.Type.INT;
        } else {
            throw new Report.InternalError();
        }

        castExpr.subExpr.accept(this, btcMethod);
        BtcCAST btcCast = new BtcCAST(btcMethod.instrCount(), from, to);
        btcMethod.addInstr(btcCast);
        return btcCast;
    }

    @Override
    public BtcInstr visit(AstWhereExpr whereExpr, BtcMETHOD btcMethod) {
        whereExpr.decls.accept(this, btcMethod);
        return whereExpr.subExpr.accept(this, btcMethod);
    }

    @Override
    public BtcInstr visit(AstStmtExpr stmtExpr, BtcMETHOD btcMethod) {
        stmtExpr.stmts.asts().forEach(stmt -> stmt.accept(this, btcMethod));
        AstStmt lastStmt = stmtExpr.stmts.asts().get(stmtExpr.stmts.asts().size() - 1);

        if (lastStmt instanceof AstExprStmt) {
            BtcRETURN.Type type = null;
            if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemInt) {
                type = BtcRETURN.Type.LONG;
            } else if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemChar) {
                type = BtcRETURN.Type.INT;
            } else if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemArr) {
                type = BtcRETURN.Type.REFERENCE;
            } else if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemPtr) {
                type = BtcRETURN.Type.REFERENCE;
            } else if (SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr) instanceof SemVoid) {
                type = BtcRETURN.Type.VOID;
            } else {
                throw new Report.InternalError();
            }
            BtcRETURN btcReturn = new BtcRETURN(btcMethod.instrCount(), type);
            btcMethod.addInstr(btcReturn);
            return btcReturn;
        }
        return null;
    }

}
