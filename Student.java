public class Student extends Person {
    private String studentId;
    private String branch;
    private String section;
    private int semester;

    public Student(String studentId, String studentName, String branch, String section, int semester) {
        super(studentName);
        setStudentId(studentId);
        setBranch(branch);
        setSection(section);
        setSemester(semester);
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = requireText(studentId, "Student ID");
    }

    public String getStudentName() {
        return getName();
    }

    public void setStudentName(String studentName) {
        setName(studentName);
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = requireText(branch, "Branch");
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        String normalizedSection = requireText(section, "Section").toUpperCase();
        if (!normalizedSection.equals("A") && !normalizedSection.equals("B") && !normalizedSection.equals("C")) {
            throw new IllegalArgumentException("Section must be A, B, or C.");
        }
        this.section = normalizedSection;
    }

    public int getSemester() {
        return semester;
    }

    public void setSemester(int semester) {
        if (semester < 1) {
            throw new IllegalArgumentException("Semester must be a positive number.");
        }
        this.semester = semester;
    }

    private String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
        return value.trim();
    }

    @Override
    public void displayDetails() {
        System.out.println(this);
    }

    @Override
    public String toString() {
        return "Student ID: " + studentId
                + " | Name: " + getName()
                + " | Branch: " + branch
                + " | Section: " + section
                + " | Semester: " + semester;
    }
}
