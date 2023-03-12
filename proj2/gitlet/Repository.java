package gitlet;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static gitlet.Utils.*;

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
    public static final File headSha1 = join(COMMITS_DIR, "head");

    /** Current commit (HEAD) */
    public static Commit head = new Commit();
    public static Set<String> removal = new HashSet<>();

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
        Commit initialCommit = head;

        String initialSha1 = initialCommit.getSha1();
        File initialCommitFile = join(COMMITS_DIR, initialSha1);
//        File headSha1 = join(COMMITS_DIR, "head");
        writeObject(initialCommitFile, initialCommit);
        writeContents(headSha1, initialSha1);
        System.out.println(initialCommit);
    }

    /** Check if there is .gitlet directory in current directory */
    public static boolean checkInit(){
        return GITLET_DIR.exists();
    }

    /** Check if the given file name is a plain file in current repo */
    public static boolean hasPlainFile(String fileName){
        return plainFilenamesIn(CWD).contains(fileName);
    }

    /** Read the head commit of the current repo (deserializing) */
    public static Commit getHead(){
        String currHeadId = readContentsAsString(join(COMMITS_DIR, "head"));
        System.out.println("Parent ID: " + currHeadId);

        Commit currHead = readObject(join(COMMITS_DIR, currHeadId), Commit.class);
        System.out.println(currHead);
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
        removal.remove(fileName);
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
     * TODO: Clear the staging area
     * TODO: serialize the new commit
     * TODO: set head pointer to new commit
     * @param msg
     */
    public static void commit(String msg){
        Commit newCommit = new Commit(msg);

        //Tracking files in staging area
        for(String fileName: plainFilenamesIn(STAGING_AREA)){
            String sha1 = createBlob(fileName);
            newCommit.track(fileName, sha1);
            File fileToDelete = join(STAGING_AREA, fileName);
            fileToDelete.delete();
        }

        //De-tracking files in removal
        for(String fileName: removal){
            newCommit.deTrack(fileName);
        }


        String newSha1 = newCommit.getSha1();
        File newCommitFile = join(COMMITS_DIR, newSha1);
        writeObject(newCommitFile, newCommit);
        head = newCommit;
        writeContents(headSha1, newSha1);

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
        if (plainFilenamesIn(STAGING_AREA).contains(fileName)) {
            File fileToDelete = join(STAGING_AREA, fileName);
            if (!fileToDelete.isDirectory()) {fileToDelete.delete();}
            res1 = true;
        }

        if(head.blobs.keySet().contains(fileName)){
            removal.add(fileName);
            if (hasPlainFile(fileName)){
                restrictedDelete(fileName);
            }
            res2 = true;
        }

        boolean res = res1 || res2;
        if(res == false){System.out.println("No reason to remove the file.");}
        return res1;
    }

}
