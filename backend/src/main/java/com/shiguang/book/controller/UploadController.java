package com.shiguang.book.controller;

import com.shiguang.book.common.BizException;
import com.shiguang.book.common.CurrentUser;
import com.shiguang.book.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final Set<String> ALLOWED = Set.of("jpg", "jpeg", "png", "webp");
    private static final long MAX_SIZE = 2L * 1024 * 1024;

    @Value("${shiguang.upload.dir}")
    private String uploadDir;

    /** 封面图上传：限 2MB、只允许 jpg/jpeg/png/webp，文件名用 UUID 防重名（BUG-03 的教训） */
    @PostMapping
    public Result<Map<String, String>> upload(HttpServletRequest request,
                                              @RequestParam("file") MultipartFile file) {
        CurrentUser.get(request);
        if (file == null || file.isEmpty()) {
            throw new BizException(400, "请选择要上传的图片");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException(400, "图片不能超过 2MB");
        }

        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String ext = original.contains(".")
                ? original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT)
                : "";
        if (!ALLOWED.contains(ext)) {
            throw new BizException(400, "只支持 jpg / png / webp 格式");
        }

        try {
            String month = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            Path dir = Paths.get(uploadDir, month).toAbsolutePath();
            Files.createDirectories(dir);
            String name = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            File target = dir.resolve(name).toFile();
            file.transferTo(target);

            Map<String, String> data = new LinkedHashMap<>();
            data.put("url", "/files/" + month + "/" + name);
            return Result.ok(data);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BizException(500, "图片上传失败，请重试");
        }
    }
}
