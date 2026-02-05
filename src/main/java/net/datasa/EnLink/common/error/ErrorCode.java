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

	// 컨텐츠(게시글 등)
	CONTENT_NOT_FOUND(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "컨텐츠를 찾을 수 없습니다.", "error.content.not_found"),
	
	// --- 모임(Club) 관련 에러
	// 모임 생성/수정 관련
	CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_NOT_FOUND", "해당 모임을 찾을 수 없습니다.", "error.club.not_found"),
	DUPLICATE_CLUB_NAME(HttpStatus.BAD_REQUEST, "CLUB_001", "이미 사용 중인 모임 이름입니다. 다른 이름을 입력해주세요.", "error.club.duplicate_name"),
	CLUB_OWN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "C001", "모임은 최대 5개까지만 개설할 수 있습니다.", "error.club.own_limit"),
	INVALID_MAX_MEMBER_UNIT(HttpStatus.BAD_REQUEST, "C005", "최대 인원은 10단위로 설정해야 합니다.", "error.club.invalid_max_member"),
	MAX_MEMBER_LESS_THAN_CURRENT(HttpStatus.BAD_REQUEST, "C005", "현재 멤버 수보다 적게 설정할 수 없습니다.", "error.club.invalid_max_member"),
	CANNOT_RESTORE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CLUB_002", "개설 가능한 모임 수를 초과하여 복구할 수 없습니다.", "error.club.restore_limit"),
	CLUB_NOT_IN_PENDING_STATE(HttpStatus.BAD_REQUEST, "CLUB_003", "삭제 대기 상태인 모임만 복구할 수 있습니다.", "error.club.not_pending"),
	
	// 가입 제한 관련
	CLUB_JOIN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CLUB_LIMIT_EXCEEDED", "최대 5개의 모임까지만 가입할 수 있습니다.", "error.club.limit_exceeded"),
	ALREADY_JOINED_OR_PENDING(HttpStatus.BAD_REQUEST, "ALREADY_JOINED", "이미 가입된 멤버이거나 승인 대기 중인 상태입니다.", "error.club.already_joined"),
	CLUB_IS_FULL(HttpStatus.BAD_REQUEST, "C006", "모임의 정원이 초과되었습니다.", "error.club.is_full"),
	TOO_MANY_CLUBS(HttpStatus.BAD_REQUEST, "C005", "해당 유저는 이미 5개의 모임에 가입되어 있어 더 이상 가입할 수 없습니다.","error.club.already_joined"),
	
	
	//멤버 할동 관련
	ALREADY_LEFT_OR_BANNED(HttpStatus.BAD_REQUEST, "MEMBER_001", "이미 탈퇴했거나 활동이 제한된 멤버입니다.", "error.member.already_inactive"),
	// 권한 및 상태 관련
	NOT_CLUB_MEMBER(HttpStatus.FORBIDDEN, "NOT_CLUB_MEMBER", "해당 모임의 멤버가 아닙니다.", "error.club.not_member"),
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "UNAUTHORIZED_MANAGE", "해당 작업에 대한 권한이 없습니다.", "error.club.unauthorized"),
	OWNER_ONLY_ACCESS(HttpStatus.FORBIDDEN, "C008", "모임장만 접근 가능합니다.", "error.club.owner_only"),
	MANAGER_UP_ACCESS(HttpStatus.FORBIDDEN, "C009", "운영진 이상의 권한이 필요합니다.", "error.club.manager_up"),
	CLUB_OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "C012", "모임장은 탈퇴할 수 없습니다. 권한 위임 후 다시 시도해주세요.", "error.club.owner_cannot_leave"),
	
	// 신청 및 승인 관련
	JOIN_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "JOIN_REQUEST_NOT_FOUND", "존재하지 않는 가입 신청입니다.", "error.club.request_not_found"),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C010", "자기 자신은 제명할 수 없습니다.", "error.club.invalid_input"),
	
	// 4. 기타
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
