package top.naccl.service;

import org.springframework.web.multipart.MultipartFile;
import top.naccl.model.vo.UploadFileVO;

public interface UploadFileService {
	UploadFileVO uploadImage(MultipartFile file) throws Exception;
}
