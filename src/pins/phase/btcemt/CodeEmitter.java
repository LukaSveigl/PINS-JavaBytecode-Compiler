package pins.phase.btcemt;

import pins.common.report.Report;
import pins.data.emt.EmtClassFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CodeEmitter {

    public String dstFileName;

    public OutputStream dstFile;

    /**
     * Emits the bytecode.
     */
    public void emit() {
        for (EmtClassFile classFile : BtcEmt.classFiles.values()) {

            this.dstFileName = classFile.dstName + ".class";

            try {
                File dstFile = new File(dstFileName);
                dstFile.createNewFile();
                this.dstFile = new FileOutputStream(dstFile, false);
            } catch (IOException e) {
                throw new Report.Error("Cannot create file " + dstFileName);
            }



            try {
                this.dstFile.write(classFile.bytecode());
            } catch (IOException e) {
                throw new Report.Error("Cannot write to file " + this.dstFileName);
            }
        }
    }

}
