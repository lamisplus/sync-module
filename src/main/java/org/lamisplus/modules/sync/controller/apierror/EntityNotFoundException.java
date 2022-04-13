package org.lamisplus.modules.sync.controller.apierror;

import org.apache.commons.lang3.StringUtils;
import org.lamisplus.modules.sync.controller.apierror.ErrorMapper;

import java.util.Map;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(Class clazz, String... searchParamsMap) {
        super(org.lamisplus.modules.sync.controller.apierror.EntityNotFoundException.generateMessage(clazz.getSimpleName(), ErrorMapper.toMap(String.class, String.class, searchParamsMap)));
    }

    private static String generateMessage(String entity, Map<String, String> searchParams) {
        return StringUtils.capitalize(entity) +
                " was not found for parameters " + searchParams;
    }
}
