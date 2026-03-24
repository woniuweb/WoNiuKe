package top.naccl.service;

import org.springframework.web.multipart.MultipartFile;
import top.naccl.model.vo.UploadFileVO;

import java.util.List;

public interface UploadFileService {
	UploadFileVO uploadImage(MultipartFile file) throws Exception;

	UploadFileVO uploadVideo(MultipartFile file) throws Exception;

	List<UploadFileVO> getVideoList();
}
