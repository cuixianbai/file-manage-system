package com.filemanage.repository;

import com.filemanage.entity.Company;
import com.filemanage.entity.FileRecord;
import com.filemanage.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRecordRepository extends JpaRepository<FileRecord, Long>, JpaSpecificationExecutor<FileRecord> {
    List<FileRecord> findByCompany(Company company);
    List<FileRecord> findByCompanyId(Long companyId);
    List<FileRecord> findByUploadedBy(User user);
    List<FileRecord> findByStatus(FileRecord.FileStatus status);
    List<FileRecord> findByCompanyIdOrderByUploadedAtDesc(Long companyId);
    List<FileRecord> findAllByOrderByUploadedAtDesc();
}
