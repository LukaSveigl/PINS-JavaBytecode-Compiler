package pins.phase.refan;

import pins.data.mem.MemAccess;
import pins.data.mem.MemRelAccess;

import java.util.HashMap;
import java.util.Vector;

/**
 * Reference analysis: computing reference candidates.
 */
public class RefAn implements AutoCloseable {

    /** Maps variables that should be implemented as references to their pointer depth. */
    public static final HashMap<MemAccess, Integer> referenceCandidates = new HashMap<MemAccess, Integer>(0);

    /** Variables that are accessed in closures. */
    public static final Vector<MemRelAccess> closureCandidates = new Vector<MemRelAccess>(0);

    /**
     * Constructs a new reference analysis phase.
     */
    public RefAn() {
    }

    public void close() {
    }

}
