package gitlet;

// TODO: any imports you need here

import java.io.Serializable;
import java.util.*;
import static gitlet.Utils.*;
import static gitlet.Repository.*;

/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    protected String message;
    protected Date timestamp;
    protected String parentID;

    protected String secondParentID;
    protected transient Commit parent;
    protected Map<String, String> blobs;
    protected Set<String> sha1Set;
    /* TODO: fill in the rest of this class. */

    public Commit(){
        this.message = "initial commit";
        this.timestamp = new Date(0);
        this.parentID = null;
        this.secondParentID = null;
        this.parent = null;
        this.blobs = new TreeMap<>();
        this.sha1Set = new TreeSet<>();
    }

    public Commit(String msg){
        this.message = msg;
        this.timestamp = new Date();
        this.parent = Branch.getHead();
        this.parentID = Branch.getHead().getSha1();
        this.secondParentID = null;
        this.blobs = this.parent.blobs;
        this.sha1Set = this.parent.sha1Set;
    }

    @Override
    public String toString(){
        return  "===" + "\n" +
                "commit " + getSha1() + "\n" +
                "Date: " + timestamp.toString() + "\n"
                + "message: " + message + "\n"
                + "ParentCommit: " + this.parentID + "\n"
                + "blobs: " + this.blobs
                + "\n";

    }

    public Commit getParent(){
        if(parentID == null){return null;}
        Commit parent = readObject(join(COMMITS_DIR, this.parentID), Commit.class);
        return parent;
    }

    public Commit getSecondParent(){
        if(secondParentID == null){return null;}
        Commit parent = readObject(join(COMMITS_DIR, this.secondParentID), Commit.class);
        return parent;
    }

    public String getSha1(){
        return sha1(serialize(this));
    }

    public String getBlobSha1(String fileName){
        return blobs.get(fileName);
    }

    public void track(String fileName, String sha1){
        sha1Set.add(sha1);
        blobs.put(fileName, sha1);
    }

    public void deTrack(String fileName) {
        if (blobs.keySet().contains(fileName)) {
            sha1Set.remove(getBlobSha1(fileName));
            String removedValue = blobs.remove(fileName);
            System.out.println("detrack: " + removedValue);
        }
    }

}
