package gitlet;

import java.io.File;
import java.util.*;

import static gitlet.Utils.*;
import static gitlet.Branch.*;


/**
 * Represents a gitlet repository.
 *
 * @author czy
 */
public class Repository {
    /**
     * The current working directory.
     */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /**
     * The .gitlet directory.
     */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /**
     * The Staging Area directory.
     */

    public static final File STAGING_AREA = join(GITLET_DIR, "staging");

    /**
     * The Commits directory.
     */

    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /**
     * The Blobs directory.
     */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branch");
    //    public static final File HEADFILE = join(BRANCH_DIR, "head");
    public static final File REMOVAL_DIR = join(STAGING_AREA, "removal");

    /**
     * Current commit (HEAD)
     */
    protected static Commit head = new Commit();

    public static void initCommand() {
        GITLET_DIR.mkdir();
        STAGING_AREA.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        REMOVAL_DIR.mkdir();

        String initialSha1 = head.getSha1();
        File initialCommitFile = join(COMMITS_DIR, initialSha1);
        writeObject(initialCommitFile, head);
        Branch.initBranch(head);
    }

    /**
     * Check if there is .gitlet directory in current directory
     */
    public static boolean checkInit() {
        return GITLET_DIR.exists();
    }

    /**
     * Check if the given file name is a plain file in current repo
     */
    public static boolean hasPlainFile(String fileName) {
        return plainFilenamesIn(CWD).contains(fileName);
    }

    public static void add(String fileName) {
        if (!hasPlainFile(fileName)) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        File file = join(CWD, fileName);
        String fileSha1 = getFileSha1(CWD, fileName);
        Commit currHead = getHead();

        // fileName is tracked by current head and is same version as tracked one
        // do not add, remove from staging area
        if (currHead.blobs.containsKey(fileName)
                && currHead.getBlobSha1(fileName).equals(fileSha1)) {
            if (plainFilenamesIn(STAGING_AREA).contains(fileName)) {
                File fileToDelete = join(STAGING_AREA, fileName);
                if (!fileToDelete.isDirectory()) {
                    fileToDelete.delete();
                }
            }
            unRemove(fileName);
            return;
        }
        // perform add
        File copyFile = join(STAGING_AREA, fileName);
        String fileContent = readContentsAsString(file);
        writeContents(copyFile, fileContent);
        unRemove(fileName);
    }

    public static String createBlob(String fileName) {
        File file = join(STAGING_AREA, fileName);
        String sha1 = sha1(readContents(file));
        File blobDir = join(BLOBS_DIR, sha1);
        blobDir.mkdir();
        File snapShot = join(blobDir, fileName);
        String fileContent = readContentsAsString(file);
        writeContents(snapShot, fileContent);
        return sha1;
    }

    //---------------------Commit---------------------------//
    public static void checkUpdate() {
        if (plainFilenamesIn(STAGING_AREA).isEmpty() && plainFilenamesIn(REMOVAL_DIR).isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
    }

    public static void commit(String msg) {
        checkUpdate();
        Commit newCommit = new Commit(msg);
        newCommit.setCommit();
    }


    public static void commit(String msg, String secondParentId) {
        checkUpdate();
        Commit newCommit = new Commit(msg, secondParentId);
        newCommit.setCommit();
    }

    //------------------remove--------------------------//

    public static boolean remove(String fileName) {
        boolean res1 = false;
        boolean res2 = false;
        Commit currHead = getHead();
        if (plainFilenamesIn(STAGING_AREA).contains(fileName)) {
            File fileToDelete = join(STAGING_AREA, fileName);
            if (!fileToDelete.isDirectory()) {
                fileToDelete.delete();
            }
            res1 = true;
        }

        if (currHead.blobs.containsKey(fileName)) {
            File copyFile = join(REMOVAL_DIR, fileName);
            writeContents(copyFile, "Stage for removal");
            if (hasPlainFile(fileName)) {
                restrictedDelete(fileName);
            }
            res2 = true;
        }

        boolean res = res1 || res2;
        if (!res) {
            System.out.println("No reason to remove the file.");
        }
        return res;
    }

    public static void unRemove(String fileName) {
        if (plainFilenamesIn(REMOVAL_DIR).contains(fileName)) {
            join(REMOVAL_DIR, fileName).delete();
        }
    }

    public static void log() {
        Commit currHead = getHead();
        while (currHead != null) {
            System.out.println(currHead);
            String parentId = currHead.parentID;
            if (parentId == null) {
                return;
            }
            currHead = readObject(join(COMMITS_DIR, parentId), Commit.class);
        }
    }

    /**
     * Display information about all commits ever made. (No order)
     */
    public static void logGlobal() {
        Commit commit;
        for (String id : plainFilenamesIn(COMMITS_DIR)) {
            if (id.equals("head")) {
                continue;
            }
            commit = readObject(join(COMMITS_DIR, id), Commit.class);
            System.out.println(commit);
        }
    }


    /**
     * Prints out the ids of all commits that have the given commit message
     */
    public static void find(String targetMessage) {
        Commit commit;
        boolean found = false;
        for (String id : plainFilenamesIn(COMMITS_DIR)) {
            if (id.equals("head")) {
                continue;
            }
            commit = readObject(join(COMMITS_DIR, id), Commit.class);
            if (commit.message.equals(targetMessage)) {
                System.out.println(id);
                found = true;
            }
        }
        if (!found) {
            System.out.println("Found no commit with that message.");
        }
    }


    //---------------------------Checkout-------------------------------//
    public static void checkout(String... args) {
        //case1: check out file name
        if (args.length == 3) {
            checkoutFile(args[2]);
        }

        //case2: check out a branch
        if (args.length == 2) {
            checkoutBranch(args[1]);
        }

        //case3: check out a file in given branch
        if (args.length == 4) {
            String targetCommitId = searchPrefix(args[1]);
            if (targetCommitId == null) { //branch name contains
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            Commit targetCommit = readObject(join(COMMITS_DIR, targetCommitId), Commit.class);
            checkoutCommitFile(targetCommit, args[3]);
        }

    }

    public static String searchPrefix(String prefix) {
        for (String fullId : plainFilenamesIn(COMMITS_DIR)) {
            if (fullId.startsWith(prefix)) {
                if (!fullId.equals(prefix) && fullId.length() > 40) {
                    continue;
                }
                return fullId;
            }
        }
        return null;
    }


    private static void checkoutFile(String fileName) {
        checkoutCommitFile(getHead(), fileName);
    }

    private static void checkoutBranch(String branchName) {
        //Three failure cases
        if (!plainFilenamesIn(BRANCH_DIR).contains(branchName)) { //branch name contains
            System.out.println("No such branch exists.");
            System.exit(0);
        }

        String currBranch = readContentsAsString(HEAD_FILE);
        if (currBranch.equals(branchName)) {
            System.out.println("No need to checkout the current branch.");
            System.exit(0);
        }

        Commit targetBranch = getBranch(branchName);
        checkoutCommit(targetBranch);
        writeContents(HEAD_FILE, branchName);
    }

    private static void checkoutCommit(Commit targetCommit) {
        Commit currHead = getHead();
        for (Map.Entry<String, String> entry : targetCommit.blobs.entrySet()) {
            String newFileName = entry.getKey();
            String newFileId = entry.getValue();
            if (hasPlainFile(newFileName)) {
                File workingFile = join(CWD, newFileName);
                String workingSha1 = sha1(readContents(workingFile));
                boolean tracked = currHead.blobs.containsKey(newFileName)
                        && currHead.getBlobSha1(newFileName).equals(workingSha1);
                if (!tracked && !newFileId.equals(workingSha1)) {
                    System.out.println("There is an untracked file in the way; "
                            + "delete it, or add and commit it first.");
                    System.exit(0);
                }
            }
        }
        // No failure case detected

        // Delete all the files in CWD and current head but not in target branch
        Set<String> currTrackingNames = currHead.blobs.keySet();
        Set<String> targetTrackingNames = targetCommit.blobs.keySet();
        for (String fileName : currTrackingNames) {
            if (plainFilenamesIn(CWD).contains(fileName)
                    && !targetTrackingNames.contains(fileName)) {
                restrictedDelete(join(CWD, fileName));
            }
        }

        //checkout target commit
        for (String fileName : targetTrackingNames) {
            checkoutCommitFile(targetCommit, fileName);
        }

        //Clear Staging area
        clearStagingArea();
    }

    private static void checkoutCommitFile(Commit targetCommit, String fileName) {
        if (!targetCommit.blobs.containsKey(fileName)) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }

        String blobSha1 = targetCommit.getBlobSha1(fileName);
        File fileToCheckout = join(BLOBS_DIR, blobSha1, fileName);
        String fileContent = readContentsAsString(fileToCheckout);

        File fileToOverwrite = join(CWD, fileName);
        writeContents(fileToOverwrite, fileContent);
    }


    /**
     * Helper function to clear the staging area
     */
    public static void clearStagingArea() {
        for (String fileName : plainFilenamesIn(STAGING_AREA)) {
            join(STAGING_AREA, fileName).delete();
        }
        for (String fileName : plainFilenamesIn(REMOVAL_DIR)) {
            join(REMOVAL_DIR, fileName).delete();
        }
    }

    //-------------------------------reset----------------------------------//
    public static void reset(String commitId) {
        String targetCommitId = searchPrefix(commitId);
        if (targetCommitId == null) { //branch name contains
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }

        Commit targetCommit = readObject(join(COMMITS_DIR, targetCommitId), Commit.class);
        checkoutCommit(targetCommit);

        // Moves the current branch’s head to that commit node.

        File currentBranchFile = getCurrBranchFile();
        writeContents(currentBranchFile, commitId);
    }

    //-------------------------------status---------------------------------//

    /**
     * helper function: get sha1 of fileName in path
     */
    public static String getFileSha1(File path, String fileName) {
        File targetFile = join(path, fileName);
        return sha1(readContentsAsString(targetFile));
    }

    /**
     * helper function: get sha1 in staging area
     */
    public static TreeSet<String> getStagingSha1() {
        TreeSet<String> stagingSha1 = new TreeSet<>();
        for (String fileName : plainFilenamesIn(STAGING_AREA)) {
            String stagingFileId = getFileSha1(STAGING_AREA, fileName);
            stagingSha1.add(stagingFileId);
        }
        return stagingSha1;
    }

    public static void printStatus() {
        // Branches
        Branch.printBranches();
        // Staged Files
        System.out.println("=== Staged Files ===");
        TreeSet<String> stagingNames = new TreeSet<>(plainFilenamesIn(STAGING_AREA));
        stagingNames.forEach(System.out::println);
        System.out.println();

        // Staged for Removal
        System.out.println("=== Removed Files ===");
        TreeSet<String> removedNames = new TreeSet<>(plainFilenamesIn(REMOVAL_DIR));
        removedNames.forEach(System.out::println);
        System.out.println();

        // Modified But Not Staged
        System.out.println("=== Modifications Not Staged For Commit ===");

        TreeSet<String> stagingSha1 = getStagingSha1();
        TreeSet<String> modifiedButNotStagedNames = new TreeSet<>();
        Commit currHead = getHead();
        // case1: Tracked in the current commit
        // changed in the working directory, but not staged
        for (String fileName : currHead.blobs.keySet()) {
            if (!plainFilenamesIn(CWD).contains(fileName)) {
                continue;
            }
            String currVersionSha1 = getFileSha1(CWD, fileName);
            if (!currVersionSha1.equals(currHead.getBlobSha1(fileName))
                    && !stagingSha1.contains(currVersionSha1)) {
                modifiedButNotStagedNames.add(fileName);
            }
        }

        // case2&3: Staged for addition, but with different contents in CWD or deleted
        for (String fileName : plainFilenamesIn(STAGING_AREA)) {
            if (!plainFilenamesIn(CWD).contains(fileName)) {
                modifiedButNotStagedNames.add(fileName);
                continue;
            }
            String currVersionSha1 = getFileSha1(CWD, fileName);
            String stagedVersionSha1 = getFileSha1(STAGING_AREA, fileName);
            if (!currVersionSha1.equals(stagedVersionSha1)) {
                modifiedButNotStagedNames.add(fileName);
            }
        }

        // case4: Not staged for removal, tracked in the current commit and deleted from CWD.
        for (String fileName : currHead.blobs.keySet()) {
            if (plainFilenamesIn(REMOVAL_DIR).contains(fileName)) {
                continue;
            }
            if (plainFilenamesIn(CWD).contains(fileName)) {
                continue;
            }
            modifiedButNotStagedNames.add(fileName);
        }

        for (String fileName : modifiedButNotStagedNames) {
            if (hasPlainFile(fileName)) {
                System.out.println(fileName + " (modified)");
            } else {
                System.out.println(fileName + " (deleted)");
            }
        }
        System.out.println();

        // Untracked Files
        TreeSet<String> untrackedNames = new TreeSet<>();
        System.out.println("=== Untracked Files ===");
        for (String fileName : plainFilenamesIn(CWD)) {
            if (!plainFilenamesIn(STAGING_AREA).contains(fileName)
                    && !currHead.blobs.containsKey(fileName)) {
                untrackedNames.add(fileName);
            }
        }
        untrackedNames.forEach(System.out::println);
        System.out.println();
    }

    //-------------------------------Merge-------------------------------//
    public static Commit loadCommit(String id) {
        return readObject(join(COMMITS_DIR, id), Commit.class);
    }

    public static Commit getSplitPoint(Commit commit1, Commit commit2) {
        TreeSet<String> path1 = new TreeSet<>();
        ArrayDeque<String> queue1 = new ArrayDeque<>();
        queue1.add(commit1.getSha1());
        TreeSet<String> path2 = new TreeSet<>();
        ArrayDeque<String> queue2 = new ArrayDeque<>();
        queue2.add(commit2.getSha1());
        String id1, id2;
        Commit pathCommit1, pathCommit2;

        while (!queue1.isEmpty() || !queue2.isEmpty()) {
            for (int i = 0; i < queue1.size(); i++) {
                id1 = queue1.remove();
                pathCommit1 = loadCommit(id1);
                if (path2.contains(id1)) {
                    return pathCommit1;
                }
                path1.add(id1);

                if (pathCommit1.parentID != null) {
                    queue1.add(pathCommit1.parentID);
                }
                if (pathCommit1.secondParentID != null) {
                    queue1.add(pathCommit1.secondParentID);
                }
            }

            for (int i = 0; i < queue2.size(); i++) {
                id2 = queue2.remove();
                pathCommit2 = loadCommit(id2);
                if (path1.contains(id2)) {
                    return pathCommit2;
                }
                path2.add(id2);

                if (pathCommit2.parentID != null) {
                    queue2.add(pathCommit2.parentID);
                }
                if (pathCommit2.secondParentID != null) {
                    queue2.add(pathCommit2.secondParentID);
                }
            }
        }
        return null;
    }


    public static void merge(String branchName) {
        if (!plainFilenamesIn(STAGING_AREA).isEmpty()
                || !plainFilenamesIn(REMOVAL_DIR).isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }

        if (!plainFilenamesIn(BRANCH_DIR).contains(branchName)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }

        if (readContentsAsString(HEAD_FILE).equals(branchName)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }

        Commit other = getBranch(branchName);
        Commit head = getHead();
        head.parent = null;

        Commit splitPoint = getSplitPoint(head, other);

        if (splitPoint.getSha1().equals(other.getSha1())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        }

        if (splitPoint.getSha1().equals(head.getSha1())) {
            checkoutCommit(other);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        // check completed, perform merging
        // get all file names that involved
        TreeSet<String> allFileNames = new TreeSet<>();
        allFileNames.addAll(head.blobs.keySet());
        allFileNames.addAll(other.blobs.keySet());
        allFileNames.addAll(splitPoint.blobs.keySet());

        // Iterate over all files involved
        for (String fileName : allFileNames) {
            // Case0: head version and target version is same, do nothing
            String headVersion = head.getBlobSha1(fileName);
            String otherVersion = other.getBlobSha1(fileName);
            if (headVersion == null && otherVersion == null) {
                continue;
            }
            if (headVersion != null && headVersion.equals(otherVersion)) {
                continue;
            }

            // Case1: not present in split node
            if (!splitPoint.blobs.containsKey(fileName)) {
                if (!head.blobs.containsKey(fileName)) {
                    // not present in head, stage for addition
                    if (plainFilenamesIn(CWD).contains(fileName)) {
                        File workingFile = join(CWD, fileName);
                        String workingSha1 = sha1(readContents(workingFile));
                        if (!other.getBlobSha1(fileName).equals(workingSha1)) {
                            System.out.println("There is an untracked file in the way; " +
                                    "delete it, or add and commit it first.");
                            System.exit(0);
                        }
                    }
                    checkoutCommitFile(other, fileName);
                    add(fileName);
                } else if (other.blobs.containsKey(fileName)) {
                    // present both in head and other
                    checkConflict(fileName, head, other);
                }
                continue;
            }
            // Other cases: need to compare modification, use helper function
            checkConflict(fileName, splitPoint, head, other);
        }
        String message = "Merged " + branchName + " into " + readContentsAsString(HEAD_FILE) + ".";
        commit(message, other.getSha1());
    }

    /**
     * Merge helper function (including parent)
     * Given head and other and parent commit, compare their version and deal with conflict
     */
    public static void checkConflict(String fileName, Commit parentCommit,
                                     Commit headCommit, Commit otherCommit) {
        // parent == other, head dominates
        if (parentCommit.getBlobSha1(fileName).equals(otherCommit.getBlobSha1(fileName))) {
            return;
        }
        // parent == head, other dominates
        if (parentCommit.getBlobSha1(fileName).equals(headCommit.getBlobSha1(fileName))) {
            if (!otherCommit.blobs.containsKey(fileName)) {
                remove(fileName);
            } else {
                checkoutCommitFile(otherCommit, fileName);
                add(fileName);
            }
            return;
        }
        //modified in different ways, check conflict
        checkConflict(fileName, headCommit, otherCommit);
    }

    /**
     * Merge helper function (not including parent)
     * Given head and other commit, compare their version and deal with conflict
     */
    public static void checkConflict(String fileName, Commit headCommit, Commit otherCommit) {
        File conflictFile = join(CWD, fileName);
        ArrayList<String> outputStrings = new ArrayList<>();
        outputStrings.add("<<<<<<< HEAD\n");

        if (headCommit.blobs.containsKey(fileName)) {
            outputStrings.add(readContentsAsString(join(BLOBS_DIR,
                    headCommit.getBlobSha1(fileName), fileName)));
        }

        outputStrings.add("=======\n");

        if (otherCommit.blobs.containsKey(fileName)) {
            outputStrings.add(readContentsAsString(join(BLOBS_DIR,
                    otherCommit.getBlobSha1(fileName), fileName)));
        }

        outputStrings.add(">>>>>>>\n");

        StringBuilder sb = new StringBuilder();
        for (String s : outputStrings) {
            sb.append(s);
        }
        String output = sb.toString();
        writeContents(conflictFile, output);
        System.out.println("Encountered a merge conflict.");
        add(fileName);
    }

}
