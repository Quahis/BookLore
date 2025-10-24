package com.adityachandel.booklore.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskCronConfigRequest {
    private String cronExpression;
    private Boolean enabled;
}

