package top.naccl.service.impl;

import lombok.extern.slf4j.Slf4j;
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
import top.naccl.service.SiteSettingService;
import top.naccl.service.UploadFileService;
import top.naccl.util.upload.UploadUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class UploadFileServiceImpl implements UploadFileService {
	private static final List<String> ALLOWED_VIDEO_TYPES = Arrays.asList("mp4", "webm", "ogg");

	@Autowired
	private UploadFileMapper uploadFileMapper;

	@Autowired
	private SiteSettingService siteSettingService;

	@Value("${upload.channel:local}")
	private String storageMode;

	@Value("${upload.file.path}")
	private String uploadFilePath;

	@Value("${video.ffmpeg.command:ffmpeg}")
	private String ffmpegCommand;

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
		return buildUploadFileVO(uploadFile, "");
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

		String posterUrl = tryGeneratePoster(targetFile, fileName);
		return buildUploadFileVO(uploadFile, posterUrl);
	}

	@Override
	public List<UploadFileVO> getVideoList() {
		List<UploadFile> uploadFiles = uploadFileMapper.getListByFileTypes(ALLOWED_VIDEO_TYPES);
		List<UploadFileVO> result = new ArrayList<>();
		for (UploadFile uploadFile : uploadFiles) {
			result.add(buildUploadFileVO(uploadFile, resolvePosterUrl(uploadFile.getFileName())));
		}
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void deleteVideo(Long id) {
		if (id == null) {
			throw new BadRequestException("视频记录不存在");
		}
		UploadFile uploadFile = uploadFileMapper.getById(id);
		if (uploadFile == null) {
			throw new BadRequestException("视频记录不存在");
		}
		if (!ALLOWED_VIDEO_TYPES.contains(normalizeFileType(uploadFile.getFileType()))) {
			throw new BadRequestException("目标记录不是视频");
		}
		if (!"local".equalsIgnoreCase(uploadFile.getStorageMode())) {
			throw new BadRequestException("当前仅支持删除本地上传视频");
		}

		String currentHomeVideoUrl = siteSettingService.getHomeVideoUrl();
		if (uploadFile.getUrl() != null && uploadFile.getUrl().equals(currentHomeVideoUrl)) {
			throw new BadRequestException("请先切换首页视频后再删除");
		}

		deleteFileIfExists(resolveVideoFile(uploadFile));
		deleteFileIfExists(resolvePosterFile(uploadFile.getFileName()));

		if (uploadFileMapper.deleteById(id) != 1) {
			throw new PersistenceException("视频记录删除失败");
		}
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

	private UploadFileVO buildUploadFileVO(UploadFile uploadFile, String posterUrl) {
		return new UploadFileVO(
			uploadFile.getId(),
			uploadFile.getUrl(),
			uploadFile.getFileName(),
			uploadFile.getOriginalName(),
			uploadFile.getFileType(),
			uploadFile.getFileSize(),
			uploadFile.getStorageMode(),
			uploadFile.getFilePath(),
			posterUrl,
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

	private Path buildPosterDirectoryPath() {
		return buildVideoDirectoryPath().resolve("poster");
	}

	private String buildVideoUrl(String fileName) {
		return "/video/" + fileName;
	}

	private String buildPosterUrl(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			return "";
		}
		int idx = fileName.lastIndexOf('.');
		String baseName = idx > -1 ? fileName.substring(0, idx) : fileName;
		return "/video/poster/" + baseName + ".jpg";
	}

	private String resolvePosterUrl(String fileName) {
		Path posterFile = resolvePosterFile(fileName);
		if (posterFile == null || Files.notExists(posterFile)) {
			return "";
		}
		return buildPosterUrl(fileName);
	}

	private String tryGeneratePoster(Path videoFile, String fileName) {
		Path posterDir = buildPosterDirectoryPath();
		try {
			Files.createDirectories(posterDir);
		} catch (IOException e) {
			log.warn("Failed to create poster directory for {}", fileName, e);
			return "";
		}

		Path posterFile = resolvePosterFile(fileName);
		if (posterFile == null) {
			return "";
		}

		ProcessBuilder processBuilder = new ProcessBuilder(
			ffmpegCommand,
			"-y",
			"-i", videoFile.toAbsolutePath().toString(),
			"-frames:v", "1",
			"-q:v", "2",
			posterFile.toAbsolutePath().toString()
		);
		processBuilder.redirectErrorStream(true);

		try {
			Process process = processBuilder.start();
			String output = readProcessOutput(process.getInputStream());
			int exitCode = process.waitFor();
			if (exitCode != 0 || Files.notExists(posterFile)) {
				log.warn("Generate video poster failed for {}, ffmpegCommand={}, exitCode={}, output={}", fileName, ffmpegCommand, exitCode, output);
				return "";
			}
			return buildPosterUrl(fileName);
		} catch (IOException e) {
			log.warn("ffmpeg command is unavailable: {}", ffmpegCommand, e);
			return "";
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Generate video poster interrupted for {}", fileName, e);
			return "";
		}
	}

	private String readProcessOutput(InputStream inputStream) throws IOException {
		StringBuilder builder = new StringBuilder();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line).append('\n');
			}
		}
		return builder.toString();
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

	private String normalizeFileType(String fileType) {
		return fileType == null ? "" : fileType.toLowerCase(Locale.ENGLISH);
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

	private Path resolveVideoFile(UploadFile uploadFile) {
		String fileName = uploadFile.getFileName();
		if (fileName == null || fileName.trim().isEmpty()) {
			fileName = extractFileNameFromUrl(uploadFile.getUrl());
		}
		if (fileName == null || fileName.trim().isEmpty()) {
			return null;
		}
		return buildVideoDirectoryPath().resolve(fileName).normalize();
	}

	private Path resolvePosterFile(String fileName) {
		if (fileName == null || fileName.trim().isEmpty()) {
			return null;
		}
		int idx = fileName.lastIndexOf('.');
		String baseName = idx > -1 ? fileName.substring(0, idx) : fileName;
		return buildPosterDirectoryPath().resolve(baseName + ".jpg").normalize();
	}

	private void deleteFileIfExists(Path path) {
		if (path == null) {
			return;
		}
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			throw new PersistenceException("文件删除失败");
		}
	}
}
