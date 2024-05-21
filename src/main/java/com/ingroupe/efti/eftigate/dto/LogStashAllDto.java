package com.ingroupe.efti.eftigate.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogStashAllDto {
    private String test;
    private String oki;
    private String proute;

    public String[] getDeclaredFields() {
        List<Field> fields = Arrays.stream(this.getClass().getDeclaredFields()).toList();
        List<String> fieldValueList = new ArrayList<>();

        fields.forEach(field -> {
            try {
                fieldValueList.add((String) field.get(this));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
        return fieldValueList.toArray(new String[0]);
    }
}
