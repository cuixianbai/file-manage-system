package com.filemanage.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirNode {
    private String name;
    private String path;
    private String type;
    private Long size;
    private Long lastModified;
    private List<DirNode> children;
}
