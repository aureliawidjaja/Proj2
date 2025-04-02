package gitlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.Serializable;
import java.util.*;
import java.time.LocalDateTime;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *  Commit class deals with staging area.
 *
 *  @author TODO
 */
public class Commit implements Serializable, Dumpable {
    /**
     * TODO: stageFileForAddition instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */
    private HashMap<String, String> blobs;
    private String commitId;
    private final String parentId;
    private final String message;
    private String timestamp;

    public Commit(String message, String parentId) {
        this.message   = message;
        this.parentId  = parentId;
        this.blobs     = new HashMap<>();
    }

    @Override
    public void dump() {
        System.out.println("commitId  = " + commitId);
        System.out.println("parentId  = " + parentId);
        System.out.println("timestamp = " + timestamp);
        System.out.println("message   = " + message);
        System.out.println("Contents of 'blobs' are as follows: ");
        for (Map.Entry<String, String> map : blobs.entrySet()) {
            System.out.println(" -> File name = " + map.getKey() + " | Blob name = " + map.getValue());
        }

    }

    public String execute() throws GitletException {
        // Step 0: Initialize timestamp and message:
        Pointer ptr;
        Index   idx;
        Branch  branch;
        this.commitId = Utils.sha1(LocalDateTime.now().toString());
        //this.commitId =  Utils.sha1(LocalDateTime.now().toString()).toString().substring(0, 7);

        if (this.parentId == null) {
            // When parentId is null, it means it is an initialization process:
            // Timestamp for EPOCH date and time = Wed Dec 31 16:00:00 1969 -0800
            this.timestamp = Repository.getTimestamp(true);
            ptr            = new Pointer();
            branch         = new Branch(ptr.getActiveBranch(), commitId);

        } else {
            this.timestamp = Repository.getTimestamp(false);
            ptr            = Pointer.deserialize();
            branch         = Branch.deserialize(ptr.getActiveBranch());
            // Files staged for addition:
            File[] filesStagedForAddition = StagingArea.getAllFilesStagedForAddition();

            // Files staged for deletion:
            File[] filesStagedForDeletion = StagingArea.getAllFilesStagedForDeletion();

            if (filesStagedForAddition.length == 0 && filesStagedForDeletion.length == 0) {
                throw new GitletException("No changes added to the commit.");
            }

            for (File f: filesStagedForAddition) {
                // Each file in this commit will be associated with a blob:
                String blobHash     = f.getName() + "-" + ptr.getActiveBranch() + "-" + commitId;
                Path blobFilePath   = Paths.get(Repository.BLOBS_DIR + File.separator + blobHash);

                try {
                    // The "move" copies source file to destination file and deletes source file.
                    Files.move(f.toPath(), blobFilePath, StandardCopyOption.REPLACE_EXISTING);


                } catch (IOException e) {
                    throw new GitletException(e.getMessage());
                }

                // Create a link between actual file name and its hash as a key -> value map.
                blobs.put(f.getName(), blobHash);
                branch.addFileNameToFileCatalog(f.getName());

                idx = new Index(f.getName());
                idx.setCommitId(this.commitId);
                idx.serialize();
            }

            for (File f: filesStagedForDeletion) {
                File cwdFile = new File(Repository.WORKING_DIR + File.separator + f.getName());
                // Delete current file from CWD:
                cwdFile.delete();
                // Delete file from staging area:
                f.delete();
            }
            // Clear staging area
            StagingArea.clearAll();
        }

        // Pointer always contains HEAD:
        ptr.setHead(commitId);
        ptr.addCommit(this);
        String currBranch = ptr.getActiveBranch();
        ptr.setCommitInBranch(currBranch, commitId);
        ptr.serialize();

        branch.setHead(commitId);
        branch.addCommit(this);
        branch.serialize();

        this.serialize();
        return commitId;
    }

    public static final boolean isFileTracked(String fileName) {
      return Index.isFileTracked(fileName);
    }

    public String getCommitId() {
        return commitId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getBlobName(String fileName) {
        return blobs.get(fileName);
    }

    public HashMap<String, String> getBlobs() {
        return this.blobs;
    }

    public static final String shortenTo8digits(String aString) {
        return aString.substring(0, 7);
    }

    public void serialize() {
        // Serialize this object instance:
        Utils.writeObject(new File(Repository.COMMITS_DIR + File.separator + shortenTo8digits(commitId)), this);
    }

    public static final Commit deserialize(String commitId)  {
        File commitFile = new File(Repository.COMMITS_DIR + File.separator + shortenTo8digits(commitId));
        if (commitFile.exists() && commitFile.isFile()) {
            return Utils.readObject(commitFile, Commit.class);
        } else {
            return null;
        }
    }

    public String globalLog() {
        String first = "===\n";
        String second = ""; // "Commit " + sourceHash + "\n";
        String third = timestamp + "\n";
        String fourth = message + "\n";
        String fifth = "\n";
        String log = first + second + third + fourth + fifth;
        return log;
    }

    public static final boolean hasCWDFileChanged(File cwdFile) {

        boolean fileHasChanged = true;
        Index idx = Index.deserialize(cwdFile.getName());

        if (idx != null) {
            // If file was committed before, its Index is not null.
            // Now let's get the latest commit associated with the index:
            Commit c  = Commit.deserialize(idx.getCommitId());
            // Obtain the blob tied to the commit and file name (Note: a commit can have an association
            // with multiple files. Hence, we need both commit id and file name to determine its
            // corresponding blob).
            File blobFile = new File(Repository.BLOBS_DIR + File.separator + c.getBlobName(cwdFile.getName()));
            // Now let's compare content of the file in CWD with the blob of the latest commit:
            if (Repository.filesHaveSameContents(cwdFile, blobFile)) {
                fileHasChanged = false;
            } else {
                fileHasChanged = true;
            }
        } else {
            // If no index has been created for the file before, then it means that the file has changed (the
            // file has come to existance in the CWD but has not as a blob).
            fileHasChanged = true;
        }
        return fileHasChanged;
    }

}
