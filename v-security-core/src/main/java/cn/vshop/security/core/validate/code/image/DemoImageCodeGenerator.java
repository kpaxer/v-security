package cn.vshop.security.core.validate.code.image;

import cn.vshop.security.core.properties.SecurityProperties;
import cn.vshop.security.core.validate.code.ValidateCodeGenerator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author alan smith
 * @version 1.0
 * @date 2020/4/5 19:44
 */
// 名字必须为 imageCodeGenerator。
// 当然，也可以自定义覆盖规则
@Component("imageCodeGenerator")
public class DemoImageCodeGenerator implements ValidateCodeGenerator, InitializingBean {

    @Autowired
    private SecurityProperties securityProperties;

    private ImageCodeGenerator imageCodeGenerator;

    @Override
    public void afterPropertiesSet() throws Exception {
        imageCodeGenerator = new ImageCodeGenerator();
        imageCodeGenerator.setSecurityProperties(securityProperties);
    }

    @Override
    public ImageCode generate(ServletWebRequest request) {
        System.out.println("更高级的图形验证码生成代码");
        return imageCodeGenerator.generate(request);
    }
}
