package gitlet;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import static gitlet.Utils.*;
import static gitlet.Branch.*;

// TODO: any imports you need here

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /** The Staging Area directory. */

    public static final File STAGING_AREA = join(GITLET_DIR, "staging");

    /** The Commits directory. */

    public static final File COMMITS_DIR = join(GITLET_DIR, "commits");

    /** The Blobs directory. */
    public static final File BLOBS_DIR = join(GITLET_DIR, "blobs");
    public static final File BRANCH_DIR = join(GITLET_DIR, "branch");
//    public static final File HEADFILE = join(BRANCH_DIR, "head");
    public static final File REMOVAL_DIR = join(STAGING_AREA, "removal");

    /** Current commit (HEAD) */
    public static Commit head = new Commit();
    //public static Set<String> removal = new TreeSet<>();

    /* TODO: fill in the rest of this class. */
//    public Repository(){
//
//    }

    /**
     * TODO: Add /commits /staging /blobs folders in /.gitlet
     * TODO: save the initial commit into /commits
     */
    public static void initCommand(){
        GITLET_DIR.mkdir();
        STAGING_AREA.mkdir();
        COMMITS_DIR.mkdir();
        BLOBS_DIR.mkdir();
        REMOVAL_DIR.mkdir();
//
        String initialSha1 = head.getSha1();
        File initialCommitFile = join(COMMITS_DIR, initialSha1);
        writeObject(initialCommitFile, head);
        Branch.initBranch(head);
        System.out.println(head);
    }

    /** Check if there is .gitlet directory in current directory */
    public static boolean checkInit(){
        return GITLET_DIR.exists();
    }

    /** Check if the given file name is a plain file in current repo */
    public static boolean hasPlainFile(String fileName){
        return plainFilenamesIn(CWD).contains(fileName);
    }

    /**
     * TODO: read head from persistence(deserialize)
     * TODO: Test the compelete function
     * @param fileName
     */
    public static void add(String fileName){
        File file = new File(fileName);
        Commit currHead = getHead();
        if(!currHead.sha1Set.contains(sha1(readContents(file)))){
            File copyFile = join(STAGING_AREA, fileName);
            String fileContent = readContentsAsString(file);
            writeContents(copyFile, fileContent);
        }else{
            if (plainFilenamesIn(STAGING_AREA).contains(fileName)) {
                File fileToDelete = join(STAGING_AREA, fileName);
                if (!fileToDelete.isDirectory()) {
                    fileToDelete.delete();
                }
            }
        }
        unRemove(fileName);
    }

    public static String createBlob(String fileName){
        File file = join(STAGING_AREA, fileName);
        String sha1 = sha1(readContents(file));
        File blobDir = join(BLOBS_DIR, sha1);
        blobDir.mkdir();
        File snapShot = join(blobDir, fileName);
        String fileContent = readContentsAsString(file);
        writeContents(snapShot, fileContent);
        return sha1;
    }

    /**
     * DONE: commit the files in current staging area: create blobs, track the blobs
     * TODO: serialize the new commit
     * TODO: set head pointer to new commit
     * @param msg
     */
    public static void commit(String msg){
        if(plainFilenamesIn(STAGING_AREA).isEmpty() && plainFilenamesIn(REMOVAL_DIR).isEmpty()){
            System.out.println("No changes added to the commit.");
            return;
        }

        Commit newCommit = new Commit(msg);

        //Tracking files in staging area
        for(String fileName: plainFilenamesIn(STAGING_AREA)){
            String sha1 = createBlob(fileName);
            newCommit.track(fileName, sha1);
            File fileToDelete = join(STAGING_AREA, fileName);
            fileToDelete.delete();
        }

        //System.out.println("Blobs after adding: " + newCommit.blobs);

        //De-tracking files in removal
        for(String fileName: plainFilenamesIn(REMOVAL_DIR)){
            newCommit.deTrack(fileName);
            unRemove(fileName);
        }
        //System.out.println("Blobs after removal: " + newCommit.blobs);

        String newSha1 = newCommit.getSha1();
        File newCommitFile = join(COMMITS_DIR, newSha1);
        writeObject(newCommitFile, newCommit);
        writeContents(getCurrBranchFile(), newSha1);
        System.out.println(newCommit);
    }

    /**
     * TODO: If the file is tracking by head commmit, stage for removal
     * TODO: do not stage, and remove it from the staging area if it is already there
     * @param fileName
     */
    public static boolean remove(String fileName) {
        boolean res1 = false;
        boolean res2 = false;
        Commit currHead = getHead();
        if (plainFilenamesIn(STAGING_AREA).contains(fileName)) {
            File fileToDelete = join(STAGING_AREA, fileName);
            if (!fileToDelete.isDirectory()) {fileToDelete.delete();}
            res1 = true;
        }

        if(currHead.blobs.keySet().contains(fileName)){
            System.out.println("FILE TO BE DELETED IS BEING TRACKED");
            File copyFile = join(REMOVAL_DIR, fileName);
            writeContents(copyFile, "Stage for removal");
            if (hasPlainFile(fileName)){
                restrictedDelete(fileName);
            }
            res2 = true;
        }

        boolean res = res1 || res2;
        if(res == false){System.out.println("No reason to remove the file.");}
        return res;
    }

    public static void unRemove(String fileName) {
        if(plainFilenamesIn(REMOVAL_DIR).contains(fileName)){
            join(REMOVAL_DIR, fileName).delete();
        }
    }

    /**
     * TODO: 删除多余commit信息
     * TODO: For merge commits (those that have two parent commits), add a line just below the first line
     * TODO: for example: "Merge: 4975af1 2c1ead1"
     * TODO: First one is the branch you were on when you did the merge; the second is that of the merged-in branch
     */
    public static void log(){
        Commit currHead = getHead();
        while(true){
            System.out.println(currHead);
            String parentId = currHead.parentID;
            if(parentId == null){return;}
            currHead = readObject(join(COMMITS_DIR, parentId), Commit.class);
        }
    }

    /**
     * Display information about all commits ever made. (No order)
     */
    public static void logGlobal(){
        Commit commit;
        for(String id: plainFilenamesIn(COMMITS_DIR)){
            if(id.equals("head")){continue;}
            commit = readObject(join(COMMITS_DIR, id), Commit.class);
            System.out.println(commit);
        }
    }



    /**Prints out the ids of all commits that have the given commit message */
    public static void find(String targetMessage){
        Commit commit;
        boolean found = false;
        for(String id: plainFilenamesIn(COMMITS_DIR)){
            if(id.equals("head")){continue;}
            commit = readObject(join(COMMITS_DIR, id), Commit.class);
            if(commit.message.equals(targetMessage)){
                System.out.println(id);
                found = true;
            }
        }
        if(!found){System.out.println("Found no commit with that message.");}
    }


    /**
     * TODO: Not Implemented
     */
    public static void status(){

    }


    /**
     * TODO: implement branch-related
     */
    public static void checkout(String... args){
        //case1: check out file name
        if(args.length == 3) {
            if (!plainFilenamesIn(CWD).contains(args[2])) {
                System.out.println("File does not exist in that commit.");
                System.exit(0);
            }
            checkoutFile(args[2]);
        }

        //case2: check out a branch
        if(args.length == 2){
            if (!plainFilenamesIn(CWD).contains(args[1])) { //branch name contains
                System.out.println("No such branch exists.");
                System.exit(0);
            }
            checkoutBranch(args[1]);
        }


        //case3:
        if(args.length == 4){
            if (!plainFilenamesIn(COMMITS_DIR).contains(args[1])) { //branch name contains
                System.out.println("No commit with that id exists.");
                System.exit(0);
            }
            checkoutCommitFile(args[1], args[3]);
        }

    }

    private static void checkoutFile(String fileName) {

    }

    private static void checkoutBranch(String branchName) {

    }

    /**
     * TODO: handle the nonexisting file error.
     * @param commitId
     * @param fileName
     */
    private static void checkoutCommitFile(String commitId, String fileName) {

    }





}
