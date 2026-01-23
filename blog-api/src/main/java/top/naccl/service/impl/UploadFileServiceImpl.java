package top.naccl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.naccl.entity.UploadFile;
import top.naccl.exception.PersistenceException;
import top.naccl.mapper.UploadFileMapper;
import top.naccl.model.vo.UploadFileVO;
import top.naccl.util.upload.UploadUtils;

import java.io.IOException;
import java.util.Date;

@Service
public class UploadFileServiceImpl implements top.naccl.service.UploadFileService {
	@Autowired
	UploadFileMapper uploadFileMapper;

	@Value("${upload.channel:local}")
	private String storageMode;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public UploadFileVO uploadImage(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("文件不能为空");
		}

		String fileType = resolveFileType(file);
		UploadUtils.ImageResource imageResource = new UploadUtils.ImageResource(file.getBytes(), fileType);
		String url = UploadUtils.upload(imageResource);

		UploadFile uploadFile = new UploadFile();
		uploadFile.setStorageMode(storageMode);
		uploadFile.setFileType(fileType);
		uploadFile.setOriginalName(file.getOriginalFilename());
		uploadFile.setFileName(extractFileNameFromUrl(url));
		uploadFile.setFilePath(extractPathFromUrl(url));
		uploadFile.setUrl(url);
		uploadFile.setFileSize(file.getSize());
		uploadFile.setCreateTime(new Date());

		if (uploadFileMapper.save(uploadFile) != 1) {
			throw new PersistenceException("文件记录保存失败");
		}

		return new UploadFileVO(
			uploadFile.getId(),
			uploadFile.getUrl(),
			uploadFile.getFileName(),
			uploadFile.getOriginalName(),
			uploadFile.getFileType(),
			uploadFile.getFileSize(),
			uploadFile.getStorageMode(),
			uploadFile.getFilePath()
		);
	}

	private String resolveFileType(MultipartFile file) {
		String contentType = file.getContentType();
		String subtype = null;
		if (contentType != null && contentType.contains("/")) {
			subtype = contentType.substring(contentType.indexOf('/') + 1);
			int idx = subtype.indexOf(';');
			if (idx > -1) {
				subtype = subtype.substring(0, idx);
			}
		}
		if (subtype == null || subtype.trim().isEmpty()) {
			subtype = "png";
		}
		return subtype;
	}

	private String extractFileNameFromUrl(String url) {
		if (url == null) {
			return null;
		}
		int idx = url.lastIndexOf('/');
		return idx > -1 ? url.substring(idx + 1) : url;
	}

	private String extractPathFromUrl(String url) {
		if (url == null) {
			return null;
		}
		int idx = url.lastIndexOf('/');
		return idx > -1 ? url.substring(0, idx + 1) : null;
	}
}
