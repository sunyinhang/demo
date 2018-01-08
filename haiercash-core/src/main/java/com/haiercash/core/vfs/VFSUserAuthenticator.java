package com.haiercash.core.vfs;

import com.bestvike.linq.exception.ArgumentOutOfRangeException;
import lombok.Data;
import org.springframework.util.Assert;

/**
 * Created by 许崇雷 on 2018-01-08.
 */
@Data
public final class VFSUserAuthenticator {
    private static final VFSType[] NET_VFS = {VFSType.HTTP, VFSType.HTTPS, VFSType.FTP, VFSType.FTPS, VFSType.SFTP};

    private final String url;
    private VFSType type;
    private String host;
    private Integer port;
    private String username;
    private String password;

    public VFSUserAuthenticator(VFSType type) {
        Assert.notNull(type, "type can not be null");
        if (!type.in(VFSType.LOCAL))
            throw new ArgumentOutOfRangeException("not supported type");

        this.type = type;
        this.url = this.type.getPrefix();
    }

    public VFSUserAuthenticator(VFSType type, String host) {
        Assert.notNull(type, "type can not be null");
        Assert.notNull(host, "host can not be null");
        if (!type.in(NET_VFS))
            throw new ArgumentOutOfRangeException("not supported type");

        this.type = type;
        this.host = host;
        this.url = String.format("%s%s", this.type.getPrefix(), this.host);
    }

    public VFSUserAuthenticator(VFSType type, String host, int port) {
        Assert.notNull(type, "type can not be null");
        Assert.notNull(host, "host can not be null");
        if (!type.in(NET_VFS))
            throw new ArgumentOutOfRangeException("not supported type");

        this.type = type;
        this.host = host;
        this.port = port;
        this.url = String.format("%s%s:%d", this.type.getPrefix(), this.host, this.port);
    }

    public VFSUserAuthenticator(VFSType type, String host, String username) {
        Assert.notNull(type, "type can not be null");
        Assert.notNull(host, "host can not be null");
        Assert.notNull(username, "username can not be null");
        if (!type.in(NET_VFS))
            throw new ArgumentOutOfRangeException("not supported type");

        this.type = type;
        this.host = host;
        this.username = username;
        this.url = String.format("%s%s@%s", this.type.getPrefix(), this.username, this.host);
    }

    public VFSUserAuthenticator(VFSType type, String host, int port, String username) {
        Assert.notNull(type, "type can not be null");
        Assert.notNull(host, "host can not be null");
        Assert.notNull(username, "username can not be null");
        if (!type.in(NET_VFS))
            throw new ArgumentOutOfRangeException("not supported type");

        this.type = type;
        this.host = host;
        this.port = port;
        this.username = username;
        this.url = String.format("%s%s@%s:%d", this.type.getPrefix(), this.username, this.host, this.port);
    }

    public VFSUserAuthenticator(VFSType type, String host, String username, String password) {
        Assert.notNull(type, "type can not be null");
        Assert.notNull(host, "host can not be null");
        Assert.notNull(username, "username can not be null");
        Assert.notNull(password, "password can not be null");
        if (!type.in(NET_VFS))
            throw new ArgumentOutOfRangeException("not supported type");

        this.type = type;
        this.host = host;
        this.username = username;
        this.password = password;
        this.url = String.format("%s%s:%s@%s", this.type.getPrefix(), this.username, this.password, this.host);
    }

    public VFSUserAuthenticator(VFSType type, String host, int port, String username, String password) {
        Assert.notNull(type, "type can not be null");
        Assert.notNull(host, "host can not be null");
        Assert.notNull(username, "username can not be null");
        Assert.notNull(password, "password can not be null");
        if (!type.in(NET_VFS))
            throw new ArgumentOutOfRangeException("not supported type");

        this.type = type;
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.url = String.format("%s%s:%s@%s:%d", this.type.getPrefix(), this.username, this.password, this.host, this.port);
    }
}
