package exceptions;

public class BorrowLimitExceededException extends Exception {

    private static final long serialVersionUID = 1L;

    public BorrowLimitExceededException(String message) {
        super(message);
    }

}