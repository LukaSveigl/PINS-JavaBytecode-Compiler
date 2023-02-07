package pins.phase.btcgen;

import pins.common.report.Report;

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

    }

}