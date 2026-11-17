package models;

public class StudentMember extends LibraryMember {

    private static final long serialVersionUID = 1L;

    // Constructor
    public StudentMember(String memberId, String fullName, String contactNumber) {

        super(memberId, fullName, contactNumber);

    }

    @Override
    public int getMaxLoans() {
        return 3;
    }

    @Override
    public int getLoanPeriodDays() {
        return 7;
    }

    @Override
    public double getFinePerDay() {
        return 5.00;
    }

}