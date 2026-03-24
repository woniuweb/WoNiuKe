package top.naccl.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
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
		return siteSetting == null ? "" : normalizeVideoUrl(siteSetting.getValue());
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
		String normalizedVideoUrl = normalizeVideoUrl(videoUrl);
		SiteSetting current = siteSettingMapper.getSiteSettingByNameEn(SiteSettingConstants.VIDEO_URL);
		if (current == null) {
			SiteSetting siteSetting = new SiteSetting();
			siteSetting.setNameEn(SiteSettingConstants.VIDEO_URL);
			siteSetting.setNameZh("首页视频");
			siteSetting.setType(1);
			siteSetting.setValue(normalizedVideoUrl);
			saveOneSiteSetting(siteSetting);
		} else {
			SiteSetting siteSetting = new SiteSetting();
			siteSetting.setNameEn(SiteSettingConstants.VIDEO_URL);
			siteSetting.setValue(normalizedVideoUrl);
			if (siteSettingMapper.updateSiteSettingValueByNameEn(siteSetting) < 1) {
				throw new PersistenceException("配置修改失败");
			}
		}
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
			siteInfo.put(siteSetting.getNameEn(), normalizeVideoUrl(siteSetting.getValue()));
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

	private String normalizeVideoUrl(String videoUrl) {
		if (videoUrl == null || videoUrl.trim().isEmpty()) {
			return "";
		}
		String trimmed = videoUrl.trim();
		int index = trimmed.indexOf("/video/");
		if (index > -1) {
			return trimmed.substring(index);
		}
		return trimmed;
	}

	private void deleteSiteInfoRedisCache() {
		redisService.deleteCacheByKey(RedisKeyConstants.SITE_INFO_MAP);
	}
}
