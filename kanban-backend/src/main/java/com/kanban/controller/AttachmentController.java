package com.kanban.controller;

import com.kanban.dto.ApiResponse;
import com.kanban.entity.Attachment;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.AttachmentMapper;
import com.kanban.service.AttachmentService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
public class AttachmentController {

    private final AttachmentService attachmentService;
    private final AttachmentMapper attachmentMapper;

    public AttachmentController(AttachmentService attachmentService, AttachmentMapper attachmentMapper) {
        this.attachmentService = attachmentService;
        this.attachmentMapper = attachmentMapper;
    }

    @GetMapping("/api/cards/{cardId}/attachments")
    public ApiResponse<?> getAttachments(@PathVariable Long cardId) {
        return ApiResponse.success(attachmentService.getAttachments(cardId));
    }

    @PostMapping("/api/cards/{cardId}/attachments")
    public ApiResponse<?> uploadAttachment(@PathVariable Long cardId,
                                           @RequestParam("file") MultipartFile file) throws IOException {
        return ApiResponse.success("上传成功", attachmentService.uploadAttachment(cardId, file));
    }

    @GetMapping("/api/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) throws IOException {
        Path filePath = attachmentService.getAttachmentFile(attachmentId);
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) throw new BusinessException(404, "附件不存在");
        String filename = attachment.getFilename();

        InputStreamResource resource = new InputStreamResource(new FileInputStream(filePath.toFile()));
        String contentType = Files.probeContentType(filePath);
        if (contentType == null) contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @DeleteMapping("/api/attachments/{attachmentId}")
    public ApiResponse<?> deleteAttachment(@PathVariable Long attachmentId) {
        attachmentService.deleteAttachment(attachmentId);
        return ApiResponse.success("删除成功", null);
    }
}
