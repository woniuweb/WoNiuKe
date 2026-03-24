package top.naccl.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.naccl.annotation.OperationLogger;
import top.naccl.entity.SiteSetting;
import top.naccl.model.vo.Result;
import top.naccl.service.SiteSettingService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 站点设置后台管理
 * @Author: Naccl
 * @Date: 2020-08-09
 */
@RestController
@RequestMapping("/admin")
public class SiteSettingAdminController {
	@Autowired
	SiteSettingService siteSettingService;

	@GetMapping("/siteSettings")
	public Result siteSettings() {
		Map<String, List<SiteSetting>> typeMap = siteSettingService.getList();
		return Result.ok("请求成功", typeMap);
	}

	@OperationLogger("更新站点设置信息")
	@PostMapping("/siteSettings")
	public Result updateAll(@RequestBody Map<String, Object> map) {
		List<LinkedHashMap> siteSettings = (List<LinkedHashMap>) map.get("settings");
		List<Integer> deleteIds = (List<Integer>) map.get("deleteIds");
		siteSettingService.updateSiteSetting(siteSettings, deleteIds);
		return Result.ok("更新成功");
	}

	@GetMapping("/siteSettings/homeVideo")
	public Result getHomeVideo() {
		return Result.ok("请求成功", siteSettingService.getHomeVideoUrl());
	}

	@OperationLogger("更新首页视频")
	@PostMapping("/siteSettings/homeVideo")
	public Result updateHomeVideo(@RequestBody Map<String, String> map) {
		String videoUrl = map.get("videoUrl");
		siteSettingService.updateHomeVideoUrl(videoUrl == null ? "" : videoUrl);
		return Result.ok("更新成功", videoUrl);
	}

	@GetMapping("/webTitleSuffix")
	public Result getWebTitleSuffix() {
		return Result.ok("请求成功", siteSettingService.getWebTitleSuffix());
	}
}
