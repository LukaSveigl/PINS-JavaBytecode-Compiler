package pins.data.btc.arithmetic;

import pins.data.btc.BtcInstr;

public class BtcBINOP extends BtcInstr {

    public enum Oper {
        ADD, SUB, MUL, DIV, REM, AND, OR, XOR, SHL, SHR, USHR
    }

    public enum Type {
        I, L, F, D
    }
}
