package com.haiercash.core.vfs;

import com.bestvike.linq.exception.InvalidOperationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;

/**
 * Created by 许崇雷 on 2018-01-08.
 */
public final class VFSUtils {
    private static final FileSystemManager MANAGER;

    static {
        try {
            MANAGER = VFS.getManager();
        } catch (FileSystemException e) {
            throw new InvalidOperationException("create file system manager failed", e);
        }
    }

    private VFSUtils() {
    }

    public static FileObject resolveFile(VFSUserAuthenticator authenticator, String path) throws FileSystemException {
        return MANAGER.resolveFile(authenticator.getUrl() + path);
    }
}
