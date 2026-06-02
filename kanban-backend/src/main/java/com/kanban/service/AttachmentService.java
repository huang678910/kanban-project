package com.kanban.service;

import com.kanban.entity.Attachment;
import com.kanban.entity.BoardList;
import com.kanban.entity.Card;
import com.kanban.exception.BusinessException;
import com.kanban.mapper.AttachmentMapper;
import com.kanban.mapper.BoardListMapper;
import com.kanban.mapper.CardMapper;
import com.kanban.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AttachmentService {

    private final AttachmentMapper attachmentMapper;
    private final CardMapper cardMapper;
    private final BoardListMapper boardListMapper;
    private final BoardService boardService;

    @Value("${file.upload.path:./uploads/}")
    private String uploadPath;

    public AttachmentService(AttachmentMapper attachmentMapper, CardMapper cardMapper,
                             BoardListMapper boardListMapper, BoardService boardService) {
        this.attachmentMapper = attachmentMapper;
        this.cardMapper = cardMapper;
        this.boardListMapper = boardListMapper;
        this.boardService = boardService;
    }

    public List<Attachment> getAttachments(Long cardId) {
        return attachmentMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Attachment>()
                        .eq(Attachment::getCardId, cardId));
    }

    @Transactional
    public Attachment uploadAttachment(Long cardId, MultipartFile file) throws IOException {
        Card card = cardMapper.selectById(cardId);
        if (card == null) throw new BusinessException(404, "卡片不存在");
        BoardList boardList = boardListMapper.selectById(card.getListId());
        boardService.checkPermission(boardList.getBoardId(), "ADMIN", "MEMBER");

        // Validate file size (10MB max)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BusinessException("文件大小不能超过10MB");
        }

        // Generate storage path: uploads/YYYY/MM/UUID_filename
        LocalDate now = LocalDate.now();
        String datePath = String.format("%d/%02d", now.getYear(), now.getMonthValue());
        String uniqueName = UUID.randomUUID().toString().substring(0, 8) + "_" + file.getOriginalFilename();

        Path storageDir = Paths.get(uploadPath, datePath);
        Files.createDirectories(storageDir);
        Path targetPath = storageDir.resolve(uniqueName);
        file.transferTo(targetPath.toFile());

        Attachment attachment = new Attachment();
        attachment.setCardId(cardId);
        attachment.setUserId(SecurityUtils.getCurrentUserId());
        attachment.setFilename(file.getOriginalFilename());
        attachment.setFilePath(datePath + "/" + uniqueName);
        attachment.setFileSize(file.getSize());
        attachment.setMimeType(file.getContentType());
        attachmentMapper.insert(attachment);

        return attachment;
    }

    public Path getAttachmentFile(Long attachmentId) {
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) throw new BusinessException(404, "附件不存在");
        return Paths.get(uploadPath, attachment.getFilePath());
    }

    @Transactional
    public void deleteAttachment(Long attachmentId) {
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) throw new BusinessException(404, "附件不存在");

        // Delete file from disk
        try {
            Files.deleteIfExists(Paths.get(uploadPath, attachment.getFilePath()));
        } catch (IOException ignored) {}

        attachmentMapper.deleteById(attachmentId);
    }
}
