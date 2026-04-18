package com.corecompass.fitness.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SupplementTypeRequest {
    @NotBlank @Size(max = 100)
    private String name;

    @Pattern(regexp = "VITAMIN|MINERAL|PROTEIN|PREWORKOUT|RECOVERY|HERBAL|OTHER")
    private String category;

    @Size(max = 300)
    private String description;
}