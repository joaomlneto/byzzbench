package byzzbench.simulator.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for the web server.
 * Mostly used for enabling CORS and configuring the Jackson object mapper.
 *
 * @see <a
 *     href="https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS">CORS</a>
 */
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE");
  }

  @Bean
  @Primary
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = JsonMapper.builder()
                              .addModule(new ParameterNamesModule())
                              .addModule(new Jdk8Module())
                              .addModule(new JavaTimeModule())
                              .build();
    StdTypeResolverBuilder typer = new ObjectMapper.DefaultTypeResolverBuilder(
        ObjectMapper.DefaultTyping.NON_FINAL) {
      @Override
      public boolean useForType(JavaType t) {
        System.out.println("useForType: " + t);
        return true;
        // return !(t.isCollectionLikeType() || t.isMapLikeType()) &&
        // super.useForType(t); return true;
      }
    };
    typer.init(JsonTypeInfo.Id.SIMPLE_NAME, null)
        .inclusion(JsonTypeInfo.As.PROPERTY)
        .typeProperty("tyype");
    mapper.setDefaultTyping(typer);
    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                                       .allowIfBaseType(Object.class)
                                       .build();
    mapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL);
    // mapper.activateDefaultTypingAsProperty(ObjectMapper.DefaultTyping.NON_FINAL,
    // "asdf", "__className__");*/
    mapper.enable(SerializationFeature.INDENT_OUTPUT);
    mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);

    mapper.setVisibility(
        mapper.getSerializationConfig()
            .getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

    mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    mapper.setVisibility(PropertyAccessor.GETTER,
                         JsonAutoDetect.Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.SETTER,
                         JsonAutoDetect.Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.CREATOR,
                         JsonAutoDetect.Visibility.NONE);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    return mapper;
  } // */
}
