package com.haiercash.payplatform.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by yu jianwei on 2017/11/8
 *
 * @Description:
 */
public class ImageUtil {
    /**
     * @Title zipImageFile
     * @Description: 图片压缩
     * @author yu jianwei
     * @date 2017/11/8 14:10
     */
    public static void zipImageFile(File oldFile, File newFile, int width, int height, float quality) {
        if (oldFile == null) {
            return;
        }
        try {
            /** 对服务器上的临时文件进行处理 */
            Image srcFile = ImageIO.read(oldFile);
            int w = srcFile.getWidth(null);
            int h = srcFile.getHeight(null);
            double bili;
            if (width > 0) {
                bili = width / (double) w;
                height = (int) (h * bili);
            } else {
                if (height > 0) {
                    bili = height / (double) h;
                    width = (int) (w * bili);
                }
            }

            String srcImgPath = newFile.getAbsoluteFile().toString();
            System.out.println(srcImgPath);
            String subfix = "jpg";
            subfix = srcImgPath.substring(srcImgPath.lastIndexOf(".") + 1, srcImgPath.length());

            BufferedImage buffImg = null;
            if (subfix.equals("png")) {
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            } else {
                buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

            Graphics2D graphics = buffImg.createGraphics();
            graphics.setBackground(new Color(255, 255, 255));
            graphics.setColor(new Color(255, 255, 255));
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(srcFile.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null);

            ImageIO.write(buffImg, subfix, new File(srcImgPath));

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
