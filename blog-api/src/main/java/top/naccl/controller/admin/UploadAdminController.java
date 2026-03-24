package top.naccl.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.naccl.model.vo.Result;
import top.naccl.model.vo.UploadFileVO;
import top.naccl.service.UploadFileService;

/**
 * @Description: 后台文件上传
 */
@RestController
@RequestMapping("/admin")
public class UploadAdminController {
	@Autowired
	private UploadFileService uploadFileService;

	@PostMapping(value = "/upload/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result uploadImage(@RequestParam("file") MultipartFile file) throws Exception {
		UploadFileVO vo = uploadFileService.uploadImage(file);
		return Result.ok("上传成功", vo);
	}

	@PostMapping(value = "/upload/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public Result uploadVideo(@RequestParam("file") MultipartFile file) throws Exception {
		UploadFileVO vo = uploadFileService.uploadVideo(file);
		return Result.ok("上传成功", vo);
	}

	@GetMapping("/upload/videos")
	public Result getVideoList() {
		return Result.ok("请求成功", uploadFileService.getVideoList());
	}
}
