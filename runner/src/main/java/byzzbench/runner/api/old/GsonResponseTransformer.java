package byzzbench.runner.api.old;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import spark.ResponseTransformer;

import java.io.IOException;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

public class GsonResponseTransformer implements ResponseTransformer {
    private final Gson gson;

    public GsonResponseTransformer() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapterFactory(new ClassNameTypeAdapterFactory())
                //.registerTypeAdapter(byte[].class, (JsonSerializer<byte[]>) (src, typeOfSrc, context) -> new JsonPrimitive(Base64.getEncoder().encodeToString(src)))
                //.registerTypeAdapter(byte[].class, (JsonDeserializer<byte[]>) (json, typeOfT, context) -> Base64.getDecoder().decode(json.getAsString()))

                .create();

    }

    @Override
    public String render(Object model) {
        JsonElement serializedModel = gson.toJsonTree(model);
        // include class name in serialized json
        serializedModel.getAsJsonObject().addProperty("__className__", model.getClass().getSimpleName());
        return serializedModel.toString();
    }

    // type adapter factory that serializes the object as usual, but also
    // includes the class name in the serialized json in the __className__ field
    public static class ClassNameTypeAdapterFactory implements TypeAdapterFactory {

        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
            final TypeAdapter<T> delegate = gson.getDelegateAdapter(this, typeToken);
            final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);

            return new TypeAdapter<T>() {
                @Override
                public void write(JsonWriter out, T value) throws IOException {
                    if (value instanceof byte[]) {
                        out.value(Base64.getEncoder().encodeToString((byte[]) value));
                        return;
                    }
                    if (value instanceof AtomicReference atomicReference) {
                        //value = ((AtomicReference<T>) value).get();
                        out.value(atomicReference.get().toString());
                        return;
                    }
                    JsonElement tree = delegate.toJsonTree(value);
                    if (tree.isJsonObject()) {
                        tree.getAsJsonObject().addProperty("__className__", value.getClass().getName());
                    }
                    elementAdapter.write(out, tree);
                }

                @Override
                public T read(JsonReader in) throws IOException {
                    return delegate.read(in);
                }
            };
        }
    }

}
