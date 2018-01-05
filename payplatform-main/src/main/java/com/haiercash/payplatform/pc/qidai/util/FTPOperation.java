package com.haiercash.payplatform.pc.qidai.util;

import com.haiercash.core.io.CharsetNames;
import com.haiercash.core.lang.ThrowableUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.util.StreamUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * FTP 操作
 * Created by 许崇雷 on 2018-01-04.
 */
public final class FTPOperation implements Closeable {
    private final Log logger = LogFactory.getLog(FTPOperation.class);
    private FTPClient ftpClient = new FTPClient();

    public FTPOperation() {
    }

    //连接登陆
    public boolean open(String host, int port, String username, String password) {
        //连接
        try {
            logger.info("连接至 ftp 服务器，地址: " + host + ", 端口号: " + port);
            ftpClient.connect(host, port);
        } catch (Exception e) {
            logger.warn("连接 ftp 失败: " + ThrowableUtils.getMessage(e));
            return false;
        }
        // 登录
        try {
            logger.info("登录 ftp 服务器, 用户名: " + username + ", 密码: " + password);
            if (!this.ftpClient.login(username, password)) {
                logger.warn("登陆失败, 可能为密码错误");
                return false;
            }
        } catch (Exception e) {
            logger.warn("登录 ftp 失败: " + ThrowableUtils.getMessage(e));
            return false;
        }

        //校验
        try {
            if (!FTPReply.isPositiveCompletion(this.ftpClient.getReply())) {
                this.ftpClient.disconnect();
                return false;
            }
        } catch (Exception e) {
            logger.warn("ftp 积极拒绝: " + ThrowableUtils.getMessage(e));
            return false;
        }

        //属性设置
        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);// 设置文件类型，二进制
            ftpClient.setBufferSize(1024 * 4);// 设置缓冲区大小
            ftpClient.setControlEncoding(CharsetNames.UTF_8);// 设置字符编码
        } catch (Exception ignored) {
        }

        return true;
    }

    //变更工作目录
    private boolean changeDir(String path) {
        try {
            this.ftpClient.makeDirectory(path);
            logger.info("成功创建了目录: " + path);
        } catch (Exception e) {
        }
        try {
            if (!this.ftpClient.changeWorkingDirectory(path)) {
                logger.warn("变更工作目录失败");
                return false;
            }
        } catch (IOException e) {
            logger.warn("变更工作目录失败: " + ThrowableUtils.getMessage(e));
            return false;
        }
        return true;
    }

    //上传文件,全路径 /dir/file.ext
    public boolean upload(String remote, InputStream inputStream) {
        File file = new File(remote);
        File parent = file.getParentFile();
        if (!this.changeDir(parent.getPath()))
            return false;
        try {
            if (!this.ftpClient.storeFile(file.getName(), inputStream)) {
                this.logger.warn("上传文件失败");
                return false;
            }
        } catch (Exception e) {
            this.logger.warn("上传文件失败: " + ThrowableUtils.getMessage(e));
            return false;
        }
        return true;
    }

    //上传文件,全路径 /dir/file.ext
    public boolean upload(String remote, String local) {
        try (InputStream inputStream = new FileInputStream(local)) {
            return this.upload(remote, inputStream);
        } catch (Exception e) {
            this.logger.warn("读取本地文件失败: " + ThrowableUtils.getMessage(e));
            return false;
        }
    }

    //创建空文件,全路径 /dir/file.ext
    public boolean create(String remote) {
        return this.upload(remote, StreamUtils.emptyInput());
    }

    //关闭
    @Override
    public void close() {
        try {
            this.ftpClient.logout();
        } catch (Exception ignored) {
        }

        try {
            this.ftpClient.disconnect();
        } catch (Exception ignored) {
        }
    }
}
