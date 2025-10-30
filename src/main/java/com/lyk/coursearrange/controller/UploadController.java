package com.lyk.coursearrange.controller;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.ClassTask;
import com.lyk.coursearrange.service.UploadService;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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

    @Autowired
    private UploadService uploadService;

    // 头像上传路径配置
    @Value("${file.upload.path:uploads/avatars/}")
    private String uploadPath;

    @Value("${server.port:8008}")
    private String serverPort;

    /**
     * 上传课程计划Excel文件
     * @return
     */
    @PostMapping("/upload")
    public ServerResponse uploadClassTaskFile(MultipartFile file) {
        log.info("文件上传被调用。。。");
        return uploadService.upload(file);
    }

    /**
     * 下载系统提供的Excel导入模板
     * @return
     */
    @GetMapping(value = "/download", consumes = MediaType.ALL_VALUE)
    public void downloadTemplate(final HttpServletResponse response) {
        // 获取文件
        File file = new File("D:/arrange/excel/课程任务导入模板.xls");
        if (!file.exists()) {
            // 没有该模板文件就调用创建模板文件方法
            log.info("创建模板文件");
            createTemplate();
        }
        // 获取文件名字
        String fileName = file.getName();
        response.reset();
        // 设置ContentType，响应内容为二进制数据流，编码为utf-8，此处设定的编码是文件内容的编码
        response.setContentType("application/octet-stream;charset=utf-8");
        // 以（Content-Disposition: attachment; filename="filename.jpg"）格式设定默认文件名，设定utf编码，此处的编码是文件名的编码，使能正确显示中文文件名
        try {
            response.setHeader("Content-Disposition", "attachment;fileName="+ fileName +";filename*=utf-8''"+ URLEncoder.encode(fileName,"utf-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        // 实现文件下载
        byte[] buffer = new byte[1024];
        FileInputStream fis = null;
        BufferedInputStream bis = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            // 获取字节流
            OutputStream os = response.getOutputStream();
            int i = bis.read(buffer);
            while (i != -1) {
                os.write(buffer, 0, i);
                i = bis.read(buffer);
            }
            log.info("文件下载成功");
        }
        catch (Exception e) {
            e.printStackTrace();
            log.info("文件下载失败: " + e.getMessage());
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 如果没有模板文件就创建模板文件
     */
    private void createTemplate() {
        ExportParams params = new ExportParams();
        params.setTitle("课程任务导入模板(请严格对照数据库信息填写)");
        params.setSheetName("课程任务模板");
        List<ClassTask> list = new ArrayList();
        Workbook workbook = ExcelExportUtil.exportExcel(params, ClassTask.class, list);
        try {
            // 输出模板到本地
            FileOutputStream fos = new FileOutputStream("D:/arrange/excel/课程任务导入模板.xls");
            workbook.write(fos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
