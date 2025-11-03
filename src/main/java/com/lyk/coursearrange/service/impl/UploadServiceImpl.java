package com.lyk.coursearrange.service.impl;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.service.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author: 15760
 * @Date: 2020/5/13
 * @Descripe: 文件上传服务（课程任务导入功能已移除）
 */
@Service
public class UploadServiceImpl implements UploadService {

    /**
     * 文件上传实现（课程任务导入功能已移除）
     *
     * @param file
     * @return
     */
    @Override
    public ServerResponse upload(MultipartFile file) {
        return ServerResponse.ofError("课程任务导入功能已移除");
    }

}
