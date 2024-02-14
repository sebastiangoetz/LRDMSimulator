package org.lrdm;

/**Represents a single data unit to be held by a {@link DataPackage}.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class Data {
    /** the file size of the data package measured in GB*/
    private final int fileSize; //in GB
    /** how much data of the file measured in GB is already received. has to be less than fileSize. */
    private int received; //in GB

    /** content of the data unit */
    private int content;

    public Data(int fileSize, int content) {
        this.fileSize = fileSize;
        received = 0;
        this.content =content;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getReceived() {
        return received;
    }

    public void setReceived(int received){
        this.received = received;
    }


    public int getContent(){
        return content;
    }

    public void setContent(int content){
        this.content = content;
    }

    public int increaseReceived(int amount) {
        received += amount;
        int difference = fileSize - received;
        if(received >= fileSize) received = fileSize;
        return difference;
    }

    /**Answers whether the data unit has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public boolean isLoaded() {
        return received == fileSize;
    }

}
