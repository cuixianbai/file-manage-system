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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
            Path targetLocation = getDirAPath();
            System.out.println("Target directory: " + targetLocation.toAbsolutePath());
            System.out.println("Directory exists: " + Files.exists(targetLocation));
            try {
                Files.createDirectories(targetLocation);
                System.out.println("Directory created successfully");
            } catch (Exception e) {
                System.out.println("Failed to create directory: " + e.getMessage());
                throw e;
            }

            // Generate new filename: CompanyName_OriginalFilename.ext
            String originalFilename = Objects.requireNonNull(file.getOriginalFilename());
            String companyName = targetCompany.getName();
            System.out.println("Company name: '" + companyName + "'");
            System.out.println("Original filename: '" + originalFilename + "'");
            
            String extension = "";
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                extension = originalFilename.substring(lastDotIndex);
            }
            String newFilename = companyName + "_" + originalFilename;
            System.out.println("New filename: '" + newFilename + "'");

            // Save file
            Path filePath = targetLocation.resolve(newFilename);
            System.out.println("File path: " + filePath.toAbsolutePath());
            System.out.println("File exists: " + Files.exists(filePath));
            
            // Clean file path to remove any extra spaces
            String cleanPath = filePath.toAbsolutePath().toString().trim();
            System.out.println("Clean file path: " + cleanPath);
            File cleanFile = new File(cleanPath);
            
            // Ensure parent directory exists
            if (!cleanFile.getParentFile().exists()) {
                System.out.println("Creating parent directory: " + cleanFile.getParentFile());
                cleanFile.getParentFile().mkdirs();
            }
            
            // Check directory permissions
            System.out.println("Parent directory exists: " + cleanFile.getParentFile().exists());
            System.out.println("Parent directory can write: " + cleanFile.getParentFile().canWrite());
            System.out.println("Parent directory can read: " + cleanFile.getParentFile().canRead());
            
            // Read file content into byte array first
            byte[] fileContent = file.getBytes();
            System.out.println("File size: " + fileContent.length + " bytes");
            
            // Try using FileOutputStream to write the byte array
            try {
                System.out.println("Attempting to save file using FileOutputStream with byte array");
                try (FileOutputStream fos = new FileOutputStream(cleanFile)) {
                    fos.write(fileContent);
                    fos.flush();
                    System.out.println("File saved successfully with FileOutputStream");
                }
            } catch (Exception e) {
                System.out.println("Failed with FileOutputStream approach: " + e.getMessage());
                
                // Try using Files.write with byte array
                try {
                    System.out.println("Attempting alternative approach with Files.write");
                    Path cleanFilePath = Paths.get(cleanPath);
                    Files.write(cleanFilePath, fileContent);
                    System.out.println("File saved successfully with Files.write");
                } catch (Exception ex) {
                    System.out.println("Alternative approach failed: " + ex.getMessage());
                    throw ex;
                }
            }

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

            // Get all files in dirA
            List<String> allFiles = Files.list(dirA)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());

            if (currentUser.isAdmin()) {
                // Admin can see all files
                return ResponseEntity.ok(allFiles);
            } else {
                // User can only see files from their company
                String companyName = currentUser.getCompanyName();
                List<String> userFiles = allFiles.stream()
                        .filter(fileName -> fileName.startsWith(companyName + "_"))
                        .collect(Collectors.toList());
                return ResponseEntity.ok(userFiles);
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
                // Admin can see all company folders and their files
                Map<String, List<String>> companyFiles = new HashMap<>();
                
                Files.list(dirB)
                        .filter(Files::isDirectory)
                        .forEach(companyPath -> {
                            String companyName = companyPath.getFileName().toString();
                            try {
                                List<String> files = Files.list(companyPath)
                                        .filter(Files::isRegularFile)
                                        .map(path -> path.getFileName().toString())
                                        .collect(Collectors.toList());
                                companyFiles.put(companyName, files);
                            } catch (IOException e) {
                                companyFiles.put(companyName, List.of());
                            }
                        });
                return ResponseEntity.ok(companyFiles);
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
