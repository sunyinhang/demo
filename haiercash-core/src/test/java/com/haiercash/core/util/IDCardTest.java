package com.haiercash.core.util;

import com.haiercash.core.lang.DateUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2017-12-27.
 */
public class IDCardTest {
    private static final String ID_COMMON15 = "430304700815461";
    private static final String ID_COMMON18 = "430304197008154615";
    private static final String ID_HONGKONG = "W119963(6)";
    private static final String ID_MACAU = "5215299(8)";
    private static final String ID_TAIWAN = "X130819721";

    @Test
    public void common15Test() {
        IDCard idCard = new IDCard(ID_COMMON15);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IDCard.CardType.COMMON15, idCard.getCardType());
        Assert.assertEquals("湖南", idCard.getProvince());
        Assert.assertEquals("1970-08-15", DateUtils.toDateString(idCard.getBirthday()));
        Assert.assertEquals(IDCard.Gender.MALE, idCard.getGender());

        IDCard idCard18 = idCard.toCommon18();
        Assert.assertTrue(idCard18.isValid());
        Assert.assertEquals(ID_COMMON18, idCard18.getIdNo());

        IDCard idCardError = new IDCard("43030470081546X");
        Assert.assertFalse(idCardError.isValid());
    }

    @Test
    public void common18Test() {
        IDCard idCard = new IDCard(ID_COMMON18);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IDCard.CardType.COMMON18, idCard.getCardType());
        Assert.assertEquals("湖南", idCard.getProvince());
        Assert.assertEquals("1970-08-15", DateUtils.toDateString(idCard.getBirthday()));
        Assert.assertEquals(IDCard.Gender.MALE, idCard.getGender());

        IDCard idCard18 = idCard.toCommon18();
        Assert.assertTrue(idCard18.isValid());
        Assert.assertEquals(ID_COMMON18, idCard18.getIdNo());

        IDCard idCardError = new IDCard("430304197008154614");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("校验位错误", idCardError.getInvalidReason());
    }

    @Test
    public void hongKongTest() {
        IDCard idCard = new IDCard(ID_HONGKONG);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IDCard.CardType.HONGKONG, idCard.getCardType());
        try {
            idCard.getProvince();
            Assert.fail("should not get province");
        } catch (Exception ignored) {
        }
        try {
            idCard.getBirthday();
            Assert.fail("should not get birthday");
        } catch (Exception ignored) {
        }
        try {
            idCard.getGender();
            Assert.fail("should not get gender");
        } catch (Exception ignored) {
        }
        try {
            idCard.toCommon18();
            Assert.fail("should not get to common 18");
        } catch (Exception ignored) {
        }

        IDCard idCardError = new IDCard("W119963(5)");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("香港身份证校验位错误", idCardError.getInvalidReason());
    }

    @Test
    public void maCauTest() {
        IDCard idCard = new IDCard(ID_MACAU);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IDCard.CardType.MACAU, idCard.getCardType());
        try {
            idCard.getProvince();
            Assert.fail("should not get province");
        } catch (Exception ignored) {
        }
        try {
            idCard.getBirthday();
            Assert.fail("should not get birthday");
        } catch (Exception ignored) {
        }
        try {
            idCard.getGender();
            Assert.fail("should not get gender");
        } catch (Exception ignored) {
        }
        try {
            idCard.toCommon18();
            Assert.fail("should not get to common 18");
        } catch (Exception ignored) {
        }

        IDCard idCardError = new IDCard("5215299($)");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("澳门身份证第 9 位必须为数字或字母", idCardError.getInvalidReason());
    }

    @Test
    public void taiWanTest() {
        IDCard idCard = new IDCard(ID_TAIWAN);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IDCard.CardType.TAIWAN, idCard.getCardType());
        try {
            idCard.getProvince();
            Assert.fail("should not get province");
        } catch (Exception ignored) {
        }
        try {
            idCard.getBirthday();
            Assert.fail("should not get birthday");
        } catch (Exception ignored) {
        }
        Assert.assertEquals(IDCard.Gender.MALE, idCard.getGender());
        try {
            idCard.toCommon18();
            Assert.fail("should not get to common 18");
        } catch (Exception ignored) {
        }

        IDCard idCardError = new IDCard("X130819720");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("台湾身份证校验位错误", idCardError.getInvalidReason());
    }
}
