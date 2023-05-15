package pins.phase._btcgen;

import pins.common.report.Report;
import pins.data.ast.AstVarDecl;
import pins.data.ast.visitor.AstVisitor;
import pins.data._btc.BtcClass;
import pins.data._btc.fpool.BtcField;
import pins.data.typ.*;
import pins.phase.seman.SemAn;

public class FieldGenerator implements AstVisitor<BtcField, BtcClass> {

    @Override
    public BtcField visit(AstVarDecl varDecl, BtcClass btcClass) {
        SemType type = SemAn.describesType.get(varDecl.type);
        String descriptor;

        if (type instanceof SemInt) {
            descriptor = "J";
        } else if (type instanceof SemChar) {
            descriptor = "C";
        } else if (type instanceof SemArr) {
            descriptor = "[";
        } else if (type instanceof SemPtr) {
            descriptor = "L";
        } else {
            throw new Report.Error("Unknown type");
        }

        btcClass.addField(varDecl.name, descriptor, null);
        return null;
    }

}
