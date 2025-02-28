/*
 * Copyright [2020] [MaxKey of copyright http://www.maxkey.top]
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 

package org.maxkey.autoconfigure;

import java.util.List;

import org.maxkey.authn.provider.AbstractAuthenticationProvider;
import org.maxkey.authn.support.basic.BasicEntryPoint;
import org.maxkey.authn.support.httpheader.HttpHeaderEntryPoint;
import org.maxkey.authn.support.kerberos.HttpKerberosEntryPoint;
import org.maxkey.authn.support.kerberos.KerberosService;
import org.maxkey.authn.web.CurrentUserMethodArgumentResolver;
import org.maxkey.authn.web.interceptor.PermissionInterceptor;
import org.maxkey.configuration.ApplicationConfig;
import org.maxkey.web.interceptor.HistorySignOnAppInterceptor;
import org.maxkey.web.interceptor.HistoryLogsInterceptor;
import org.maxkey.web.interceptor.SingleSignOnInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@EnableWebMvc
@AutoConfiguration
public class MaxKeyMvcConfig implements WebMvcConfigurer {
    private static final  Logger _logger = LoggerFactory.getLogger(MaxKeyMvcConfig.class);
   
    @Value("${maxkey.login.basic.enable:false}")
    private boolean basicEnable;
    
    @Value("${maxkey.login.httpheader.enable:false}")
    private boolean httpHeaderEnable;
    
    @Value("${maxkey.login.httpheader.headername:iv-user}")
    private String httpHeaderName;
    
    @Autowired
  	ApplicationConfig applicationConfig;
    
    @Autowired
    AbstractAuthenticationProvider authenticationProvider ;
    
    @Autowired
    KerberosService kerberosService;
    
    @Autowired
    PermissionInterceptor permissionInterceptor;
    
    @Autowired
    HistoryLogsInterceptor historyLogsInterceptor;
    
    
    @Autowired
    SingleSignOnInterceptor singleSignOnInterceptor;
    
    @Autowired
    HistorySignOnAppInterceptor historySignOnAppInterceptor;
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	_logger.debug("add Resource Handlers");
        _logger.debug("add statics");
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
        _logger.debug("add templates");
        registry.addResourceHandler("/templates/**")
                .addResourceLocations("classpath:/templates/");
        
        _logger.debug("add swagger");
        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        _logger.debug("add knife4j");
        registry.addResourceHandler("doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        
        _logger.debug("add Resource Handler finished .");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //addPathPatterns 用于添加拦截规则 ， 先把所有路径都加入拦截， 再一个个排除
        //excludePathPatterns 表示改路径不用拦截
        
        _logger.debug("add Http Kerberos Entry Point");
        registry.addInterceptor(new HttpKerberosEntryPoint(
    			authenticationProvider,kerberosService,applicationConfig,true))
    		.addPathPatterns("/login");
        
        
        if(httpHeaderEnable) {
            registry.addInterceptor(new HttpHeaderEntryPoint(httpHeaderName,httpHeaderEnable))
                    .addPathPatterns("/*");
            _logger.debug("add Http Header Entry Point");
        }
        
        if(basicEnable) {
            registry.addInterceptor(new BasicEntryPoint(basicEnable))
                    .addPathPatterns("/*");
            _logger.debug("add Basic Entry Point");
        }
        
        //for frontend
        registry.addInterceptor(permissionInterceptor)
                .addPathPatterns("/config/**")
                .addPathPatterns("/historys/**")
                .addPathPatterns("/access/session/**")
                .addPathPatterns("/access/session/**/**")
                .addPathPatterns("/appList")
                .addPathPatterns("/appList/**")
                .addPathPatterns("/socialsignon/**")
                .addPathPatterns("/authz/credential/**")
                .addPathPatterns("/authz/oauth/v20/approval_confirm/**")
        		.addPathPatterns("/authz/oauth/v20/authorize/approval/**")
        		.addPathPatterns("/logon/oauth20/bind/**")
        		.addPathPatterns("/logout")
                .addPathPatterns("/logout/**")
                .addPathPatterns("/authz/refused")
                ;
        
        _logger.debug("add Permission Interceptor");
        
        registry.addInterceptor(historyLogsInterceptor)
                .addPathPatterns("/config/changePassword/**")
                ;
        _logger.debug("add historyLogs Interceptor");

        //for Single Sign On
        registry.addInterceptor(singleSignOnInterceptor)
                .addPathPatterns("/authz/basic/*")
                //Form based
                .addPathPatterns("/authz/formbased/*")
                //Token based
                .addPathPatterns("/authz/tokenbased/*")
                //JWT
                .addPathPatterns("/authz/jwt/*")
                //SAML
                .addPathPatterns("/authz/saml20/idpinit/*")
                .addPathPatterns("/authz/saml20/assertion")
                .addPathPatterns("/authz/saml20/assertion/")
                //CAS
                .addPathPatterns("/authz/cas/*")
                .addPathPatterns("/authz/cas/*/*")
                .addPathPatterns("/authz/cas/login")
                .addPathPatterns("/authz/cas/login/")
                .addPathPatterns("/authz/cas/granting/*")
                //cas1.0 validate
                .excludePathPatterns("/authz/cas/validate")
                //cas2.0 Validate
                .excludePathPatterns("/authz/cas/serviceValidate")
                .excludePathPatterns("/authz/cas/proxyValidate")
                .excludePathPatterns("/authz/cas/proxy")
                //cas3.0 Validate
                .excludePathPatterns("/authz/cas/p3/serviceValidate")
                .excludePathPatterns("/authz/cas/p3/proxyValidate")
                .excludePathPatterns("/authz/cas/p3/proxy")
                //rest
                .excludePathPatterns("/authz/cas/v1/tickets")
                .excludePathPatterns("/authz/cas/v1/tickets/*")
                
                //OAuth
                .addPathPatterns("/authz/oauth/v20/authorize")
                .addPathPatterns("/authz/oauth/v20/authorize/*")
                
                //OAuth TENCENT_IOA
                .addPathPatterns("/oauth2/authorize")
                .addPathPatterns("/oauth2/authorize/*")
                
                //online ticket Validate
                .excludePathPatterns("/onlineticket/ticketValidate")
                .excludePathPatterns("/onlineticket/ticketValidate/*")
        ;
        _logger.debug("add Single SignOn Interceptor");
        
        registry.addInterceptor(historySignOnAppInterceptor)
                .addPathPatterns("/authz/basic/*")
                .addPathPatterns("/authz/ltpa/*")
                //Extend api
                .addPathPatterns("/authz/api/*")
                //Form based
                .addPathPatterns("/authz/formbased/*")
                //Token based
                .addPathPatterns("/authz/tokenbased/*")
                //JWT
                .addPathPatterns("/authz/jwt/*")
                //SAML
                .addPathPatterns("/authz/saml20/idpinit/*")
                .addPathPatterns("/authz/saml20/assertion")
                //CAS
                .addPathPatterns("/authz/cas/granting")
                //OAuth
                .addPathPatterns("/authz/oauth/v20/approval_confirm")
        ;
        _logger.debug("add history SignOn App Interceptor");
        

    }
    
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(currentUserMethodArgumentResolver());
    }
    
    @Bean
    public CurrentUserMethodArgumentResolver currentUserMethodArgumentResolver() {
        return new CurrentUserMethodArgumentResolver();
    }
    
}
