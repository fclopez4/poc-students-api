package com.scotiabank.studentsapi.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.reactive.config.PathMatchConfigurer;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebConfiguration implements WebFluxConfigurer {

	/*@Override
	public void configureApiVersioning(ApiVersionConfigurer configurer) {
		configurer.usePathSegment(1).setDefaultVersion("v1");
	}*/

	@Override
	public void configurePathMatching(PathMatchConfigurer configurer) {
		configurer.addPathPrefix(
				"/api", HandlerTypePredicate.forAnnotation(RestController.class));
	}

}