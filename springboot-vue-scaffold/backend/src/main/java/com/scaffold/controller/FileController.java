package com.scaffold.controller;

import com.scaffold.common.Result;
import com.scaffold.service.FileUploadService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    @Autowired
    private FileUploadService fileUploadService;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        String url = fileUploadService.upload(file);
        return Result.ok(Map.of("url", url));
    }
}
