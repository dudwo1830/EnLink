package net.datasa.EnLink.common.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private final ErrorCode errorCode;
	private final String fieldName;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getDefaultMessage());
		this.errorCode = errorCode;
		this.fieldName = null;
	}

	public BusinessException(ErrorCode errorCode, String fieldName) {
        super(errorCode.getDefaultMessage());
        this.errorCode = errorCode;
        this.fieldName = fieldName;
    }
}
