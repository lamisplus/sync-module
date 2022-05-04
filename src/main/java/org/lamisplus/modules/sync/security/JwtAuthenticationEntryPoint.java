package org.lamisplus.modules.sync.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lamisplus.modules.sync.controller.apierror.ApiError;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint, Serializable {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        //set the response object
        response.setStatus(401);
        response.setContentType("application/json");

        //pass down the actual obj that exception handler normally send
        ApiError apiError = new ApiError(UNAUTHORIZED);
        apiError.setStatusCode(401);
        apiError.setMessage("Invalid Token");
        apiError.setDebugMessage(authException.getMessage());

        ObjectMapper objectMapper = new ObjectMapper();
        JSONObject jsonObject = null;

        try {
            jsonObject = new JSONObject(objectMapper.writeValueAsString(apiError));
            JSONObject apiErrorJson = jsonObject.getJSONObject("apierror");

            apiErrorJson.remove("timestamp");
            apiErrorJson.put("timestamp", apiError.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        PrintWriter out = response.getWriter();
        out.print(jsonObject);
        out.flush();
    }
}
