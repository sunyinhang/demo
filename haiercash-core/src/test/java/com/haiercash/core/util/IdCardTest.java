package com.haiercash.core.util;

import com.haiercash.core.lang.DateUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by 许崇雷 on 2017-12-27.
 */
public class IdCardTest {
    private static final String ID_COMMON15 = "430304700815461";
    private static final String ID_COMMON18 = "430304197008154615";
    private static final String ID_HONGKONG = "W119963(6)";
    private static final String ID_MACAU = "5215299(8)";
    private static final String ID_TAIWAN = "X130819721";

    @Test
    public void common15Test() {
        IdCard idCard = new IdCard(ID_COMMON15);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IdCard.CardType.COMMON15, idCard.getCardType());
        Assert.assertEquals("湖南", idCard.getProvince());
        Assert.assertEquals("1970-08-15", DateUtils.toDateString(idCard.getBirthday()));
        Assert.assertEquals(IdCard.Gender.MALE, idCard.getGender());

        IdCard idCard18 = idCard.toCommon18();
        Assert.assertTrue(idCard18.isValid());
        Assert.assertEquals(ID_COMMON18, idCard18.getIdNo());

        IdCard idCardError = new IdCard("43030470081546X");
        Assert.assertFalse(idCardError.isValid());
    }

    @Test
    public void common18Test() {
        IdCard idCard = new IdCard(ID_COMMON18);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IdCard.CardType.COMMON18, idCard.getCardType());
        Assert.assertEquals("湖南", idCard.getProvince());
        Assert.assertEquals("1970-08-15", DateUtils.toDateString(idCard.getBirthday()));
        Assert.assertEquals(IdCard.Gender.MALE, idCard.getGender());

        IdCard idCard18 = idCard.toCommon18();
        Assert.assertTrue(idCard18.isValid());
        Assert.assertEquals(ID_COMMON18, idCard18.getIdNo());

        IdCard idCardError = new IdCard("430304197008154614");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("校验位错误", idCardError.getInvalidReason());
    }

    @Test
    public void hongKongTest() {
        IdCard idCard = new IdCard(ID_HONGKONG);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IdCard.CardType.HONGKONG, idCard.getCardType());
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

        IdCard idCardError = new IdCard("W119963(5)");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("香港身份证校验位错误", idCardError.getInvalidReason());
    }

    @Test
    public void maCauTest() {
        IdCard idCard = new IdCard(ID_MACAU);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IdCard.CardType.MACAU, idCard.getCardType());
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

        IdCard idCardError = new IdCard("5215299($)");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("澳门身份证第 9 位必须为数字或字母", idCardError.getInvalidReason());
    }

    @Test
    public void taiWanTest() {
        IdCard idCard = new IdCard(ID_TAIWAN);
        Assert.assertTrue(idCard.isValid());
        Assert.assertEquals(IdCard.CardType.TAIWAN, idCard.getCardType());
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
        Assert.assertEquals(IdCard.Gender.MALE, idCard.getGender());
        try {
            idCard.toCommon18();
            Assert.fail("should not get to common 18");
        } catch (Exception ignored) {
        }

        IdCard idCardError = new IdCard("X130819720");
        Assert.assertFalse(idCardError.isValid());
        Assert.assertEquals("台湾身份证校验位错误", idCardError.getInvalidReason());
    }
}
