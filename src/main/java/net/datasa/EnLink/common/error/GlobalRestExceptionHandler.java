package net.datasa.EnLink.common.error;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Locale;

@RestControllerAdvice(annotations = RestController.class)
@RequiredArgsConstructor
public class GlobalRestExceptionHandler {

	private final MessageSource messageSource;
	
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e, Locale locale) {
		ErrorCode errorCode = e.getErrorCode();

		String message = messageSource.getMessage(
				errorCode.getMessageCode(),
				null,
				errorCode.getDefaultMessage(), // fallback
				locale);

		ErrorResponse response = new ErrorResponse(
				errorCode.getCode(),
				errorCode.getDefaultMessage(),
				message, null);

		return ResponseEntity.status(errorCode.getStatus()).body(response);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleNotValidException(MethodArgumentNotValidException e) {
		List<FieldErrorResponse> fieldErrors = e.getBindingResult()
				.getFieldErrors()
				.stream()
				.map(error -> new FieldErrorResponse(
						error.getField(),
						error.getDefaultMessage() // locale 적용
				))
				.toList();
		ErrorResponse response = ErrorResponse.validation(ErrorCode.VALIDATION_ERROR, fieldErrors);
		return ResponseEntity.status(ErrorCode.VALIDATION_ERROR.getStatus()).body(response);
	}
}
