package models;

import java.io.Serializable;
import interfaces.BorrowingPolicy;

public abstract class LibraryMember implements BorrowingPolicy, Serializable {

    private static final long serialVersionUID = 1L;
    private String memberId;
    private String fullname;
    private String contactNumber;

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
