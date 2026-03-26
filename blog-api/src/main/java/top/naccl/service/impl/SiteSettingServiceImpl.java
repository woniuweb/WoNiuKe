package top.naccl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import top.naccl.constant.RedisKeyConstants;
import top.naccl.constant.SiteSettingConstants;
import top.naccl.entity.SiteSetting;
import top.naccl.exception.PersistenceException;
import top.naccl.mapper.SiteSettingMapper;
import top.naccl.model.vo.Badge;
import top.naccl.model.vo.Copyright;
import top.naccl.model.vo.Favorite;
import top.naccl.model.vo.Introduction;
import top.naccl.service.RedisService;
import top.naccl.service.SiteSettingService;
import top.naccl.util.JacksonUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SiteSettingServiceImpl implements SiteSettingService {
	private static final Pattern PATTERN = Pattern.compile("\"(.*?)\"");

	@Autowired
	private SiteSettingMapper siteSettingMapper;

	@Autowired
	private RedisService redisService;

	@Value("${upload.file.path}")
	private String uploadFilePath;

	@Override
	public Map<String, List<SiteSetting>> getList() {
		List<SiteSetting> siteSettings = siteSettingMapper.getList();
		List<SiteSetting> type1 = new ArrayList<>();
		List<SiteSetting> type2 = new ArrayList<>();
		List<SiteSetting> type3 = new ArrayList<>();
		for (SiteSetting siteSetting : siteSettings) {
			switch (siteSetting.getType()) {
				case 1:
					type1.add(siteSetting);
					break;
				case 2:
					type2.add(siteSetting);
					break;
				case 3:
					type3.add(siteSetting);
					break;
				default:
					break;
			}
		}
		Map<String, List<SiteSetting>> map = new HashMap<>(8);
		map.put("type1", type1);
		map.put("type2", type2);
		map.put("type3", type3);
		return map;
	}

	@Override
	public Map<String, Object> getSiteInfo() {
		String redisKey = RedisKeyConstants.SITE_INFO_MAP;
		Map<String, Object> siteInfoMapFromRedis = redisService.getMapByValue(redisKey);
		if (siteInfoMapFromRedis != null) {
			return siteInfoMapFromRedis;
		}

		List<SiteSetting> siteSettings = siteSettingMapper.getList();
		Map<String, Object> siteInfo = new HashMap<>(8);
		List<Badge> badges = new ArrayList<>();
		Introduction introduction = new Introduction();
		List<Favorite> favorites = new ArrayList<>();
		List<String> rollTexts = new ArrayList<>();

		for (SiteSetting siteSetting : siteSettings) {
			switch (siteSetting.getType()) {
				case 1:
					handleType1SiteSetting(siteInfo, siteSetting);
					break;
				case 2:
					handleType2SiteSetting(introduction, favorites, rollTexts, siteSetting);
					break;
				case 3:
					badges.add(JacksonUtils.readValue(siteSetting.getValue(), Badge.class));
					break;
				default:
					break;
			}
		}

		introduction.setFavorites(favorites);
		introduction.setRollText(rollTexts);
		Map<String, Object> map = new HashMap<>(8);
		map.put("introduction", introduction);
		map.put("siteInfo", siteInfo);
		map.put("badges", badges);
		redisService.saveMapToValue(redisKey, map);
		return map;
	}

	@Override
	public String getWebTitleSuffix() {
		return siteSettingMapper.getWebTitleSuffix();
	}

	@Override
	public String getHomeVideoUrl() {
		SiteSetting siteSetting = siteSettingMapper.getSiteSettingByNameEn(SiteSettingConstants.VIDEO_URL);
		return siteSetting == null ? "" : normalizeMediaUrl(siteSetting.getValue());
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateSiteSetting(List<LinkedHashMap> siteSettings, List<Integer> deleteIds) {
		for (Integer id : deleteIds) {
			deleteOneSiteSettingById(id);
		}
		for (LinkedHashMap siteSettingMap : siteSettings) {
			SiteSetting siteSetting = JacksonUtils.convertValue(siteSettingMap, SiteSetting.class);
			if (siteSetting.getId() != null) {
				updateOneSiteSetting(siteSetting);
			} else {
				saveOneSiteSetting(siteSetting);
			}
		}
		deleteSiteInfoRedisCache();
	}

	@Transactional(rollbackFor = Exception.class)
	@Override
	public void updateHomeVideoUrl(String videoUrl) {
		String normalizedVideoUrl = normalizeMediaUrl(videoUrl);
		String posterUrl = resolvePosterUrl(normalizedVideoUrl);
		saveOrUpdateSingleType1Setting(SiteSettingConstants.VIDEO_URL, "首页视频", normalizedVideoUrl);
		saveOrUpdateSingleType1Setting(SiteSettingConstants.VIDEO_POSTER, "首页视频封面", posterUrl);
		deleteSiteInfoRedisCache();
	}

	public void saveOneSiteSetting(SiteSetting siteSetting) {
		if (siteSettingMapper.saveSiteSetting(siteSetting) != 1) {
			throw new PersistenceException("配置添加失败");
		}
	}

	public void updateOneSiteSetting(SiteSetting siteSetting) {
		if (siteSettingMapper.updateSiteSetting(siteSetting) != 1) {
			throw new PersistenceException("配置修改失败");
		}
	}

	public void deleteOneSiteSettingById(Integer id) {
		if (siteSettingMapper.deleteSiteSettingById(id) != 1) {
			throw new PersistenceException("配置删除失败");
		}
	}

	private void handleType1SiteSetting(Map<String, Object> siteInfo, SiteSetting siteSetting) {
		if (SiteSettingConstants.COPYRIGHT.equals(siteSetting.getNameEn())) {
			Copyright copyright = JacksonUtils.readValue(siteSetting.getValue(), Copyright.class);
			siteInfo.put(siteSetting.getNameEn(), copyright);
			return;
		}
		if (SiteSettingConstants.VIDEO_URL.equals(siteSetting.getNameEn())) {
			siteInfo.put(siteSetting.getNameEn(), normalizeMediaUrl(siteSetting.getValue()));
			return;
		}
		if (SiteSettingConstants.VIDEO_POSTER.equals(siteSetting.getNameEn())) {
			siteInfo.put(siteSetting.getNameEn(), resolvePosterUrl(siteSetting.getValue()));
			return;
		}
		siteInfo.put(siteSetting.getNameEn(), siteSetting.getValue());
	}

	private void handleType2SiteSetting(Introduction introduction, List<Favorite> favorites, List<String> rollTexts, SiteSetting siteSetting) {
		switch (siteSetting.getNameEn()) {
			case SiteSettingConstants.AVATAR:
				introduction.setAvatar(siteSetting.getValue());
				break;
			case SiteSettingConstants.NAME:
				introduction.setName(siteSetting.getValue());
				break;
			case SiteSettingConstants.GITHUB:
				introduction.setGithub(siteSetting.getValue());
				break;
			case SiteSettingConstants.TELEGRAM:
				introduction.setTelegram(siteSetting.getValue());
				break;
			case SiteSettingConstants.QQ:
				introduction.setQq(siteSetting.getValue());
				break;
			case SiteSettingConstants.BILIBILI:
				introduction.setBilibili(siteSetting.getValue());
				break;
			case SiteSettingConstants.NETEASE:
				introduction.setNetease(siteSetting.getValue());
				break;
			case SiteSettingConstants.EMAIL:
				introduction.setEmail(siteSetting.getValue());
				break;
			case SiteSettingConstants.FAVORITE:
				favorites.add(JacksonUtils.readValue(siteSetting.getValue(), Favorite.class));
				break;
			case SiteSettingConstants.ROLL_TEXT:
				Matcher matcher = PATTERN.matcher(siteSetting.getValue());
				while (matcher.find()) {
					rollTexts.add(matcher.group(1));
				}
				break;
			default:
				break;
		}
	}

	private void saveOrUpdateSingleType1Setting(String nameEn, String nameZh, String value) {
		SiteSetting current = siteSettingMapper.getSiteSettingByNameEn(nameEn);
		if (current == null) {
			SiteSetting siteSetting = new SiteSetting();
			siteSetting.setNameEn(nameEn);
			siteSetting.setNameZh(nameZh);
			siteSetting.setType(1);
			siteSetting.setValue(value);
			saveOneSiteSetting(siteSetting);
			return;
		}
		SiteSetting siteSetting = new SiteSetting();
		siteSetting.setNameEn(nameEn);
		siteSetting.setValue(value);
		if (siteSettingMapper.updateSiteSettingValueByNameEn(siteSetting) < 1) {
			throw new PersistenceException("配置修改失败");
		}
	}

	private String normalizeMediaUrl(String url) {
		if (url == null || url.trim().isEmpty()) {
			return "";
		}
		String trimmed = url.trim();
		int index = trimmed.indexOf("/video/");
		if (index > -1) {
			return trimmed.substring(index);
		}
		return trimmed;
	}

	private String resolvePosterUrl(String candidate) {
		String normalized = normalizeMediaUrl(candidate);
		if (normalized.isEmpty()) {
			return "";
		}
		if (!normalized.startsWith("/video/poster/")) {
			if (!normalized.startsWith("/video/")) {
				return "";
			}
			normalized = derivePosterUrl(normalized);
		}
		Path posterFile = resolvePosterPath(normalized);
		if (posterFile == null || Files.notExists(posterFile)) {
			return "";
		}
		return normalized;
	}

	private String derivePosterUrl(String videoUrl) {
		if (videoUrl == null || videoUrl.trim().isEmpty()) {
			return "";
		}
		String normalized = normalizeMediaUrl(videoUrl);
		if (!normalized.startsWith("/video/")) {
			return "";
		}
		String fileName = normalized.substring("/video/".length());
		int idx = fileName.lastIndexOf('.');
		String baseName = idx > -1 ? fileName.substring(0, idx) : fileName;
		return "/video/poster/" + baseName + ".jpg";
	}

	private Path resolvePosterPath(String posterUrl) {
		if (posterUrl == null || posterUrl.trim().isEmpty()) {
			return null;
		}
		String relativePath = posterUrl.startsWith("/") ? posterUrl.substring(1) : posterUrl;
		String normalized = uploadFilePath;
		if (!normalized.endsWith("/") && !normalized.endsWith("\\")) {
			normalized += File.separator;
		}
		return new File(normalized + relativePath).toPath().toAbsolutePath().normalize();
	}

	private void deleteSiteInfoRedisCache() {
		redisService.deleteCacheByKey(RedisKeyConstants.SITE_INFO_MAP);
	}
}
