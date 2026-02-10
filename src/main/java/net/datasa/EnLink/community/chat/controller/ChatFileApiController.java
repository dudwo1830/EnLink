package net.datasa.EnLink.community.chat.controller;

import lombok.RequiredArgsConstructor;
import net.datasa.EnLink.community.chat.service.FileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatFileApiController {
	
	
	private final FileService fileService;
	
	@PostMapping("/upload")
	public ResponseEntity<String> uploadFile(@RequestParam("file")MultipartFile file) {
		// 1. 파일 저장 로직 (로컬 또는 S3)
		// 2. 저장된 파일의 접근 URL 생성
		String fileUrl = fileService.saveFile(file);
		
		return ResponseEntity.ok(fileUrl);
	}
}
