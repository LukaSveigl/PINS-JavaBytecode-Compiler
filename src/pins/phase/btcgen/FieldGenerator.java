package pins.phase.btcgen;

import pins.common.report.Report;
import pins.data.ast.AstVarDecl;
import pins.data.ast.visitor.AstVisitor;
import pins.data.btc.BtcCLASS;
import pins.data.btc.var.BtcFIELD;
import pins.data.btc.var.BtcVar;
import pins.data.typ.*;
import pins.phase.seman.SemAn;

import java.util.Stack;

/**
 * Bytecode class field generator.
 */
public class FieldGenerator implements AstVisitor<BtcFIELD, BtcCLASS> {

    /**
     * Visits the variable declaration and generates the corresponding bytecode field.
     *
     * @param varDecl  The variable declaration.
     * @param btcClass The current bytecode class.
     * @return The result of the visit.
     */
    @Override
    public BtcFIELD visit(AstVarDecl varDecl, BtcCLASS btcClass) {
        BtcFIELD.Type type;
        SemType semType = SemAn.describesType.get(varDecl.type);
        Stack<BtcFIELD.Type> subTypes = new Stack<>();

        if (semType instanceof SemInt) {
            type = BtcFIELD.Type.LONG;
        } else if (semType instanceof SemChar) {
            type = BtcFIELD.Type.CHAR;
        } else if (semType instanceof SemArr arrType) {
            type = BtcFIELD.Type.ARRAY;
            if (arrType.elemType instanceof SemInt) {
                subTypes.add(BtcFIELD.Type.LONG);
            } else if (arrType.elemType instanceof SemChar) {
                subTypes.add(BtcFIELD.Type.CHAR);
            } else if (arrType.elemType instanceof SemPtr) {
                subTypes.add(BtcFIELD.Type.OBJECT);
            } else if (arrType.elemType instanceof SemArr) {
                subTypes.add(BtcFIELD.Type.ARRAY);

                SemType elemType = ((SemArr) arrType.elemType).elemType;
                do {
                    if (elemType instanceof SemArr) {
                        subTypes.add(BtcFIELD.Type.ARRAY);
                        elemType = ((SemArr) elemType).elemType;
                    } else if (elemType instanceof SemInt) {
                        subTypes.add(BtcFIELD.Type.LONG);
                        break;
                    } else if (elemType instanceof SemChar) {
                        subTypes.add(BtcFIELD.Type.CHAR);
                        break;
                    } else if (elemType instanceof SemPtr) {
                        // TODO: Implement this.
                    } else {
                        throw new Report.InternalError();
                    }
                } while (true);
            }else {
                throw new Report.InternalError();
            }
        } else if (semType instanceof SemPtr ptrType) {
            // TODO: Implement this.
            type = BtcFIELD.Type.ARRAY;
            if (ptrType.baseType instanceof SemInt) {
                subTypes.add(BtcFIELD.Type.LONG);
            } else if (ptrType.baseType instanceof SemChar) {
                subTypes.add(BtcFIELD.Type.INT);
            } else if (ptrType.baseType instanceof SemPtr) {
                subTypes.add(BtcFIELD.Type.OBJECT);
            } else if (ptrType.baseType instanceof SemArr) {
                subTypes.add(BtcFIELD.Type.ARRAY);
            } else {
                throw new Report.InternalError();
            }
        } else {
            throw new Report.InternalError();
        }
        // NOTE: If a new type is added, add it here.

        BtcFIELD field = new BtcFIELD(varDecl.name, type, subTypes);
        btcClass.addField(varDecl, field);
        return field;
    }

}
