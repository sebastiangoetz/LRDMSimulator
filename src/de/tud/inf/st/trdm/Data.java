package de.tud.inf.st.trdm;

public class Data {
    /** the file size of the data package measured in GB*/
    private final int fileSize; //in GB
    /** how much data of the file measured in GB is already received. has to be less than fileSize. */
    private int received; //in GB

    private int data;

    public Data(int fileSize, int data) {
        this.fileSize = fileSize;
        received = 0;
        this.data =data;
    }

    public int getFileSize() {
        return fileSize;
    }

    public int getReceived() {
        return received;
    }

    public int getData(){
        return data;
    }

    public void increaseReceived(int amount) {
        received += amount;
        if(received > fileSize) received = fileSize;
    }

    public boolean isLoaded() {
        return received == fileSize;
    }

}
