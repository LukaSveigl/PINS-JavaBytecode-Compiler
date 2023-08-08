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
import pins.data.btc.method.instr.stack.*;
import pins.data.btc.var.BtcFIELD;
import pins.data.btc.var.BtcLOCAL;
import pins.data.btc.var.BtcVar;
import pins.data.mem.MemAbsAccess;
import pins.data.mem.MemAccess;
import pins.data.mem.MemRelAccess;
import pins.data.typ.*;
import pins.phase.memory.Memory;
import pins.phase.refan.RefAn;
import pins.phase.seman.SemAn;

import java.util.Map;
import java.util.Vector;

/**
 * Bytecode method generator.
 */
public class MethodGenerator implements AstVisitor<BtcInstr, BtcMETHOD> {

    /**
     * Visits the ASTs in the source file and generates the corresponding bytecode methods.
     *
     * @param asts      The ASTs in the source file.
     * @param btcMethod The current bytecode method.
     * @return The result of the visit.
     */
    @Override
    public BtcInstr visit(ASTs<?> asts, BtcMETHOD btcMethod) {
        for (AST ast : asts.asts()) {
            ast.accept(this, btcMethod);
        }
        return null;
    }

    // DECLARATIONS

    /**
     * Visits the function declaration and generates the corresponding bytecode method. It also handles the difference
     * between top level functions and nested functions, by performing lambda lifting on the latter.
     * In case a top level function is the main entry point of the program, it also initializes the class fields that
     * are of the array or pointer type.
     *
     * @param funDecl   The function declaration to visit.
     * @param btcMethod The current bytecode method.
     * @return null.
     * @throws Report.InternalError If the type of the function is not one of the supported types.
     */
    @Override
    public BtcInstr visit(AstFunDecl funDecl, BtcMETHOD btcMethod) {
        BtcMETHOD.Type type;
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

            BtcGen.btcMethods.put(funDecl, btcMethod);

            for (AstParDecl parDecl : funDecl.pars.asts()) {
                parDecl.accept(this, btcMethod);
            }

            // In main function, initialize all the global arrays and pointers.
            if (funDecl.name.equals("main")) {
                for (Map.Entry<AstVarDecl, BtcFIELD> entry : BtcGen.btcClasses.peek().fields().entrySet()) {
                    if (entry.getValue().type == BtcVar.Type.ARRAY) {
                        entry.getKey().accept(this, btcMethod);
                    }
                }
            }

            funDecl.expr.accept(this, btcMethod);

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
            // Create a new method for the nested function. Also perform the necessary renaming.
            BtcMETHOD btcNestedMethod = new BtcMETHOD(btcMethod.name + "$" + funDecl.name, type);
            // Make the nested method a top level method -> lambda lifting.
            BtcGen.btcClasses.peek().addMethod(btcNestedMethod);
            // Store the mapping between the nested function and the newly created method.
            BtcGen.btcMethods.put(funDecl, btcNestedMethod);

            // Pass variables as parameters.
            for (Map.Entry<MemRelAccess, BtcLOCAL> mapping : btcMethod.getAccessesToLocals().entrySet()) {
                btcNestedMethod.addPar(mapping.getKey(), mapping.getValue());
            }

            for (AstParDecl parDecl : funDecl.pars.asts()) {
                parDecl.accept(this, btcNestedMethod);
            }

            // Resolve the function body.
            funDecl.expr.accept(this, btcNestedMethod);

            // If the last instruction of a method is not a return instruction, artificially add it.
            if (!(btcNestedMethod.instrs().get(btcNestedMethod.instrs().size() - 1) instanceof BtcRETURN)) {
                BtcRETURN.Type returnInstrType = null;
                switch (type) {
                    case INT -> returnInstrType = BtcRETURN.Type.INT;
                    case FLOAT -> returnInstrType = BtcRETURN.Type.FLOAT;
                    case LONG -> returnInstrType = BtcRETURN.Type.LONG;
                    case DOUBLE -> returnInstrType = BtcRETURN.Type.DOUBLE;
                    case ARRAY, OBJECT -> returnInstrType = BtcRETURN.Type.REFERENCE;
                    case VOID -> returnInstrType = BtcRETURN.Type.VOID;
                }
                BtcRETURN btcReturn = new BtcRETURN(btcNestedMethod.instrCount(), returnInstrType);
                btcNestedMethod.addInstr(btcReturn);
            }
        }
        return null;
    }

    /**
     * Visits the parameter declaration and adds it to the method as a local variable. In case the reference analysis
     * has identified the parameter as either a reference or closure candidate, the parameter is added as an array.
     *
     * @param parDecl   The parameter declaration.
     * @param btcMethod The method to which the parameter is added.
     * @return null
     * @throws Report.InternalError If the type of the parameter is invalid as per the PINS'22 specifications.
     */
    @Override
    public BtcInstr visit(AstParDecl parDecl, BtcMETHOD btcMethod) {
        BtcLOCAL.Type type;

        if (RefAn.closureCandidates.contains(Memory.parAccesses.get(parDecl))) {
            type = BtcLOCAL.Type.OBJECT;
        } else {
            if (SemAn.describesType.get(parDecl.type) instanceof SemInt) {
                type = BtcLOCAL.Type.LONG;
            } else if (SemAn.describesType.get(parDecl.type) instanceof SemChar) {
                type = BtcLOCAL.Type.CHAR;
            } else if (SemAn.describesType.get(parDecl.type) instanceof SemArr) {
                // This should never happen, due to the PINS specification regarding parameters.
                type = BtcLOCAL.Type.ARRAY;
            } else if (SemAn.describesType.get(parDecl.type) instanceof SemPtr) {
                type = BtcLOCAL.Type.ARRAY;
            } else {
                throw new Report.InternalError();
            }
        }

        MemRelAccess access = Memory.parAccesses.get(parDecl);

        // If the variable is a closure candidate, mark it as a pointer.
        if (RefAn.closureCandidates.contains(access) && type != BtcLOCAL.Type.ARRAY) {
            type = BtcLOCAL.Type.OBJECT;
        }

        if (RefAn.referenceCandidates.containsKey(access) && RefAn.referenceCandidates.get(access) > 0 && type != BtcLOCAL.Type.ARRAY) {
            type = BtcLOCAL.Type.OBJECT;
        }

        BtcLOCAL btcLocal = new BtcLOCAL(btcMethod.localCount(), type);
        btcMethod.addPar(access, btcLocal);

        BtcGen.btcLocals.put(parDecl, btcLocal);

        return null;
    }

    /**
     * Visits the variable declaration and adds it to the method as a local variable. In case the reference analysis
     * has identified the variable as either a reference or closure candidate, the variable is added as an array.
     *
     * @param varDecl   The variable declaration.
     * @param btcMethod The method to which the variable is added.
     * @return null
     * @throws Report.InternalError If the type of the variable is invalid as per the PINS'22 specifications.
     */
    @Override
    public BtcInstr visit(AstVarDecl varDecl, BtcMETHOD btcMethod) {
        // TODO: REFACTOR THIS.
        BtcLOCAL.Type type;

        if (SemAn.describesType.get(varDecl.type) instanceof SemInt) {
            type = BtcLOCAL.Type.LONG;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemChar) {
            type = BtcLOCAL.Type.CHAR;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemArr) {
            type = BtcLOCAL.Type.ARRAY;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemPtr) {
            type = BtcLOCAL.Type.OBJECT;
        } else {
            throw new Report.InternalError();
        }

        if (Memory.varAccesses.get(varDecl) instanceof MemAbsAccess) {
            // Type will always be an array or pointer in case of global values.

            BtcNEWARRAY.Type subType = null;

            SemType semType;

            if (SemAn.describesType.get(varDecl.type) instanceof SemArr) {
                semType = ((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType();
            } else {
                semType = ((SemPtr) SemAn.describesType.get(varDecl.type)).baseType.actualType();
            }

            if (semType instanceof SemInt) {
                subType = BtcNEWARRAY.Type.LONG;
            } else if (semType instanceof SemChar) {
                subType = BtcNEWARRAY.Type.CHAR;
            } else if (semType instanceof SemArr) {
                BtcMULTIANEWARRAY.Type subType2;

                int dimensions = 2;

                long numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                SemArr arrType = (SemArr) ((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType();
                numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                do {
                    if (arrType.elemType.actualType() instanceof SemInt) {
                        subType2 = BtcMULTIANEWARRAY.Type.LONG;
                        break;
                    } else if (arrType.elemType.actualType() instanceof SemChar) {
                        subType2 = BtcMULTIANEWARRAY.Type.CHAR;
                        break;
                    } else if (arrType.elemType.actualType() instanceof SemArr) {
                        arrType = (SemArr) arrType.elemType.actualType();
                        numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
                        btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                        btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                        dimensions++;
                    }
                } while (true);

                BtcFIELD btcFIELD = BtcGen.btcClasses.peek().getField(varDecl);

                btcMethod.addInstr(new BtcMULTIANEWARRAY(btcMethod.instrCount(), dimensions, subType2));
                btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.PUT, btcFIELD));
                return null;
            }

            BtcFIELD btcFIELD = BtcGen.btcClasses.peek().getField(varDecl);

            long numElems = 1;

            if (SemAn.describesType.get(varDecl.type) instanceof SemArr){
                numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
            }
            btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
            btcMethod.addInstr(new BtcNEWARRAY(btcMethod.instrCount(), subType));
            btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.PUT, btcFIELD));

            return null;
        }

        MemRelAccess access = (MemRelAccess) Memory.varAccesses.get(varDecl);

        // If the variable is a closure candidate, mark it as a pointer.
        if (RefAn.closureCandidates.contains(access)) {
            type = BtcLOCAL.Type.OBJECT;
        }

        if (RefAn.referenceCandidates.containsKey(access) && RefAn.referenceCandidates.get(access) > 0 && type != BtcLOCAL.Type.ARRAY) {
            type = BtcLOCAL.Type.OBJECT;
        }

        BtcLOCAL btcLocal = new BtcLOCAL(btcMethod.localCount(), type);
        btcMethod.addLocal(access, btcLocal);

        BtcGen.btcLocals.put(varDecl, btcLocal);

        if (type == BtcLOCAL.Type.ARRAY) {
            BtcNEWARRAY.Type subType = null;

            if (((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType() instanceof SemInt) {
                subType = BtcNEWARRAY.Type.LONG;
            } else if (((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType() instanceof SemChar) {
                subType = BtcNEWARRAY.Type.CHAR;
            } else if (((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType() instanceof SemArr) {
                BtcMULTIANEWARRAY.Type subType2;

                int dimensions = 2;

                long numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                SemArr arrType = (SemArr) ((SemArr) SemAn.describesType.get(varDecl.type)).elemType.actualType();
                numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                do {
                    if (arrType.elemType.actualType() instanceof SemInt) {
                        subType2 = BtcMULTIANEWARRAY.Type.LONG;
                        break;
                    } else if (arrType.elemType.actualType() instanceof SemChar) {
                        subType2 = BtcMULTIANEWARRAY.Type.CHAR;
                        break;
                    } else if (arrType.elemType.actualType() instanceof SemArr) {
                        arrType = (SemArr) arrType.elemType.actualType();
                        numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
                        btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                        btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                        dimensions++;
                    }
                } while (true);

                btcMethod.addInstr(new BtcMULTIANEWARRAY(btcMethod.instrCount(), dimensions, subType2));
                btcMethod.addInstr(new BtcSTORE(btcMethod.instrCount(), btcLocal.index, BtcSTORE.Type.ARR));
                return null;
            }

            long numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
            btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
            btcMethod.addInstr(new BtcNEWARRAY(btcMethod.instrCount(), subType));

            btcMethod.addInstr(new BtcSTORE(btcMethod.instrCount(), btcLocal.index, BtcSTORE.Type.ARR));
        } else if (type == BtcLOCAL.Type.OBJECT) {
            BtcNEWARRAY.Type subType = null;
            SemType baseType;

            if (SemAn.describesType.get(varDecl.type) instanceof SemPtr) {
                baseType = ((SemPtr) SemAn.describesType.get(varDecl.type)).baseType.actualType();
            } else {
                baseType = SemAn.describesType.get(varDecl.type);
            }

            if (baseType instanceof SemArr) {
                BtcMULTIANEWARRAY.Type arrayType = null;

                int dimensions = 2;
                long numElems = 1;

                // Get the number of elements in the first dimension.
                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                // Get the number of elements in the second dimension.
                numElems = ((SemArr) baseType).numElems;
                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                SemArr semType = (SemArr) baseType;

                do {
                    if (semType.elemType.actualType() instanceof SemInt) {
                        arrayType = BtcMULTIANEWARRAY.Type.LONG;
                        break;
                    } else if (semType.elemType.actualType() instanceof SemChar) {
                        arrayType = BtcMULTIANEWARRAY.Type.CHAR;
                        break;
                    } else if (semType.elemType.actualType() instanceof SemArr) {
                        semType = (SemArr) semType.elemType.actualType();
                        numElems = ((SemArr) SemAn.describesType.get(varDecl.type)).numElems;
                        btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                        btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                        dimensions++;
                    }
                } while (true);

                btcMethod.addInstr(new BtcMULTIANEWARRAY(btcMethod.instrCount(), dimensions, arrayType));
                btcMethod.addInstr(new BtcSTORE(btcMethod.instrCount(), btcLocal.index, BtcSTORE.Type.ARR));
                return null;
            } else if (baseType instanceof SemPtr) {
                // TODO: Implement this.
            } else {
                if (baseType instanceof SemInt) {
                    subType = BtcNEWARRAY.Type.LONG;
                } else if (baseType instanceof SemChar) {
                    subType = BtcNEWARRAY.Type.INT;
                }

                long numElems = 1;
                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), numElems, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                btcMethod.addInstr(new BtcNEWARRAY(btcMethod.instrCount(), subType));
                btcMethod.addInstr(new BtcSTORE(btcMethod.instrCount(), btcLocal.index, BtcSTORE.Type.ARR));
                return null;
            }
        }

        return null;
    }

    // STATEMENTS

    /**
     * Visits an if statement and generates the appropriate instruction sequence based on the condition and if the
     * statement contains an else block.
     *
     * @param ifStmt    The if statement to visit.
     * @param btcMethod The method the if statement belongs to.
     * @return The conditional jump instruction of the if statement.
     * @throws Report.InternalError If the condition of the if statement is not a binary expression.
     */
    @Override
    public BtcInstr visit(AstIfStmt ifStmt, BtcMETHOD btcMethod) {
        BtcCJUMP.Oper oper = null;

        if (ifStmt.condExpr instanceof AstBinExpr binExpr) {
            if (binExpr.oper == AstBinExpr.Oper.AND || binExpr.oper == AstBinExpr.Oper.OR) {
                oper = BtcCJUMP.Oper.EQ;
            } else {
                switch (binExpr.oper) {
                    case EQU -> oper = BtcCJUMP.Oper.NE;
                    case NEQ -> oper = BtcCJUMP.Oper.EQ;
                    case LTH -> oper = BtcCJUMP.Oper.GE;
                    case LEQ -> oper = BtcCJUMP.Oper.GT;
                    case GTH -> oper = BtcCJUMP.Oper.LE;
                    case GEQ -> oper = BtcCJUMP.Oper.LT;
                }
            }
        } else {
            throw new Report.InternalError();
        }

        ifStmt.condExpr.accept(this, btcMethod);

        assert oper != null;

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

    /**
     * Visits the while statement and generates the appropriate instruction sequence.
     *
     * @param whileStmt The while statement to visit.
     * @param btcMethod The method to which the while statement belongs.
     * @return The conditional instruction of the while statement.
     * @throws Report.InternalError If the condition of the while statement is not a binary expression.
     */
    @Override
    public BtcInstr visit(AstWhileStmt whileStmt, BtcMETHOD btcMethod) {
        BtcCJUMP.Oper oper = null;

        // Generate the appropriate operator for the condition.
        if (whileStmt.condExpr instanceof AstBinExpr binExpr) {
            if (binExpr.oper == AstBinExpr.Oper.AND || binExpr.oper == AstBinExpr.Oper.OR) {
                oper = BtcCJUMP.Oper.EQ;
            } else {
                switch (binExpr.oper) {
                    case EQU -> oper = BtcCJUMP.Oper.NE;
                    case NEQ -> oper = BtcCJUMP.Oper.EQ;
                    case LTH -> oper = BtcCJUMP.Oper.GE;
                    case LEQ -> oper = BtcCJUMP.Oper.GT;
                    case GTH -> oper = BtcCJUMP.Oper.LE;
                    case GEQ -> oper = BtcCJUMP.Oper.LT;
                }
            }
        } else {
            throw new Report.InternalError();
        }

        assert oper != null;

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

    /**
     * Visits the expression statement and accepts the subexpression.
     *
     * @param exprStmt  The expression statement to visit.
     * @param btcMethod The method to which the expression statement belongs.
     * @return The instruction that was generated.
     */
    @Override
    public BtcInstr visit(AstExprStmt exprStmt, BtcMETHOD btcMethod) {
        return exprStmt.expr.accept(this, btcMethod);
    }

    // TODO: ADD JAVADOC AND REFACTOR.
    @Override
    public BtcInstr visit(AstAssignStmt assignStmt, BtcMETHOD btcMethod) {
        // TODO: Code cleanup. This is a mess.

        // Storing into array.
        if (assignStmt.fstSubExpr instanceof AstBinExpr) {
            AstBinExpr binExpr = (AstBinExpr) assignStmt.fstSubExpr;
            if (binExpr.oper == AstBinExpr.Oper.ARR) {
                BtcASTORE.Type type;

                if (SemAn.exprOfType.get(binExpr) instanceof SemInt) {
                    type = BtcASTORE.Type.LONG;
                } else if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
                    type = BtcASTORE.Type.CHAR;
                } else {
                    throw new Report.InternalError();
                }

                AstNameExpr nameExpr;

                if (((AstBinExpr)assignStmt.fstSubExpr).fstSubExpr instanceof AstPstExpr) {
                    nameExpr = (AstNameExpr) ((AstPstExpr) ((AstBinExpr) assignStmt.fstSubExpr).fstSubExpr).subExpr;
                } else if (((AstBinExpr)assignStmt.fstSubExpr).fstSubExpr instanceof AstBinExpr) {
                    AstBinExpr binExpr2 = (AstBinExpr) ((AstBinExpr)assignStmt.fstSubExpr).fstSubExpr;
                    while (binExpr2.fstSubExpr instanceof AstBinExpr) {
                        binExpr2 = (AstBinExpr) binExpr2.fstSubExpr;
                    }
                    nameExpr = (AstNameExpr) binExpr2.fstSubExpr;
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

                    AstExpr expr = ((AstBinExpr) assignStmt.fstSubExpr).fstSubExpr;

                    do {
                        if (expr instanceof AstBinExpr) {
                            ((AstBinExpr) expr).sndSubExpr.accept(this, btcMethod);
                            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                            btcMethod.addInstr(new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.REF));

                            expr = ((AstBinExpr) expr).fstSubExpr;
                        } else {
                            break;
                        }
                    } while (true);

                    ((AstBinExpr) assignStmt.fstSubExpr).sndSubExpr.accept(this, btcMethod);
                    btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                    assignStmt.sndSubExpr.accept(this, btcMethod);
                } else if (access instanceof MemRelAccess) {
                    BtcLOCAL btcLOCAL = btcMethod.getLocal((MemRelAccess) access);
                    btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLOCAL.index, BtcLOAD.Type.ARR));

                    AstExpr expr = ((AstBinExpr) assignStmt.fstSubExpr).fstSubExpr;

                    do {
                        if (expr instanceof AstBinExpr) {
                            ((AstBinExpr) expr).sndSubExpr.accept(this, btcMethod);
                            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                            btcMethod.addInstr(new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.REF));

                            expr = ((AstBinExpr) expr).fstSubExpr;
                        } else {
                            break;
                        }
                    } while (true);

                    ((AstBinExpr) assignStmt.fstSubExpr).sndSubExpr.accept(this, btcMethod);
                    btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                    assignStmt.sndSubExpr.accept(this, btcMethod);
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
                // Load reference to pointer.

                AstNameExpr nameExpr = (AstNameExpr) pstExpr.subExpr;

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
                } else if (access instanceof MemRelAccess) {
                    BtcLOCAL btcLOCAL = btcMethod.getLocal((MemRelAccess) access);
                    btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLOCAL.index, BtcLOAD.Type.ARR));
                }

                // Create index of 0.

                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), 0, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                // Load value to store.

                assignStmt.sndSubExpr.accept(this, btcMethod);

                // Store value.

                // Get store type.
                BtcASTORE.Type type = null;

                if (SemAn.exprOfType.get(nameExpr) instanceof SemPtr semPtr) {

                    if (semPtr.baseType.actualType() instanceof SemInt) {
                        type = BtcASTORE.Type.LONG;
                    } else if (SemAn.exprOfType.get(nameExpr) instanceof SemChar) {
                        type = BtcASTORE.Type.CHAR;
                    } else if (SemAn.exprOfType.get(nameExpr) instanceof SemPtr) {
                        type = BtcASTORE.Type.REF;
                    } else {
                        throw new Report.InternalError();
                    }
                } else {
                    if (SemAn.exprOfType.get(nameExpr) instanceof SemInt) {
                        type = BtcASTORE.Type.LONG;
                    } else if (SemAn.exprOfType.get(nameExpr) instanceof SemChar) {
                        type = BtcASTORE.Type.CHAR;
                    } else if (SemAn.exprOfType.get(nameExpr) instanceof SemPtr) {
                        type = BtcASTORE.Type.REF;
                    } else {
                        throw new Report.InternalError();
                    }
                }

                BtcInstr btcInstr = new BtcASTORE(btcMethod.instrCount(), type);
                btcMethod.addInstr(btcInstr);
                return btcInstr;

            } else if (pstExpr.subExpr instanceof AstPstExpr pstExpr1) {
                // Load reference to pointer.

                pstExpr1.subExpr.accept(this, btcMethod);

                // Create index of 0.

                btcMethod.addInstr(new BtcLDC(btcMethod.instrCount(), 0, BtcLDC.Type.LONG));
                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                // Load value to store.

                assignStmt.sndSubExpr.accept(this, btcMethod);

                // Store value.

                BtcInstr btcInstr = new BtcASTORE(btcMethod.instrCount(), BtcASTORE.Type.LONG);
                btcMethod.addInstr(btcInstr);
                return btcInstr;

            } else {
                if (pstExpr.subExpr instanceof AstCastExpr castExpr) {
                    if (castExpr.subExpr instanceof AstBinExpr binExpr) {
                        if (binExpr.fstSubExpr instanceof AstCastExpr castExpr2) {

                            // Accept the named expression -> variable. Should get the array reference.
                            //castExpr2.subExpr.accept(this, btcMethod);

                            MemAccess access;

                            AstNameExpr nameExpr = (AstNameExpr) castExpr2.subExpr;
                            if (SemAn.declaredAt.get(nameExpr) instanceof AstParDecl) {
                                access = Memory.parAccesses.get((AstParDecl) SemAn.declaredAt.get(nameExpr));
                            } else {
                                access = Memory.varAccesses.get((AstVarDecl) SemAn.declaredAt.get(nameExpr));
                            }

                            if (access instanceof MemAbsAccess) {
                                BtcFIELD btcFIELD = BtcGen.btcClasses.peek().getField((AstVarDecl) SemAn.declaredAt.get(nameExpr));
                                btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, btcFIELD));
                            } else {
                                BtcLOCAL btcLOCAL = btcMethod.getLocal((MemRelAccess) access);
                                btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLOCAL.index, BtcLOAD.Type.ARR));
                            }

                            // Accept the second expression -> index. Should get the index.
                            binExpr.sndSubExpr.accept(this, btcMethod);
                            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                            // Accept the right hand side of the assignment -> value.
                            assignStmt.sndSubExpr.accept(this, btcMethod);

                            BtcASTORE.Type type;

                            if (SemAn.exprOfType.get(castExpr) instanceof SemInt) {
                                type = BtcASTORE.Type.LONG;
                            } else if (SemAn.exprOfType.get(castExpr) instanceof SemChar) {
                                type = BtcASTORE.Type.CHAR;
                            } else if (SemAn.exprOfType.get(castExpr) instanceof SemPtr semPtr) {
                                if (semPtr.baseType instanceof SemInt) {
                                    type = BtcASTORE.Type.LONG;
                                } else if (semPtr.baseType instanceof SemChar) {
                                    type = BtcASTORE.Type.CHAR;
                                } else {
                                    throw new Report.InternalError();
                                }
                            } else {
                                throw new Report.InternalError();
                            }

                            BtcInstr btcInstr = new BtcASTORE(btcMethod.instrCount(), type);
                            btcMethod.addInstr(btcInstr);
                            return btcInstr;
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

            AstDecl decl = SemAn.declaredAt.get(assignStmt.fstSubExpr);
            MemAccess access = null;
            if (decl instanceof AstVarDecl) {
                access = Memory.varAccesses.get(decl);
            } else if (decl instanceof AstParDecl) {
                access = Memory.parAccesses.get(decl);
            }

            BtcASTORE.Type atype = null;

            if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemInt) {
                atype = BtcASTORE.Type.LONG;
            } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemChar) {
                atype = BtcASTORE.Type.CHAR;
            } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemPtr semPtr) {
                if (semPtr.baseType instanceof SemInt) {
                    atype = BtcASTORE.Type.LONG;
                } else if (semPtr.baseType instanceof SemChar) {
                    atype = BtcASTORE.Type.CHAR;
                } else if (semPtr.baseType instanceof SemPtr) {
                    atype = BtcASTORE.Type.REF;
                } else {
                    throw new Report.InternalError();
                }
            } else {
                throw new Report.InternalError();
            }

            if (access instanceof MemAbsAccess) {
                BtcFIELD btcField = BtcGen.btcClasses.peek().getField((AstVarDecl) decl);

                if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemPtr) {
                    // Arrayref.
                    btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, btcField));
                    // Index
                    btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));
                    // Value
                    assignStmt.sndSubExpr.accept(this, btcMethod);

                    // Store into array.
                    BtcInstr btcInstr = new BtcASTORE(btcMethod.instrCount(), atype);
                    btcMethod.addInstr(btcInstr);
                    return btcInstr;
                } else {
                    assignStmt.sndSubExpr.accept(this, btcMethod);
                }

                BtcInstr btcInstr = new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.PUT, btcField);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            } else if (access instanceof MemRelAccess) {
                BtcSTORE.Type type;

                BtcLOCAL btcLocal = btcMethod.getLocal((MemRelAccess) access);

                switch (btcLocal.type) {
                    case LONG -> {
                        assignStmt.sndSubExpr.accept(this, btcMethod);
                        type = BtcSTORE.Type.LONG;
                    }
                    case CHAR -> {
                        assignStmt.sndSubExpr.accept(this, btcMethod);
                        type = BtcSTORE.Type.INT;
                    }
                    case ARRAY, OBJECT -> {
                        type = BtcSTORE.Type.ARR;
                    }
                    default -> throw new Report.InternalError();
                }

                if (btcLocal.type == BtcLOCAL.Type.OBJECT) {

                    // TODO: Fix. This is a hack when storing pointer to pointer.
                    if (assignStmt.sndSubExpr instanceof AstNameExpr) {

                        AstExpr fstExpr = assignStmt.fstSubExpr;
                        AstExpr sndExpr = assignStmt.sndSubExpr;

                        if (SemAn.exprOfType.get(fstExpr) instanceof SemPtr && SemAn.exprOfType.get(sndExpr) instanceof SemPtr) {

                            MemAccess fstLocal, sndLocal;
                            AstDecl fstDecl = SemAn.declaredAt.get(fstExpr);
                            AstDecl sndDecl = SemAn.declaredAt.get(sndExpr);

                            if (fstDecl instanceof AstVarDecl) {
                                fstLocal = Memory.varAccesses.get(fstDecl);
                            } else if (fstDecl instanceof AstParDecl) {
                                fstLocal = Memory.parAccesses.get(fstDecl);
                            } else {
                                throw new Report.InternalError();
                            }

                            if (sndDecl instanceof AstVarDecl) {
                                sndLocal = Memory.varAccesses.get(sndDecl);
                            } else if (sndDecl instanceof AstParDecl) {
                                sndLocal = Memory.parAccesses.get(sndDecl);
                            } else {
                                throw new Report.InternalError();
                            }

                            if (sndLocal instanceof MemAbsAccess) {
                                BtcFIELD btcField = BtcGen.btcClasses.peek().getField((AstVarDecl) sndDecl);
                                btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, btcField));
                            } else if (sndLocal instanceof MemRelAccess) {
                                BtcLOCAL btcLocal1 = btcMethod.getLocal((MemRelAccess) sndLocal);
                                btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLocal1.index, BtcLOAD.Type.ARR));
                            } else {
                                throw new Report.InternalError();
                            }

                            if (fstLocal instanceof MemAbsAccess) {
                                BtcFIELD btcField = BtcGen.btcClasses.peek().getField((AstVarDecl) fstDecl);
                                btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.PUT, btcField));
                            } else if (fstLocal instanceof MemRelAccess) {
                                BtcLOCAL btcLocal1 = btcMethod.getLocal((MemRelAccess) fstLocal);
                                btcMethod.addInstr(new BtcSTORE(btcMethod.instrCount(), btcLocal1.index, BtcSTORE.Type.ARR));
                            } else {
                                throw new Report.InternalError();
                            }

                            return null;
                        }

                        return null;
                    }


                    // First get the array reference.
                    BtcLOCAL local = btcMethod.getLocal((MemRelAccess) access);
                    btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), local.index, BtcLOAD.Type.ARR));

                    // Get the index.
                    btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));

                    // Get the value.
                    if (assignStmt.sndSubExpr instanceof AstPreExpr preExpr && preExpr.oper == AstPreExpr.Oper.NEW) {
                        BtcNEWARRAY.Type newArrayType;

                        preExpr.subExpr.accept(this, btcMethod);

                        SemPtr semPtr = (SemPtr) SemAn.exprOfType.get(assignStmt.fstSubExpr);

                        if (semPtr.baseType.actualType() instanceof SemInt) {
                            newArrayType = BtcNEWARRAY.Type.LONG;
                        } else if (semPtr.baseType.actualType() instanceof SemChar) {
                            newArrayType = BtcNEWARRAY.Type.CHAR;
                        } else {
                            throw new Report.InternalError();
                        }

                        btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                        btcMethod.addInstr(new BtcNEWARRAY(btcMethod.instrCount(), newArrayType));
                    } else if (assignStmt.sndSubExpr instanceof AstPreExpr preExpr && preExpr.oper == AstPreExpr.Oper.PTR) {
                        assignStmt.sndSubExpr.accept(this, btcMethod);
                        BtcLOCAL btcLOCAL = btcMethod.getLocal((MemRelAccess) access);
                        btcMethod.addInstr(new BtcSTORE(btcMethod.instrCount(), btcLOCAL.index, BtcSTORE.Type.ARR));
                        return null;
                    } else {
                        assignStmt.sndSubExpr.accept(this, btcMethod);
                    }

                    if (assignStmt.sndSubExpr instanceof AstPreExpr) {
                        if (((AstPreExpr) assignStmt.sndSubExpr).oper == AstPreExpr.Oper.NEW) {
                            // If creating a new pointer, store the reference.
                            BtcLOCAL btcLOCAL = btcMethod.getLocal((MemRelAccess) access);
                            BtcInstr btcInstr = new BtcSTORE(btcMethod.instrCount(), btcLOCAL.index, BtcSTORE.Type.ARR);
                            btcMethod.addInstr(btcInstr);
                            return btcInstr;
                        }
                    }

                    BtcASTORE.Type aatype = null;

                    if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemInt) {
                        aatype = BtcASTORE.Type.LONG;
                    } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemChar) {
                        aatype = BtcASTORE.Type.CHAR;
                    } else if (SemAn.exprOfType.get(assignStmt.fstSubExpr) instanceof SemPtr semPtr) {
                        if (semPtr.baseType.actualType() instanceof SemInt) {
                            aatype = BtcASTORE.Type.LONG;
                        } else if (semPtr.baseType.actualType() instanceof SemChar) {
                            aatype = BtcASTORE.Type.CHAR;
                        } else {
                            throw new Report.InternalError();
                        }
                    } else {
                        throw new Report.InternalError();
                    }

                    // Store into array.
                    BtcInstr btcInstr = new BtcASTORE(btcMethod.instrCount(), aatype);
                    btcMethod.addInstr(btcInstr);
                    return btcInstr;
                }

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

    /**
     * Visits a constant expression and generates the appropriate LDC or CONST instruction based on the type and value.
     *
     * @param constExpr The constant expression to visit.
     * @param btcMethod The method to which the constant expression belongs to.
     * @return The generated instruction.
     * @throws Report.InternalError If the constant expression is of an invalid type or value.
     */
    @Override
    public BtcInstr visit(AstConstExpr constExpr, BtcMETHOD btcMethod) {
        BtcInstr btcInstr;

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

    /**
     * Visits a name expression and generates the appropriate load instructions based on the type of the expression, if
     * the expression is a variable or parameter, or a field access.
     *
     * @param nameExpr  The name expression to visit.
     * @param btcMethod The method to which the name expression belongs to.
     * @return The generated instruction.
     * @throws Report.InternalError If the name expression is of an invalid type.
     */
    @Override
    public BtcInstr visit(AstNameExpr nameExpr, BtcMETHOD btcMethod) {
        // TODO: REFACTOR THIS.
        AstDecl decl = SemAn.declaredAt.get(nameExpr);
        MemAccess memAccess;
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
            memAccess = Memory.parAccesses.get((AstParDecl) decl);
        }

        BtcLOAD.Type type;

        BtcLOCAL btcLocal = btcMethod.getLocal((MemRelAccess) memAccess);

        switch (btcLocal.type) {
            case LONG -> type = BtcLOAD.Type.LONG;
            case INT, CHAR -> type = BtcLOAD.Type.INT;
            case ARRAY, OBJECT -> type = BtcLOAD.Type.ARR;
            default -> throw new Report.InternalError();
        }

        BtcInstr btcInstr = new BtcLOAD(btcMethod.instrCount(), btcLocal.index, type);
        btcMethod.addInstr(btcInstr);

        if (btcLocal.type == BtcLOCAL.Type.OBJECT) {

            AstFunDecl funDecl = null;

            for (Map.Entry<AstFunDecl, BtcMETHOD> entry : BtcGen.btcMethods.entrySet()) {
                if (entry.getValue().name.equals(btcMethod.name)) {
                    funDecl = entry.getKey();
                    break;
                }
            }
            if (funDecl != null) {
                if (Memory.frames.get(funDecl).depth > 1 || RefAn.referenceCandidates.get(memAccess) > 0) {
                    btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));
                    if (SemAn.exprOfType.get(nameExpr) instanceof SemInt) {
                        btcMethod.addInstr(new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.LONG));
                    } else if (SemAn.exprOfType.get(nameExpr) instanceof SemChar) {
                        btcMethod.addInstr(new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.CHAR));
                    } else if (SemAn.exprOfType.get(nameExpr) instanceof SemPtr) {
                        btcMethod.addInstr(new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.REF));
                    } else {
                        throw new Report.InternalError();
                    }
                }
            }
        }

        return btcInstr;
    }

    /**
     * Visits a binary expression and generates the appropriate instruction sequence based on the type and operator of
     * the expression.
     *
     * @param binExpr   The binary expression to visit.
     * @param btcMethod The method to which the binary expression belongs to.
     * @return The generated instruction.
     * @throws Report.InternalError If the binary expression is of an invalid type or operator.
     */
    @Override
    public BtcInstr visit(AstBinExpr binExpr, BtcMETHOD btcMethod) {
        // TODO: REFACTOR THIS.
        if (binExpr.oper != AstBinExpr.Oper.ARR && binExpr.oper != AstBinExpr.Oper.AND && binExpr.oper != AstBinExpr.Oper.OR) {
            binExpr.fstSubExpr.accept(this, btcMethod);
            binExpr.sndSubExpr.accept(this, btcMethod);
        }

        BtcARITHM.Type operType = null;

        if (SemAn.exprOfType.get(binExpr) instanceof SemInt) {
            operType = BtcARITHM.Type.LONG;
        } else if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
            operType = BtcARITHM.Type.INT;
        } else if (SemAn.exprOfType.get(binExpr) instanceof SemArr) {
            // Ignore.
        } else {
            throw new Report.InternalError();
        }

        // Arithmetic expression.
        switch (binExpr.oper) {
            case ADD -> {
                assert operType != null;
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.ADD, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case SUB -> {
                assert operType != null;
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.SUB, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case MUL -> {
                assert operType != null;
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.MUL, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case DIV -> {
                assert operType != null;
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.DIV, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case MOD -> {
                assert operType != null;
                BtcInstr btcInstr = new BtcARITHM(btcMethod.instrCount(), BtcARITHM.Oper.REM, operType);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            case AND -> {
                BtcCJUMP.Oper oper;

                switch (((AstBinExpr) binExpr.fstSubExpr).oper) {
                    case EQU -> oper = BtcCJUMP.Oper.NE;
                    case NEQ, AND, OR -> oper = BtcCJUMP.Oper.EQ;
                    case LTH -> oper = BtcCJUMP.Oper.GE;
                    case GTH -> oper = BtcCJUMP.Oper.LE;
                    case LEQ -> oper = BtcCJUMP.Oper.GT;
                    case GEQ -> oper = BtcCJUMP.Oper.LT;
                    default -> throw new Report.InternalError();
                }

                binExpr.fstSubExpr.accept(this, btcMethod);
                BtcCJUMP btcCJUMP1 = new BtcCJUMP(btcMethod.instrCount(), oper);
                btcMethod.addInstr(btcCJUMP1);

                switch (((AstBinExpr) binExpr.sndSubExpr).oper) {
                    case EQU -> oper = BtcCJUMP.Oper.NE;
                    case NEQ, AND, OR -> oper = BtcCJUMP.Oper.EQ;
                    case LTH -> oper = BtcCJUMP.Oper.GE;
                    case GTH -> oper = BtcCJUMP.Oper.LE;
                    case LEQ -> oper = BtcCJUMP.Oper.GT;
                    case GEQ -> oper = BtcCJUMP.Oper.LT;
                    default -> throw new Report.InternalError();
                }

                binExpr.sndSubExpr.accept(this, btcMethod);
                BtcCJUMP btcCJUMP2 = new BtcCJUMP(btcMethod.instrCount(), oper);
                btcMethod.addInstr(btcCJUMP2);

                btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 1, BtcCONST.Type.INT));

                BtcGOTO btcGOTO = new BtcGOTO(btcMethod.instrCount());
                btcMethod.addInstr(btcGOTO);

                btcCJUMP1.setTarget(btcMethod.instrCount());
                btcCJUMP2.setTarget(btcMethod.instrCount());

                btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));

                btcGOTO.setTarget(btcMethod.instrCount());
                return null;
            }
            case OR -> {
                BtcCJUMP.Oper oper;

                if (binExpr.fstSubExpr instanceof AstBinExpr) {
                    switch (((AstBinExpr) binExpr.fstSubExpr).oper) {
                        case EQU -> oper = BtcCJUMP.Oper.EQ;
                        case NEQ, AND, OR -> oper = BtcCJUMP.Oper.NE;
                        case LTH -> oper = BtcCJUMP.Oper.LT;
                        case GTH -> oper = BtcCJUMP.Oper.GT;
                        case LEQ -> oper = BtcCJUMP.Oper.LE;
                        case GEQ -> oper = BtcCJUMP.Oper.GE;
                        default -> throw new Report.InternalError();
                    }
                } else {
                    oper = BtcCJUMP.Oper.NE;
                }

                binExpr.fstSubExpr.accept(this, btcMethod);
                BtcCJUMP btcCJUMP1 = new BtcCJUMP(btcMethod.instrCount(), oper);
                btcMethod.addInstr(btcCJUMP1);

                if (binExpr.fstSubExpr instanceof AstBinExpr) {
                    switch (((AstBinExpr) binExpr.sndSubExpr).oper) {
                        case EQU -> oper = BtcCJUMP.Oper.EQ;
                        case NEQ, AND, OR -> oper = BtcCJUMP.Oper.NE;
                        case LTH -> oper = BtcCJUMP.Oper.LT;
                        case GTH -> oper = BtcCJUMP.Oper.GT;
                        case LEQ -> oper = BtcCJUMP.Oper.LE;
                        case GEQ -> oper = BtcCJUMP.Oper.GE;
                        default -> throw new Report.InternalError();
                    }
                } else {
                    oper = BtcCJUMP.Oper.NE;
                }

                binExpr.sndSubExpr.accept(this, btcMethod);
                BtcCJUMP btcCJUMP2 = new BtcCJUMP(btcMethod.instrCount(), oper);
                btcMethod.addInstr(btcCJUMP2);

                btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));

                BtcGOTO btcGOTO = new BtcGOTO(btcMethod.instrCount());
                btcMethod.addInstr(btcGOTO);

                btcCJUMP1.setTarget(btcMethod.instrCount());
                btcCJUMP2.setTarget(btcMethod.instrCount());

                btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 1, BtcCONST.Type.INT));

                btcGOTO.setTarget(btcMethod.instrCount());
                return null;
            }
        }

        if (binExpr.oper == AstBinExpr.Oper.ARR) {
            // TODO: Fix casts to depend on types.

            AstExpr expr = binExpr.fstSubExpr;

            if (!(binExpr.fstSubExpr instanceof AstBinExpr)) {
                if (!(binExpr.fstSubExpr instanceof AstPstExpr)) {
                    binExpr.fstSubExpr.accept(this, btcMethod);
                } else {
                    AstNameExpr nameExpr = null;
                    if (((AstPstExpr) binExpr.fstSubExpr).subExpr instanceof AstNameExpr) {
                        nameExpr = (AstNameExpr) ((AstPstExpr) binExpr.fstSubExpr).subExpr;
                    } else {
                        AstExpr looper = ((AstPstExpr) binExpr.fstSubExpr).subExpr;

                        // Get the name expression.
                        // TODO: FIX THIS.
                        do {
                            if (looper instanceof AstPstExpr) {
                                looper = ((AstPstExpr) looper).subExpr;
                            } else if (looper instanceof AstBinExpr) {
                                looper = ((AstBinExpr) looper).fstSubExpr;
                            } else {
                                nameExpr = (AstNameExpr) looper;
                                break;
                            }
                        } while (true);

                    }
                    // TODO: Make it work with parameters.
                    AstDecl decl = null;
                    //AstVarDecl varDecl = (AstVarDecl) SemAn.declaredAt.get(nameExpr);
                    MemAccess access = null;
                    if (SemAn.declaredAt.get(nameExpr) instanceof AstVarDecl) {
                        decl = SemAn.declaredAt.get(nameExpr);
                        access = Memory.varAccesses.get((AstVarDecl) decl);
                    } else {
                        decl = SemAn.declaredAt.get(nameExpr);
                        access = Memory.parAccesses.get((AstParDecl) decl);
                    }

                    //MemAccess access = Memory.varAccesses.get(varDecl);

                    // Load the array reference.
                    if (access instanceof MemRelAccess localAccess) {
                        BtcLOCAL btcLocal = btcMethod.getLocal(localAccess);
                        btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLocal.index, BtcLOAD.Type.ARR));
                    } else {
                        BtcFIELD btcField = BtcGen.btcClasses.peek().getField((AstVarDecl) decl);
                        btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, btcField));
                    }
                }
            } else {
                AstBinExpr expr2 = (AstBinExpr) binExpr.fstSubExpr;
                while (expr2.fstSubExpr instanceof AstBinExpr) {
                    expr2 = (AstBinExpr) expr2.fstSubExpr;
                }
                AstNameExpr nameExpr = (AstNameExpr) expr2.fstSubExpr;
                nameExpr.accept(this, btcMethod);
                ((AstBinExpr) binExpr.fstSubExpr).sndSubExpr.accept(this, btcMethod);
            }

            do {
                if (expr instanceof AstBinExpr) {
                    if (btcMethod.getInstr(btcMethod.instrs().size() - 1) instanceof BtcALOAD btcALOAD) {
                        if (btcALOAD.type == BtcALOAD.Type.REF) {
                            ((AstBinExpr) expr).sndSubExpr.accept(this, btcMethod);
                        }
                    }

                    btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));
                    btcMethod.addInstr(new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.REF));

                    expr = ((AstBinExpr) expr).fstSubExpr;
                } else {
                    break;
                }
            } while (true);

            binExpr.sndSubExpr.accept(this, btcMethod);

            btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

            if (SemAn.exprOfType.get(binExpr) instanceof SemChar) {
                BtcInstr btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.CHAR);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            } else {
                BtcInstr btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.LONG);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
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

    /**
     * Visits a prefix expression and generates the appropriate instruction sequence based on the type and operator of
     * the expression.
     *
     * @param preExpr   The prefix expression to visit.
     * @param btcMethod The method to generate the instruction sequence for.
     * @return The last instruction generated.
     * @throws Report.InternalError If the expression is of an invalid type/operator combination.
     */
    @Override
    public BtcInstr visit(AstPreExpr preExpr, BtcMETHOD btcMethod) {
        preExpr.subExpr.accept(this, btcMethod);
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
                BtcNEWARRAY.Type type;

                if (SemAn.exprOfType.get(preExpr.subExpr) instanceof SemInt) {
                    type = BtcNEWARRAY.Type.LONG;
                } else if (SemAn.exprOfType.get(preExpr.subExpr) instanceof SemChar) {
                    type = BtcNEWARRAY.Type.CHAR;
                } else {
                    throw new Report.InternalError();
                }

                btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                btcInstr = new BtcNEWARRAY(btcMethod.instrCount(), type);
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
            }
            case PTR -> {
                if (!(preExpr.subExpr instanceof AstNameExpr)) {
                    preExpr.subExpr.accept(this, btcMethod);
                }

                AstNameExpr nameExpr = (AstNameExpr) preExpr.subExpr;
                MemAccess memAccess;

                if (SemAn.declaredAt.get(nameExpr) instanceof AstVarDecl) {
                    memAccess = Memory.varAccesses.get((AstVarDecl) SemAn.declaredAt.get(nameExpr));
                } else {
                    memAccess = Memory.parAccesses.get((AstParDecl) SemAn.declaredAt.get(nameExpr));
                }

                btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcMethod.getLocal((MemRelAccess) memAccess).index, BtcLOAD.Type.ARR));
                return null;
            }
            case ADD -> {
                // Ignore, it's just a unary plus (identity).
            }
        }
        btcMethod.addInstr(btcInstr);
        return btcInstr;
    }

    /**
     * Visits a postfix expression and generates the appropriate instruction sequence based on the type and operator of
     * the expression.
     *
     * @param pstExpr   The postfix expression to visit.
     * @param btcMethod The method to which the postfix expression belongs to.
     * @return The last instruction generated.
     * @throws Report.InternalError If the expression is of an invalid type/operator combination.
     */
    @Override
    public BtcInstr visit(AstPstExpr pstExpr, BtcMETHOD btcMethod) {
        // TODO: REFACTOR THIS.
        //BtcInstr btcInstr = pstExpr.subExpr.accept(this, btcMethod);
        // TODO: Implement de-referencing.

        /*#{ Pst -> Cast -> Add -> Cast -> Name }#
        (((x: int) + counter) : ^int)^ = counter;*/

        if (pstExpr.subExpr instanceof AstNameExpr) {
            AstNameExpr nameExpr = (AstNameExpr) pstExpr.subExpr;

            // TODO: Make it work with parameters.
            AstDecl decl = null;
            //AstVarDecl varDecl = (AstVarDecl) SemAn.declaredAt.get(nameExpr);
            MemAccess access = null;
            if (SemAn.declaredAt.get(nameExpr) instanceof AstVarDecl) {
                decl = SemAn.declaredAt.get(nameExpr);
                access = Memory.varAccesses.get((AstVarDecl) decl);
            } else {
                decl = SemAn.declaredAt.get(nameExpr);
                access = Memory.parAccesses.get((AstParDecl) decl);
            }

            //MemAccess access = Memory.varAccesses.get(varDecl);

            // Load the array reference.
            if (access instanceof MemRelAccess localAccess) {
                BtcLOCAL btcLocal = btcMethod.getLocal(localAccess);
                btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLocal.index, BtcLOAD.Type.ARR));
            } else {
                BtcFIELD btcField = BtcGen.btcClasses.peek().getField((AstVarDecl) decl);
                btcMethod.addInstr(new BtcACCESS(btcMethod.instrCount(), BtcACCESS.Dir.GET, btcField));
            }

            btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));

            // Get type for ALOAD. If loading a reference, use REF, otherwise use LONG.
            BtcInstr btcInstr = null;

            if (SemAn.exprOfType.get(pstExpr.subExpr) instanceof SemChar) {
                btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.CHAR);
            } else if (SemAn.exprOfType.get(pstExpr.subExpr) instanceof SemInt) {
                btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.LONG);
            } else if (SemAn.exprOfType.get(pstExpr.subExpr) instanceof SemPtr) {
                // TODO: Fix this. It doesn't work with multiple references.
                btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.LONG);
            } else {
                throw new Report.InternalError();
            }

            //BtcInstr btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.LONG);
            btcMethod.addInstr(btcInstr);
            return btcInstr;

        } else if (pstExpr.subExpr instanceof AstPstExpr pstExpr1) {
            pstExpr1.subExpr.accept(this, btcMethod);

            if (!(pstExpr1.subExpr instanceof AstPstExpr)) {
                btcMethod.addInstr(new BtcCONST(btcMethod.instrCount(), 0, BtcCONST.Type.INT));
                BtcInstr btcInstr = new BtcALOAD(btcMethod.instrCount(), BtcALOAD.Type.LONG);
                btcMethod.addInstr(btcInstr);
            }
        } else {
            if (pstExpr.subExpr instanceof AstCastExpr) {
                AstCastExpr castExpr = (AstCastExpr) pstExpr.subExpr;

                if (castExpr.subExpr instanceof AstBinExpr) {
                    AstBinExpr binExpr = (AstBinExpr) castExpr.subExpr;

                    if (binExpr.fstSubExpr instanceof AstCastExpr) {
                        AstCastExpr castExpr2 = (AstCastExpr) binExpr.fstSubExpr;

                        // Accept the named expression -> variable. Should get the array reference.
                        castExpr2.subExpr.accept(this, btcMethod);
                        // Accept the second expression -> index. Should get the index.
                        binExpr.sndSubExpr.accept(this, btcMethod);
                        btcMethod.addInstr(new BtcCAST(btcMethod.instrCount(), BtcCAST.Type.LONG, BtcCAST.Type.INT));

                        BtcALOAD.Type type;

                        if (SemAn.exprOfType.get(castExpr) instanceof SemInt) {
                            type = BtcALOAD.Type.LONG;
                        } else if (SemAn.exprOfType.get(castExpr) instanceof SemChar) {
                            type = BtcALOAD.Type.CHAR;
                        } else if (SemAn.exprOfType.get(castExpr) instanceof SemPtr semPtr) {
                            if (semPtr.baseType instanceof SemInt) {
                                type = BtcALOAD.Type.LONG;
                            } else {
                                type = BtcALOAD.Type.CHAR;
                            }
                        } else {
                            throw new Report.InternalError();
                        }

                        BtcInstr btcInstr = new BtcALOAD(btcMethod.instrCount(), type);

                        btcMethod.addInstr(btcInstr);
                        return btcInstr;
                    }
                } else {
                    throw new Report.InternalError();
                }
            } else {
                throw new Report.InternalError();
            }
        }

        return null;
    }

    /**
     * Visits a call expression and generates the appropriate instruction sequence. It also handles invocation of the
     * builtin functions such as 'putInt' and 'putChar'.
     *
     * @param callExpr  The call expression to visit.
     * @param btcMethod The method to which the call expression belongs.
     * @return The last instruction generated.
     * @throws Report.InternalError In case of a wrong type when handling local passthrough in case of lambda lifting.
     */
    @Override
    public BtcInstr visit(AstCallExpr callExpr, BtcMETHOD btcMethod) {
        // TODO: REFACTOR THIS.
        BtcINVOKE.Type type;

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

            BtcFIELD field;

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

            BtcMETHOD callee = BtcGen.btcMethods.get((AstFunDecl) SemAn.declaredAt.get(callExpr));

            int count = 0;

            // In case of nested function calls, load the locals onto the stack as they are passed to
            // the nested function as parameters.
            if (Memory.frames.get((AstFunDecl) SemAn.declaredAt.get(callExpr)).depth > 1) {
                for (BtcLOCAL btcLOCAL : btcMethod.locals()) {
                    // Load the locals onto the stack.

                    // Ensure to not load too many locals onto the stack.
                    count++;
                    if (callExpr.name.equals(btcMethod.name.split("\\$")[btcMethod.name.split("\\$").length - 1])) {
                        if (count > btcMethod.parTypes().size() - callExpr.args.asts().size()) {
                            continue;
                        }
                    }

                    BtcLOAD.Type locType = switch (btcLOCAL.type) {
                        case ARRAY, OBJECT -> BtcLOAD.Type.ARR;
                        case INT -> BtcLOAD.Type.INT;
                        case LONG -> BtcLOAD.Type.LONG;
                        case FLOAT -> BtcLOAD.Type.FLOAT;
                        case DOUBLE -> BtcLOAD.Type.DOUBLE;
                        default -> throw new Report.InternalError();
                    };

                    btcMethod.addInstr(new BtcLOAD(btcMethod.instrCount(), btcLOCAL.index, locType));
                }
            }

            callExpr.args.accept(this, btcMethod);
            if (callee == null) {
                BtcInstr btcInstr = new BtcINVOKE(btcMethod.instrCount(), type, callExpr.name);
                btcMethod.addInstr(btcInstr);
                return btcInstr;
            }
            BtcInstr btcInstr = new BtcINVOKE(btcMethod.instrCount(), type, callee.name);
            btcMethod.addInstr(btcInstr);
            return btcInstr;
        }

        callExpr.args.accept(this, btcMethod);

        BtcInstr btcInstr = new BtcINVOKE(btcMethod.instrCount(), type, callExpr.name);
        btcMethod.addInstr(btcInstr);
        return btcInstr;
    }

    /**
     * Visits a cast expression.
     *
     * @param castExpr  The cast expression to visit.
     * @param btcMethod The method to which the cast expression belongs.
     * @return The cast instruction.
     * @throws Report.InternalError If the cast expression is of an invalid type.
     */
    @Override
    public BtcInstr visit(AstCastExpr castExpr, BtcMETHOD btcMethod) {
        BtcCAST.Type from;
        BtcCAST.Type to;

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

    /**
     * Visits a where expression and accepts all declarations and the subexpression of the where expression. In case of
     * declarations, the variable declarations are visited first, followed by the function declarations. The subexpression
     * is visited last.
     *
     * @param whereExpr The where expression to visit.
     * @param btcMethod The method to which the where expression belongs.
     * @return The instruction generated by the subexpression.
     */
    @Override
    public BtcInstr visit(AstWhereExpr whereExpr, BtcMETHOD btcMethod) {
        // First resolve all variable declarations.
        for (AstDecl decl : whereExpr.decls.asts()) {
            if (decl instanceof AstVarDecl) {
                decl.accept(this, btcMethod);
            }
        }
        for (AstDecl decl : whereExpr.decls.asts()) {
            if (decl instanceof AstFunDecl) {
                decl.accept(this, btcMethod);
            }
        }
        return whereExpr.subExpr.accept(this, btcMethod);
    }

    /**
     * Visits a statement expression and accepts all statements in the statement expression. It also generates a return
     * instruction, but as of the time of writing, this return instruction is never used.
     *
     * @param stmtExpr  The statement expression to visit.
     * @param btcMethod The method to which the statement expression belongs.
     * @return A return instruction.
     * @throws Report.InternalError If the last statement in the statement expression is of an invalid type.
     */
    @Override
    public BtcInstr visit(AstStmtExpr stmtExpr, BtcMETHOD btcMethod) {
        stmtExpr.stmts.asts().forEach(stmt -> stmt.accept(this, btcMethod));
        AstStmt lastStmt = stmtExpr.stmts.asts().get(stmtExpr.stmts.asts().size() - 1);

        if (lastStmt instanceof AstExprStmt) {
            BtcRETURN.Type type;
            SemType semType = SemAn.exprOfType.get(((AstExprStmt) lastStmt).expr);
            if (semType instanceof SemInt) {
                type = BtcRETURN.Type.LONG;
            } else if (semType instanceof SemChar) {
                type = BtcRETURN.Type.INT;
            } else if (semType instanceof SemArr) {
                type = BtcRETURN.Type.REFERENCE;
            } else if (semType instanceof SemPtr) {
                type = BtcRETURN.Type.REFERENCE;
            } else if (semType instanceof SemVoid) {
                type = BtcRETURN.Type.VOID;
            } else {
                throw new Report.InternalError();
            }
            return new BtcRETURN(btcMethod.instrCount(), type);
        }
        return null;
    }

}
