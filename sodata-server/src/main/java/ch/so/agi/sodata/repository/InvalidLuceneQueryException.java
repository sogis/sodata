package ch.so.agi.sodata.repository;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)
public class InvalidLuceneQueryException extends Exception {

    private static final long serialVersionUID = 1L;
    
    public InvalidLuceneQueryException(String msg) {
        super(msg);
    }
}