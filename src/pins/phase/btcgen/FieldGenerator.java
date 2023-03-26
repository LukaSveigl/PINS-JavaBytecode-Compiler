package pins.phase.btcgen;

import pins.data.ast.AstVarDecl;
import pins.data.ast.visitor.AstVisitor;
import pins.data.btc.BtcClass;
import pins.data.btc.fpool.BtcField;

public class FieldGenerator implements AstVisitor<BtcField, BtcClass> {

    @Override
    public BtcField visit(AstVarDecl varDecl, BtcClass btcClass) {
        return null;
    }

}
