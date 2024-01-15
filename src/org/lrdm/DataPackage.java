package org.lrdm;

/**Represents a single data package to be held by mirrors.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class DataPackage {
    /** the file size of the data package measured in GB*/
    private final int fileSize; //in GB
    /** how much data of the file measured in GB is already received. has to be less than fileSize. */
    private int received; //in GB

    public DataPackage(int fileSize) {
        this.fileSize = fileSize;
        received = 0;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getReceived() {
        return received;
    }

    /**Increases the amount of received data (in GB) until {@link #fileSize} is reached.
     *
     * @param amount the amount of data in GB received
     */
    public void increaseReceived(int amount) {
        received += amount;
        if(received > fileSize) received = fileSize;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public boolean isLoaded() {
        return received == fileSize;
    }

    /**Resets the amount of received data to 0.
     */
    public void reset() {
        this.received = 0;
    }
}
