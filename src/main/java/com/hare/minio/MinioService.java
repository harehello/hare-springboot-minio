package com.hare.minio;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Hare
 */
@Component
public class MinioService {

    private final MinioClient minioClient;
    private final String bucket;

    public MinioService(MinioClient minioClient, @Value("${minio.bucket}") String bucket) {
        this.minioClient = minioClient;
        this.bucket = bucket;
        createBucketIfNotExists();

        Map<String, Object> policy = new HashMap<>();
        policy.put("Version", "2012-10-17");// 使用官方示例中使用的版本
        List<Map<String, String>> statements = new ArrayList<>();
        policy.put("Statement", statements);

        try {
            // 清除策略
            setBucketPolicy(new ObjectMapper().writeValueAsString(policy));
            // 设置策略
            Map<String, String> statement = new HashMap<>();
            statement.put("Effect", "Allow");// 允许或拒绝
            statement.put("Principal", "*");// 谁：* 表示匿名用户
            statement.put("Action", "s3:GetObject");// 允许的操作
            statement.put("Resource", "arn:aws:s3:::test/20250829/*");// 作用资源
            statements.add(statement);

            setBucketPolicy(new ObjectMapper().writeValueAsString(policy));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

    private void createBucketIfNotExists() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("无法创建 bucket: " + bucket, e);
        }
    }

    /**
     * 设置策略
     * @param config
     */
    private void setBucketPolicy(String config) {
        try {
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(config)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("设置策略失败 bucket: " + bucket, e);
        }
    }

    public void upload(MultipartFile file) throws Exception {
        String objectName = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()){
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
        }

    }

    public InputStream download(String objectName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }

    public void remove(String objectName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .build()
        );
    }

    /**
     * 生成预签名下载链接（可分享）
     * @param objectName
     * @return
     * @throws Exception
     */
    public String getPresignedUrl(String objectName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(objectName)
                        .expiry((int) TimeUnit.MINUTES.toSeconds(5L))
                        .build()
        );
    }

}
