package org.lamisplus.modules.sync.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadDTO {
    @NotNull(message = "facilityId is mandatory")
    private Long facilityId;

    @NotBlank(message = "serverUrl is mandatory")
    private String serverUrl;

    //@NotNull(message = "remoteAccessTokenId is mandatory")
    private Long remoteAccessTokenId;

}
