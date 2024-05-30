package byzzbench.simulator.service;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@org.springframework.context.annotation.Configuration
public class MyObjectMapper {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = JsonMapper.builder()
                .addModule(new ParameterNamesModule())
                .addModule(new Jdk8Module())
                .addModule(new JavaTimeModule())
                .build();
        StdTypeResolverBuilder typer = new ObjectMapper.DefaultTypeResolverBuilder(ObjectMapper.DefaultTyping.NON_FINAL) {
            {
                init(JsonTypeInfo.Id.CLASS, null);
                inclusion(JsonTypeInfo.As.PROPERTY);
                typeProperty("__className__");
            }

            @Override
            public boolean useForType(JavaType t) {
                //System.out.println(t.getRawClass().getName());
                return !t.isContainerType()
                        && !t.isPrimitive()
                        && !t.isArrayType()
                        && !t.isEnumType()
                        && !t.isMapLikeType()
                        && !t.isCollectionLikeType()
                        && !t.isEnumImplType()
                        && !t.isJavaLangObject()
                        && super.useForType(t);
            }
        };
        mapper.setDefaultTyping(typer);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, false);
        /*
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));*/
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        mapper.setVisibility(PropertyAccessor.CREATOR, JsonAutoDetect.Visibility.NONE);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return mapper;
    }
}
