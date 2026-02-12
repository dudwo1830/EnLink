package net.datasa.EnLink.community.chat.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	// 파일을 저장하고 저장된 파일의 접근 URL을 반환
	String saveFile(MultipartFile file);
}
