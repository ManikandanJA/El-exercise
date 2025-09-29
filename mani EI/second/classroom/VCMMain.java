import java.util.*;
import java.time.*;
import java.time.format.*;
import java.util.logging.*;

// ---------------- Main class ----------------
public class VCMMain {
    private static final Logger LOGGER = LoggerUtil.getLogger(VCMMain.class.getName());

    public static void main(String[] args) {
        LOGGER.info("Starting Virtual Classroom Manager...");
        VirtualClassroomManager manager = VirtualClassroomManager.getInstance();
        Scanner sc = new Scanner(System.in);
        printHelp();

        while (true) {
            System.out.print("vcm> ");
            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;
            String[] tokens = line.split("\\s+", 2);
            String cmd = tokens[0].toLowerCase();
            String payload = tokens.length > 1 ? tokens[1] : "";

            try {
                switch (cmd) {
                    case "help": printHelp(); break;
                    case "exit": LOGGER.info("Exiting..."); sc.close(); return;
                    case "add_classroom": manager.createClassroom(payload); break;
                    case "list_classrooms": manager.listClassrooms(); break;
                    case "remove_classroom": manager.removeClassroom(payload); break;
                    case "add_student": manager.addStudentToClass(payload); break;
                    case "list_students": manager.listStudents(payload); break;
                    case "schedule_assignment": manager.scheduleAssignment(payload); break;
                    case "submit_assignment": manager.submitAssignment(payload); break;
                    case "list_assignments": manager.listAssignments(payload); break;
                    case "help_commands": printCommands(); break;
                    default:
                        System.out.println("Unknown command. Type 'help' or 'help_commands'.");
                }
            } catch (VCMException e) {
                LOGGER.log(Level.WARNING, e.getMessage());
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error", e);
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }

    private static void printHelp() {
        System.out.println("Virtual Classroom Manager - Console");
        System.out.println("Type 'help_commands' to see available commands. Type 'exit' to quit.");
    }

    private static void printCommands() {
        System.out.println("Commands:");
        System.out.println("add_classroom <classroom_name>");
        System.out.println("remove_classroom <classroom_name>");
        System.out.println("list_classrooms");
        System.out.println("add_student <student_id>;<student_name>;<classroom_name>");
        System.out.println("list_students <classroom_name>");
        System.out.println("schedule_assignment <classroom_name>;<assignment_id>;<title>;<dueDate YYYY-MM-DD>");
        System.out.println("list_assignments <classroom_name>");
        System.out.println("submit_assignment <student_id>;<classroom_name>;<assignment_id>;<submission_text>");
    }
}

// ---------------- Logger Utility ----------------
class LoggerUtil {
    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new SimpleFormatter());
        ch.setLevel(Level.INFO);
        logger.addHandler(ch);
        logger.setLevel(Level.INFO);
        return logger;
    }
}

// ---------------- Custom Exception ----------------
class VCMException extends Exception {
    public VCMException(String message) { super(message); }
}

// ---------------- Manager ----------------
class VirtualClassroomManager {
    private static VirtualClassroomManager instance;
    private static final Object LOCK = new Object();
    private final Map<String, Classroom> classrooms = new LinkedHashMap<>();
    private final Map<String, Student> students = new LinkedHashMap<>();
    private static final Logger LOGGER = LoggerUtil.getLogger(VirtualClassroomManager.class.getName());

    private VirtualClassroomManager() {}

    public static VirtualClassroomManager getInstance() {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) instance = new VirtualClassroomManager();
            }
        }
        return instance;
    }

    public void createClassroom(String payload) throws VCMException {
        String name = payload == null ? "" : payload.trim();
        if (name.isEmpty()) throw new VCMException("Classroom name cannot be empty.");
        if (classrooms.containsKey(name)) throw new VCMException("Classroom already exists: " + name);
        Classroom c = new Classroom(name);
        classrooms.put(name, c);
        LOGGER.info("Classroom created: " + name);
        System.out.println("Classroom '" + name + "' created.");
    }

    public void removeClassroom(String payload) throws VCMException {
        String name = payload == null ? "" : payload.trim();
        if (!classrooms.containsKey(name)) throw new VCMException("Classroom not found: " + name);
        classrooms.remove(name);
        LOGGER.info("Classroom removed: " + name);
        System.out.println("Classroom '" + name + "' removed.");
    }

    public void listClassrooms() {
        if (classrooms.isEmpty()) {
            System.out.println("No classrooms available.");
            return;
        }
        System.out.println("Classrooms:");
        classrooms.keySet().forEach(k -> System.out.println(" - " + k));
    }

    public void addStudentToClass(String payload) throws VCMException {
        String[] parts = splitPayload(payload, 3);
        String sid = parts[0].trim();
        String sname = parts[1].trim();
        String cname = parts[2].trim();
        if (!classrooms.containsKey(cname)) throw new VCMException("Classroom does not exist: " + cname);
        Student s = students.getOrDefault(sid, new Student(sid, sname));
        students.putIfAbsent(sid, s);
        Classroom c = classrooms.get(cname);
        c.enrollStudent(s);
        LOGGER.info("Student " + sid + " enrolled in " + cname);
        System.out.println("Student '" + sname + "' (" + sid + ") enrolled in '" + cname + "'.");
    }

    public void listStudents(String payload) throws VCMException {
        String cname = payload == null ? "" : payload.trim();
        if (!classrooms.containsKey(cname)) throw new VCMException("Classroom not found: " + cname);
        Classroom c = classrooms.get(cname);
        List<Student> list = c.getStudents();
        if (list.isEmpty()) {
            System.out.println("No students enrolled in '" + cname + "'.");
            return;
        }
        System.out.println("Students in '" + cname + "':");
        list.forEach(s -> System.out.println(" - " + s.getId() + ": " + s.getName()));
    }

    public void scheduleAssignment(String payload) throws VCMException {
        String[] parts = splitPayload(payload, 4);
        String cname = parts[0].trim();
        String aid = parts[1].trim();
        String title = parts[2].trim();
        String due = parts[3].trim();
        if (!classrooms.containsKey(cname)) throw new VCMException("Classroom not found: " + cname);
        LocalDate dueDate;
        try { dueDate = LocalDate.parse(due); }
        catch (DateTimeParseException e) { throw new VCMException("Invalid date format. Use YYYY-MM-DD."); }
        Assignment assignment = new Assignment(aid, title, dueDate);
        Classroom c = classrooms.get(cname);
        c.addAssignment(assignment);
        LOGGER.info("Assignment " + aid + " scheduled for " + cname);
        System.out.println("Assignment '" + title + "' (" + aid + ") scheduled for '" + cname + "' due " + dueDate + ".");
    }

    public void listAssignments(String payload) throws VCMException {
        String cname = payload == null ? "" : payload.trim();
        if (!classrooms.containsKey(cname)) throw new VCMException("Classroom not found: " + cname);
        Classroom c = classrooms.get(cname);
        List<Assignment> list = c.getAssignments();
        if (list.isEmpty()) {
            System.out.println("No assignments for '" + cname + "'.");
            return;
        }
        System.out.println("Assignments for '" + cname + "':");
        list.forEach(a -> System.out.println(" - " + a.getId() + ": " + a.getTitle() + " (Due: " + a.getDueDate() + ")"));
    }

    public void submitAssignment(String payload) throws VCMException {
        String[] parts = splitPayload(payload, 4);
        String sid = parts[0].trim();
        String cname = parts[1].trim();
        String aid = parts[2].trim();
        String text = parts[3].trim();
        if (!students.containsKey(sid)) throw new VCMException("Student not found: " + sid);
        if (!classrooms.containsKey(cname)) throw new VCMException("Classroom not found: " + cname);
        Classroom c = classrooms.get(cname);
        Assignment a = c.getAssignmentById(aid);
        if (a == null) throw new VCMException("Assignment not found: " + aid);
        Submission sub = new Submission(sid, aid, text);
        a.addSubmission(sub);
        LOGGER.info("Submission by " + sid + " for " + aid);
        System.out.println("Submission received for assignment '" + a.getTitle() + "' from student " + sid + ".");
    }

    private String[] splitPayload(String payload, int expected) throws VCMException {
        if (payload == null) throw new VCMException("Invalid arguments.");
        String[] parts = payload.split(";", -1);
        if (parts.length < expected) throw new VCMException("Insufficient arguments. Expected " + expected + " separated by ';'.");
        return parts;
    }
}

// ---------------- Classroom ----------------
class Classroom {
    private final String name;
    private final List<Student> students = new ArrayList<>();
    private final List<Assignment> assignments = new ArrayList<>();

    public Classroom(String name) { this.name = name; }

    public String getName() { return name; }

    public synchronized void enrollStudent(Student s) {
        if (!students.stream().anyMatch(x -> x.getId().equals(s.getId()))) students.add(s);
    }

    public synchronized List<Student> getStudents() { return Collections.unmodifiableList(students); }

    public synchronized void addAssignment(Assignment a) {
        if (!assignments.stream().anyMatch(x -> x.getId().equals(a.getId()))) assignments.add(a);
    }

    public synchronized List<Assignment> getAssignments() { return Collections.unmodifiableList(assignments); }

    public synchronized Assignment getAssignmentById(String id) {
        return assignments.stream().filter(a -> a.getId().equals(id)).findFirst().orElse(null);
    }
}

// ---------------- Student ----------------
class Student {
    private final String id;
    private final String name;
    public Student(String id, String name) { this.id = id; this.name = name; }
    public String getId() { return id; }
    public String getName() { return name; }
}

// ---------------- Assignment ----------------
class Assignment {
    private final String id;
    private final String title;
    private final LocalDate dueDate;
    private final List<Submission> submissions = new ArrayList<>();
    public Assignment(String id, String title, LocalDate dueDate) { this.id=id; this.title=title; this.dueDate=dueDate; }
    public String getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getDueDate() { return dueDate; }
    public synchronized void addSubmission(Submission s) { submissions.add(s); }
    public synchronized List<Submission> getSubmissions() { return Collections.unmodifiableList(submissions); }
}

// ---------------- Submission ----------------
class Submission {
    private final String studentId;
    private final String assignmentId;
    private final String text;
    private final LocalDateTime submittedAt;
    public Submission(String studentId, String assignmentId, String text) {
        this.studentId = studentId;
        this.assignmentId = assignmentId;
        this.text = text;
        this.submittedAt = LocalDateTime.now();
    }
    public String getStudentId() { return studentId; }
    public String getAssignmentId() { return assignmentId; }
    public String getText() { return text; }
    public LocalDateTime getSubmittedAt() { return submittedAt; }
}
