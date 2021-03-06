package com.visualkhh.api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visualkhh.api.model.ApiHeader;
import com.visualkhh.api.service.DeviceService;
import com.visualkhh.api.service.UserService;
import com.visualkhh.common.code.MsgCode;
import com.visualkhh.common.config.CommonWebMvcConfigurerAdapter;
import com.visualkhh.common.config.properties.ProjectProperties;
import com.visualkhh.common.exception.ErrorMsgException;
import com.visualkhh.common.filter.RequestLoggingFilter;
import com.visualkhh.common.model.error.Error;
import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.support.OpenSessionInViewFilter;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.validation.BindException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.validation.Valid;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Locale;

@Configuration
@Import({CommonWebMvcConfigurerAdapter.class})
@EnableWebMvc
@EnableConfigurationProperties(ProjectProperties.class)
@EnableScheduling
public class WebMvcConfigurerAdapter extends org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter {

    @Value("${project.properties.header-name}")
    String headerName = null;
    @Autowired ProjectProperties projectProperties;
    @Autowired
    DeviceService deviceService;
    @Autowired
    UserService userService;


   @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver slr = new AcceptHeaderLocaleResolver();
        slr.setDefaultLocale(Locale.KOREA);
//        List localeList = new ArrayList();
//        localeList.add(Locale.KOREA);
//        localeList.add(Locale.CHINA);
//        localeList.add(Locale.US);
//        slr.setSupportedLocales(localeList);
        return slr;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return ApiHeader.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
                String json = webRequest.getHeader(headerName);
                ApiHeader rval = null;
                if(json==null || json.isEmpty()){
                    if(parameter.hasParameterAnnotation(Valid.class) || parameter.hasParameterAnnotation(Validated.class))
                        throw  new ErrorMsgException(MsgCode.E20001, HttpStatus.INTERNAL_SERVER_ERROR);
                    else
                        return new ApiHeader();
                } else {
                    try {
                        rval = new ObjectMapper().readValue(json, ApiHeader.class);
                        if (parameter.hasParameterAnnotation(Valid.class) || parameter.hasParameterAnnotation(Validated.class)) {
                            WebDataBinder binder = binderFactory.createBinder(webRequest, rval, parameter.getParameterName());
                            this.validateIfApplicable(binder, parameter);
                            if (binder.getBindingResult().hasErrors()) {
                                throw new BindException(binder.getBindingResult());
                            }
                        }
                    } catch (BindException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new ErrorMsgException(MsgCode.E20002, HttpStatus.INTERNAL_SERVER_ERROR);
                    }
                }
				rval = userService.headerToUserAndDevMerge(rval);
                return rval;
            }

            protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {
                Annotation[] annotations = methodParam.getParameterAnnotations();
                for (Annotation ann : annotations) {
                    Validated validatedAnn = AnnotationUtils.getAnnotation(ann, Validated.class);
                    if (validatedAnn != null || ann.annotationType().getSimpleName().startsWith("Valid")) {
                        Object hints = (validatedAnn != null ? validatedAnn.value() : AnnotationUtils.getValue(ann));
                        Object[] validationHints = (hints instanceof Object[] ? (Object[]) hints : new Object[]{hints});
                        binder.validate(validationHints);
                        break;
                    }
                }
            }
        });
    }


    //filter
    @Bean
    public FilterRegistrationBean commonsRequestLoggingFilter() {
//        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
//        filter.setIncludeQueryString(true);
//        filter.setIncludePayload(true);
//        filter.setMaxPayloadLength(10000);
//        filter.setIncludeHeaders(false);
//        filter.setAfterMessagePrefix("REQUEST DATA : ");

//        CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
//        filter.setIncludeClientInfo(true);
//        filter.setIncludeQueryString(true);
//        filter.setIncludePayload(true);
//        LogRequestFilter filter = new LogRequestFilter();
        RequestLoggingFilter filter = new RequestLoggingFilter();
//        RequestDumperFilter filter = new RequestDumperFilter();
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(filter);
        registrationBean.setOrder(5);
        return registrationBean;
    }



    //리소스 패스 설정
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/assets/**")	            .addResourceLocations("/assets/");
        registry.addResourceHandler("/static/**")	            .addResourceLocations("/static/");
        registry.addResourceHandler("/resource/**")	            .addResourceLocations("/resource/");
        registry.addResourceHandler("/*.map")	                .addResourceLocations("/");
        registry.addResourceHandler("/*.js")	                .addResourceLocations("/");
    }
//    @Override
//    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
//        configurer.favorPathExtension(false);
//        configurer.ignoreAcceptHeader(true);
//        configurer.defaultContentType(MediaType.APPLICATION_JSON);
//    }


}
