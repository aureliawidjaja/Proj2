package gitlet;

import java.io.File;
import java.time.*;

public class Main {
    public static void main(String[] args) {

        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        Repository repo = new Repository();

        try {
            // Process first argument (i.e. args[0]):
            switch (args[0]) {
                case "init":
                    repo.init();
                    break;

                case "add":
                    if (args.length == 1) {
                        System.out.println("Please enter a file name.");
                        System.exit(0);
                    }

                    repo.add(args[1]);
                    break;

                case "commit":
                    if (args.length == 1 || args[1].equals("")) {
                        System.out.println("Please enter a commit message.");
                        System.exit(0);
                    }

                    repo.commit(args[1]);
                    break;

                case "global-log":
                    repo.global_log();
                    break;

                case "log":
                    repo.log();
                    break;

                case "restore":
                    if (args.length <= 2) {
                        System.out.println("Please enter a file name, or a commit Id and a file name.");
                        System.exit(0);
                    }

                    // There are 2 restore methods. One takes one parameter and the other one takes 2.
                    switch (args.length) {
                        case 3:
                            // args[1] = --
                            // args[2] = file name

                            if (!args[1].equals("--")) {
                                System.out.println("Incorrect operands.");
                                System.exit(0);
                            }
                            repo.restore(args[2]);
                            //System.out.println("File "+args[1]+" was restored.");
                            break;

                        case 4:
                            // args[1] = commit id
                            // args[2] = --
                            // args[3] = file name

                            if (!args[2].equals("--")) {
                                System.out.println("Incorrect operands.");
                                System.exit(0);
                            }

                            repo.restore(args[1], args[3]);
                            break;

                        default:

                    }
                    break;

                case "rm":
                    if (args.length == 1) {
                        System.out.println("Please enter a file name");
                        System.exit(0);
                    }

                    repo.rm(args[1]);
                    break;

                case "branch":

                    if (args.length == 1) {
                        System.out.println("Please enter a branch name");
                        System.exit(0);
                    }
                    repo.branch(args[1]);
                    break;

                case "switch":
                    if (args.length == 1) {
                        System.out.println("Please enter a branch name");
                        System.exit(0);
                    }
                    repo.switchToBranch(args[1]);
                    break;

                case "rm-branch":
                    if (args.length == 1) {
                        System.out.println("Please enter a branch name");
                        System.exit(0);
                    }
                    repo.rmBranch(args[1]);
                    break;

                case "find":
                    if (args.length == 1) {
                        System.out.println("Please enter a message");
                        System.exit(0);
                    }
                    repo.find(args[1]);
                    break;

                case "status":
                    repo.status();
                    break;

                case "reset":
                    if (args.length == 1) {
                        System.out.println("Please enter a commitId.");
                        System.exit(0);
                    }

                    repo.reset(args[1]);
                    break;

                default:
                    System.out.println("No command with that name exists.");
                    System.exit(0);
                    break;
            }
        }
        catch (GitletException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
    }
}
