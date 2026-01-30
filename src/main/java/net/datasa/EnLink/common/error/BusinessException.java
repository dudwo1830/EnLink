package net.datasa.EnLink.common.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
	private ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getDefaultMessage());
		this.errorCode = errorCode;
	}
}
