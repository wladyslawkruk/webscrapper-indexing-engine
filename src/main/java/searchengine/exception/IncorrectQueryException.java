package searchengine.exception;

import lombok.Data;

@Data
public class IncorrectQueryException extends Exception{
    private String error;
    public IncorrectQueryException(String customMessage) {
        this.error = error;
    }
}
