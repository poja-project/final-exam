package com.example.demo.service;

import com.example.demo.exception.DomainException;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.model.Cohort;
import com.example.demo.model.Track;
import com.example.demo.repository.CohortRepository;
import com.example.demo.service.dto.GraduateResult;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GraduateExportService {

  private final CohortRepository cohortRepository;
  private final GraduationEligibilityService graduationEligibilityService;
  private final BucketComponent bucketComponent;

  @Transactional(readOnly = true)
  public List<GraduateResult> listGraduates(UUID cohortId, Track track) {
    requireCohort(cohortId);
    return graduationEligibilityService.computeGraduates(cohortId, track);
  }

  @Transactional(readOnly = true)
  public void exportGraduatesToBucket(UUID cohortId, Track track) {
    Cohort cohort = requireCohort(cohortId);
    List<GraduateResult> graduates = graduationEligibilityService.computeGraduates(cohortId, track);
    byte[] xlsx = XlsxGraduateExporter.export(graduates);

    File tempFile = writeToTempFile(xlsx, "graduates", ".xlsx");
    try {
      bucketComponent.upload(tempFile, bucketKey(cohort, track));
    } finally {
      tempFile.delete();
    }
  }

  public File downloadGraduatesFile(UUID cohortId, Track track) {
    Cohort cohort = requireCohort(cohortId);
    return bucketComponent.download(bucketKey(cohort, track));
  }

  public String fileName(UUID cohortId, Track track) {
    Cohort cohort = requireCohort(cohortId);
    return "graduates-%s-%s.xlsx".formatted(cohort.getLabel().replace(" ", "_"), track);
  }

  private Cohort requireCohort(UUID cohortId) {
    return cohortRepository
        .findById(cohortId)
        .orElseThrow(() -> DomainException.notFound("Cohort not found: " + cohortId));
  }

  private String bucketKey(Cohort cohort, Track track) {
    return "graduates/%s/%s.xlsx".formatted(cohort.getId(), track);
  }

  private File writeToTempFile(byte[] content, String prefix, String suffix) {
    try {
      File file = File.createTempFile(prefix, suffix);
      Files.write(file.toPath(), content);
      return file;
    } catch (IOException e) {
      throw new UncheckedIOException("Failed to write temp file for upload", e);
    }
  }
}
