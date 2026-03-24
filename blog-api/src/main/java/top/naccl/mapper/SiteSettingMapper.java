package top.naccl.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import top.naccl.entity.SiteSetting;

import java.util.List;

/**
 * @Description: 站点设置持久层接口
 * @Author: Naccl
 * @Date: 2020-08-03
 */
@Mapper
@Repository
public interface SiteSettingMapper {
	List<SiteSetting> getList();

	SiteSetting getSiteSettingByNameEn(String nameEn);

	List<SiteSetting> getFriendInfo();

	String getWebTitleSuffix();

	int updateSiteSetting(SiteSetting siteSetting);

	int deleteSiteSettingById(Integer id);

	int saveSiteSetting(SiteSetting siteSetting);

	int updateSiteSettingValueByNameEn(SiteSetting siteSetting);

	int updateFriendInfoContent(String content);

	int updateFriendInfoCommentEnabled(Boolean commentEnabled);
}
