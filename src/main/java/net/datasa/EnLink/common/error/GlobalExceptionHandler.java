package net.datasa.EnLink.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
	
	/**
	 * 모든 BusinessException을 통합 처리
	 */
	@ExceptionHandler(BusinessException.class)
	public Object handleBusinessException(BusinessException e, HandlerMethod handlerMethod) {
		log.error("BusinessException 발생: {}", e.getErrorCode().getDefaultMessage());
		
		// 1. 에러가 발생한 위치가 @RestController인지 확인
		boolean isRest = handlerMethod.getBeanType().isAnnotationPresent(RestController.class);
		
		if (isRest) {
			// [A] API 요청인 경우: 기존처럼 JSON 데이터 반환
			ErrorCode errorCode = e.getErrorCode();
			return ResponseEntity
					.status(errorCode.getStatus())
					.body(ErrorResponse.from(errorCode));
		} else {
			// [B] View(페이지 이동) 요청인 경우: alertAndBack.html 반환
			ModelAndView mav = new ModelAndView();
			mav.setViewName("common/error/alertAndBack");
			mav.addObject("message", e.getErrorCode().getDefaultMessage());
			return mav;
		}
	}
}
