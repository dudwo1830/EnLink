package net.datasa.EnLink.common.error;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalViewExceptionHandler {
	public ModelAndView handleBusinessException(BusinessException e){
		ModelAndView mav = new ModelAndView();
		mav.setViewName("common/error/alertAndBack");
		mav.addObject("message", e.getErrorCode().getDefaultMessage());
		return mav;
	}
}
