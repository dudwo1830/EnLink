package net.datasa.EnLink.common.error;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ErrorResponse {
	private String code;
	private String defaultMessage;
	private String messageCode;

	// valid
	private List<FieldErrorResponse> errors;

	public static ErrorResponse from(ErrorCode errorCode) {
		return ErrorResponse.builder()
				.code(errorCode.getCode())
				.defaultMessage(errorCode.getDefaultMessage())
				.messageCode(errorCode.getMessageCode())
				.build();
	}

	public static ErrorResponse internal() {
		return ErrorResponse.builder()
				.code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
				.defaultMessage(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage())
				.messageCode(ErrorCode.INTERNAL_SERVER_ERROR.getMessageCode())
				.build();
	}

	public static ErrorResponse validation(ErrorCode errorCode, List<FieldErrorResponse> errors) {
		return ErrorResponse.builder().code(errorCode.getCode()).defaultMessage(errorCode.getDefaultMessage())
				.messageCode(errorCode.getMessageCode()).errors(errors).build();
	}
}
