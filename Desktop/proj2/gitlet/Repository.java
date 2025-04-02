package gitlet;

import java.io.*;

//import static gitlet.Utils.*;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.lang.String.join;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: stageFileForAddition instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** Working directory */

    public static final String WORKING_DIR = System.getProperty("user.dir");

    /** Gitlet repo's folder */
    public static final String REPO_DIR = WORKING_DIR + File.separator + ".gitlet";

    /** All other folders: */
    public static final String INDEXES_DIR = REPO_DIR + File.separator + "indexes";

    public static final String STAGING_AREA_DIR = REPO_DIR + File.separator + "staging_area";
    public static final String STAGED_FOR_ADDITION = STAGING_AREA_DIR + File.separator +  "addition";
    public static final String STAGED_FOR_DELETION = STAGING_AREA_DIR + File.separator + "deletion";

    public static final String COMMITS_DIR = REPO_DIR + File.separator + "commits";
    public static final String LOGS_DIR = REPO_DIR + File.separator + "logs";

    public static final String BRANCHES_DIR = REPO_DIR + File.separator + "branches";

    public static final String BLOBS_DIR = REPO_DIR + File.separator + "blobs";

    public static final String DATE_FORMAT = "EEE MMM d HH:mm:ss yyyy Z";
    /* TODO: fill in the rest of this class. */
    public Repository() {
    }

    public static final boolean repoExists() {
        // Test whether gitlet repo already exists:
        return (new File(REPO_DIR).exists());
    }
    public static final void init() throws GitletException {
        File gitlet = new File(REPO_DIR);

        if (repoExists()){
            throw new GitletException("A Gitlet version-control system "
                                       + "already exists in the current directory.");
        }
        else {
            try {
                // Let's initialize all the relevant directories:
                File dir = new File(REPO_DIR);
                dir.mkdir();

                dir = new File(STAGING_AREA_DIR);
                dir.mkdir();

                dir = new File(STAGING_AREA_DIR);
                dir.mkdir();

                dir = new File(STAGED_FOR_ADDITION);
                dir.mkdir();

                dir = new File(STAGED_FOR_DELETION);
                dir.mkdir();

                dir = new File(COMMITS_DIR);
                dir.mkdir();

                dir = new File(BRANCHES_DIR);
                dir.mkdir();

                dir = new File(BLOBS_DIR);
                dir.mkdir();

                dir = new File(INDEXES_DIR);
                dir.mkdir();
            }
            catch (SecurityException e) {
                throw new GitletException(e.getMessage());
            }

            Commit initialCommit = new Commit("initial commit",  null);
            initialCommit.execute();
        }
    }

    public void add(String fileName) throws GitletException {
        StagingArea.stageFileForAddition(fileName);
    }

    /**
     * Description:
     * Unstage the file if it is currently staged for addition. If the file is tracked in the current commit,
     * stage it for removal and remove the file from the working directory if the user has not already done so
     * (do not remove it unless it is tracked in the current commit).
     *
     * Failure cases:
     * If the file is neither staged nor tracked by the head commit, print the error message:
     * "No reason to remove the file".
     *
     * @param fileName : File to be removed
     **/
    public static final void rm(String fileName) throws GitletException {
        // If the file is neither staged nor tracked by the head commit, print the error message:
        // "No reason to remove the file".
        if (!StagingArea.isFileStaged(fileName) && !Commit.isFileTracked(fileName)) {
            throw new GitletException("No reason to remove the file.");
        }

        File cwdFile = new File(WORKING_DIR + File.separator + fileName);

        // Criteria #1:
        // Unstage the file if it is currently staged for addition
        if (StagingArea.isFileStagedForAddition(fileName)) {
            StagingArea.deleteFileStagedForAddition(fileName);
        }

        // Criteria #2:
        // If the file is tracked in the current commit, stage it for removal and remove the file from
        // the working directory if the user has not already done so (do not remove it unless it is
        // tracked in the current commit).
        if (Commit.isFileTracked(fileName)) {
            StagingArea.stageFileForDeletion(fileName);
            if (cwdFile.exists()) {
                cwdFile.delete();
            }
        }
    }

    public static final void log() {
        Pointer ptr = Pointer.deserialize();
        String currBranch = ptr.getActiveBranch();
        ptr.displayBranchLog(currBranch);
    }

    /**
     * Description:
     * Creates a new branch with the given name, and points it at the current head commit. A branch is nothing more
     * than a name for a reference (a SHA-1 identifier) to a commit node. This command does NOT immediately switch
     * to the newly created branch (just as in real Git). Before you ever call branch, your code should be running
     * with a default branch called “main”.
     *
     * Runtime: Should be constant relative to any significant measure.
     *
     * Failure cases: If a branch with the given name already exists, print the error message A branch with that
     * name already exists.
     *
     * */
    public static final void branch(String branchName) throws GitletException {
        Pointer ptr = Pointer.deserialize();
        ptr.createBranch(branchName);
    }

    /**
     * Description:
     * Switches to the branch with the given name. Takes all files in the commit at the head of the given branch,
     * and puts them in the working directory, overwriting the versions of the files that are already there if
     * they exist. Also, at the end of this command, the given branch will now be considered the current branch (HEAD).
     * Any files that are tracked in the current branch but are not present in the checked-out branch are deleted.
     * The staging area is cleared, unless the checked-out branch is the current branch (see Failure cases below).
     *
     * Runtime: Should be constant relative to any significant measure.
     *
     * Failure cases: If no branch with that name exists, print No such branch exists. If that branch is the current
     * branch, print No need to switch to the current branch. If a working file is untracked in the current branch and
     * would be overwritten by the switch, print There is an untracked file in the way; delete it, or add and commit it
     * first. and exit; perform this check before doing anything else. Do not change the CWD.
     */
    public static final void switchToBranch(String branchName) throws GitletException {

        Pointer ptr = Pointer.deserialize();

        if (ptr.getCommitIdOfBranch(branchName) == null) {
            throw new GitletException("No such branch exists.");
        }

        String currentHead   = ptr.getHead();
        Commit currentCommit = Commit.deserialize(currentHead);
        HashMap<String, String> currentBlobs = currentCommit.getBlobs();
        Branch currentBranch = Branch.deserialize(ptr.getActiveBranch());

        String checkOutHead   = ptr.switchToBranch(branchName); // Pointer will set HEAD to the check-out branch
        Commit checkOutCommit = Commit.deserialize(checkOutHead);
        HashMap<String, String> checkOutBlobs = checkOutCommit.getBlobs();
        Branch checkOutBranch = Branch.deserialize(ptr.getActiveBranch());

        // If a working file is untracked in the current branch and would be overwritten by the switch,
        // print There is an untracked file in the way; delete it, or add and commit it first
        for (File f : Repository.getAllFilesInCWD()) {
            if (!f.isDirectory() &&
                !currentBranch.isFileTracked(f.getName()) &&
                !StagingArea.isFileStagedForAddition(f.getName())) {
                throw new GitletException("There is an untracked file in the way; delete it, "
                        + "or add and commit it first.");
            }
        }

        Branch checkOutBranch = Branch.deserialize(ptr.getActiveBranch());
        for (Map.Entry<String, String> map : checkOutBlobs.entrySet()) {
            // Instruction #1:
            // Takes all files in the commit at the head of the given branch, and puts them in the working directory,
            // overwriting the versions of the files that are already there if they exist. Also, at the end of
            // this command, the given branch will now be considered the current branch (HEAD).
            try {
                File blobFile     = new File(BLOBS_DIR + File.separator + map.getValue());
                File checkOutFile = new File(WORKING_DIR + File.separator + map.getKey());
                Files.copy(blobFile.toPath(), checkOutFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            catch (IOException e) {
                //System.out.println(e.getMessage());
                //System.exit(0);
                throw new GitletException(e.getMessage());
            }
        }

        for (Map.Entry<String, String> map : currentBlobs.entrySet()) {
            // Instruction #2:
            // Any files that are tracked in the current branch but are not present in the checked-out branch
            // will be deleted.
            if (!checkOutBranch.isFileTracked(map.getKey())) {
                // Current tracked file is not tracked in the check-out branch so we'll delete the file from CWD:
                File cwdFile = new File(Repository.WORKING_DIR + File.separator + map.getKey());
                if (cwdFile.exists()) {
                    cwdFile.delete();
                }
            }
        }

        StagingArea.clearAll();
    }

    public static final void commit(String message) throws GitletException {
        if (message == null || message == "") {
            throw new GitletException("Please enter a commit message.");
        }

        Pointer ptr     = Pointer.deserialize();
        String parentId = ptr.getHead();

        Commit c = new Commit(message, parentId);
        c.execute();
    }

    public static final void restore(String fileName) throws GitletException {
        Pointer ptr = Pointer.deserialize();
        String commitId = ptr.getHead();
        restore(commitId, fileName);
    }

    public static final void restore(String commitId, String fileName) throws GitletException {
        Commit c = Commit.deserialize(commitId);
        if (c == null) {
            //System.out.println("No commit with that id exists.");
            //System.exit(0);
            //return false;
            throw new GitletException("No commit with that id exists.");
        }
        HashMap<String, String> blobs = c.getBlobs();
        String blobName = blobs.get(fileName);

        if (blobName == null) {
            ////System.out.println("File does not exist in that commit.");
            //System.exit(0);
            throw new GitletException("File does not exist in that commit.");
        }

        File blobFile   = new File(BLOBS_DIR + File.separator + blobName);
        File targetFile = new File(WORKING_DIR + File.separator + fileName);

        try {
            Files.copy(blobFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            //System.out.println(e.getMessage());
            //System.exit(0);
            throw new GitletException(e.getMessage());
        }
    }

<<<<<<< HEAD
=======
    public static final void deleteCWDFile(String fileName) {
        File cwdFile = new File(WORKING_DIR + File.separator + fileName);

        if (cwdFile.exists() && !cwdFile.isDirectory()) {
            cwdFile.delete();
        }
    }
>>>>>>> 26e5feaeb9f5b74e717121fd0d193187e893830e
/**
 * Description:
 * Restores all the files tracked by the given commit. Removes tracked files that are not present in that commit.
 * Also moves the current branch’s head to that commit node. See the intro for an example of what happens to the
 * head pointer after using reset. The [commit id] may be abbreviated as for restore. The staging area is cleared.
 *
 * Runtime:
 * Should be linear with respect to the total size of files tracked by the given commit’s snapshot.
 * Should be constant with respect to any measure involving number of commits.
 *
 * Failure case:
 * If no commit with the given id exists, print No commit with that id exists. If a working file is untracked in
 * the current branch and would be overwritten by the reset, print There is an untracked file in the way; delete it,
 * or add and commit it first. and exit; perform this check before doing anything else.
 */

public static final void reset(String commitId) throws GitletException {
    public static final void reset(String commitId) throws GitletException {
        Commit newHeadCommit = Commit.deserialize(commitId);
        Pointer ptr = Pointer.deserialize();
        Branch currentBranch = Branch.deserialize(ptr.getActiveBranch());

        if(newHeadCommit == null) {
            throw new GitletException("No commit with that id exists.");
        }

        // If a working file is untracked in the current branch and would be overwritten by the reset, print
        // There is an untracked file in the way; delete it, or add and commit it first. and exit
        for (File f : Repository.getAllFilesInCWD()) {
            if (!f.isDirectory() &&
                !currentBranch.isFileTracked(f.getName()) && // If file is not tracked in current branch
                !StagingArea.isFileStagedForAddition(f.getName())) { // If file is not staged for addition:
                    throw new GitletException("There is an untracked file in the way; delete it, "
                            + "or add and commit it first.");
            }
        }

        // Instruction #1:
        // Restores all the files tracked by the given commit:
        HashMap<String, String> blobs = newHeadCommit.getBlobs();
        Set fileSet = blobs.keySet();
        Iterator<String> fileIterator = fileSet.iterator();

        while (fileIterator.hasNext()) {
            Repository.restore(commitId, fileIterator.next());
        }

        // Instruction #2:
        // Removes tracked files that are not present in that commit.
        File[] filesStagedForAddition = StagingArea.getAllFilesStagedForAddition();
        if (filesStagedForAddition != null) {
            for (int i = 0; i < filesStagedForAddition.length; i++) {
                if (!fileSet.contains(filesStagedForAddition[i])) {
                    StagingArea.deleteFileStagedForAddition(filesStagedForAddition[i].getName());
                }
            }
        }

        File[] cwdFiles = getAllFilesInCWD();
        if (cwdFiles != null) {
            for (int i = 0; i < cwdFiles.length; i++) {
                if (!fileSet.contains(cwdFiles[i])) {
                    StagingArea.deleteFileStagedForAddition(cwdFiles[i].getName());
                }
            }
        }
        File[] filesStagedForDeletion = StagingArea.getAllFilesStagedForDeletion();
        if (filesStagedForDeletion != null) {
            for (int i = 0; i < filesStagedForDeletion.length; i++) {
                if (!fileSet.contains(filesStagedForDeletion[i])) {
                    StagingArea.deleteFileStagedForDeletion(filesStagedForDeletion[i].getName());
                }
            }
        }

        // Instruction #3:
        // Moves the current branch’s head to that commit node.
        ptr.setHead(commitId);
        currentBranch.setHead(commitId);

        LinkedList<Commit> commitList   = currentBranch.getCommitList();
        Iterator<Commit> commitIterator = commitList.iterator();
        while (commitIterator.hasNext()) {
            Commit c = commitIterator.next();
            if (!c.getCommitId().equals(commitId)) {
                commitList.removeFirst();
                break;
            }
        }

        currentBranch.setCommitList(commitList);

        currentBranch.serialize();
        ptr.serialize();
>>>>>>> 26e5feaeb9f5b74e717121fd0d193187e893830e

    Commit newHeadCommit = Commit.deserialize(commitId);
    if(newHeadCommit == null) {
        throw new GitletException("No commit with that id exists.");
    }
<<<<<<< HEAD

    // Instruction #1:
    // Restores all the files tracked by the given commit:
    HashMap<String, String> blobs = newHeadCommit.getBlobs();
    Set fileSet = blobs.keySet();
    Iterator<String> fileIterator = fileSet.iterator();

    while (fileIterator.hasNext()) {
        Repository.restore(commitId, fileIterator.next());
    }


    // Instruction #2:
    // Removes tracked files that are not present in that commit.
    File[] filesStagedForAddition = StagingArea.getAllFilesStagedForAddition();
    if (filesStagedForAddition != null) {
        for (int i = 0; i < filesStagedForAddition.length; i++) {
            if (!fileSet.contains(filesStagedForAddition[i])) {
                StagingArea.deleteFileStagedForAddition(filesStagedForAddition[i].getName());
            }
        }
    }

    File[] filesStagedForDeletion = StagingArea.getAllFilesStagedForDeletion();
    if (filesStagedForDeletion != null) {
        for (int i = 0; i < filesStagedForDeletion.length; i++) {
            if (!fileSet.contains(filesStagedForDeletion[i])) {
                StagingArea.deleteFileStagedForDeletion(filesStagedForDeletion[i].getName());
            }
        }
    }

    // Instruction #3:
    // Moves the current branch’s head to that commit node.
    Pointer ptr = Pointer.deserialize();
    ptr.setHead(commitId);

    Branch currentBranch = Branch.deserialize(ptr.getActiveBranch());
    currentBranch.setHead(commitId);

    // Now we are going to delete all trailing commits since
    // the commit with commitId is the NEW HEAD now.
    LinkedList<Commit> commitList = ptr.getCommitList();
    int size = commitList.size();
    for (int i = size - 1; i > 0; i--) {
        if (commitList.getLast().getCommitId().equals(commitId)) {
            ptr.serialize();
            break;
        } else {
            commitList.removeLast();
        }
    }
    LinkedList<Commit> commitListBranch = currentBranch.getCommitList();
    int sizeBranch = commitListBranch.size();
    for (int i = 0; i < sizeBranch - 1 ; i++) {
        if (commitListBranch.getFirst().getCommitId().equals(commitId)) {
            currentBranch.setCommitList(commitListBranch);
            currentBranch.serialize();
            break;
        } else {
            commitListBranch.removeFirst();
        }
    }


    ptr.setCommitList(commitList);
    ptr.serialize();

}

=======
>>>>>>> 26e5feaeb9f5b74e717121fd0d193187e893830e

    public static final void find(String msg) {
        boolean matchFound = false;
        Pointer ptr = Pointer.deserialize();
        LinkedList<Commit> commitList = ptr.getCommitList();

        for (Commit c : commitList) {
            if (c.getMessage().equals(msg)) {
                matchFound = true;
                System.out.println(c.getCommitId());
            }
        }
        if (!matchFound) {
            System.out.println("Found no commit with that message.");
        }
    }

    public static final void global_log() {
        Pointer ptr = Pointer.deserialize();
        ptr.displayLog();
    }

    /**
    * Description: Deletes the branch with the given name. This only means to delete the pointer associated with
     * the branch; it does not mean to delete all commits that were created under the branch, or anything like that.
     *
     * Runtime: Should be constant relative to any significant measure.
     *
     * Failure cases: If a branch with the given name does not exist, aborts. Print the error message A branch with
     * that name does not exist. If you try to remove the branch you’re currently on, aborts, printing the error
     * message Cannot remove the current branch.
    */
    public static final void rmBranch(String branchName) {
        Pointer ptr = Pointer.deserialize();
        ptr.removeBranch(branchName);
        ptr.serialize();
    }

    public static final boolean isRepoInitialized() {
        return new File(REPO_DIR).exists();
    }

    public static final void status() throws GitletException {
        if (!isRepoInitialized()) {
            throw new GitletException("Not in an initialized Gitlet directory.");
        }


        Pointer ptr         = Pointer.deserialize();
        String activeBranch = ptr.getActiveBranch();

        File[] stagedFiles  = StagingArea.getAllFilesStagedForAddition();
        File[] removedFiles = StagingArea.getAllFilesStagedForDeletion();
        File[] filesCWD     = getAllFilesInCWD();
        File cwdFile;

        System.out.println("=== Branches ===");
        System.out.println("*" + activeBranch);
        for (Map.Entry<String, String> map : ptr.getBranchList().entrySet()) {
            if (!map.getKey().equals(activeBranch)) {
                System.out.println(map.getKey());
            }
        }
        //Retrieve other branches
        System.out.println();

        System.out.println("=== Staged Files ===");
        if (stagedFiles != null) {
            for (File f : stagedFiles) {
                System.out.println(f.getName());
            }
        }
        System.out.println();

        System.out.println("=== Removed Files ===");
        if (removedFiles != null) {
            for (File f : removedFiles) {
                System.out.println(f.getName());
            }
        }
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        if (filesCWD != null) {
            for (File f : filesCWD) {
                // For all files in CWD:

                // Criteria #1: Tracked in the current commit, changed in the working directory, but not staged;
                if (Commit.isFileTracked(f.getName()) &&
                    Commit.hasCWDFileChanged(f) &&
                    !StagingArea.isFileStaged(f.getName())) {

                    System.out.println(f.getName() + " (modified)");
                }

                // Criteria #2: Staged for addition, but with different contents than in the working directory
                if (StagingArea.isFileStaged(f.getName()) && StagingArea.hasCWDFileChanged(f)) {
                    System.out.println(f.getName() + " (modified)");
                }
            }
        }

        if (stagedFiles != null) {
            // Criteria #3: Staged for addition, but deleted in the working directory
            for (File f : stagedFiles) {
                cwdFile = new File(Repository.WORKING_DIR + File.separator + f.getName());
                if (!cwdFile.exists()) {
                    // If current file doesn't exist in CWD (anymore), it might have been deleted
                    // (or moved to another folder or cloud)
                    System.out.println(f.getName() + " (deleted)");
                }
            }
        }

        // Criteria #4: Not staged for removal, but tracked in the current commit and
        // deleted from the working directory.
        //
        // Fact: Each of all files tracked in the current commit has an index with the
        // SAME file name that is stored in the "indexes" directory.
 /*       File[] indexFiles = Index.getAllIndexFiles();
        if (indexFiles != null) {
            for (File f : indexFiles) {
                if (!StagingArea.isFileStagedForDeletion(f.getName())) {
                    cwdFile = new File(Repository.WORKING_DIR + File.separator + f.getName());
                    if (!cwdFile.exists()) {
                        System.out.println(f.getName() + " (deleted)");
                    }
                }
            }
        }*/
        System.out.println();

        System.out.println("=== Untracked Files ===");
        // The final category (“Untracked Files”) is for files present in the working directory but neither
        // staged for addition nor tracked. This includes files that have been staged for removal, but then
        // re-created without Gitlet’s knowledge. Ignore any subdirectories that may have been introduced,
        // since Gitlet does not deal with them.
        //
        // Note that “Untracked Files” does not include untracked files that are staged for addition.
        // This is distinct from our definition of “untracked” elsewhere. In all other usages of “untracked”
        // outside of this status category, we mean files that are not tracked in the current commit, regardless
        // of whether they are staged.
        if (filesCWD != null) {
            for (File f : filesCWD) {
                if (!f.isDirectory()) {
                    // We don't track directories
                    // Criteria: f is present in the working directory but neither staged for addition nor tracked
                    Index idx = Index.deserialize(f.getName());
                    if (idx == null && !StagingArea.isFileStaged(f.getName())) {
                        // If idx is null, it means that file has not been committed (yet).
                        System.out.println(f.getName());
                    }
                }

            }
        }
    }

    public static final File[] getAllFilesInCWD() {
        File CWD = new File(Repository.WORKING_DIR);
        return CWD.listFiles();
    }

    public static final boolean isFileTrackedByCurrentCommit(String fileName) {
        return StagingArea.isFileStaged(fileName);
    }

    public static final boolean filesHaveSameContents(File file1, File file2) {
        String file1Str = Utils.readContentsAsString(file1);
        String file2Str = Utils.readContentsAsString(file2);
        return (file1Str.equals(file2Str));
    }
    /**
     *
     * @param returnEpochTime if true it will return the EPOCH date time, otherwise current date time.
     * @return Epoch or current date time in this format: EEE MMM d HH:mm:ss yyyy Z
     * Examples:
     * Epoch date time = Wed Dec 31 16:00:00 1969 -0800
     * Current date time = Sun Jul 16 13:49:45 2023 -0700
     */
    public static final String getTimestamp(boolean returnEpochTime) {
        Date d;

        if (returnEpochTime) {
            d = new Date(0);
        }
        else {
            d = new Date();
        }

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        return formatter.format(d);
    }
}
