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
	
	// --- 모임 관리 (Clubs) ---
	CLUB_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_001", "해당 모임을 찾을 수 없습니다.", "error.club.not_found"),
	DUPLICATE_NAME(HttpStatus.BAD_REQUEST, "CLUB_002", "이미 사용 중인 모임 이름입니다.", "error.club.duplicate_name"),
	OWN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CLUB_003", "모임은 인당 최대 5개까지만 개설할 수 있습니다.", "error.club.own_limit"),
	RESTORE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CLUB_004", "개설 가능한 모임 수를 초과하여 복구할 수 없습니다.", "error.club.restore_limit"),
	NOT_PENDING_STATE(HttpStatus.BAD_REQUEST, "CLUB_005", "삭제 대기 상태인 모임만 복구할 수 있습니다.", "error.club.not_pending"),
	
	// --- 모임 설정 (Settings) ---
	INVALID_MAX_MEMBER(HttpStatus.BAD_REQUEST, "CLUB_SET_001", "최대 인원은 10명 단위로만 설정 가능합니다.", "error.club.invalid_max_member"),
	MAX_MEMBER_UNDER_CURRENT(HttpStatus.BAD_REQUEST, "CLUB_SET_002", "현재 멤버 수보다 적게 정원을 줄일 수 없습니다.", "error.club.max_member_less"),
	
	// --- 가입 및 멤버 제한 (Join/Limit) ---
	JOIN_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "CLUB_JOIN_001", "최대 5개의 모임까지만 가입(활동)할 수 있습니다.", "error.club.limit_exceeded"),
	ALREADY_JOINED_OR_PENDING(HttpStatus.BAD_REQUEST, "CLUB_JOIN_002", "이미 가입된 멤버이거나 승인 대기 중입니다.", "error.club.already_joined"),
	CLUB_IS_FULL(HttpStatus.BAD_REQUEST, "CLUB_JOIN_003", "모임 정원이 초과되어 가입할 수 없습니다.", "error.club.is_full"),
	REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "CLUB_JOIN_004", "존재하지 않는 가입 신청입니다.", "error.club.request_not_found"),
	
	// --- 멤버 상태 변경 (Member Action) ---
	ALREADY_INACTIVE(HttpStatus.BAD_REQUEST, "CLUB_MEM_001", "이미 탈퇴했거나 활동이 제한된 멤버입니다.", "error.member.already_inactive"),
	OWNER_CANNOT_LEAVE(HttpStatus.BAD_REQUEST, "CLUB_MEM_002", "모임장은 탈퇴할 수 없습니다. 권한 위임 후 다시 시도해주세요.", "error.club.owner_cannot_leave"),
	// --- 권한 및 접근 제어 (Auth) ---
	NOT_CLUB_MEMBER(HttpStatus.FORBIDDEN, "CLUB_AUTH_001", "해당 모임의 멤버가 아닙니다.", "error.club.not_member"),
	UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "CLUB_AUTH_002", "해당 작업에 대한 권한이 없습니다.", "error.club.unauthorized"),
	OWNER_ONLY(HttpStatus.FORBIDDEN, "CLUB_AUTH_003", "모임장만 접근 가능한 메뉴입니다.", "error.club.owner_only"),
	MANAGER_UP(HttpStatus.FORBIDDEN, "CLUB_AUTH_004", "운영진 이상의 권한이 필요합니다.", "error.club.manager_up"),
	SELF_ACTION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "CLUB_AUTH_005", "자기 자신에 대한 해당 요청은 처리할 수 없습니다.", "error.club.self_action"),
	
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
