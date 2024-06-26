package com.ingroupe.efti.eftilogger.dto;

import com.ingroupe.efti.eftilogger.exception.TechnicalTypeException;
import lombok.experimental.SuperBuilder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@SuperBuilder
public class LogAllDto {

    public final String messageEndDate;
    public final String componentType;
    public final String componentId;
    public final String componentCountry;
    public final String requestingComponentType;
    public final String requestingComponentId;
    public final String requestingComponentCountry;
    public final String respondingComponentType;
    public final String respondingComponentId;
    public final String respondingComponentCountry;
    public final String messageContent;
    public final String statusMessage;
    public final String errorCodeMessage;
    public final String errorDescriptionMessage;
    public final String timeoutComponentType;

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
