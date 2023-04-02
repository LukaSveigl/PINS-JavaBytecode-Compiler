package pins.phase.btcgen;

import pins.common.report.Report;
import pins.data.btc.BtcClass;

import java.io.*;

public class BtcEmitter {

    /** The destination file name. */
    private final String dstFileName;

    /** The destination file writer. */
    private final OutputStream dstFile;

    /**
     * Constructs a new bytecode emitter.
     *
     * @param dstFileName The destination file name.
     */
    public BtcEmitter(String dstFileName) {
        this.dstFileName = dstFileName;
        System.out.println("Writing to " + dstFileName);
        try {
            File dstFile = new File(dstFileName);
            dstFile.createNewFile();
            this.dstFile = new FileOutputStream(dstFile, false);
        } catch (IOException e) {
            throw new Report.Error("Cannot create file " + dstFileName);
        }
    }

    /**
     * Emits the bytecode.
     */
    public void emit() {
        for (BtcClass btcClass : BtcGen.btcClasses) {
            try {
                this.dstFile.write(btcClass.toBytecode());
            } catch (IOException e) {
                throw new Report.Error("Cannot write to file " + this.dstFileName);
            }
        }
    }

}
