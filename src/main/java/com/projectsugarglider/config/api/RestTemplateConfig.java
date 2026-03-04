package com.projectsugarglider.config.api;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

@Configuration
public class RestTemplateConfig {


    /**
     * RestTemplate 설정 코드입니다.
     * 인코딩은 GenericExternalApiService.buildUrl 에서 핸들링 하기 때문에
     * EncodingMode.NONE으로 자동 인코딩 기능을 사용하지 않습니다.
     * 
     * 
     * 
     * @param builder
     * @return
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        DefaultUriBuilderFactory factory = new DefaultUriBuilderFactory();
        factory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        RestTemplate restTemplate = builder
            .uriTemplateHandler(factory)
            .build();

        /**
         * 사용하는 타입을 지정해줍니다.
         * 1. application/json
         * 2. text/plain
         * 3. text/plain charset=UTF-8
         * 
         * 데이터를 받아올때 문자열 인코딩으로 오류가 발생하여
         * 명시하기 위해 작성된 코드입니다.
         */
        for (HttpMessageConverter<?> converter : restTemplate.getMessageConverters()) {
            if (converter instanceof MappingJackson2HttpMessageConverter jacksonConv) {
                List<MediaType> types = Arrays.asList(
                    MediaType.APPLICATION_JSON,                                         //application/json
                    MediaType.TEXT_PLAIN,                                               // text/plain
                    new MediaType("text", "plain", StandardCharsets.UTF_8) // text/plain;charset=UTF-8
                );
                jacksonConv.setSupportedMediaTypes(types);
            }
        }

        return restTemplate;
    }
}
