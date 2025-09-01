package com.hare.minio;

import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;

/**
 *
 * @author Hare
 */
@RestController
@RequestMapping("/api/file")
public class FileController {

    private final MinioService minioService;

    public FileController(MinioService minioService) {
        this.minioService = minioService;
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) throws Exception {
        minioService.upload(file);
        return "上传成功: " + file.getOriginalFilename();
    }

    @GetMapping("/download")
    public void download(@RequestParam String filename, HttpServletResponse response) throws Exception {

        String objectName = filename; // 可改进为更安全的格式

        try (InputStream is = minioService.download(objectName)) {
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename.split("/")[1]);
//            is.transferTo(response.getOutputStream());
            IOUtils.copy(is, response.getOutputStream());
        }
    }

    @GetMapping("/share-url")
    public String getShareUrl(@RequestParam String filename) throws Exception {

        return minioService.getPresignedUrl(filename);
    }
}
