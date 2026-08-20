package com.example.demo.endpoint.rest.controller;

import com.example.demo.model.Track;
import com.example.demo.service.GraduateExportService;
import com.example.demo.service.dto.GraduateResult;
import java.io.File;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cohorts/{id}/graduates")
@RequiredArgsConstructor
public class GraduateController {

  private final GraduateExportService graduateExportService;

  @GetMapping
  public List<GraduateResult> listGraduates(@PathVariable UUID id, @RequestParam Track track) {
    return graduateExportService.listGraduates(id, track);
  }

  @PostMapping("/export")
  public ResponseEntity<Void> exportGraduates(@PathVariable UUID id, @RequestParam Track track) {
    graduateExportService.exportGraduatesToBucket(id, track);
    return ResponseEntity.status(HttpStatus.CREATED).build();
  }

  @GetMapping("/download")
  public ResponseEntity<FileSystemResource> downloadGraduates(
      @PathVariable UUID id, @RequestParam Track track) {
    File file = graduateExportService.downloadGraduatesFile(id, track);
    String fileName = graduateExportService.fileName(id, track);

    return ResponseEntity.ok()
        .contentType(
            MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
        .contentLength(file.length())
        .body(new FileSystemResource(file));
  }
}
