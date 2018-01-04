package com.haiercash.core.io;


import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;

/**
 * Created by 许崇雷 on 2016/7/15.
 * 路径拼合
 */
public final class Path {
    private String path;//路径

    public Path() {
        this.path = Environment.Slash;
    }

    /**
     * 构造函数
     *
     * @param path 路径
     */
    public Path(String path) {
        if (StringUtils.isEmpty(path)) {
            this.path = Environment.Slash;
            return;
        }
        this.path = path.replace(Environment.BackSlashChar, Environment.SlashChar);
    }

    /**
     * 获取绝对路径
     *
     * @return
     */
    public String getPath() {
        return path;
    }

    /**
     * 在当前路径的基础上追加新路径,并返回当前实例
     *
     * @param path
     * @return
     */
    public Path combine(String path) {
        if (StringUtils.isEmpty(path))
            return this;
        path = path.replace(Environment.BackSlashChar, Environment.SlashChar);

        if (this.path.endsWith(Environment.Slash)) {
            if (path.startsWith(Environment.Slash))
                this.path += StringUtils.trimStart(path, Environment.SlashChar);
            else
                this.path += path;
        } else {
            if (path.startsWith(Environment.Slash))
                this.path += path;
            else
                this.path += Environment.Slash + path;
        }
        return this;
    }

    /**
     * 获取路径
     *
     * @return
     */
    @Override
    public String toString() {
        return this.path;
    }
}
