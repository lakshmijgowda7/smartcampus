public class Faculty extends Person {
    private String facultyId;
    private String department;
    private String email;
    private String phoneNumber;

    public Faculty(String facultyId, String facultyName, String department, String email, String phoneNumber) {
        super(facultyName);
        setFacultyId(facultyId);
        setDepartment(department);
        setEmail(email);
        setPhoneNumber(phoneNumber);
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = requireText(facultyId, "Faculty ID");
    }

    public String getFacultyName() {
        return getName();
    }

    public void setFacultyName(String facultyName) {
        setName(facultyName);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = requireText(department, "Department");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        String normalizedEmail = requireText(email, "Email");
        if (!normalizedEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Enter a valid email address.");
        }
        this.email = normalizedEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        String normalizedPhone = requireText(phoneNumber, "Phone number");
        String digitsOnlyPhone = normalizedPhone.replaceAll("[\\s()-]", "");
        if (!digitsOnlyPhone.matches("\\+?[0-9]{7,15}")) {
            throw new IllegalArgumentException("Enter a phone number with 7 to 15 digits.");
        }
        this.phoneNumber = normalizedPhone;
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

    public void displayFaculty() {
        displayDetails();
    }

    @Override
    public String toString() {
        return "Faculty ID: " + facultyId
                + " | Name: " + getName()
                + " | Department: " + department
                + " | Email: " + email
                + " | Phone: " + phoneNumber;
    }
}
