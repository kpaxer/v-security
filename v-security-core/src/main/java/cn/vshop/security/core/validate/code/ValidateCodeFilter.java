package cn.vshop.security.core.validate.code;

import cn.vshop.security.core.properties.SecurityProperties;
import cn.vshop.security.core.validate.code.image.ImageCode;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.social.connect.web.HttpSessionSessionStrategy;
import org.springframework.social.connect.web.SessionStrategy;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.mysql.jdbc.interceptors.SessionAssociationInterceptor.getSessionKey;

/**
 * 图形校验码的过滤器
 *
 * @author alan smith
 * @version 1.0
 * @date 2020/4/5 11:39
 */
@Slf4j
@Setter
public class ValidateCodeFilter
        // SpringMVC 提供的工具类，能确保此Filter每次请求只被调用一次
        extends OncePerRequestFilter
        // 在其他参数都加载完毕时，组装URLs的值
        implements InitializingBean {

    /**
     * 要拦截的url
     */
    private Set<String> urls = new HashSet<>();

    private SecurityProperties securityProperties;

    /**
     * spring的工具类，用来匹配Ant风格路径，如：“/user/*”
     */
    private AntPathMatcher pathMatcher = new AntPathMatcher();

    /**
     * 操作session的工具类
     */
    private SessionStrategy sessionStrategy = new HttpSessionSessionStrategy();

    // 我们前面自定义的验证失败处理器
    private AuthenticationFailureHandler authenticationFailureHandler;

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
        String[] configUrls = securityProperties.getCode().getImage().getUrls();
        for (String url : configUrls) {
            urls.add(url);
        }
        urls.add("/authentication/form");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 循环判断是否执行过滤
        boolean action = false;
        for (String url : urls) {
            if (pathMatcher.match(url, request.getRequestURI())) {
                action = true;
                break;
            }
        }

        // 如果请求的是执行登录URL
        if (action) {
            // 尝试校验
            try {
                // 封装成spring的request，方便后续操作
                validate(new ServletWebRequest(request));
            } catch (ValidateCodeException e) {
                // 捕获到自定义的验证码校验异常
                // 用我们之前自定义的错误处理器，进行校验失败的处理
                authenticationFailureHandler.onAuthenticationFailure(request, response, e);
                return;
            }
        }
        // 校验通过or不是登录请求
        filterChain.doFilter(request, response);
    }

    /**
     * 校验的逻辑
     *
     * @param request
     */
    private void validate(ServletWebRequest request) throws ServletRequestBindingException {

        // 从session中获取封装好的ImageCode
        ImageCode codeInSession = (ImageCode) sessionStrategy.getAttribute(request, getCodeSessionKey());
        // 从request中获取请求参数imageCode
        String codeInRequest = ServletRequestUtils.getStringParameter(request.getRequest(), "imageCode");
        if (StringUtils.isBlank(codeInRequest)) {
            throw new ValidateCodeException("验证码的值不能为空");
        }
        if (codeInSession == null) {
            throw new ValidateCodeException("验证码不存在");
        }
        if (codeInSession.isExpired()) {
            // 如果过期了，就移除验证码
            sessionStrategy.removeAttribute(request, getCodeSessionKey());
            // 然后再抛异常
            throw new ValidateCodeException("验证码已过期");
        }
        if (!StringUtils.equals(codeInSession.getCode(), codeInRequest)) {
            throw new ValidateCodeException("验证码不匹配");
        }
        sessionStrategy.removeAttribute(request, getCodeSessionKey());
    }

    /**
     * 获取校验码存储在session中的key
     * @return
     */
    private String getCodeSessionKey() {
        return ValidateCodeProcessor.SESSION_KEY_PREFIX + "IMAGE";
    }

}
