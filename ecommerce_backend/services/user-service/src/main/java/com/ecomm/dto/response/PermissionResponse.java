package com.ecomm.dto.response;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PermissionResponse {
    private Long id;
    private String name;
    private String description;
}
