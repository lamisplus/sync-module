package org.lamisplus.modules.sync.controller.apierror;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

public class IllegalTypeException extends RuntimeException {

    public IllegalTypeException(Class clazz, String... searchParamsMap) {
        super(IllegalTypeException.generateMessage(clazz.getSimpleName(), ErrorMapper.toMap(String.class, String.class, searchParamsMap)));
    }

    private static String generateMessage(String entity, Map<String, String> searchParams) {
        return StringUtils.capitalize(entity) +
                " Type Illegal " + searchParams;
    }
}
