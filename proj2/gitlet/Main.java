package gitlet;

/**
 * Driver class for Gitlet, a subset of the Git version-control system.
 *
 * @author czy
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND1> <OPERAND2> ...
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            return;
        }
        String firstArg = args[0];
        switch (firstArg) {
            case "init" -> {
                validateNumArgs(firstArg, args, 1);
                Repository.initCommand();
            }
            case "add" -> {
                validateNumArgs(firstArg, args, 2);
                Repository.add(args[1]);
            }
            case "commit" -> {
                validateNumArgs(firstArg, args, 2);
                Repository.commit(args[1]);
            }
            case "rm" -> {
                validateNumArgs(firstArg, args, 2);
                Repository.remove(args[1]);
            }
            case "log" -> {
                validateNumArgs(firstArg, args, 1);
                Repository.log();
            }
            case "global-log" -> {
                validateNumArgs(firstArg, args, 1);
                Repository.logGlobal();
            }
            case "find" -> {
                validateNumArgs(firstArg, args, 2);
                Repository.find(args[1]);
            }
            case "checkout" -> {
                validateNumArgs(firstArg, args, 4);
                Repository.checkout(args);
            }
            case "branch" -> {
                validateNumArgs(firstArg, args, 2);
                Branch.createBranch(args[1]);
            }
            case "rm-branch" -> {
                validateNumArgs(firstArg, args, 2);
                Branch.removeBranch(args[1]);
            }
            case "status" -> {
                validateNumArgs(firstArg, args, 1);
                Repository.printStatus();
            }
            case "reset" -> {
                validateNumArgs(firstArg, args, 2);
                Repository.reset(args[1]);
            }
            case "merge" -> {
                validateNumArgs(firstArg, args, 2);
                Repository.merge(args[1]);
            }
            case "add-remote" -> {
                validateNumArgs(firstArg, args, 3);
                Remote.addRemote(args[1], args[2]);
            }

            case "rm-remote" -> {
                validateNumArgs(firstArg, args, 2);
                Remote.rmRemote(args[1]);
            }
            case "push" -> {
                validateNumArgs(firstArg, args, 3);
                Remote.push(args[1], args[2]);
            }
            case "fetch" -> {
                validateNumArgs(firstArg, args, 3);
                Remote.fetch(args[1], args[2]);
            }
            case "pull" -> {
                validateNumArgs(firstArg, args, 3);
                Remote.pull(args[1], args[2]);
            }
            default -> System.out.println("No command with that name exists.");
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (cmd.equals("init") && Repository.checkInit()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        } else if (!cmd.equals("init") && !Repository.checkInit()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }

        switch (cmd) {
            case "commit" -> {
                if (args.length == 1 || (args.length == 2 && args[1].isBlank())) {
                    System.out.println("Please enter a commit message.");
                    System.exit(0);
                }
                if (args.length > n) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            }
            case "checkout" -> {
                if (args.length > n || args.length <= 1) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length == 3 && !args[1].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                if (args.length == 4 && !args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
                break;
            }
            default -> {
                if (args.length != n) {
                    System.out.println("Incorrect operands.");
                    System.exit(0);
                }
            }
        }
    }
}

