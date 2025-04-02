<<<<<<< HEAD
package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Pointer always contains HEAD and MASTER
 */
public class Pointer implements Serializable, Dumpable {
    private String HEAD;
    private LinkedList<Commit> commitList;
    private String activeBranch;
    private HashMap<String, ArrayList<String>> branchCommitList;
    private HashMap<String, String> branchList;
    public static final String POINTER_OBJ = Repository.COMMITS_DIR + File.separator + "POINTER";
    public static final String DEFAULT_BRANCH = "main";

    public Pointer() {
        activeBranch = DEFAULT_BRANCH;

        // branchList contains a key value pair of branch name and latest commit id of the branch
        branchList   = new HashMap<>();

        commitList = new LinkedList<>();
        branchCommitList = new HashMap<>();
    }

    public void createBranch(String branchName) throws GitletException {
        if (branchList.get(branchName) != null) {
            throw new GitletException("A branch with that name already exists.");
        }
        // When a branch is newly created, it points to HEAD
        branchList.put(branchName, HEAD);

        Branch newBranch = new Branch(branchName, HEAD);
        newBranch.setCommitList(commitList);
        newBranch.serialize();
        serialize();
    }

    public String switchToBranch(String branchName) throws GitletException {
        if (branchList.get(branchName) == null) {
            throw new GitletException("A branch with that name does NOT exist.");
        }

        if (activeBranch == branchName) {
            throw new GitletException("No need to switch to the current branch.");
        }

        Branch newBranch = Branch.deserialize(branchName);

        activeBranch  = branchName;
        HEAD          = newBranch.getHead();
        commitList    = newBranch.getCommitList();
        branchList.put(branchName, HEAD);


        serialize();
        return HEAD;
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
    public void removeBranch(String branchName) throws GitletException {
        if (getActiveBranch().equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        if (!branchList.containsKey(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        branchList.remove(branchName);
    }

    public HashMap<String, String> getBranchList() {
        return branchList;
    }
    public void updateBranch(String branchName, String commitId) {
        branchList.put(branchName, commitId);
    }

    public void addCommit(Commit c) {
        // New commit is always on top of the list:
        commitList.addFirst(c);
    }

    public void setActiveBranch(String activeBranch) {
        this.activeBranch = activeBranch;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public LinkedList<Commit> getCommitList() {
        return commitList;
    }

    public void setCommitList(LinkedList<Commit> commitList) {
        this.commitList = commitList;
    }

    public String getCommitIdByIndex(int i) {
        return commitList.get(i).getCommitId();
    }

    public void setHeadToPreviousCommitId() {
        Commit previousCommit = commitList.get(1);
        HEAD = previousCommit.getCommitId();
        commitList.removeFirst();
    }

    public void displayLog() {
        for (Commit c : commitList) {
            System.out.println("===");
            System.out.println("commit " + c.getCommitId());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage() + "\n");
        }
    }
    public ArrayList<String> collectBranchCommits(String branchName) {
        ArrayList<String> commits = branchCommitList.get(branchName);
        return commits;
    }
    public void displayBranchLog(String branchName) {
        /*Branch currentBranch = Branch.deserialize(branchName);
        LinkedList<Commit> commits = currentBranch.getCommitList();
        for (int i = 0 ; i < commits.size()-1 ; i++) {
            //index was implemented to ensure that it reads the commits in reverse order
            int index = Math.abs(i-commits.size()+1);
            Commit curr = Commit.deserialize(commits.getLast().getCommitId());
            System.out.println("===");
            System.out.println("commit " + curr.getCommitId());
            System.out.println("Date: " + curr.getTimestamp());
            System.out.println(curr.getMessage() + "\n");
            commits.removeLast();
        } */
        Branch currentBranch = Branch.deserialize(branchName);
        Iterator<Commit> iter = currentBranch.getCommitList().iterator();
        while (iter.hasNext()) {
            Commit c =  iter.next();
            System.out.println("===");
            System.out.println("commit " + c.getCommitId());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage() + "\n");
        }
    }

    public String getCommitIdOfBranch(String branchName) {
        return branchList.get(branchName);
    }

    public void setCommitInBranch(String branchName, String commitId) {
        if (!branchCommitList.containsKey(branchName)) {
            ArrayList<String> commitList = new ArrayList<>();
            branchCommitList.put(branchName, commitList);
        }
        ArrayList<String> list = branchCommitList.get(branchName);
        list.add(commitId);
    }
    public void setHead(String commitId) {
        this.HEAD = commitId;
        branchList.put(activeBranch, this.HEAD);
    }

    public String getHead() {
        return HEAD;
    }

    public void serialize() {
        Utils.writeObject(new File(POINTER_OBJ), this);
    }

    public static final Pointer deserialize() {
        File ptrFile = new File(POINTER_OBJ);
        if (ptrFile.exists()) {
            return Utils.readObject(ptrFile, Pointer.class);
        }
        else {
            return null;
        }
    }

    @Override
    public void dump() {
        System.out.println("HEAD  = " + HEAD);
        System.out.println("Active branch  = " + activeBranch);

        System.out.println("List of branches: ");
        for (Map.Entry<String, String> map : branchList.entrySet()) {
            System.out.println(" -> Branch name = " + map.getKey() + " | Id = " + map.getValue());
        }
        System.out.println();
        System.out.println("List of commits: ");
        for (int i = 0; i < commitList.size(); i++)  {
            commitList.get(i).dump();
            System.out.println("___");
        }
    }
}
=======
package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Pointer always contains HEAD and MASTER
 */
public class Pointer implements Serializable, Dumpable {
    private String HEAD;
    private LinkedList<Commit> commitList;
    private String activeBranch;
    private HashMap<String, ArrayList<String>> branchCommitList;
    private HashMap<String, String> branchList;
    public static final String POINTER_OBJ = Repository.COMMITS_DIR + File.separator + "POINTER";
    public static final String DEFAULT_BRANCH = "main";

    public Pointer() {
        activeBranch = DEFAULT_BRANCH;

        // branchList contains a key value pair of branch name and latest commit id of the branch
        branchList   = new HashMap<>();

        commitList = new LinkedList<>();
        branchCommitList = new HashMap<>();
    }

    public void createBranch(String branchName) throws GitletException {
        if (branchList.get(branchName) != null) {
            throw new GitletException("A branch with that name already exists.");
        }
        // When a branch is newly created, it points to HEAD
        branchList.put(branchName, HEAD);

        Branch newBranch = new Branch(branchName, HEAD);
        newBranch.setCommitList((LinkedList<Commit>) commitList.clone());
        newBranch.serialize();
        serialize();
    }

    public String switchToBranch(String branchName) throws GitletException {
        if (branchList.get(branchName) == null) {
            throw new GitletException("A branch with that name does NOT exist.");
        }

        if (activeBranch == branchName) {
            throw new GitletException("No need to switch to the current branch.");
        }

        Branch newBranch = Branch.deserialize(branchName);

        activeBranch  = branchName;
        HEAD          = newBranch.getHead();
        commitList    = newBranch.getCommitList();
        branchList.put(branchName, HEAD);


        serialize();
        return HEAD;
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
    public void removeBranch(String branchName) throws GitletException {
        if (getActiveBranch().equals(branchName)) {
            throw new GitletException("Cannot remove the current branch.");
        }
        if (!branchList.containsKey(branchName)) {
            throw new GitletException("A branch with that name does not exist.");
        }
        branchList.remove(branchName);
    }

    public HashMap<String, String> getBranchList() {
        return branchList;
    }
    public void updateBranch(String branchName, String commitId) {
        branchList.put(branchName, commitId);
    }

    public void addCommit(Commit c) {
        // New commit is always on top of the list:
        commitList.addFirst(c);
    }

    public void setActiveBranch(String activeBranch) {
        this.activeBranch = activeBranch;
    }

    public String getActiveBranch() {
       return activeBranch;
    }

    public LinkedList<Commit> getCommitList() {
        return commitList;
    }

    public void setCommitList(LinkedList<Commit> commitList) {
        this.commitList = commitList;
    }

    public String getCommitIdByIndex(int i) {
        return commitList.get(i).getCommitId();
    }

    public void setHeadToPreviousCommitId() {
        Commit previousCommit = commitList.get(1);
        HEAD = previousCommit.getCommitId();
        commitList.removeFirst();
    }

    public void displayLog() {
        for (Commit c : commitList) {
            System.out.println("===");
            System.out.println("commit " + c.getCommitId());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage() + "\n");
        }
    }
    public ArrayList<String> collectBranchCommits(String branchName) {
        ArrayList<String> commits = branchCommitList.get(branchName);
        return commits;
    }
    public void displayBranchLog(String branchName) {
 /*       ArrayList<String> commits = collectBranchCommits(branchName);
        for (int i = 0 ; i < commits.size() ; i++) {
            //index was implemented to ensure that it reads the commits in reverse order
            int index = Math.abs(i-commits.size()+1);
            Commit curr = Commit.deserialize(commits.get(index));
            System.out.println("===");
            System.out.println("commit " + curr.getCommitId());
            System.out.println("Date: " + curr.getTimestamp());
            System.out.println(curr.getMessage() + "\n");
        }*/
        Branch currentBranch = Branch.deserialize(branchName);
        Iterator<Commit> iter = currentBranch.getCommitList().iterator();
        while (iter.hasNext()) {
            Commit c =  iter.next();
            System.out.println("===");
            System.out.println("commit " + c.getCommitId());
            System.out.println("Date: " + c.getTimestamp());
            System.out.println(c.getMessage() + "\n");
        }
    }

    public String getCommitIdOfBranch(String branchName) {
        return branchList.get(branchName);
    }

    public void setCommitInBranch(String branchName, String commitId) {
        if (!branchCommitList.containsKey(branchName)) {
            ArrayList<String> commitList = new ArrayList<>();
            branchCommitList.put(branchName, commitList);
        }
        ArrayList<String> list = branchCommitList.get(branchName);
        list.add(commitId);
    }
    public void setHead(String commitId) {
        this.HEAD = commitId;
        branchList.put(activeBranch, this.HEAD);
    }

    public String getHead() {
        return HEAD;
    }

    public void serialize() {
        Utils.writeObject(new File(POINTER_OBJ), this);
    }

    public static final Pointer deserialize() {
        File ptrFile = new File(POINTER_OBJ);
        if (ptrFile.exists()) {
            return Utils.readObject(ptrFile, Pointer.class);
        }
        else {
            return null;
        }
    }

    @Override
    public void dump() {
        System.out.println("HEAD  = " + HEAD);
        System.out.println("Active branch  = " + activeBranch);

        System.out.println("List of branches: ");
        for (Map.Entry<String, String> map : branchList.entrySet()) {
            System.out.println(" -> Branch name = " + map.getKey() + " | Id = " + map.getValue());
        }
        System.out.println();
        System.out.println("List of commits: ");
        for (int i = 0; i < commitList.size(); i++)  {
            commitList.get(i).dump();
            System.out.println("___");
        }
    }
}
>>>>>>> 26e5feaeb9f5b74e717121fd0d193187e893830e
