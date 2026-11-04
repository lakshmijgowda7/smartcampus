import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class FileManager {
    private static final List<String> STUDENT_HEADER =
            Arrays.asList("studentId", "name", "branch", "section", "semester");
    private static final List<String> FACULTY_HEADER =
            Arrays.asList("facultyId", "name", "department", "email", "phoneNumber");
    private static final List<String> COURSE_HEADER =
            Arrays.asList("courseId", "courseName", "courseCode", "credits", "department");

    private final Path dataDirectory;

    public FileManager() {
        this(Paths.get("data"));
    }

    public FileManager(Path dataDirectory) {
        if (dataDirectory == null) {
            throw new IllegalArgumentException("A data directory is required.");
        }
        this.dataDirectory = dataDirectory;
    }

    public List<Student> loadStudents() throws IOException {
        return readRecords("students.csv", STUDENT_HEADER, values -> {
            requireColumnCount(values, STUDENT_HEADER.size());
            return new Student(values.get(0), values.get(1), values.get(2), values.get(3),
                    Integer.parseInt(values.get(4)));
        });
    }

    public List<Faculty> loadFaculty() throws IOException {
        return readRecords("faculty.csv", FACULTY_HEADER, values -> {
            requireColumnCount(values, FACULTY_HEADER.size());
            return new Faculty(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4));
        });
    }

    public List<Course> loadCourses() throws IOException {
        return readRecords("courses.csv", COURSE_HEADER, values -> {
            requireColumnCount(values, COURSE_HEADER.size());
            return new Course(values.get(0), values.get(1), values.get(2), Integer.parseInt(values.get(3)),
                    values.get(4));
        });
    }

    public void saveStudents(List<Student> students) throws IOException {
        writeRecords("students.csv", STUDENT_HEADER, students, student -> Arrays.asList(
                student.getStudentId(), student.getStudentName(), student.getBranch(), student.getSection(),
                Integer.toString(student.getSemester())));
    }

    public void saveFaculty(List<Faculty> facultyMembers) throws IOException {
        writeRecords("faculty.csv", FACULTY_HEADER, facultyMembers, faculty -> Arrays.asList(
                faculty.getFacultyId(), faculty.getFacultyName(), faculty.getDepartment(), faculty.getEmail(),
                faculty.getPhoneNumber()));
    }

    public void saveCourses(List<Course> courses) throws IOException {
        writeRecords("courses.csv", COURSE_HEADER, courses, course -> Arrays.asList(
                course.getCourseId(), course.getCourseName(), course.getCourseCode(),
                Integer.toString(course.getCredits()), course.getDepartment()));
    }

    private <T> List<T> readRecords(
            String fileName, List<String> expectedHeader, Function<List<String>, T> recordParser) throws IOException {
        Path file = dataDirectory.resolve(fileName);
        ArrayList<T> records = new ArrayList<>();
        if (Files.notExists(file)) {
            return records;
        }
        if (!Files.isRegularFile(file)) {
            throw new IOException("Saved record path is not a regular file: " + file);
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                return records;
            }
            try {
                if (!parseCsvRow(header).equals(expectedHeader)) {
                    throw new IOException("Unexpected CSV header in " + file);
                }
            } catch (IllegalArgumentException exception) {
                throw new IOException("Invalid CSV header in " + file, exception);
            }

            String line;
            int physicalLine = 1;
            int recordStartLine = 2;
            StringBuilder csvRecord = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                physicalLine++;
                if (csvRecord.length() > 0) {
                    csvRecord.append('\n');
                }
                csvRecord.append(line);
                if (!isCsvRecordComplete(csvRecord)) {
                    continue;
                }
                try {
                    records.add(recordParser.apply(parseCsvRow(csvRecord.toString())));
                } catch (IllegalArgumentException exception) {
                    System.err.println("Skipping invalid record in " + file + " at line " + recordStartLine
                            + ": " + exception.getMessage());
                }
                csvRecord.setLength(0);
                recordStartLine = physicalLine + 1;
            }
            if (csvRecord.length() > 0) {
                System.err.println("Skipping invalid record in " + file + " at line " + recordStartLine
                        + ": unclosed quoted CSV value.");
            }
        }
        return records;
    }

    private <T> void writeRecords(
            String fileName, List<String> header, List<T> records, Function<T, List<String>> rowWriter)
            throws IOException {
        if (records == null || records.contains(null)) {
            throw new IllegalArgumentException("A valid record list is required.");
        }

        Files.createDirectories(dataDirectory);
        Path file = dataDirectory.resolve(fileName);
        Path temporaryFile = Files.createTempFile(dataDirectory, fileName, ".tmp");
        try {
            try (BufferedWriter writer = Files.newBufferedWriter(temporaryFile, StandardCharsets.UTF_8)) {
                writeCsvRow(writer, header);
                for (T record : records) {
                    writeCsvRow(writer, rowWriter.apply(record));
                }
            }

            try {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporaryFile, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaryFile);
        }
    }

    private static void writeCsvRow(BufferedWriter writer, List<String> values) throws IOException {
        for (int index = 0; index < values.size(); index++) {
            if (index > 0) {
                writer.write(',');
            }
            String value = values.get(index);
            boolean quote = value.contains(",") || value.contains("\"")
                    || value.contains("\n") || value.contains("\r");
            if (quote) {
                writer.write('"');
            }
            for (int characterIndex = 0; characterIndex < value.length(); characterIndex++) {
                char character = value.charAt(characterIndex);
                if (character == '"') {
                    writer.write("\"\"");
                } else {
                    writer.write(character);
                }
            }
            if (quote) {
                writer.write('"');
            }
        }
        writer.newLine();
    }

    private static List<String> parseCsvRow(String row) {
        ArrayList<String> values = new ArrayList<>();
        StringBuilder value = new StringBuilder();
        boolean quoted = false;
        boolean quoteClosed = false;

        for (int index = 0; index < row.length(); index++) {
            char character = row.charAt(index);
            if (quoted) {
                if (character == '"' && index + 1 < row.length() && row.charAt(index + 1) == '"') {
                    value.append('"');
                    index++;
                } else if (character == '"') {
                    quoted = false;
                    quoteClosed = true;
                } else {
                    value.append(character);
                }
            } else if (character == ',') {
                values.add(value.toString());
                value.setLength(0);
                quoteClosed = false;
            } else if (character == '"' && value.length() == 0 && !quoteClosed) {
                quoted = true;
            } else if (character == '"' || quoteClosed) {
                throw new IllegalArgumentException("Unexpected text after a quoted CSV value.");
            } else {
                value.append(character);
            }
        }

        if (quoted) {
            throw new IllegalArgumentException("Unclosed quoted CSV value.");
        }
        values.add(value.toString());
        return values;
    }

    private static boolean isCsvRecordComplete(CharSequence row) {
        boolean quoted = false;
        for (int index = 0; index < row.length(); index++) {
            if (row.charAt(index) == '"') {
                if (quoted && index + 1 < row.length() && row.charAt(index + 1) == '"') {
                    index++;
                } else {
                    quoted = !quoted;
                }
            }
        }
        return !quoted;
    }

    private static void requireColumnCount(List<String> values, int expectedCount) {
        if (values.size() != expectedCount) {
            throw new IllegalArgumentException("Expected " + expectedCount + " columns but found " + values.size()
                    + ".");
        }
    }
}
