package net.datasa.EnLink.common.error;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalRestExceptionHandler {
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e){
			ErrorCode errorCode = e.getErrorCode();
			return ResponseEntity
					.status(errorCode.getStatus())
					.body(ErrorResponse.from(errorCode));
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
