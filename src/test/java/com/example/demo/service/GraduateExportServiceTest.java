package com.example.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.exception.DomainException;
import com.example.demo.file.bucket.BucketComponent;
import com.example.demo.model.Cohort;
import com.example.demo.model.Track;
import com.example.demo.repository.CohortRepository;
import com.example.demo.service.dto.GraduateResult;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GraduateExportServiceTest {

  @Mock private CohortRepository cohortRepository;
  @Mock private GraduationEligibilityService graduationEligibilityService;
  @Mock private BucketComponent bucketComponent;

  @InjectMocks private GraduateExportService graduateExportService;

  private final UUID cohortId = UUID.randomUUID();

  private Cohort cohort() {
    Cohort c = new Cohort();
    c.setId(cohortId);
    c.setLabel("Cohort 2023");
    return c;
  }

  @Test
  void listGraduates_returnsWhatTheEligibilityServiceComputes() {
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort()));
    List<GraduateResult> expected =
        List.of(new GraduateResult(1, UUID.randomUUID(), "STD-1", "A", "B", new BigDecimal("15")));
    when(graduationEligibilityService.computeGraduates(cohortId, Track.EL)).thenReturn(expected);

    List<GraduateResult> result = graduateExportService.listGraduates(cohortId, Track.EL);

    assertEquals(expected, result);
  }

  @Test
  void listGraduates_withUnknownCohort_throwsNotFound() {
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.empty());

    assertThrows(
        DomainException.class, () -> graduateExportService.listGraduates(cohortId, Track.EL));
  }

  @Test
  void exportGraduatesToBucket_uploadsAFile_withTheExpectedKeyPattern() {
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort()));
    when(graduationEligibilityService.computeGraduates(cohortId, Track.TN)).thenReturn(List.of());

    graduateExportService.exportGraduatesToBucket(cohortId, Track.TN);

    verify(bucketComponent)
        .upload(any(java.io.File.class), eq("graduates/" + cohortId + "/TN.xlsx"));
  }

  @Test
  void downloadGraduatesFile_readsFromTheSameKeyPatternUsedForExport() {
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort()));
    when(bucketComponent.download("graduates/" + cohortId + "/EL.xlsx"))
        .thenReturn(new java.io.File("dummy.xlsx"));

    graduateExportService.downloadGraduatesFile(cohortId, Track.EL);

    verify(bucketComponent).download("graduates/" + cohortId + "/EL.xlsx");
  }

  @Test
  void fileName_includesCohortLabelAndTrack() {
    when(cohortRepository.findById(cohortId)).thenReturn(Optional.of(cohort()));

    String fileName = graduateExportService.fileName(cohortId, Track.EL);

    assertEquals("graduates-Cohort_2023-EL.xlsx", fileName);
  }
}
