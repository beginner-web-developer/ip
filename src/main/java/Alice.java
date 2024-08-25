import java.util.ArrayList;
import java.util.Scanner;

public class Alice {
    private static ArrayList<Task> tasks = new ArrayList<>();
    private final Storage storage;

    public Alice(String filePath) {
        storage = new Storage(filePath);
        // load tasks
        storage.loadTasks();
        Alice.tasks = storage.getTasks();
    }

    public static void main(String[] args) {
        // create instance of Alice with loaded tasks
        new Alice("data.txt").run();
    }

    // starts the program
    public void run() {
        System.out.println("Hello! I am Alice! \nWhat can I do for you?");
        System.out.println("------------------------------------------");

        // get commands from the user while response is not "bye"
        String response = "";
        Scanner scanner = new Scanner(System.in);
        while (true) {
            if (!scanner.hasNext()) {
                return;
            }
            response = scanner.nextLine();

            // if user says bye, exit the loop
            if (response.equals("bye")) {
                break;
            }
            // if user wants list, print out all the tasks added
            else if (response.equals("list")) {
                System.out.println("------------------------------------------");
                System.out.println("Here are the tasks in your list: ");
                for (int i = 0; i < tasks.size(); i++) {
                    System.out.printf("%d. %s%n", i + 1, tasks.get(i).toString());
                }
                System.out.println("------------------------------------------");
                continue;
            }

            try {
                handleTask(response);
            } catch (AliceException e) {
                System.out.println(e);
                System.out.println("------------------------------------------");
            }
        }

        System.out.println("------------------------------------------");
        System.out.println("Bye. Hope to see you again soon!");
        scanner.close();
        storage.saveTasks(tasks);
    }

    // for marking tasks as done or undone: separate the string from the index
    // all other strings are tasks that should be added
    public static void handleTask(String response) throws AliceException{
        String[] result = response.split(" ", 2);
        Command command = Command.stringToCommand(result[0]);
        switch (command) {
            case MARK:
                int taskNumber;
                try {
                    taskNumber = Integer.parseInt(result[1]);
                } catch (NumberFormatException e) {
                    System.out.println(result[1] + " is not a number!");
                    System.out.println("------------------------------------------");
                    break;
                }
                if (taskNumber > tasks.size()) {
                    throw new InvalidTaskException(response, taskNumber);
                }
                Task markTask = tasks.get(taskNumber - 1);
                markTask.markAsDone();
                System.out.println("------------------------------------------");
                System.out.println("Nice! I've marked this task as done: ");
                System.out.println(markTask);
                System.out.println("------------------------------------------");
                break;
            case UNMARK:
                int taskNum;
                try {
                    taskNum = Integer.parseInt(result[1]);
                } catch (NumberFormatException e) {
                    System.out.println(result[1] + " is not a number!");
                    System.out.println("------------------------------------------");
                    break;
                }
                if (taskNum > tasks.size()) {
                    throw new InvalidTaskException(response, taskNum);
                }
                Task unmarkTask = tasks.get(taskNum - 1);
                unmarkTask.markAsUndone();
                System.out.println("------------------------------------------");
                System.out.println("Ok, I've marked this task as not done yet: ");
                System.out.println(unmarkTask);
                System.out.println("------------------------------------------");
                break;
            case DELETE:
                int taskNo;
                try {
                    taskNo = Integer.parseInt(result[1]);
                } catch (NumberFormatException e) {
                    System.out.println(result[1] + " is not a number!");
                    System.out.println("------------------------------------------");
                    break;
                }
                if (taskNo > tasks.size()) {
                    throw new InvalidTaskException(response, taskNo);
                }
                Task task = tasks.remove(taskNo - 1);
                System.out.println("------------------------------------------");
                System.out.println("Noted. I've removed this task: ");
                System.out.println(task);
                System.out.printf("Now you have %d tasks in the list%n", tasks.size());
                System.out.println("------------------------------------------");
                break;
            case TODO:
                // result[1] contains description
                if (result.length != 2) {
                    throw new MissingArgumentException("Todo", new String[]{"description"});
                }
                Todo todoTask = new Todo(result[1]);
                tasks.add(todoTask);
                break;
            case DEADLINE:
                // result[1] contains description /by deadline
                if (result.length != 2) {
                    throw new MissingArgumentException("Deadline", new String[]{"description, by"});
                }
                String[] deadlineInfo = result[1].split("/by ");
                if (deadlineInfo.length != 2) {
                    throw new MissingArgumentException("Deadline", new String[]{"description, by"});
                }
                // deadlineInfo[0] = description, deadlineInfo[1] = deadline
                Deadline deadlineTask = new Deadline(deadlineInfo[0], deadlineInfo[1].strip());
                tasks.add(deadlineTask);
                break;
            case EVENT:
                // result[1] contains description /from from /to to
                if (result.length != 2) {
                    throw new MissingArgumentException("Event", new String[]{"description, from, to"});
                }
                String[] eventInfo = result[1].split("/from ");
                if (eventInfo.length != 2) {
                    throw new MissingArgumentException("Event", new String[]{"description, from, to"});
                }
                String[] times = eventInfo[1].split("/to ");
                if (times.length != 2) {
                    throw new MissingArgumentException("Event", new String[]{"description, from, to"});
                }
                Event eventTask = new Event(eventInfo[0], times[0].strip(), times[1].strip());
                tasks.add(eventTask);
                break;
            default:
                throw new AliceException(response);
        }
        if (command == Command.MARK || command == Command.UNMARK || command == Command.DELETE) {
            return;
        }

        System.out.println("------------------------------------------");
        System.out.println("Got it. I've added this task: ");
        System.out.println(tasks.get(tasks.size() - 1));
        System.out.printf("Now you have %d tasks in the list%n", tasks.size());
        System.out.println("------------------------------------------");
    }
}
