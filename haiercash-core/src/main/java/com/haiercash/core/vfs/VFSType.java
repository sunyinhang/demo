package com.haiercash.core.vfs;

import com.haiercash.core.collection.ArrayUtils;

/**
 * Created by 许崇雷 on 2018-01-08.
 */
public enum VFSType {
    LOCAL("file://"),
    HTTP("http://"),
    HTTPS("https://"),
    FTP("ftp://"),
    FTPS("ftps://"),
    SFTP("sftp://");

    private final String prefix;

    VFSType(String prefix) {
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public boolean in(VFSType... types) {
        return types != null && ArrayUtils.contains(types, this);
    }
}
