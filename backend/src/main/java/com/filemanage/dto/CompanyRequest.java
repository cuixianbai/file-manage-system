package com.filemanage.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyRequest {
    @NotBlank(message = "公司名称不能为空")
    private String name;
}
