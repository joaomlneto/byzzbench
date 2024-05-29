package byzzbench.runner.api.old;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import spark.ResponseTransformer;

import java.io.IOException;
import java.io.StringWriter;

public class JsonResponseTransformer implements ResponseTransformer {
    @Override
    public String render(Object data) {
        try {
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
            mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e) {
            System.out.println("Error in JsonResponseTransformer parsing data to JSON: " + e.getMessage());
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }
}
