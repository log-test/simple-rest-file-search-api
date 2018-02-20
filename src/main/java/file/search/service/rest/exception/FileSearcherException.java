package file.search.service.rest.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;



@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
public class FileSearcherException extends Exception {

	private static final long serialVersionUID = 4941959372750095512L;

	public FileSearcherException(String msg) {
		super(msg);
	}
	
}
