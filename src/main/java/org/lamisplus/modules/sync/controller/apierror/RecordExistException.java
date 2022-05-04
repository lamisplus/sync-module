package org.lamisplus.modules.sync.controller.apierror;


import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@RequiredArgsConstructor
public class RecordExistException extends RuntimeException {

    public RecordExistException(Class clazz, String... searchParamsMap) {
        super(RecordExistException.generateMessage(clazz.getSimpleName(), ErrorMapper.toMap(String.class, String.class, searchParamsMap)));
    }

    private static String generateMessage(String entity, Map<String, String> searchParams) {
        return StringUtils.capitalize(entity) +
                " already exist " + searchParams;
    }
}
