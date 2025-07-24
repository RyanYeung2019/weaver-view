package org.weaver.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 *
 * @author <a href="mailto:30808333@qq.com">Ryan Yeung</a>
 * 
 */

@Configuration
@ComponentScan(basePackages = "org.weaver")
public class LoadConfig implements ApplicationListener<ContextRefreshedEvent> {

	@Autowired
	ViewDefine viewDefine;
	@Autowired
	LangDefine langDefine;

	private static final Logger log = LoggerFactory.getLogger(LoadConfig.class);

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		try {
			langDefine.loadLang();
			viewDefine.loadView();
			log.debug("load finished!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}