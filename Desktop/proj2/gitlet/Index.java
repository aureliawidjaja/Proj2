package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Index stores timestamp of a file that is committed (i.e. timestamp of a commit). It is assumed that
 * if a file has been changed, then its timestamp (i.e. last change date and time) has also changed
 *  (or been changed by the file system or operating system).
 */
public class Index implements Serializable, Dumpable {
    private String fileName;
    private long timestamp;
    private String commitId;
    /** 
     * @fileName: Relative path
     */
    public Index(String fileName) {
        this.fileName = fileName;

        // actualFile is the file to be indexed or already has an index:
        File actualFile = new File(Repository.WORKING_DIR + File.separator + fileName);
        if (actualFile.exists()) {
            // Timestamp of last file modification:
            timestamp = actualFile.lastModified();
        }
    }

    @Override
    public void dump() {
        System.out.println("fileName = " + fileName);
        System.out.println("commitId = " + commitId);
        Date d = new Date(this.timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat(Repository.DATE_FORMAT);
        System.out.println("timestamp = " + formatter.format(d));
    }

    public static final File[] getAllIndexFiles() {
        File indexDirectory = new File(Repository.INDEXES_DIR);
        return indexDirectory.listFiles();
    }

    public boolean hasChanged() {
        boolean indexChanged;

        File indexedFile = new File(Repository.INDEXES_DIR + File.separator + this.fileName);

        if (indexedFile.exists()) {
            // If indexed file has not yet been created and is to be created, it means
            // that the file represented by fileName has not been committed (yet).
            Index previousIndex = deserialize(this.fileName);

            // We assume that if stored/serialized timestamp is NOT the same as timestamp of current file,
            // it means that the file has ALREADY been modified.
            if (previousIndex.getTimestamp() != this.getTimestamp() && previousIndex.getCommitId() != null) {
                indexChanged = true;
            }
            else {
                indexChanged = false;
            }
        }
        else {
            indexChanged = true;
        }
        return indexChanged;
    }

    public static final boolean isFileCommitted(String fileName) {
        File indexFile = new File(Repository.INDEXES_DIR + File.separator + fileName);
        // If index file exists (with the same name as the original/actual file name, then we assume that
        // it has already been committed before:
        return indexFile.exists();
    }

    public static final  boolean isFileTracked(String fileName) {
        File indexFile = new File(Repository.INDEXES_DIR + File.separator + fileName);
        return (indexFile.exists() && indexFile.isFile());
    }
    public static final Index deserialize(String fileName) {
        File indexFile = new File(Repository.INDEXES_DIR + File.separator + fileName);
        if (indexFile.exists() && indexFile.isFile()) {
            return Utils.readObject(indexFile, Index.class);
        }
        else {
            return null;
        }
    }

    public void serialize() {
        Utils.writeObject(new File(Repository.INDEXES_DIR + File.separator + this.fileName), this);
    }
    public long getTimestamp() {
        return timestamp;
    }

    public void setCommitId(String commitId) {
        this.commitId = commitId;
    }

    public String getCommitId() {
        return commitId;
    }
    public static final void resetAll() {
        File indexDirectory = new File(Repository.INDEXES_DIR);
        File[] listOfFiles  = indexDirectory.listFiles();

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                listOfFiles[i].delete();
            }
        }
    }
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
