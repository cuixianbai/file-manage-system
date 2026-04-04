package com.filemanage.controller;

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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "companyId", required = false) Long companyId) {

        UserDetailsImpl currentUser = getCurrentUser();

        try {
            // Determine target company
            Company targetCompany;
            if (currentUser.isAdmin()) {
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

            // Create directory if not exists
            Path targetLocation = getDirAPath().resolve(targetCompany.getName());
            Files.createDirectories(targetLocation);

            // Generate new filename: CompanyName_OriginalFilename.ext
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalFilename.substring(lastDotIndex);
            }
            String newFilename = targetCompany.getName() + "_" + originalFilename;

            // Save file
            Path filePath = targetLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Save record to database
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
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("文件上传失败: " + e.getMessage()));
        }
    }

    @GetMapping("/records")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<FileRecord>> getFileRecords() {
        UserDetailsImpl currentUser = getCurrentUser();

        List<FileRecord> records;
        if (currentUser.isAdmin()) {
            records = fileRecordRepository.findAllByOrderByUploadedAtDesc();
        } else {
            records = fileRecordRepository.findByCompanyIdOrderByUploadedAtDesc(currentUser.getCompanyId());
        }

        return ResponseEntity.ok(records);
    }

    @GetMapping("/dirA")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getDirAFiles() {
        UserDetailsImpl currentUser = getCurrentUser();

        try {
            Path dirA = getDirAPath();

            if (currentUser.isAdmin()) {
                // Admin can see all company folders
                List<String> companies = Files.list(dirA)
                        .filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                return ResponseEntity.ok(companies);
            } else {
                // User can only see their company folder
                Path companyDir = dirA.resolve(currentUser.getCompanyName());
                if (!Files.exists(companyDir)) {
                    return ResponseEntity.ok(List.of());
                }

                List<String> files = Files.list(companyDir)
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                return ResponseEntity.ok(files);
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("读取目录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/dirB")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> getDirBFiles() {
        UserDetailsImpl currentUser = getCurrentUser();

        try {
            Path dirB = getDirBPath();

            if (currentUser.isAdmin()) {
                // Admin can see all company folders
                List<String> companies = Files.list(dirB)
                        .filter(Files::isDirectory)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                return ResponseEntity.ok(companies);
            } else {
                // User can only see their company folder
                Path companyDir = dirB.resolve(currentUser.getCompanyName());
                if (!Files.exists(companyDir)) {
                    return ResponseEntity.ok(List.of());
                }

                List<String> files = Files.list(companyDir)
                        .filter(Files::isRegularFile)
                        .map(path -> path.getFileName().toString())
                        .collect(Collectors.toList());
                return ResponseEntity.ok(files);
            }
        } catch (IOException e) {
            return ResponseEntity.badRequest()
                    .body(MessageResponse.error("读取目录失败: " + e.getMessage()));
        }
    }

    @GetMapping("/dirB/{companyName}/{filename}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<?> downloadDirBFile(
            @PathVariable String companyName,
            @PathVariable String filename,
            HttpServletRequest request) {

        UserDetailsImpl currentUser = getCurrentUser();

        // Check permission
        if (!currentUser.isAdmin() && !currentUser.getCompanyName().equals(companyName)) {
            return ResponseEntity.status(403)
                    .body(MessageResponse.error("无权访问其他公司的文件"));
        }

        try {
            Path filePath = getDirBPath().resolve(companyName).resolve(filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = determineContentType(request, resource);

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
