package net.datasa.EnLink.common.error;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler; // 추가
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalViewExceptionHandler {
	
	// 🚀 @ExceptionHandler 어노테이션을 꼭 붙여야 스프링이 에러를 가로챕니다!
	@ExceptionHandler(BusinessException.class)
	public ModelAndView handleBusinessException(BusinessException e){
		ModelAndView mav = new ModelAndView();
		
		// 1. 디자인하신 HTML 경로 설정
		mav.setViewName("common/error/alertAndBack");
		
		// 2. 다국어 처리를 위해 defaultMessage 대신 messageCode를 보내는 것이 좋습니다.
		// (html에서 th:text="#{${message}}" 로 쓰면 자동으로 번역됨)
		mav.addObject("message", e.getErrorCode().getMessageCode());
		
		return mav;
	}
}