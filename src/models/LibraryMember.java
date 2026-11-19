package models;

import java.io.Serializable;
import java.time.LocalDate;

import interfaces.BorrowingPolicy;

public abstract class LibraryMember implements BorrowingPolicy, Serializable {

    public enum MemberStatus {
        ACTIVE,
        INACTIVE,
        SUSPENDED,
        ARCHIVED,
        Single
    }

    private static final long serialVersionUID = 1L;
    private String memberId;
    private String fullname;
    private String contactNumber;
    // Object keeps older serialized enum values readable after enum moves.
    private Object status = MemberStatus.ACTIVE;
    private LocalDate registrationDate = LocalDate.now();

    public LibraryMember(String memberId, String fullname, String contactNumber) {
        this.memberId = requireText(memberId, "Member ID");
        this.fullname = requireText(fullname, "Full name");
        this.contactNumber = requireText(contactNumber, "Contact number");
    }

    public String getMemberId() {
        return memberId;
    }

    public String getFullname() {
        return fullname;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public MemberStatus getStatus() {
        // Older serialized members have no status field and default to null.
        if (!(status instanceof Enum<?>)) {
            return MemberStatus.ACTIVE;
        }
        return MemberStatus.valueOf(((Enum<?>) status).name());
    }

    public void setStatus(MemberStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Member status cannot be null.");
        }
        this.status = status;
    }

    public boolean canBorrow() {
        return getStatus() == MemberStatus.ACTIVE;
    }

    public void setMemberId(String memberId) {
        this.memberId = requireText(memberId, "Member ID");
    }

    public void setFullname(String fullname) {
        this.fullname = requireText(fullname, "Full name");
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = requireText(contactNumber, "Contact number");
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be empty");
        }
        return value.trim();
    }
}
