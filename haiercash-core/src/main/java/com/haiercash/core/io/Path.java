package com.haiercash.core.io;

import com.haiercash.core.lang.Environment;
import com.haiercash.core.lang.StringUtils;

/**
 * 路径拼合,不可变
 * Created by 许崇雷 on 2016/7/15.
 */
public final class Path {
    private final String path;//路径

    public Path() {
        this.path = Environment.Slash;
    }

    public Path(String path) {
        if (StringUtils.isEmpty(path)) {
            this.path = Environment.Slash;
            return;
        }
        this.path = path.replace(Environment.BackSlashChar, Environment.SlashChar);
    }

    private Path(String path, boolean b) {
        this.path = path;
    }

    /**
     * 在当前路径的基础上追加新路径,并返回新实例
     *
     * @param path
     * @return
     */
    public Path combine(String path) {
        if (StringUtils.isEmpty(path))
            return new Path(this.path, true);
        path = path.replace(Environment.BackSlashChar, Environment.SlashChar);

        if (this.path.endsWith(Environment.Slash)) {
            if (path.startsWith(Environment.Slash))
                return new Path(this.path + StringUtils.trimStart(path, Environment.SlashChar), true);
            else
                return new Path(this.path + path, true);
        } else {
            if (path.startsWith(Environment.Slash))
                return new Path(this.path + path, true);
            else
                return new Path(this.path + Environment.Slash + path, true);
        }
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
