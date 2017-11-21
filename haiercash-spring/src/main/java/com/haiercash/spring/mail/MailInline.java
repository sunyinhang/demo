package com.haiercash.spring.mail;

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
public final class MailInline {
    private String contentId;
    private File file;
}
