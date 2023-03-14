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
                validateNumArgs(firstArg, args, 1);
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
                Repository.commit(args[1]);
                break;
            case "rm":
                validateNumArgs(firstArg, args, 2);
                Repository.remove(args[1]);
                break;
            case "log":
                validateNumArgs(firstArg, args, 1);
                Repository.log();
                break;
            case "global-log":
                validateNumArgs(firstArg, args, 1);
                Repository.logGlobal();
                break;
            case "find":
                validateNumArgs(firstArg, args, 2);
                Repository.find(args[1]);
                break;
            case "checkout":
                validateNumArgs(firstArg, args, 4);
                Repository.checkout(args);
                break;
            case "branch":
                validateNumArgs(firstArg, args, 2);
                Branch.createBranch(args[1]);
                break;
            default:
                System.out.println("No command with that name exists.");
                break;
        }
        return;
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if(cmd.equals("init") && Repository.checkInit()){
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }else if(!cmd.equals("init") && !Repository.checkInit()){
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        switch (cmd){
            case "commit":{
                if(args.length == 1 || (args.length == 2 && args[1].isBlank())) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                if (args.length > n) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            }

            case "checkout": {
                if (args.length > n || args.length <= 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            }

            default: {
                if (args.length != n) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            }
        }
    }
}

