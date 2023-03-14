package gitlet;

import java.io.File;

import static gitlet.Utils.*;

public class Branch extends Repository{
    public static final File HEAD_FILE = join(BRANCH_DIR, "head");
    public static final File MASTER_FILE = join(BRANCH_DIR, "master");

    public static void initBranch(Commit initialCommit){
        BRANCH_DIR.mkdir();
        String initialSha1 = head.getSha1();
        writeContents(HEAD_FILE, "master");
        writeContents(MASTER_FILE, initialSha1);
    }

    /** Get the head file of current branch */
    public static File getCurrBranchFile(){
        String currBranch = readContentsAsString(HEAD_FILE);
        return join(BRANCH_DIR, currBranch);
    }

    /** Read the head commit of the current repo (deserializing) */
    public static Commit getHead(){
        File currBranchFile = getCurrBranchFile();
        String currHeadId = readContentsAsString(currBranchFile);
        Commit currHead = readObject(join(COMMITS_DIR, currHeadId), Commit.class);
        System.out.println("Current Head \n" + currHead);
        return currHead;
    }


    public static void createBranch(String branchName){
        if(plainFilenamesIn(BRANCH_DIR).contains(branchName)){
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        File branchFile = join(BRANCH_DIR, branchName);
        File currBranchFile = getCurrBranchFile();
        String currHeadId = readContentsAsString(currBranchFile);
        writeContents(branchFile, currHeadId);
    }
}
