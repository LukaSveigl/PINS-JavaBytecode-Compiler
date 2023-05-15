package pins.phase.btcgen;

import pins.common.report.Report;
import pins.data.ast.AstVarDecl;
import pins.data.ast.visitor.AstVisitor;
import pins.data.btc.BtcCLASS;
import pins.data.btc.var.BtcFIELD;
import pins.data.typ.SemArr;
import pins.data.typ.SemChar;
import pins.data.typ.SemInt;
import pins.data.typ.SemPtr;
import pins.phase.seman.SemAn;

/**
 * Bytecode class field generator.
 */
public class FieldGenerator implements AstVisitor<BtcFIELD, BtcCLASS> {

    @Override
    public BtcFIELD visit(AstVarDecl varDecl, BtcCLASS btcClass) {
        BtcFIELD.Type type = null;
        BtcFIELD.Type subType = null;
        if (SemAn.describesType.get(varDecl.type) instanceof SemInt) {
            type = BtcFIELD.Type.LONG;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemChar) {
            type = BtcFIELD.Type.INT;
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemArr) {
            type = BtcFIELD.Type.ARRAY;
            SemArr arrType =  (SemArr) SemAn.describesType.get(varDecl.type);
            if (arrType.elemType instanceof SemInt) {
                subType = BtcFIELD.Type.LONG;
            } else if (arrType.elemType instanceof SemChar) {
                subType = BtcFIELD.Type.INT;
            } else if (arrType.elemType instanceof SemPtr) {
                subType = BtcFIELD.Type.OBJECT;
            } else {
                throw new Report.InternalError();
            }
        } else if (SemAn.describesType.get(varDecl.type) instanceof SemPtr) {
            type = BtcFIELD.Type.OBJECT;
            SemPtr ptrType = (SemPtr) SemAn.describesType.get(varDecl.type);
            if (ptrType.baseType instanceof SemInt) {
                subType = BtcFIELD.Type.LONG;
            } else if (ptrType.baseType instanceof SemChar) {
                subType = BtcFIELD.Type.INT;
            } else if (ptrType.baseType instanceof SemPtr) {
                subType = BtcFIELD.Type.OBJECT;
            } else {
                throw new Report.InternalError();
            }
        } else {
            throw new Report.InternalError();
        }
        // NOTE: If a new type is added, add it here.

        BtcFIELD field = new BtcFIELD(varDecl.name, type, subType);
        btcClass.addField(varDecl, field);
        return field;
    }

}
