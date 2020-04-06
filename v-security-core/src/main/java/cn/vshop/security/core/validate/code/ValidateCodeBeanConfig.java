package cn.vshop.security.core.validate.code;

import cn.vshop.security.core.validate.code.email.DefaultEmailCodeSender;
import cn.vshop.security.core.validate.code.email.EmailCodeGenerator;
import cn.vshop.security.core.validate.code.email.EmailCodeSender;
import cn.vshop.security.core.validate.code.image.ImageCodeGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 校验码生成逻辑的配置类
 *
 * @author alan smith
 * @version 1.0
 * @date 2020/4/5 17:39
 */
@Configuration
public class ValidateCodeBeanConfig {
    /**
     * 如果用户没有注入名字为imageCodeGenerator的bean，那么就使用默认的
     *
     * @return 默认的 ValidateCodeGenerator 实现
     */
    @Bean("imageCodeGenerator")
    @ConditionalOnMissingBean(name = "imageCodeGenerator")
    public ValidateCodeGenerator imageCodeGenerator() {
        return new ImageCodeGenerator();
    }

    @Bean("emailCodeGenerator")
    @ConditionalOnMissingBean(name = "emailCodeGenerator")
    public ValidateCodeGenerator emailCodeGenerator() {
        return new EmailCodeGenerator();
    }

    @Bean("emailCodeSender")
    @ConditionalOnMissingBean(name = "emailCodeSender")
    public EmailCodeSender emailCodeSender() {
        return new DefaultEmailCodeSender();
    }
}
