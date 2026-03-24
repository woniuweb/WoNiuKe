package top.naccl.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import top.naccl.config.properties.UploadProperties;
import top.naccl.interceptor.AccessLimitInterceptor;

import java.io.File;

/**
 * @Description: 配置CORS跨域支持、拦截器
 * @Author: Naccl
 * @Date: 2020-07-22
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Autowired
	AccessLimitInterceptor accessLimitInterceptor;
	@Autowired
	UploadProperties uploadProperties;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("*")
			.allowedHeaders("*")
			.allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS")
			.maxAge(3600);
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(accessLimitInterceptor);
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(uploadProperties.getAccessPath()).addResourceLocations(uploadProperties.getResourcesLocations());
		registry.addResourceHandler("/video/**").addResourceLocations(buildVideoResourceLocation());
	}

	private String buildVideoResourceLocation() {
		String basePath = uploadProperties.getPath();
		if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
			basePath += File.separator;
		}
		return "file:" + basePath + "video" + File.separator;
	}
}
