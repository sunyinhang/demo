package com.haiercash.spring.mail.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.File;

/**
 * Created by 许崇雷 on 2017-11-21.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MailAttachment {
    private String fileName;
    private File file;
}
