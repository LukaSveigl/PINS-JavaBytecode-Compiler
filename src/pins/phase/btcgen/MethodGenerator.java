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
import pins.data.btc.method.instr.object.*;
import pins.data.btc.method.instr.stack.BtcCONST;
import pins.data.btc.method.instr.stack.BtcLDC;
import pins.data.btc.method.instr.stack.BtcLOAD;
import pins.data.btc.method.instr.stack.BtcSTORE;
import pins.data.btc.var.BtcFIELD;
import pins.data.btc.var.BtcLOCAL;
import pins.data.btc.var.BtcVar;
import pins.data.mem.MemAbsAccess;
import pins.data.mem.MemAccess;
import pins.data.mem.MemRelAccess;
import pins.data.typ.*;
import pins.phase.memory.Memory;
import pins.phase.seman.SemAn;

import java.util.Map;

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

        // Top level function.
        if (Memory.frames.get(funDecl).depth == 1) {
            btcMethod = new BtcMETHOD(funDecl.name, type);
            BtcGen.btcClasses.peek().addMethod(btcMethod);

            for (AstParDecl parDecl : funDecl.pars.asts()) {
                parDecl.accept(this, btcMethod);
            }

            // In main function, initialize all the global arrays.
            if (funDecl.name.equals("main")) {
                for (Map.Entry<AstVarDecl, BtcFIELD> entry : BtcGen.btcClasses.peek().fields().entrySet()) {
                    if (entry.getValue().type == BtcVar.Type.ARRAY) {
                        entry.getKey().accept(this, btcMethod);
                    }
                }
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
                    case INT -> returnInstrType = BtcRETURN.Type.INT;
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
            // This should never happen, due to the specification regarding parameters.
            type = BtcLOCAL.Type.ARRAY;
        } else if (SemAn.describesType.get(parDecl.type) instanceof SemPtr) {
            type = BtcLOCAL.Type.ARRAY;
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

        if (Memory.varAccesses.get(varDecl) instanceof MemAbsAccess) {
            // Type will always be an array in case of global values.

            BtcNEWARRAY.Type subType = null;

            if (((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType() instanceof SemInt) {
                subType = BtcNEWARRAY.Type.LONG;
            } else if (((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType() instanceof SemChar) {
                subType = BtcNEWARRAY.Type.CHAR;
            }

            BtcFIELD btcFIELD = BtcGen.btcClasses.peek().getField(varDecl);

            long numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
            btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
            btcMethod.addInstr(new BtcNEWARRAY(btcMethod.instrCount(), subType));
            btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.PUT, btcFIELD));

            return null;
        }

        MemRelAccess access = (MemRelAccess) Memory.varAccesses.get(varDecl);

        BtcLOCAL btcLocal = new BtcLOCAL(btcMethod.localCount(), type);
        btcMethod.addLocal(access, btcLocal);

        if (type == BtcLOCAL.Type.ARRAY) {
            BtcNEWARRAY.Type subType = null;

            if (((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType() instanceof SemInt) {
                subType = BtcNEWARRAY.Type.LONG;
            } else if (((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType() instanceof SemChar) {
                subType = BtcNEWARRAY.Type.CHAR;
            }

            long numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
            btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
            btcMethod.addInstr(new BtcNEWARRAY(btcMethod.instrCount(), subType));

            btcMethod.addInstr(new BtcSTORE(btcMethod.instrCount(), btcLocal.index, BtcSTORE.Type.ARR));
        }

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
                case EQU -> oper = BtcCJUMP.Oper.NE;
                case NEQ -> oper = BtcCJUMP.Oper.EQ;
                case LTH -> oper = BtcCJUMP.Oper.GE;
                case LEQ -> oper = BtcCJUMP.Oper.GT;
                case GTH -> oper = BtcCJUMP.Oper.LE;
                case GEQ -> oper = BtcCJUMP.Oper.LT;
            }
        }

        int start = btcMethod.instrCount();
        whileStmt.condExpr.accept(this, btcMethod);
        BtcCJUMP btcCJUMP = new BtcCJUMP(btcMethod.instrCount(), oper);
        btcMethod.addInstr(btcCJUMP);

        whileStmt.bodyStmt.accept(this, btcMethod);

        BtcGOTO btcGOTO = new BtcGOTO(btcMethod.instrCount());
        btcGOTO.setTarget(start);
        btcMethod.addInstr(btcGOTO);

        btcCJUMP.setTarget(btcMethod.instrCount());

        return btcCJUMP;
    }

    @Override
    public BtcInstr visit(AstExprStmt exprStmt, BtcMETHOD btcMethod) {
        return exprStmt.expr.accept(this, btcMethod);
    }

    @Override
    public BtcInstr visit(AstAssignStmt assignStmt, BtcMETHOD btcMethod) {
        //BtcInstr sndSubExpr = assignStmt.sndSubExpr.accept(this, btcMethod);

        // Storing into array.
        if (assignStmt.fstSubExpr instanceof AstBinExpr) {
            AstBinExpr binExpr = (AstBinExpr) assignStmt.fstSubExpr;
            // Pointer to array.
            /*if (binExpr.fstSubExpr instanceof AstPstExpr) {

            }
            else*/ if (binExpr.oper == AstBinExpr.Oper.ARR) {
                BtcASTORE.Type type = null;

                if (SemAn.exprOfType.get(binExpr) instanceof SemInt) {
                    type = BtcASTORE.Type.LONG;
                } else if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
                    type = BtcASTORE.Type.INT;
                } else {
                    throw new Report.InternalError();
                }

                //AstNameExpr nameExpr = (AstNameExpr) ((AstBinExpr)assignStmt.fstSubExpr).fstSubExpr;

                AstNameExpr nameExpr = null;

                if (((AstBinExpr)assignStmt.fstSubExpr).fstSubExpr instanceof AstPstExpr) {
                    nameExpr = (AstNameExpr) ((AstPstExpr) ((AstBinExpr) assignStmt.fstSubExpr).fstSubExpr).subExpr;
                } else {
                    nameExpr = (AstNameExpr) ((AstBinExpr)assignStmt.fstSubExpr).fstSubExpr;
                }

                AstDecl decl = SemAn.declaredAt.get(nameExpr);
                MemAccess access = null;
                if (decl instanceof AstVarDecl) {
                    access = Memory.varAccesses.get(decl);
                } else if (decl instanceof AstParDecl) {
                    access = Memory.parAccesses.get(decl);
                }

                if (access instanceof MemAbsAccess) {
                    BtcFIELD btcFIELD = BtcGen.btcClasses.peek().getField((AstVarDecl) decl);
                    btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, btcFIELD));

                    BtcInstr fstSubExpr = ((AstBinExpr) assignStmt.fstSubExpr).sndSubExpr.accept(this, btcMethod);
                    btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                    BtcInstr sndSubExpr = assignStmt.sndSubExpr.accept(this, btcMethod);
                } else if (access instanceof MemRelAccess) {
                    BtcLOCAL btcLOCAL = btcMethod.getLocal((MemRelAccess) access);
                    btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLOCAL.index, BtcLOAD.Type.ARR));

                    BtcInstr fstSubExpr = ((AstBinExpr) assignStmt.fstSubExpr).sndSubExpr.accept(this, btcMethod);
                    btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                    BtcInstr sndSubExpr = assignStmt.sndSubExpr.accept(this, btcMethod);
                }

                BtcInstr btcInstr = new BtcASTORE(btcMethod.instrCount(), type);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
        }
        // Storing into pointer.
        else if (assignStmt.fstSubExpr instanceof AstPstExpr) {

            AstPstExpr pstExpr = (AstPstExpr) assignStmt.fstSubExpr;

            if (pstExpr.subExpr instanceof AstNameExpr) {

            } else if (pstExpr.subExpr instanceof AstBinExpr) {

            } else {
                if (pstExpr.subExpr instanceof AstCastExpr) {
                    AstCastExpr castExpr = (AstCastExpr) pstExpr.subExpr;

                    if (castExpr.subExpr instanceof AstBinExpr) {
                        AstBinExpr binExpr = (AstBinExpr) castExpr.subExpr;

                        if (binExpr.fstSubExpr instanceof AstCastExpr) {
                            AstCastExpr castExpr2 = (AstCastExpr) binExpr.fstSubExpr;

                            // Accept the named expression -> variable. Should get the array reference.
                            BtcInstr btcName = castExpr2.subExpr.accept(this, btcMethod);
                            // Accept the second expression -> index. Should get the index.
                            binExpr.sndSubExpr.accept(this, btcMethod);
                            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                            // Accept the right hand side of the assignment -> value.
                            assignStmt.sndSubExpr.accept(this, btcMethod);

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
                        } else {

                        }
                    } else {
                        throw new Report.InternalError();
                    }
                } else {
                    throw new Report.InternalError();
                }
            }

        }
        // Storing into variable.
        else if (assignStmt.fstSubExpr instanceof AstNameExpr) {
            BtcInstr sndSubExpr = assignStmt.sndSubExpr.accept(this, btcMethod);

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
                } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemPtr) {
                    type = BtcSTORE.Type.ARR;
                }
                else {
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

        } else if (SemAn.exprOfType.get(constExpr) instanceof SemVoid) {
            btcInstr = new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.VOID);
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
            // TODO: Fix casts to depend on types.
            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
            BtcInstr btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.LONG);
            btcMethod.addInstr(btcInstr);
            return btcInstr;
        }

        if (SemAn.exprOfType.get(binExpr.fstSubExpr) instanceof SemChar) {
            operType = BtcARITHM.Type.INT;
        }

        // Logical expression.
        if (operType == BtcARITHM.Type.LONG) {
            BtcInstr btcInstr = new BtcCMP(btcMethod.instrCount(), BtcCMP.Oper.CMP, BtcCMP.Type.LONG);
            btcMethod.addInstr(btcInstr);
            return btcInstr;
        } else {
            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.INT, BtcCAST.Type.LONG));
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
                BtcNEWARRAY.Type type = null;

                if (SemAn.exprOfType.get(preExpr.subExpr) instanceof SemInt) {
                    type = BtcNEWARRAY.Type.LONG;
                } else if (SemAn.exprOfType.get(preExpr.subExpr) instanceof SemChar) {
                    type = BtcNEWARRAY.Type.INT;
                } else {
                    throw new Report.InternalError();
                }

                btcInstr = new BtcNEWARRAY(btcMethod.instrCount(), type);
                // TODO: Implement arrays.
            }
            case DEL -> {
                BtcCONST btcConst = new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.VOID);

                AstVarDecl varDecl = (AstVarDecl) SemAn.declaredAt.get((AstName) preExpr.subExpr);
                MemRelAccess localAccess = (MemRelAccess) Memory.varAccesses.get(varDecl);

                BtcLOCAL btcLocal = btcMethod.getLocal(localAccess);

                BtcSTORE btcStore = new BtcSTORE(btcMethod.instrCount(), btcLocal.index, BtcSTORE.Type.ARR);

                btcMethod.addInstr(btcConst);
                btcMethod.addInstr(btcStore);
                return btcStore;
                // TODO: Nullify the pointer.
            }
            case PTR -> {
                // TODO: Implement referencing.
                return null;
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
        //BtcInstr btcInstr = pstExpr.subExpr.accept(this, btcMethod);
        // TODO: Implement de-referencing.

        /*#{ Pst -> Cast -> Add -> Cast -> Name }#
        (((x: int) + counter) : ^int)^ = counter;*/

        if (pstExpr.subExpr instanceof AstNameExpr) {

        } else if (pstExpr.subExpr instanceof AstBinExpr) {

        } else {
            if (pstExpr.subExpr instanceof AstCastExpr) {
                AstCastExpr castExpr = (AstCastExpr) pstExpr.subExpr;

                if (castExpr.subExpr instanceof AstBinExpr) {
                    AstBinExpr binExpr = (AstBinExpr) castExpr.subExpr;

                    if (binExpr.fstSubExpr instanceof AstCastExpr) {
                        AstCastExpr castExpr2 = (AstCastExpr) binExpr.fstSubExpr;


                        // Accept the named expression -> variable. Should get the array reference.
                        BtcInstr btcName = castExpr2.subExpr.accept(this, btcMethod);
                        // Accept the second expression -> index. Should get the index.
                        binExpr.sndSubExpr.accept(this, btcMethod);
                        btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                        BtcALOAD.Type type = null;

                        if (SemAn.exprOfType.get(binExpr) instanceof SemInt) {
                            type = BtcALOAD.Type.LONG;
                        } else if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
                            type = BtcALOAD.Type.INT;
                        } else {
                            throw new Report.InternalError();
                        }

                        BtcInstr btcInstr = new BtcALOAD(btcMethod.instrCount(), type);
                        btcMethod.addInstr(btcInstr);
                        return btcInstr;
                    } else {

                    }
                } else {
                    throw new Report.InternalError();
                }
            } else {
                throw new Report.InternalError();
            }
        }

        return null;

        //return btcInstr;
    }

    @Override
    public BtcInstr visit(AstCallExpr callExpr, BtcMETHOD btcMethod) {
        BtcINVOKE.Type type = null;

        /*for (BtcMETHOD.Flags flag : btcMethod.flags()) {
            if (flag == BtcMETHOD.Flags.STATIC) {
                type = BtcINVOKE.Type.STATIC;
                break;
            }
        }*/
        /*if (type == null) {
            type = BtcINVOKE.Type.VIRTUAL;

            if (callExpr.name.equals("putInt") || callExpr.name.equals("putChar")) {
                BtcFIELD field = new BtcFIELD("PrintStream", BtcVar.Type.OBJECT, null);
                BtcGen.btcClasses.peek().addField(null, field);
                BtcACCESS fieldRef = new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, field);
                btcMethod.addInstr(fieldRef);
            }
        }*/
        if (callExpr.name.equals("putInt") || callExpr.name.equals("putChar")) {
            type = BtcINVOKE.Type.VIRTUAL;

            boolean exists = false;

            // Check if field exists already.
            for (BtcFIELD bf : BtcGen.btcClasses.peek().fields().values()) {
                if (bf.name.equals("PrintStream")) {
                    exists = true;
                    break;
                }
            }

            BtcFIELD field = null;

            if (!exists) {
                field = new BtcFIELD("PrintStream", BtcVar.Type.OBJECT, null);
                BtcGen.btcClasses.peek().addField(null, field);
            } else {
                field = BtcGen.btcClasses.peek().getField(null);
            }
            BtcACCESS fieldRef = new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, field);
            btcMethod.addInstr(fieldRef);
        } else if (callExpr.name.equals("getInt") || callExpr.name.equals("getChar")) {
            btcMethod.addInstr(new BtcINVOKE(btcMethod.instrCount(), BtcINVOKE.Type.STATIC, "java/lang/System.console:()Ljava/io/Console;"));
            btcMethod.addInstr(new BtcINVOKE(btcMethod.instrCount(), BtcINVOKE.Type.VIRTUAL, "java/io/Console.readLine:()Ljava/lang/String;"));

            if (callExpr.name.equals("getInt")) {
                btcMethod.addInstr(new BtcINVOKE(btcMethod.instrCount(), BtcINVOKE.Type.STATIC, "java/lang/Long.parseLong:(Ljava/lang/String;)J"));
            } else {
                btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));
                btcMethod.addInstr(new BtcINVOKE(btcMethod.instrCount(), BtcINVOKE.Type.VIRTUAL, "java/lang/String.charAt:(I)C"));
            }

            return null;
        } else {
            type = BtcINVOKE.Type.STATIC;
        }

        callExpr.args.accept(this, btcMethod);

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
        } else if (SemAn.exprOfType.get(castExpr.subExpr) instanceof SemPtr) {
            castExpr.subExpr.accept(this, btcMethod);
            return null;
        } else {
            throw new Report.InternalError();
        }

        if (SemAn.describesType.get(castExpr.type) instanceof SemInt) {
            to = BtcCAST.Type.LONG;
        } else if (SemAn.describesType.get(castExpr.type) instanceof SemChar) {
            to = BtcCAST.Type.INT;
        } else if (SemAn.describesType.get(castExpr.type) instanceof SemPtr) {
            castExpr.subExpr.accept(this, btcMethod);
            return null;
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
