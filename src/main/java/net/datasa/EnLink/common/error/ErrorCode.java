package net.datasa.EnLink.common.error;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
	/**
	 * 변수(HttpStatus, 에러 코드, 기본 메세지, 메세지 코드)로 작성
	 * throw new BusinessException(ErrorCode.변수); 형태로 사용
	 */

	// 사용자
	USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", "error.user.not_found"),
	
	SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_NOT_FOUND", "message", ""),

	// 컨텐츠(게시글 등)
	CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "컨텐츠를 찾을 수 없습니다.", "error.content.not_found"),

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "서버 내부 에러", "error.internal");

	// HttpStatus
	private final HttpStatus status;
	// 에러 코드
	// 시스템 식별용
	private final String code;
	// 기본 메세지
	// 개발자가 확인할 수 있을 정도
	private final String defaultMessage;
	// 실제 사용자에게 제공될 메세지 코드
	// 프론트에서 해당 데이터를 통해 메세지 출력
	private final String messageCode;
}
