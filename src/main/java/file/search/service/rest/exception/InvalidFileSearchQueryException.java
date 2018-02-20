package file.search.service.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(value=HttpStatus.BAD_REQUEST)
public class InvalidFileSearchQueryException extends Exception {

	private static final long serialVersionUID = -3278380923689647254L;
	
	public InvalidFileSearchQueryException(String msg) {
		super(msg);
	}

}
