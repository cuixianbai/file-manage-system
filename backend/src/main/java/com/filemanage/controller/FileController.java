package com.filemanage.controller;

import com.filemanage.dto.DirNode;
import com.filemanage.dto.FileInfo;
import com.filemanage.dto.MessageResponse;
import com.filemanage.entity.Company;
import com.filemanage.entity.FileRecord;
import com.filemanage.entity.User;
import com.filemanage.repository.CompanyRepository;
import com.filemanage.repository.FileRecordRepository;
import com.filemanage.repository.UserRepository;
import com.filemanage.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/files")
public class FileController {

    @Value("${file.storage.dir-a}")
    private String dirAPath;

    @Value("${file.storage.dir-b}")
    private String dirBPath;

    @Autowired
    private FileRecordRepository fileRecordRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    private Path getDirAPath() {
        return Paths.get(dirAPath).toAbsolutePath().normalize();
    }

    private Path getDirBPath() {
        return Paths.get(dirBPath).toAbsolutePath().normalize();
    }

    private UserDetailsImpl getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (UserDetailsImpl) authentication.getPrincipal();
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "companyId", required = false) Long companyId) {

        UserDetailsImpl currentUser = getCurrentUser();

        try {
            Company targetCompany;
            if (currentUser.isAdmin() || currentUser.isManager()) {
                if (companyId == null) {
                    return ResponseEntity.badRequest()
                            .body(MessageResponse.error("管理员必须选择公司"));
                }
                targetCompany = companyRepository.findById(companyId)
                        .orElseThrow(() -> new RuntimeException("公司不存在"));
            } else {
                targetCompany = companyRepository.findById(currentUser.getCompanyId())
                        .orElseThrow(() -> new RuntimeException("公司不存在"));
            }

            Path targetLocation = getDirAPath();
            Files.createDirectories(targetLocation);

            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String companyName = targetCompany.getName();
            String newFilename = companyName + "_" + originalFilename;

            Path filePath = targetLocation.resolve(newFilename);
            String cleanPath = filePath.toAbsolutePath().toString().trim();
            File cleanFile = new File(cleanPath);
            
            if (!cleanFile.getParentFile().exists()) {
                cleanFile.getParentFile().mkdirs();
            }
            
            byte[] fileContent = file.getBytes();
            try (FileOutputStream fos = new FileOutputStream(cleanFile)) {
                fos.write(fileContent);
                fos.flush();
            }

            User uploadedBy = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            FileRecord fileRecord = FileRecord.builder()
                    .originalFilename(originalFilename)
                    .newFilename(newFilename)
                    .filePath(filePath.toString())
                    .fileSize(file.getSize())
                    .company(targetCompany)
                    .uploadedBy(uploadedBy)
                    .status(FileRecord.FileStatus.UPLOADED)
                    .build();

            fileRecordRepository.save(fileRecord);

            return ResponseEntity.ok(MessageResponse.success("文件上传成功"));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("文件上传失败: " + e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("文件上传失败: " + e.getMessage()));
        }
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<List<FileRecord>> getFileRecords(
            @RequestParam(required = false) String originalFilename,
            @RequestParam(required = false) String newFilename,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        UserDetailsImpl currentUser = getCurrentUser();

        Specification<FileRecord> spec = Specification.where(null);

        if (!currentUser.isAdmin() && !currentUser.isManager()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("company").get("id"), currentUser.getCompanyId()));
        }

        if (originalFilename != null && !originalFilename.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("originalFilename")), "%" + originalFilename.toLowerCase() + "%"));
        }

        if (newFilename != null && !newFilename.isEmpty()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("newFilename")), "%" + newFilename.toLowerCase() + "%"));
        }

        if (startDate != null && !startDate.isEmpty()) {
            try {
                spec = spec.and((root, query, cb) -> 
                    cb.greaterThanOrEqualTo(root.get("uploadedAt"), java.time.LocalDateTime.parse(startDate)));
            } catch (Exception ignored) {
            }
        }

        if (endDate != null && !endDate.isEmpty()) {
            try {
                spec = spec.and((root, query, cb) -> 
                    cb.lessThanOrEqualTo(root.get("uploadedAt"), java.time.LocalDateTime.parse(endDate)));
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok(fileRecordRepository.findAll(spec));
    }

    @GetMapping("/dirA")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> getDirAFiles() {
        UserDetailsImpl currentUser = getCurrentUser();

        try {
            Path dirA = getDirAPath();

            List<FileInfo> allFiles = Files.list(dirA)
                    .filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return new FileInfo(
                                    path.getFileName().toString(),
                                    Files.size(path),
                                    Files.getLastModifiedTime(path).toMillis()
                            );
                        } catch (IOException e) {
                            return new FileInfo(path.getFileName().toString(), 0L, 0L);
                        }
                    })
                    .collect(Collectors.toList());

            if (currentUser.isAdmin() || currentUser.isManager()) {
                return ResponseEntity.ok(allFiles);
            } else {
                String companyName = currentUser.getCompanyName();
                List<FileInfo> userFiles = allFiles.stream()
                        .filter(fileInfo -> fileInfo.getFileName().startsWith(companyName + "_"))
                        .collect(Collectors.toList());
                return ResponseEntity.ok(userFiles);
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("读取目录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/dirB")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> getDirBFiles() {
        UserDetailsImpl currentUser = getCurrentUser();

        try {
            Path dirB = getDirBPath();

            if (currentUser.isAdmin() || currentUser.isManager()) {
                Map<String, DirNode> companyFiles = new HashMap<>();
                
                Files.list(dirB)
                        .filter(Files::isDirectory)
                        .forEach(companyPath -> {
                            String companyName = companyPath.getFileName().toString();
                            try {
                                DirNode node = buildDirectoryTree(companyPath, companyName);
                                companyFiles.put(companyName, node);
                            } catch (IOException e) {
                                companyFiles.put(companyName, DirNode.builder()
                                        .name(companyName)
                                        .path(companyName)
                                        .type("directory")
                                        .children(new ArrayList<>())
                                        .build());
                            }
                        });
                return ResponseEntity.ok(companyFiles);
            } else {
                Path companyDir = dirB.resolve(currentUser.getCompanyName());
                if (!Files.exists(companyDir)) {
                    return ResponseEntity.ok(DirNode.builder()
                            .name(currentUser.getCompanyName())
                            .path(currentUser.getCompanyName())
                            .type("directory")
                            .children(new ArrayList<>())
                            .build());
                }

                DirNode node = buildDirectoryTree(companyDir, currentUser.getCompanyName());
                return ResponseEntity.ok(node);
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("读取目录失败: " + e.getMessage()));
        }
    }

    private DirNode buildDirectoryTree(Path path, String relativePath) throws IOException {
        List<DirNode> children = new ArrayList<>();
        
        Files.list(path).forEach(childPath -> {
            try {
                String childName = childPath.getFileName().toString();
                String childRelativePath = relativePath + "/" + childName;
                
                if (Files.isDirectory(childPath)) {
                    DirNode childNode = buildDirectoryTree(childPath, childRelativePath);
                    children.add(childNode);
                } else {
                    children.add(DirNode.builder()
                            .name(childName)
                            .path(childRelativePath)
                            .type("file")
                            .size(Files.size(childPath))
                            .lastModified(Files.getLastModifiedTime(childPath).toMillis())
                            .build());
                }
            } catch (IOException e) {
                // Ignore errors for individual files
            }
        });

        return DirNode.builder()
                .name(path.getFileName().toString())
                .path(relativePath)
                .type("directory")
                .children(children)
                .build();
    }

    @GetMapping("/dirB/download")
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER') or hasRole('USER')")
    public ResponseEntity<?> downloadDirBFile(
            @RequestParam String path,
            HttpServletRequest request) {

        UserDetailsImpl currentUser = getCurrentUser();

        try {
            path = URLDecoder.decode(path, StandardCharsets.UTF_8);
            
            String[] parts = path.split("/", 2);
            if (parts.length < 1) {
                return ResponseEntity.badRequest()
                        .body(MessageResponse.error("无效的文件路径"));
            }
            
            String companyName = parts[0];
            
            if (!currentUser.isAdmin() && !currentUser.isManager() && !currentUser.getCompanyName().equals(companyName)) {
                return ResponseEntity.status(403)
                        .body(MessageResponse.error("无权访问其他公司的文件"));
            }

            Path filePath = getDirBPath().resolve(path);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(request, resource);
            String filename = filePath.getFileName().toString();

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + filename + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("文件路径错误"));
        }
    }

    private String determineContentType(HttpServletRequest request, Resource resource) {
        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException e) {
            // Ignore
        }
        if (contentType == null) {
            contentType = "application/octet-stream";
        }
        return contentType;
    }
}
