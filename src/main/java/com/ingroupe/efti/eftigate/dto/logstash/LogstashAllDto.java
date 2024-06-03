package com.ingroupe.efti.eftigate.dto.logstash;

import com.ingroupe.efti.eftigate.exception.TechnicalTypeException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LogstashAllDto {

    public String messageStartDate;
    public String messageEndDate;
    public String componentType;
    public String componentId;
    public String componentCountry;
    public String requestingComponentType;
    public String requestingComponentId;
    public String requestingComponentCountry;
    public String respondingComponentType;
    public String respondingComponentId;
    public String respondingComponentCountry;
    public String messageContent;
    public String statusMessage;
    public String errorCodeMessage;
    public String errorDescriptionMessage;
    public String timeoutComponentType;

    private static List<Field> getAllFields(final List<Field> fields, final Class<?> type) {
        if (type.getSuperclass() != null) {
            getAllFields(fields, type.getSuperclass());
        }

        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        return fields;
    }


    public String[] getLinkedListFields() {
        final List<Field> fields = getAllFields(new LinkedList<>(), this.getClass());
        final List<String> fieldValueList = new ArrayList<>();
        fields.forEach(field -> {
            try {
                fieldValueList.add((String) field.get(this));
            } catch (IllegalAccessException e) {
                throw new TechnicalTypeException("Error with type String.", e);
            }
        });
        return  fieldValueList.toArray(new String[0]);
    }
}
