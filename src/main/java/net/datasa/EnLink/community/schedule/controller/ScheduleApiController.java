package net.datasa.EnLink.community.schedule.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.common.error.BusinessException;
import net.datasa.EnLink.common.error.ErrorCode;
import net.datasa.EnLink.community.schedule.dto.ScheduleDTO;
import net.datasa.EnLink.community.schedule.dto.ScheduleParticipantDTO;
import net.datasa.EnLink.community.schedule.service.ScheduleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleApiController {
	
	private final ScheduleService scheduleService;
	
	// 일정 생성
	@PostMapping("/create")
	public ResponseEntity<String> create(
			@RequestBody ScheduleDTO dto,
			@AuthenticationPrincipal UserDetails user) {
		try {
			// 현재 로그인한 사용자 아이디 추출
			String loginMemberId = user.getUsername();
			
			// 서비스 호출하여 저장 로직 수행
			scheduleService.createSchedule(dto, loginMemberId);
			
			return ResponseEntity.ok("일정이 성공적으로 등록되었습니다.");
		} catch (Exception e) {
			// 실패 시 에러 메시지 반환
			return ResponseEntity.badRequest().body("일정 등록 실패: " + e.getMessage());
		}
	}
	
	@GetMapping("/list")
	public ResponseEntity<List<ScheduleDTO>> getList(@RequestParam("clubId") Integer clubId,@AuthenticationPrincipal UserDetails user) {
		try {
			// 서비스 호출 시 user.getUsername()을 같이 넘겨줍니다.
			List<ScheduleDTO> list = scheduleService.getScheduleList(clubId, user.getUsername());
			
			// 성공 시 데이터와 함께 200 OK 응답
			return ResponseEntity.ok(list);
		} catch (Exception e) {
			// 에러 발생 시 로그를 찍고 빈 리스트 반환 혹은 에러 응답
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}
	
	@GetMapping("test")
	public void test(){
		throw new BusinessException(ErrorCode.SCHEDULE_NOT_FOUND);
	}
	
	@PostMapping("/apply")
	public ResponseEntity<?> apply(@RequestParam("schedId") Integer schedId,
								   @AuthenticationPrincipal UserDetails user) {
		try {
			ScheduleParticipantDTO dto = ScheduleParticipantDTO.builder()
					.schedId(schedId)
					.memberId(user.getUsername())
					.status("JOIN")
					.build();
			scheduleService.applySchedule(dto);
			return ResponseEntity.ok("신청 완료!");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	@PostMapping("/cancel")
	public ResponseEntity<String> cancel(@RequestParam("schedId") Integer schedId,
										 @AuthenticationPrincipal UserDetails user) {
		// user.getUsername()을 통해 현재 로그인한 본인의 상태만 수정하게 됨
		scheduleService.cancelSchedule(schedId, user.getUsername());
		return ResponseEntity.ok("참가 신청이 취소되었습니다.");
	}
	
	// 일정 수정 요청
	@PutMapping("/update")
	public ResponseEntity<?> updateSchedule(@RequestBody ScheduleDTO dto,
											@AuthenticationPrincipal UserDetails user) {
		try {
			scheduleService.updateSchedule(dto, user.getUsername());
			return ResponseEntity.ok("일정이 성공적으로 수정되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	// 일정 삭제 요청
	@DeleteMapping("/delete")
	public ResponseEntity<?> deleteSchedule(@RequestParam("schedId") Integer schedId,
											@AuthenticationPrincipal UserDetails user) {
		try {
			scheduleService.deleteSchedule(schedId, user.getUsername());
			return ResponseEntity.ok("일정이 삭제되었습니다.");
		} catch (Exception e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		}
	}
	
	// 참여자 명단 조회
	@GetMapping("/participants")
	public ResponseEntity<List<ScheduleParticipantDTO>> getParticipants(@RequestParam("schedId") Integer schedId) {
		try {
			List<ScheduleParticipantDTO> list = scheduleService.getParticipantList(schedId);
			return ResponseEntity.ok(list);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().build();
		}
	}
}
