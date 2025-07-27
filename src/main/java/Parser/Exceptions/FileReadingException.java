package Parser.Exceptions;

public class FileReadingException extends RuntimeException {

    public FileReadingException(String fileName) {
        super("Failed reading file with name : " + fileName);
    }

}
