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

    public static Commit getHead(){
        File currBranchFile = getCurrBranchFile();
        String currHeadId = readContentsAsString(currBranchFile);
        Commit currHead = readObject(join(COMMITS_DIR, currHeadId), Commit.class);
        //System.out.println("Current Head: \n" + currHead);
        return currHead;
    }

    public static Commit getBranch(String branchName) {
        File targetBranchFile = join(BRANCH_DIR, branchName);
        String targetBranchId = readContentsAsString(targetBranchFile);
        Commit targetBranch = readObject(join(COMMITS_DIR, targetBranchId), Commit.class);
        //System.out.println("Target Branch: \n" + targetBranch);
        return targetBranch;
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

    public static void removeBranch(String branchName){
        if(!plainFilenamesIn(BRANCH_DIR).contains(branchName)){
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if(readContentsAsString(HEAD_FILE).equals(branchName)){
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        File branchFile = join(BRANCH_DIR, branchName);
        branchFile.delete();
    }

    //----------------------testing methods--------------------//
    public static void printHead(){
        System.out.println("Curr Head: \n" + getHead());
    }

    public static void printCurrBranch(){
        System.out.println("Curr Branch: \n" + readContentsAsString(HEAD_FILE));
    }

    public static void printBranch(String branchName){
        System.out.println(branchName + "\n" + getBranch(branchName));
    }
}
