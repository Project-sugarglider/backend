package com.projectsugarglider.config.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

@Configuration
public class JacksonXmlConfig {

  /**
   * Json데이터를 Object에 매핑합니다.
   * 이번 프로젝트에서는 Json을 Primary로 우선처리하고, 
   * 예외를 xmlMapper로 처리하게 됩니다.
   * 만약 Mapper에서 문제가 발생할 경우에
   * json/xml Mapper가 재대로 적용되었는지 확인해 주시기 바랍니다.
   * 
   * @param builder 
   * @return
   */
  @Bean
  @Primary
  public ObjectMapper jsonMapper(Jackson2ObjectMapperBuilder builder) {
    return builder
             .createXmlMapper(false)
             .build();
  }
/**
 * Xml데이터를 Object에 매핑합니다.
 * 만약 Json 데이터를 매핑하고 싶다면 ObjectMapper를 사용해 주세요.
 * 
 * @param builder
 * @return
 */

  @Bean
  public XmlMapper xmlMapper(Jackson2ObjectMapperBuilder builder) {
    return builder
             .createXmlMapper(true)
             .build();
  }
}