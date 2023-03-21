package gitlet;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Branch.*;
import static gitlet.Repository.*;

public class Remote {
    public static final File REMOTE_DIR = join(GITLET_DIR, "remote");

    public static void addRemote(String name, String directory) {
        if (plainFilenamesIn(REMOTE_DIR).contains(name)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        File remote = join(REMOTE_DIR, name);
        writeContents(remote, directory);
    }


    public static void rmRemote(String remoteName) {
        if (!plainFilenamesIn(REMOTE_DIR).contains(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        join(REMOTE_DIR, remoteName).delete();
    }

    //-------------------------Push-------------------------//
    public static void checkRemoteExist(String remoteName) {
        String remoteDir = readContentsAsString(join(REMOTE_DIR, remoteName));
        File targetRepo = new File(remoteDir);
        if (!targetRepo.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
    }

    public static boolean isParent(String sonID, String parentID) {
        if (!plainFilenamesIn(COMMITS_DIR).contains(parentID)) {
            return false;
        }
        Commit targetCommit = loadCommit(parentID);
        Commit currCommit = loadCommit(sonID);
        String splitNodeID = getSplitPoint(targetCommit, currCommit).getSha1();
        if (!parentID.equals(splitNodeID)) {
            return false;
        }
        return true;
    }

    public static void push(String remoteName, String branchName) {
        checkRemoteExist(remoteName);
        String remoteDir = readContentsAsString(join(REMOTE_DIR, remoteName));
        File targetCommitFile = join(remoteDir, "branch", branchName);

        // If remote Gitlet system exists but does not have the input branch
        // Simply add the branch to the remote Gitlet
        if (!targetCommitFile.exists()) {
            String remoteHeadBranch = readContentsAsString(join(remoteDir, "branch", "head"));
            String remoteHeadID = readContentsAsString(join(remoteDir, "branch", remoteHeadBranch));
            writeContents(targetCommitFile, remoteHeadID);
            return;
        }

        String targetBranchCommitId = readContentsAsString(targetCommitFile);
        if (!isParent(getHead().getSha1(), targetBranchCommitId)) {
            System.out.println("Please pull down remote changes before pushing.");
            System.exit(0);
        }

        Commit curr = getHead();
        Set<String> differentCommitsID = new TreeSet<>();
        while (!curr.getSha1().equals(targetBranchCommitId)) {
            if (curr.secondParentID != null && isParent(curr.secondParentID, targetBranchCommitId)) {
                differentCommitsID.add(curr.secondParentID);
                curr = loadCommit(curr.secondParentID);
            } else {
                differentCommitsID.add(curr.parentID);
                curr = loadCommit(curr.parentID);
            }
        }

        // add different commits into remote branch
        File remoteBlobDir = join(remoteDir, "blobs");
        File remoteCommitDir = join(remoteDir, "commits");
        for (String id : differentCommitsID) {
            Commit newCommit = loadCommit(id);
            //copy commit
            writeObject(join(remoteCommitDir, id), newCommit);
            //copy blobs
            for (Map.Entry<String, String> entry : newCommit.blobs.entrySet()) {
                String fileID = entry.getValue();
                String fileName = entry.getKey();
                File targetBlob = join(remoteBlobDir, fileID);
                targetBlob.mkdir();
                File targetFile = join(targetBlob, fileName);
                String currFileContents = readContentsAsString(join(BLOBS_DIR, fileID, fileName));
                writeContents(targetFile, currFileContents);
            }
        }
    }

    //----------------------------fetch---------------------------//
    public static Commit loadRemoteCommit(String remoteName, String id) {
        String remoteDir = readContentsAsString(join(REMOTE_DIR, remoteName));
        return readObject(join(remoteDir, "commits", id), Commit.class);
    }


    /**
     * copies all commits and blobs from the given branch in the remote repository
     * (that are not already in the current repository)
     * into a branch named [remote name]/[remote branch name]
     * changing [remote name]/[remote branch name] to point to the head commit
     * (thus copying the contents of the branch from the remote repository to the current one).
     * This branch is created in the local repository if it did not previously exist.
     *
     * @param remoteName
     * @param branchName
     */
    public static void fetch(String remoteName, String branchName) {
        checkRemoteExist(remoteName);
        String remoteDir = readContentsAsString(join(REMOTE_DIR, remoteName));
        File targetCommitFile = join(remoteDir, "branch", branchName);

//        System.out.println("target branch commit: " + readContentsAsString(targetCommitFile));

        if (!targetCommitFile.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }

        Set<String> remoteCommitsPath = new TreeSet<>();
        ArrayDeque<String> queue = new ArrayDeque<>();
        queue.add(readContentsAsString(targetCommitFile));
        String currID;
        Commit currCommit;
        while (!queue.isEmpty()) {
            currID = queue.remove();
            remoteCommitsPath.add(currID);
            currCommit = loadRemoteCommit(remoteName, currID);
            if (currCommit.parentID != null) {
                queue.add(currCommit.parentID);
            }
            if (currCommit.secondParentID != null) {
                queue.add(currCommit.secondParentID);
            }
        }

        // copy different commits into local repo
        File remoteBlobDir = join(remoteDir, "blobs");
        for (String id : remoteCommitsPath) {
            Commit newCommit = loadRemoteCommit(remoteName, id);
            //copy commit
            writeObject(join(COMMITS_DIR, id), newCommit);
            //copy blobs
            for (Map.Entry<String, String> entry : newCommit.blobs.entrySet()) {
                String fileID = entry.getValue();
                String fileName = entry.getKey();
                File localNewBlob = join(BLOBS_DIR, fileID);
                localNewBlob.mkdir();
                File localNewFile = join(localNewBlob, fileName);
                String remoteFileContents = readContentsAsString(join(remoteBlobDir, fileID, fileName));
                writeContents(localNewFile, remoteFileContents);
            }
        }

        // create local branch
        String newBranchName = remoteName + "_" + branchName;
        if (!plainFilenamesIn(BRANCH_DIR).contains(newBranchName)) {
            createBranch(newBranchName);
        }
        File newBranchFile = join(BRANCH_DIR, newBranchName);
        writeContents(newBranchFile, readContentsAsString(targetCommitFile));

    }

    public static void pull(String remoteName, String branchName) {
        fetch(remoteName, branchName);
        merge(remoteName + "/" + branchName);
    }
}
