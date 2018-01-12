package com.haiercash.core.zip;

import com.haiercash.core.lang.Base64Utils;
import org.junit.Assert;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

/**
 * Created by 许崇雷 on 2018-01-12.
 */
public class GZipUtilsTest {
    private static final String DEMO = "　　这几天心里颇不宁静。今晚在院子里坐着乘凉，忽然想起日日走过的荷塘，在这满月的光里，总该另有一番样子吧。月亮渐渐地升高了，墙外马路上孩子们的欢笑，已经听不见了；妻在屋里拍着闰儿，迷迷糊糊地哼着眠歌。我悄悄地披了大衫，带上门出去。\n" +
            "　　沿着荷塘，是一条曲折的小煤屑路。这是一条幽僻的路；白天也少人走，夜晚更加寂寞。荷塘四面，长着许多树，蓊蓊郁郁的。路的一旁，是些杨柳，和一些不知道名字的树。没有月光的晚上，这路上阴森森的，有些怕人。今晚却很好，虽然月光也还是淡淡的。\n" +
            "　　路上只我一个人，背着手踱着。这一片天地好像是我的；我也像超出了平常的自己，到了另一世界里。我爱热闹，也爱冷静；爱群居，也爱独处。像今晚上，一个人在这苍茫的月下，什么都可以想，什么都可以不想，便觉是个自由的人。白天里一定要做的事，一定要说的话，现在都可不理。这是独处的妙处，我且受用这无边的荷香月色好了。\n";

    @Test
    public void compress() {
        byte[] demoBytes = DEMO.getBytes(StandardCharsets.UTF_8);
        System.out.println(String.format("原始 串:%d 字节:%d", DEMO.length(), demoBytes.length));
        //压缩
        byte[] compressBytes = GZipUtils.compress(demoBytes);
        String encodeStr = Base64Utils.encode(compressBytes);
        System.out.println(String.format("压缩后 串:%d 字节:%d", encodeStr.length(), compressBytes.length));
        //解压
        byte[] decodeStr = Base64Utils.decode(encodeStr);
        byte[] uncompress = GZipUtils.decompress(decodeStr);
        String demo2 = new String(uncompress, StandardCharsets.UTF_8);
        System.out.println(String.format("解压后 串:%d 字节:%d", demo2.length(), uncompress.length));

        Assert.assertEquals(DEMO, demo2);
    }

    @Test
    public void decompress() {
    }
}
