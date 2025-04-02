package gitlet;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.io.Serializable;

public class StagingArea implements Serializable {
    public static final String SERIALIZED_STAGING_AREA = Repository.STAGING_AREA_DIR + File.separator + "stagedObj";
    
    public StagingArea() {
    }

    public void serialize() {
        Utils.writeObject(new File(SERIALIZED_STAGING_AREA), this);
    }
    public static final StagingArea deserialize() {
        return Utils.readObject(new File(SERIALIZED_STAGING_AREA), StagingArea.class);
    }

    /**
     * Description:
     * Adds a copy of the file as it currently exists to the staging area (see the description of the commit command).
     * For this reason, adding a file is also called staging the file for addition. Staging an already-staged file
     * overwrites the previous entry in the staging area with the new contents. The staging area should be somewhere
     * in .gitlet. If the current working version of the file is identical to the version in the current commit,
     * do not stage it to be added, and remove it from the staging area if it is already there (as can happen when
     * a file is changed, added, and then changed back to it’s original version). The file will no longer be staged
     * for removal (see gitlet rm), if it was at the time of the command.
     * @param fileName
     */
    public static final void stageFileForAddition(String fileName) throws GitletException {
        Path cwdFilePath = Paths.get(Repository.WORKING_DIR + File.separator + fileName);

        if (!cwdFilePath.toFile().exists()) {
            throw new GitletException("File does not exist.");
        }

        if (!cwdFilePath.toFile().isFile()) {
            throw new GitletException(fileName + " is not a file.");
        }

        // Compare current file in CWD with the one in MOST RECENT COMMIT (if it's been committed before).
        // If the current working version of the file is identical to the version in the current commit,
        // do not stage it to be added, and remove it from the staging area if it is already there (as can
        // happen when a file is changed, added, and then changed back to it’s original version).  The file
        // will no longer be staged for removal (see gitlet rm), if it was at the time of the command.
        if (!Commit.hasCWDFileChanged(cwdFilePath.toFile())) {
            StagingArea.deleteFileFromStagingArea(fileName);
            return; // Leave the method immediately as there is nothing else to be done!
        }

        Path stagedFilePath = Paths.get(Repository.STAGED_FOR_ADDITION + File.separator + fileName);

        try {
            // Copy file to the staging area and keep original file name in the staging directory:
            Files.copy(cwdFilePath, stagedFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            throw new GitletException(e.getMessage());
        }
    }

    /**
     * This method compare file in CWD with its STAGED version
     * @param cwdFile file in current working directory
     * @return true if file in CWD has changed, or false otherwise
     */
    public static final boolean hasCWDFileChanged(File cwdFile) {
        File stagedFile = new File(Repository.STAGED_FOR_ADDITION + File.separator + cwdFile.getName());

        if (!stagedFile.exists()) {
            return true;
        }

        if (Repository.filesHaveSameContents(cwdFile, stagedFile)) {
            // if contents of the file in CWD are the same as those of the staged file,
            // then nothing has changed. Thus return false.
            return false;
        } else {
            return true;
        }
    }
    public static final void stageFileForDeletion(String fileName) throws GitletException {
        Path cwdFilePath = Paths.get(Repository.WORKING_DIR + File.separator + fileName);

//        if (!cwdFile.toFile().exists()) {
//            throw new GitletException("File does not exist.");
//        }

//        if (!cwdFile.toFile().isFile()) {
//            throw new GitletException(fileName + "is not a file.");
//        }

        Path stagedFilePath = Paths.get(Repository.STAGED_FOR_DELETION + File.separator + fileName);

        try {
            if (cwdFilePath.toFile().exists()) {
                // If file still exists in CWD then copy the file to the staging area and
                // keep original file name in the staging directory:
                Files.copy(cwdFilePath, stagedFilePath, StandardCopyOption.REPLACE_EXISTING);
            }
            else {
                // But if the file doesn't exist in CWD anymore, then create a blank file
                // in the staging area to indicate that the file is marked to be removed.
                Utils.writeContents(stagedFilePath.toFile(), "");
            }
        }
        catch (IOException e) {
            // throw new GitletException(e.getMessage());
        }
    }

    /** *
     * Delete all files in the staging area.
     */
    public static final void clearAll() throws GitletException {
        deleteAllFilesStagedForAddition();
        deleteAllFilesStagedForDeletion();
    }

    public static final File[] getAllFilesStagedForAddition() {
        File stagingAreaDirectory = new File(Repository.STAGED_FOR_ADDITION);
        return stagingAreaDirectory.listFiles();
    }

    public static final File[] getAllFilesStagedForDeletion() {
        File stagingAreaDirectory = new File(Repository.STAGED_FOR_DELETION);
        return stagingAreaDirectory.listFiles();
    }

    public static final boolean isFileStagedForAddition(String fileName) {
        File stagingAreaDirectory = new File(Repository.STAGED_FOR_ADDITION + File.separator + fileName);
        return stagingAreaDirectory.exists();
    }

    public static final boolean isFileStagedForDeletion(String fileName) {
        File stagingAreaDirectory = new File(Repository.STAGED_FOR_DELETION + File.separator + fileName);
        return stagingAreaDirectory.exists();
    }

    public static final boolean isFileStaged(String fileName) {
        return isFileStagedForAddition(fileName) || isFileStagedForDeletion(fileName);
    }

    public static final void deleteFileFromStagingArea(String fileName) {
        // Delete from staging area for addition:
        deleteFileStagedForAddition(fileName);
        // Delete from staging area for deletion:
        deleteFileStagedForDeletion(fileName);
    }

    public static final void deleteFileStagedForAddition(String fileName) throws GitletException {
        // Delete from staging area for addition:
        File fileStagedForAddition = new File(Repository.STAGED_FOR_ADDITION + File.separator + fileName);
        if (fileStagedForAddition.exists() && fileStagedForAddition.isFile()){
            try {
                fileStagedForAddition.delete();
            }
            catch (SecurityException e) {
                throw new GitletException(e.getMessage());
            }
        }
    }

    public static final void deleteFileStagedForDeletion(String fileName) throws GitletException {
        // Delete from staging area for addition:
        File fileStagedForDeletion = new File(Repository.STAGED_FOR_DELETION + File.separator + fileName);
        if (fileStagedForDeletion.exists() && fileStagedForDeletion.isFile()){
            try {
                fileStagedForDeletion.delete();
            }
            catch (SecurityException e) {
                throw new GitletException(e.getMessage());
            }
        }
    }
    public static final void deleteAllFilesStagedForAddition() throws GitletException {
        File[] allFilesStagedForAddition = getAllFilesStagedForAddition();

        for (File f: allFilesStagedForAddition) {
            if (f.isFile()) {
                try {
                    f.delete();
                }
                catch (SecurityException e) {
                    throw new GitletException(e.getMessage());
                }
            }
        }
    }


    public static final void deleteAllFilesStagedForDeletion() throws GitletException {
        File[] allFilesStagedForDeletion = getAllFilesStagedForDeletion();

        for (File f: allFilesStagedForDeletion) {
            if (f.isFile()) {
                try {
                    f.delete();
                }
                catch (SecurityException e) {
                    throw new GitletException(e.getMessage());
                }
            }
        }
    }
}