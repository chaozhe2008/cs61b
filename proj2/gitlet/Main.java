package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?
        if(args.length == 0){
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                if(!validateNumArgs(firstArg, args, 1)){return;}
                Repository.initCommand();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateNumArgs(firstArg, args, 2);
                if(!Repository.hasPlainFile(args[1])){
                    System.out.println("File does not exist.");
                    return;
                }
                Repository.add(args[1]);
                break;
            case "commit":
                validateNumArgs(firstArg, args, 2);
                if(args[1].isBlank()){
                    System.out.println("Please enter a commit message.");
                }
                else{
                    Repository.commit(args[1]);
                }
                break;
            case "rm":
                validateNumArgs(firstArg, args, 2);
                Repository.remove(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
        return;
    }

    public static boolean validateNumArgs(String cmd, String[] args, int n) {
        if(cmd.equals("init") && Repository.checkInit()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            return false;
        }else if(!cmd.equals("init") && !Repository.checkInit()){
            System.out.println("Not in an initialized Gitlet directory.");
            return false;
        }
        if (args.length != n) {
            System.out.println("Incorrect operands.");
            return false;
        }
        return true;
    }
}

