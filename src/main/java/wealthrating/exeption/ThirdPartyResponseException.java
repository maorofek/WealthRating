package wealthrating.exeption;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FAILED_DEPENDENCY)
public class ThirdPartyResponseException extends RuntimeException {
    public ThirdPartyResponseException(String message) {
        super(message);
    }
}