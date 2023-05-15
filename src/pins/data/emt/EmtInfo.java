package pins.data.emt;

import pins.common.logger.Loggable;

/**
 * JVM class file structure.
 */
public interface EmtInfo extends Loggable {

    /**
     * Returns the info structure as a stream of bytes.
     *
     * @return The info structure as a stream of bytes.
     */
    byte[] bytecode();

    /**
     * Returns the size of the info structure in bytes.
     *
     * @return The size of the info structure in bytes.
     */
    int size();

}
