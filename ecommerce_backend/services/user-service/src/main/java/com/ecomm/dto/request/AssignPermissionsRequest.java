package com.ecomm.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Set;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AssignPermissionsRequest {
    @NotEmpty
    private Set<String> permissionNames;
}
