package org.lrdm;

/**Represents a single data package to be held by mirrors.
 *
 * @author Sebastian Götz <sebastian.goetz1@tu-dresden.de>
 */
public class Data {
    /** the file size of the data package measured in GB*/
    private final int fileSize; //in GB
    /** how much data of the file measured in GB is already received. has to be less than fileSize. */
    private int received; //in GB

    private int content;

    public Data(int fileSize, int content) {
        this.fileSize = fileSize;
        received = 0;
        this.content =content;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public int getFileSize() {
        return fileSize;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public int getReceived() {
        return received;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @param received the true if all data has been received, else false
     */
    public void setReceived(int received){
        this.received = received;
    }


    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public int getContent(){
        return content;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @param content the true if all data has been received, else false
     */
    public void setContent(int content){
        this.content = content;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @param amount the true if all data has been received, else false
     * @return true if all data has been received, else false
     */
    public int increaseReceived(int amount) {
        received += amount;
        int difference = fileSize - received;
        if(received >= fileSize) received = fileSize;
        return difference;
    }

    /**Answers whether the complete data package has been loaded.
     *
     * @return true if all data has been received, else false
     */
    public boolean isLoaded() {
        return received == fileSize;
    }

}
