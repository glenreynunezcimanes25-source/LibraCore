package models;

public class FacultyMember extends LibraryMember {

    private static final long serialVersionUID = 1L;

    public FacultyMember(String memberId, String fullName, String contactNumber) {
        super(memberId, fullName, contactNumber);
    }

    @Override
    public int getMaxLoans() {
        return 5;
    }

    @Override
    public int getLoanPeriodDays() {
        return 14;
    }

    @Override
    public double getFinePerDay() {
        return 5.00;
    }

}
