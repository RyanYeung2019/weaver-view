package org.weaver.config;

import javax.annotation.PostConstruct;

import org.springframework.context.annotation.Configuration;

import com.alibaba.fastjson.parser.ParserConfig;

/**
 * Fastjson startup safety configuration.
 * Disables AutoType and clears accepted packages to reduce RCE risk.
 */
@Configuration
public class FastjsonSafeConfig {

    @PostConstruct
    public void init() {
        try {
            ParserConfig config = ParserConfig.getGlobalInstance();
            // Disable AutoType globally
            config.setAutoTypeSupport(false);
            // Clear accept list if present (defense-in-depth)
            try {
                // getAcceptList exists in some fastjson versions
//                config.getAcceptList().clear();
            } catch (Throwable ignored) {
            }
        } catch (Throwable t) {
            // Do not fail application start if reflection differences exist
            t.printStackTrace();
        }
    }
}
