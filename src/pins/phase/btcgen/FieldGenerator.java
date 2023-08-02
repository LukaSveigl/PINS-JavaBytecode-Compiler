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

    @Override
    public BtcFIELD visit(AstVarDecl varDecl, BtcCLASS btcClass) {
        BtcFIELD.Type type = null;
        Stack<BtcFIELD.Type> subTypes = new Stack<>();
        if (SemAn.describesType.get(varDecl.type) instanceof SemInt) {
            type = BtcFIELD.Type.LONG;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemChar) {
            type = BtcFIELD.Type.CHAR;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemArr) {
            type = BtcFIELD.Type.ARRAY;
            SemArr arrType = (SemArr) SemAn.describesType.get(varDecl.type);
            if (arrType.elemType instanceof SemInt) {
                subTypes.add(BtcFIELD.Type.LONG);
                //subType = BtcFIELD.Type.LONG;
            } else if (arrType.elemType instanceof SemChar) {
                subTypes.add(BtcFIELD.Type.CHAR);
                //subType = BtcFIELD.Type.INT;
            } else if (arrType.elemType instanceof SemPtr) {
                subTypes.add(BtcFIELD.Type.OBJECT);
                //subType = BtcFIELD.Type.OBJECT;
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
                //subType = BtcFIELD.Type.ARRAY;
            }else {
                throw new Report.InternalError();
            }
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemPtr) {
            // TODO: Implement this.
            type = BtcFIELD.Type.ARRAY;
            SemPtr ptrType = (SemPtr) SemAn.describesType.get(varDecl.type);
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
