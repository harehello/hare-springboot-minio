# minio与springboot整合示例

在 Java 项目中使用 MinIO，主要通过 **MinIO 官方提供的 Java SDK** 来操作对象存储服务（兼容 Amazon S3 API）。你可以实现文件上传、下载、删除、创建桶、设置策略等操作。

------



## 一、环境准备

### 1. 添加依赖（Maven）

````xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.17</version>
</dependency>
````

> 最新版本查看：https://docs.min.io/community/minio-object-store/developers/minio-drivers.html#java-sdk

### 2. 启动 MinIO 服务（确保已运行）

确保你的 MinIO 服务正在运行，例如：

- 地址：`http://localhost:9000`
- 控制台：`http://localhost:9001`
- Access Key：`minioadmin`
- Secret Key：`minioadmin`

## 二、Spring Boot 集成

见示例代码