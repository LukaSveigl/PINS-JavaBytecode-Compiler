package pins.data.lin;

import pins.data.mem.*;

/**
 * A chunk of data.
 */
public class LinDataChunk extends LinChunk {

	/** The label where data is placed at. */
	public final MemLabel label;

	/** The size of data. */
	public final long size;

	/** The initial value. */
	public final String init;

	public LinDataChunk(MemAbsAccess absAccess) {
		this.label = absAccess.label;
		this.size = absAccess.size;
		this.init = absAccess.init;
	}

}
