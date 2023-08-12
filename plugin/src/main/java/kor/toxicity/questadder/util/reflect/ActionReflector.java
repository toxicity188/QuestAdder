package kor.toxicity.questadder.util.reflect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import kor.toxicity.questadder.QuestAdder;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;

public class ActionReflector<T extends DataObject> {

    private final T object;

    public ActionReflector(T object, JsonObject jsonObject) {
        this.object = object;
        var fields = new ArrayList<AnnotatedField>();
        for (Field field : object.getClass().getFields()) {
            var annotation = field.getAnnotation(DataField.class);
            if (annotation != null) fields.add(new AnnotatedField(field,annotation));
        }
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            fields.stream().filter(e -> e.field.getName().toLowerCase().equals(entry.getKey()) || Arrays.stream(e.dataField.aliases()).anyMatch(s -> s.equals(entry.getKey()))).findFirst().ifPresent(f -> {
                var type = f.field.getType();
                var entryValue = entry.getValue();
                Object value;
                if (type.isEnum()) {
                    var upperCase = entry.getValue().getAsString().toUpperCase();
                    value = Arrays.stream(type.getEnumConstants()).filter(obj -> upperCase.equals(obj.toString())).findFirst().orElse(null);
                    if (value == null) QuestAdder.Companion.warn("not found error: no enum constant \"" + upperCase + "\" found.");
                } else {
                    var parse = DataType.findByClass(type);
                    value = (parse != null) ? parse.apply(entryValue) : null;
                }
                if (value != null) try {
                    f.field.set(object,value);
                } catch (IllegalAccessException e) {
                    throwReflectionError(f.field.getName());
                }
            });
        }
        for (AnnotatedField field : fields) {
            try {
                if (field.dataField.throwIfNull() && field.field.get(object) == null) throw new RuntimeException("the field \"" + field.field.getName() + "\" is null.");
            } catch (IllegalAccessException exception) {
                throwReflectionError(field.field.getName());
            }
        }
        object.initialize();
    }
    private void throwReflectionError(String n) {
        QuestAdder.Companion.warn("reflection error: cannot invoke field \"" + n + "\".");
    }

    public T getResult() {
        return object;
    }

    private record AnnotatedField(Field field, DataField dataField) {
    }

    private enum DataType {
        DOUBLE(new Class[] {
                Double.TYPE, Double.class
        },JsonElement::getAsDouble),
        INTEGER(new Class[]{
                Integer.TYPE, Integer.class
        },JsonElement::getAsInt),
        LONG(new Class[]{
                Long.TYPE, Long.class
        },JsonElement::getAsLong),
        SHORT(new Class[]{
                Short.TYPE, Short.class
        },JsonElement::getAsShort),
        BYTE(new Class[]{
                Byte.TYPE, Byte.class
        },JsonElement::getAsByte),
        FLOAT(new Class[]{
                Float.TYPE, Float.class
        },JsonElement::getAsFloat),
        BIG_DECIMAL(new Class[]{
                BigDecimal.class
        },JsonElement::getAsBigDecimal),
        BOOLEAN(new Class[]{
                Boolean.TYPE, Boolean.class
        },JsonElement::getAsBoolean),
        JSON_ARRAY(new Class[]{
                JsonArray.class
        },JsonElement::getAsJsonArray),
        JSON_OBJECT(new Class[]{
                JsonObject.class
        },JsonElement::getAsJsonObject),
        STRING(new Class[]{
                String.class
        },JsonElement::getAsString)

        ;
        private final Class<?>[] classes;
        private final Function<JsonElement,?> function;
        DataType(Class<?>[] classes, Function<JsonElement,?> function) {
            this.classes = classes;
            this.function = function;
        }

        private static Function<JsonElement,?> findByClass(Class<?> clazz) {
            return Arrays.stream(values()).filter(e -> Arrays.stream(e.classes).anyMatch(clazz::isAssignableFrom)).findFirst().map(e -> e.function).orElse(null);
        }
    }
}
