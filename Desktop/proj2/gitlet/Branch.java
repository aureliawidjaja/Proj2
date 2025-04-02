package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class Branch implements Serializable {
    private String name;
    private String HEAD;
    private HashSet<String> fileCatalog;
    private LinkedList<Commit> commitList;

    public Branch() {
        initContainers();
    }
    public Branch(String name, String headId) {
        this.name = name;
        this.HEAD = headId;

        initContainers();
    }

    private void initContainers() {
        fileCatalog = new HashSet<>();
        commitList = new LinkedList<>();
    }

    public String getName() {
        return name;
    }

    public String getHead() {
        return HEAD;
    }

    public void setName(String name) {
<<<<<<< HEAD
        this.name   = name;
=======
        this.name = name;
>>>>>>> 26e5feaeb9f5b74e717121fd0d193187e893830e
    }

    public void setHead(String HEAD) {
        this.HEAD = HEAD;
    }

    public void addCommit(Commit c) {
        commitList.addFirst(c);
    }

    public void addFileNameToFileCatalog(String fileName) {
        fileCatalog.add(fileName);
    }

    public void removeFileNameFromFileCatalog (String fileName) {
        fileCatalog.remove(fileName);
    }

    public boolean isFileTracked(String fileName) {
        return fileCatalog.contains(fileName);
    }
    public LinkedList<Commit> getCommitList() {
        return commitList;
    }

    public void setCommitList(LinkedList<Commit> commitList) {
        this.commitList = commitList;

        Iterator<Commit> commitIterator = commitList.iterator();

        // Reinitialize and populate fileCatalog with the files that
        // all the commits in commitList have tracked:
        fileCatalog = new HashSet<>();
        while(commitIterator.hasNext()) {
            Commit c = commitIterator.next();
            for (Map.Entry<String, String> map : c.getBlobs().entrySet()) {
                fileCatalog.add(map.getKey());
            }
        }
    }
    public void serialize() {
        Utils.writeObject(new File(Repository.BRANCHES_DIR + File.separator + name), this);
    }

    public static final Branch deserialize(String name) {
        File branchFile = new File(Repository.BRANCHES_DIR + File.separator + name);

        if (branchFile.exists()) {
            return Utils.readObject(branchFile, Branch.class);
        }
        else {
            return null;
        }
    }
}
