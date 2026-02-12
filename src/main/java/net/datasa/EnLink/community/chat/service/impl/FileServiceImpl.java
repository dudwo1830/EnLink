package net.datasa.EnLink.community.chat.service.impl;

import net.datasa.EnLink.community.chat.service.FileService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
	
	// 💡 application.properties에서 경로를 가져옴
	@Value("${file.upload.path}")
	private String rootPath;
	
	// 💡 채팅 전용 하위 폴더명
	private final String CHAT_SUB_DIR = "chat/";
	
	@Override
	public String saveFile(MultipartFile file) {
		if (file.isEmpty()) return null;
		
		try {
			// 💡 실제 저장 경로: C:/enrink_storage/chat/
			String fullPath = rootPath + CHAT_SUB_DIR;
			
			String originalFilename = file.getOriginalFilename();
			String storeFilename = UUID.randomUUID() + "." + extractExt(originalFilename);
			
			// 폴더 생성
			File folder = new File(fullPath);
			if (!folder.exists()) folder.mkdirs();
			
			// 파일 저장
			file.transferTo(new File(fullPath + storeFilename));
			
			// URL 반환 (브라우저는 /chatImg/로 요청)
			return "/chatImg/" + storeFilename;
		} catch (IOException e) {
			throw new RuntimeException("파일 저장 실패", e);
		}
	}
	
	private String extractExt(String originalFilename) {
		int pos = originalFilename.lastIndexOf(".");
		return originalFilename.substring(pos + 1);
	}
}
