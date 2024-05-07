package bftbench.runner.api;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder;
import spark.ResponseTransformer;

import java.io.IOException;
import java.io.StringWriter;

public class JsonResponseTransformer implements ResponseTransformer {
    @Override
    public String render(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
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
            throw new RuntimeException("IOException from a StringWriter?");
        }
    }
}
