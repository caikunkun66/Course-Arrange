package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author: 15760
 * @Date: 2020/5/12
 * @Descripe: 文件上传，读取文件
 */
@RestController
public class UploadController {

    private Logger log = LoggerFactory.getLogger(UploadController.class);

    // 头像上传路径配置
    @Value("${file.upload.path:uploads/avatars/}")
    private String uploadPath;

    @Value("${server.port:8008}")
    private String serverPort;

    /**
     * 上传学生头像
     * @param file 头像文件
     * @return 返回头像访问URL
     */
    @PostMapping("/upload/avatar")
    public ServerResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
        log.info("头像上传被调用...");

        // 验证文件
        if (file == null || file.isEmpty()) {
            log.error("上传文件为空");
            return ServerResponse.ofError("上传文件不能为空");
        }

        // 获取原始文件名
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            log.error("文件名为空");
            return ServerResponse.ofError("文件名不能为空");
        }

        // 验证文件类型
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("image/jpeg") && !contentType.equals("image/png"))) {
            log.error("文件类型不支持: " + contentType);
            return ServerResponse.ofError("只支持 JPG 和 PNG 格式的图片");
        }

        // 验证文件大小（2MB）
        long maxSize = 2 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            log.error("文件大小超出限制: " + file.getSize());
            return ServerResponse.ofError("文件大小不能超过2MB");
        }

        try {
            // 获取文件扩展名
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            
            // 生成唯一文件名：时间戳_UUID
            String dateFolder = new SimpleDateFormat("yyyyMMdd").format(new Date());
            String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) 
                            + "_" + UUID.randomUUID().toString().substring(0, 8) 
                            + extension;

            // 创建上传目录（在项目根目录下）
            String projectPath = System.getProperty("user.dir");
            String fullUploadPath = projectPath + File.separator + uploadPath + dateFolder;
            File uploadDir = new File(fullUploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
                log.info("创建上传目录: " + fullUploadPath);
            }

            // 保存文件
            String filePath = fullUploadPath + File.separator + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);
            log.info("文件保存成功: " + filePath);

            // 生成访问URL
            String fileUrl = "http://localhost:" + serverPort + "/uploads/avatars/" + dateFolder + "/" + fileName;
            
            // 返回结果
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("fileName", fileName);
            
            log.info("头像上传成功，访问地址: " + fileUrl);
            return ServerResponse.ofSuccess("上传成功", result);

        } catch (Exception e) {
            log.error("头像上传失败: " + e.getMessage(), e);
            return ServerResponse.ofError("上传失败: " + e.getMessage());
        }
    }

}
