package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.*;

import static gitlet.Branch.getCurrBranchFile;
import static gitlet.Utils.*;
import static gitlet.Repository.*;

/**
 * Represents a gitlet commit object.
 *
 * @author czy
 */
public class Commit implements Serializable {
    protected String message;
    protected String timestamp;
    protected String parentID;

    protected String secondParentID;
    protected transient Commit parent;
    protected Map<String, String> blobs;
    protected Set<String> sha1Set;

    public Commit() {
        this.message = "initial commit";
        this.timestamp = convertDate(new Date(0));
        this.parentID = null;
        this.secondParentID = null;
        this.parent = null;
        this.blobs = new TreeMap<>();
        this.sha1Set = new TreeSet<>();
    }

    public Commit(String msg) {
        this.message = msg;
        this.timestamp = convertDate(new Date());
        this.parent = Branch.getHead();
        this.parentID = Branch.getHead().getSha1();
        this.secondParentID = null;
        this.blobs = this.parent.blobs;
        this.sha1Set = this.parent.sha1Set;
    }

    public Commit(String msg, String secondParentID) {
        this.message = msg;
        this.timestamp = convertDate(new Date());
        this.parent = Branch.getHead();
        this.parentID = Branch.getHead().getSha1();
        this.secondParentID = secondParentID;
        this.blobs = this.parent.blobs;
        this.sha1Set = this.parent.sha1Set;
    }

    public void setCommit() {
        //Tracking files in staging area
        for (String fileName : plainFilenamesIn(STAGING_AREA)) {
            String sha1 = createBlob(fileName);
            track(fileName, sha1);
            File fileToDelete = join(STAGING_AREA, fileName);
            fileToDelete.delete();
        }


        //De-tracking files in removal
        for (String fileName : plainFilenamesIn(REMOVAL_DIR)) {
            deTrack(fileName);
            unRemove(fileName);
        }

        String newSha1 = getSha1();
        File newCommitFile = join(COMMITS_DIR, newSha1);
        writeObject(newCommitFile, this);
        writeContents(getCurrBranchFile(), newSha1);
    }


    private static String convertDate(Date date) {
        Formatter formatter = new Formatter(Locale.US);
        formatter.format("%1$ta %1$tb %1$td %1$tT %1$tY %1$tz", date);
        return formatter.toString().replace("+", "-");
    }

    @Override
    public String toString() {
        String mergeMessage = "";
        if (secondParentID != null) {
            mergeMessage = "Merge: " + parentID.substring(0, 7)
                    + " " + secondParentID.substring(0, 7) + "\n";
        }
        return "===\n" + "commit " + getSha1() + "\n" + mergeMessage
                + "Date: " + timestamp + "\n"
                + message + "\n";
    }

    public void print() {
        System.out.println("===\n" +
                "commit " + getSha1() + "\n" +
                "Date: " + timestamp + "\n"
                + message + "\n"
                + "ParentCommit: " + this.parentID + "\n"
                + "blobs: " + this.blobs
                + "\n");
    }

//    public Commit getParent() {
//        if (parentID == null) {
//            return null;
//        }
//        Commit parentCommit = readObject(join(COMMITS_DIR, this.parentID), Commit.class);
//        parentCommit.parent = null;
//        return parent;
//    }
//
//    public Commit getSecondParent() {
//        if (secondParentID == null) {
//            return null;
//        }
//        Commit secondParent = readObject(join(COMMITS_DIR, this.secondParentID), Commit.class);
//        secondParent.parent = null;
//        return secondParent;
//    }

    public String getSha1() {
        return sha1(serialize(this));
    }

    public String getBlobSha1(String fileName) {
        return blobs.get(fileName);
    }

    public void track(String fileName, String sha1) {
        sha1Set.add(sha1);
        blobs.put(fileName, sha1);
    }

    public void deTrack(String fileName) {
        if (blobs.containsKey(fileName)) {
            sha1Set.remove(getBlobSha1(fileName));
            String removedValue = blobs.remove(fileName);
        }
    }

}
