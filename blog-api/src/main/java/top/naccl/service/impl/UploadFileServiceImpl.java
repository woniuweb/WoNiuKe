package top.naccl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import top.naccl.entity.UploadFile;
import top.naccl.exception.BadRequestException;
import top.naccl.exception.PersistenceException;
import top.naccl.mapper.UploadFileMapper;
import top.naccl.model.vo.UploadFileVO;
import top.naccl.service.UploadFileService;
import top.naccl.util.upload.UploadUtils;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class UploadFileServiceImpl implements UploadFileService {
	private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList("mp4", "webm", "ogg");

	@Autowired
	private UploadFileMapper uploadFileMapper;

	@Value("${upload.channel:local}")
	private String storageMode;

	@Value("${upload.file.path}")
	private String uploadFilePath;

	@Transactional(rollbackFor = Exception.class)
	@Override
	public UploadFileVO uploadImage(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("文件不能为空");
		}

		String fileType = resolveFileType(file, "png");
		UploadUtils.ImageResource imageResource = new UploadUtils.ImageResource(file.getBytes(), fileType);
		String url = UploadUtils.upload(imageResource);

		UploadFile uploadFile = buildUploadFile(file, fileType, url);
		saveUploadFile(uploadFile);
		return buildUploadFileVO(uploadFile);
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public UploadFileVO uploadVideo(MultipartFile file) throws Exception {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("文件不能为空");
		}
		if (!"local".equalsIgnoreCase(storageMode)) {
			throw new BadRequestException("首页视频暂只支持本地上传模式");
		}

		String fileType = resolveFileType(file, null);
		if (!ALLOWED_VIDEO_TYPES.contains(fileType)) {
			throw new BadRequestException("仅支持上传 mp4、webm、ogg 格式的视频");
		}

		String fileName = UUID.randomUUID().toString() + "." + fileType;
		Path targetDir = buildVideoDirectoryPath();
		Files.createDirectories(targetDir);

		Path targetFile = targetDir.resolve(fileName);
		try (InputStream inputStream = file.getInputStream()) {
			Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
		}

		String url = buildVideoUrl(fileName);
		UploadFile uploadFile = buildUploadFile(file, fileType, url);
		uploadFile.setFileName(fileName);
		uploadFile.setFilePath(targetDir.toAbsolutePath().toString() + File.separator);
		saveUploadFile(uploadFile);
		return buildUploadFileVO(uploadFile);
	}

	@Override
	public List<UploadFileVO> getVideoList() {
		List<UploadFile> uploadFiles = uploadFileMapper.getListByFileTypes(ALLOWED_VIDEO_TYPES);
		List<UploadFileVO> result = new ArrayList<>();
		for (UploadFile uploadFile : uploadFiles) {
			result.add(buildUploadFileVO(uploadFile));
		}
		return result;
	}

	private void saveUploadFile(UploadFile uploadFile) {
		if (uploadFileMapper.save(uploadFile) != 1) {
			throw new PersistenceException("文件记录保存失败");
		}
	}

	private UploadFile buildUploadFile(MultipartFile file, String fileType, String url) {
		UploadFile uploadFile = new UploadFile();
		uploadFile.setStorageMode(storageMode);
		uploadFile.setFileType(fileType);
		uploadFile.setOriginalName(file.getOriginalFilename());
		uploadFile.setFileName(extractFileNameFromUrl(url));
		uploadFile.setFilePath(extractPathFromUrl(url));
		uploadFile.setUrl(url);
		uploadFile.setFileSize(file.getSize());
		uploadFile.setCreateTime(new Date());
		return uploadFile;
	}

	private UploadFileVO buildUploadFileVO(UploadFile uploadFile) {
		return new UploadFileVO(
			uploadFile.getId(),
			uploadFile.getUrl(),
			uploadFile.getFileName(),
			uploadFile.getOriginalName(),
			uploadFile.getFileType(),
			uploadFile.getFileSize(),
			uploadFile.getStorageMode(),
			uploadFile.getFilePath(),
			uploadFile.getCreateTime()
		);
	}

	private Path buildVideoDirectoryPath() {
		String normalized = uploadFilePath;
		if (!normalized.endsWith("/") && !normalized.endsWith("\\")) {
			normalized += File.separator;
		}
		return new File(normalized + "video").toPath().toAbsolutePath().normalize();
	}

	private String buildVideoUrl(String fileName) {
		return "/video/" + fileName;
	}

	private String resolveFileType(MultipartFile file, String defaultType) {
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
			subtype = getExtension(file.getOriginalFilename());
		}
		if (subtype == null || subtype.trim().isEmpty()) {
			subtype = defaultType;
		}
		if (subtype == null || subtype.trim().isEmpty()) {
			throw new BadRequestException("无法识别文件类型");
		}
		return subtype.toLowerCase(Locale.ENGLISH);
	}

	private String getExtension(String fileName) {
		if (fileName == null) {
			return null;
		}
		int idx = fileName.lastIndexOf('.');
		if (idx < 0 || idx == fileName.length() - 1) {
			return null;
		}
		return fileName.substring(idx + 1);
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
