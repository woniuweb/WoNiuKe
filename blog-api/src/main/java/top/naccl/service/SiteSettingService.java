package top.naccl.service;

import top.naccl.entity.SiteSetting;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface SiteSettingService {
	Map<String, List<SiteSetting>> getList();

	Map<String, Object> getSiteInfo();

	String getWebTitleSuffix();

	String getHomeVideoUrl();

	void updateHomeVideoUrl(String videoUrl);

	void updateSiteSetting(List<LinkedHashMap> siteSettings, List<Integer> deleteIds);
}
