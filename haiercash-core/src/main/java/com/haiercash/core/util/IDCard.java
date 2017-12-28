package com.haiercash.core.util;

import com.bestvike.linq.exception.InvalidOperationException;
import com.haiercash.core.lang.DateUtils;
import com.haiercash.core.lang.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 许崇雷 on 2017-12-11.
 */
public final class IDCard {
    private static final int[] COMMON_POWER = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    private static final char[] COMMON_VERIFY = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
    private static final Map<String, String> COMMON_PROVINCES = new HashMap<>();
    private static final Map<Character, Integer> HONGKONG_TYPES = new HashMap<>();
    private static final Map<Character, Integer> MACAU_TYPES = new HashMap<>();
    private static final Map<Character, Integer> TAIWAN_CITIES = new HashMap<>();

    static {
        //大陆省份
        COMMON_PROVINCES.put("11", "北京");
        COMMON_PROVINCES.put("12", "天津");
        COMMON_PROVINCES.put("13", "河北");
        COMMON_PROVINCES.put("14", "山西");
        COMMON_PROVINCES.put("15", "内蒙古");
        COMMON_PROVINCES.put("21", "辽宁");
        COMMON_PROVINCES.put("22", "吉林");
        COMMON_PROVINCES.put("23", "黑龙江");
        COMMON_PROVINCES.put("31", "上海");
        COMMON_PROVINCES.put("32", "江苏");
        COMMON_PROVINCES.put("33", "浙江");
        COMMON_PROVINCES.put("34", "安徽");
        COMMON_PROVINCES.put("35", "福建");
        COMMON_PROVINCES.put("36", "江西");
        COMMON_PROVINCES.put("37", "山东");
        COMMON_PROVINCES.put("41", "河南");
        COMMON_PROVINCES.put("42", "湖北");
        COMMON_PROVINCES.put("43", "湖南");
        COMMON_PROVINCES.put("44", "广东");
        COMMON_PROVINCES.put("45", "广西");
        COMMON_PROVINCES.put("46", "海南");
        COMMON_PROVINCES.put("50", "重庆");
        COMMON_PROVINCES.put("51", "四川");
        COMMON_PROVINCES.put("52", "贵州");
        COMMON_PROVINCES.put("53", "云南");
        COMMON_PROVINCES.put("54", "西藏");
        COMMON_PROVINCES.put("61", "陕西");
        COMMON_PROVINCES.put("62", "甘肃");
        COMMON_PROVINCES.put("63", "青海");
        COMMON_PROVINCES.put("64", "宁夏");
        COMMON_PROVINCES.put("65", "新疆");
        COMMON_PROVINCES.put("71", "台湾");
        COMMON_PROVINCES.put("81", "香港");
        COMMON_PROVINCES.put("82", "澳门");
        COMMON_PROVINCES.put("91", "国外");
        //香港证件类型
        HONGKONG_TYPES.put('A', 1);
        HONGKONG_TYPES.put('B', 2);
        HONGKONG_TYPES.put('C', 3);
        HONGKONG_TYPES.put('N', 14);
        HONGKONG_TYPES.put('O', 15);
        HONGKONG_TYPES.put('R', 18);
        HONGKONG_TYPES.put('U', 21);
        HONGKONG_TYPES.put('W', 23);
        HONGKONG_TYPES.put('X', 24);
        HONGKONG_TYPES.put('Z', 26);
        //澳门证件类型
        MACAU_TYPES.put('1', 0);
        MACAU_TYPES.put('5', 0);
        MACAU_TYPES.put('7', 0);
        //台湾城市
        TAIWAN_CITIES.put('A', 10);
        TAIWAN_CITIES.put('B', 11);
        TAIWAN_CITIES.put('C', 12);
        TAIWAN_CITIES.put('D', 13);
        TAIWAN_CITIES.put('E', 14);
        TAIWAN_CITIES.put('F', 15);
        TAIWAN_CITIES.put('G', 16);
        TAIWAN_CITIES.put('H', 17);
        TAIWAN_CITIES.put('J', 18);
        TAIWAN_CITIES.put('K', 19);
        TAIWAN_CITIES.put('L', 20);
        TAIWAN_CITIES.put('M', 21);
        TAIWAN_CITIES.put('N', 22);
        TAIWAN_CITIES.put('P', 23);
        TAIWAN_CITIES.put('Q', 24);
        TAIWAN_CITIES.put('R', 25);
        TAIWAN_CITIES.put('S', 26);
        TAIWAN_CITIES.put('T', 27);
        TAIWAN_CITIES.put('U', 28);
        TAIWAN_CITIES.put('V', 29);
        TAIWAN_CITIES.put('X', 30);
        TAIWAN_CITIES.put('Y', 31);
        TAIWAN_CITIES.put('W', 32);
        TAIWAN_CITIES.put('Z', 33);
        TAIWAN_CITIES.put('I', 34);
        TAIWAN_CITIES.put('O', 35);
    }

    private final String idNo;
    private boolean valid;
    private String invalidReason;
    private CardType cardType;

    //构造函数
    public IDCard(String idNo) {
        this.idNo = idNo;
        this.verify();
    }

    //region property

    //是否 0-9
    private static boolean isInt10(char ch) {
        return ch >= '0' && ch <= '9';
    }

    //转换 0-9 为数字
    private static int toInt10(char ch) {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        throw new IllegalArgumentException("The character " + ch + " is not in the range '0' - '9'");
    }

    //是否 0-9 或 A
    private static boolean isInt11(char ch) {
        return ch >= '0' && ch <= '9' || ch == 'A';
    }

    //转换 0-9 或 A 为数字
    private static int toInt11(char ch) {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        else if (ch == 'A')
            return 10;
        else
            throw new IllegalArgumentException("The character " + ch + " is not in the range '0' - '9' or 'A'");
    }

    //endregion

    //是否 0-9 或 A-Z
    private static boolean isInt36(char ch) {
        return ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'Z';
    }

    //转换 0-9 或 A-Z 为数字
    private static int toInt36(char ch) {
        if (ch >= '0' && ch <= '9')
            return ch - '0';
        else if (ch >= 'A' && ch <= 'Z')
            return ch - 'A' + 10;
        else
            throw new IllegalArgumentException("The character " + ch + " is not in the range '0' - '9' or 'A' - 'Z'");
    }

    public String getIdNo() {
        return idNo;
    }

    public boolean isValid() {
        return valid;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public CardType getCardType() {
        return cardType;
    }

    //获取省份
    public String getProvince() {
        if (!this.valid)
            throw new InvalidOperationException("无效的身份证");
        switch (this.cardType) {
            case COMMON15:
            case COMMON18:
                return COMMON_PROVINCES.get(this.idNo.substring(0, 2));
            default:
                throw new InvalidOperationException("只有大陆身份证才能获取省份");
        }
    }

    //获取生日
    public Date getBirthday() {
        if (!this.valid)
            throw new InvalidOperationException("无效的身份证");
        switch (this.cardType) {
            case COMMON15:
                return DateUtils.fromString("19" + this.idNo.substring(6, 12), "yyyyMMdd");
            case COMMON18:
                return DateUtils.fromString(this.idNo.substring(6, 14), "yyyyMMdd");
            default:
                throw new InvalidOperationException("只有大陆身份证才能获取出生日期");
        }
    }

    //获取周岁
    public int getAge() {
        return AgeUtils.getAge(this.getBirthday(), DateUtils.now());
    }

    //获取性别
    public Gender getGender() {
        if (!this.valid)
            throw new InvalidOperationException("无效的身份证");
        switch (this.cardType) {
            case COMMON15:
                return toInt10(this.idNo.charAt(14)) % 2 == 0 ? Gender.FEMALE : Gender.MALE;
            case COMMON18:
                return toInt10(this.idNo.charAt(16)) % 2 == 0 ? Gender.FEMALE : Gender.MALE;
            case TAIWAN:
                return this.idNo.charAt(1) == '1' ? Gender.MALE : Gender.FEMALE;
            default:
                throw new InvalidOperationException("只有大陆和台湾身份证才能获取性别");
        }
    }

    //获取性别代码,男:10 女:20
    public String getGenderCode() {
        return this.getGender() == Gender.MALE ? "10" : "20";
    }

    //转换为 18 位身份证
    public IDCard toCommon18() {
        if (!this.valid)
            throw new InvalidOperationException("无效的身份证");
        switch (this.cardType) {
            case COMMON15:
                String idNo = this.idNo.substring(0, 6) + "19" + this.idNo.substring(6);
                //校验位
                int sum = 0;
                for (int i = 0; i < 17; i++)
                    sum += toInt10(idNo.charAt(i)) * COMMON_POWER[i];
                char verifyCode = COMMON_VERIFY[sum % COMMON_VERIFY.length];
                return new IDCard(idNo + verifyCode);
            case COMMON18:
                return this;
            default:
                throw new InvalidOperationException("非大陆身份证不能转换");
        }
    }

    //校验
    private void verify() {
        if (StringUtils.isEmpty(this.idNo)) {
            this.invalidReason = "身份证号为空";
            this.valid = false;
            return;
        }
        //按长度简略分类
        switch (this.idNo.length()) {
            case 10:
                if (this.idNo.contains("(") && this.idNo.contains(")")) {
                    char first = this.idNo.charAt(0);
                    if (MACAU_TYPES.containsKey(first))
                        this.verifyMaCau();
                    else
                        this.verifyHongKong();
                    break;
                }
                this.verifyTaiWan();
                break;
            case 15:
                this.verifyCommon15();
                break;
            case 18:
                this.verifyCommon18();
                break;
            default:
                this.invalidReason = "身份证号位数错误";
                this.valid = false;
                break;
        }
    }

    //大陆 15 位身份证
    private void verifyCommon15() {
        //省份
        String province = this.idNo.substring(0, 2);
        if (!COMMON_PROVINCES.containsKey(province)) {
            this.invalidReason = "省份错误";
            this.valid = false;
            return;
        }

        //出生日期
        try {
            String birthday = "19" + this.idNo.substring(6, 12);
            LocalDate.parse(birthday, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            this.invalidReason = "出生日期错误";
            this.valid = false;
            return;
        }

        //格式
        for (int i = 0; i < 15; i++) {
            if (!isInt10(this.idNo.charAt(i))) {
                this.invalidReason = String.format("第 %d 位必须是数字", i + 1);
                this.valid = false;
                return;
            }
        }

        this.cardType = CardType.COMMON15;
        this.valid = true;
    }

    //大陆 18 位身份证
    private void verifyCommon18() {
        //省份
        String province = this.idNo.substring(0, 2);
        if (!COMMON_PROVINCES.containsKey(province)) {
            this.invalidReason = "省份错误";
            this.valid = false;
            return;
        }

        //出生日期
        try {
            String birthday = this.idNo.substring(6, 14);
            LocalDate.parse(birthday, DateTimeFormatter.BASIC_ISO_DATE);
        } catch (Exception e) {
            this.invalidReason = "出生日期错误";
            this.valid = false;
            return;
        }

        //格式
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            char ch = this.idNo.charAt(i);
            if (!isInt10(ch)) {
                this.invalidReason = String.format("第 %d 位必须是数字", i + 1);
                this.valid = false;
                return;
            }
            sum += toInt10(ch) * COMMON_POWER[i];
        }

        //校验位
        char verifyCode = COMMON_VERIFY[sum % COMMON_VERIFY.length];
        if (verifyCode != this.idNo.charAt(17)) {
            this.invalidReason = "校验位错误";
            this.valid = false;
            return;
        }

        this.cardType = CardType.COMMON18;
        this.valid = true;
    }

    //香港身份证
    private void verifyHongKong() {
        //类型
        char type = this.idNo.charAt(0);
        if (!HONGKONG_TYPES.containsKey(type)) {
            this.invalidReason = "香港身份证第 1 位必须是字母";
            this.valid = false;
            return;
        }

        //格式
        int sum = HONGKONG_TYPES.get(type) * 8;
        for (int i = 1; i < 7; i++) {
            char ch = this.idNo.charAt(i);
            if (!isInt10(ch)) {
                this.invalidReason = String.format("香港身份证第 %d 位必须是数字", i + 1);
                this.valid = false;
                return;
            }
            sum += toInt10(ch) * (8 - i);
        }
        if (this.idNo.charAt(7) != '(') {
            this.invalidReason = "香港身份证第 8 位必须是 '('";
            this.valid = false;
            return;
        }
        char last = this.idNo.charAt(8);
        if (!isInt11(last)) {
            this.invalidReason = "香港身份证第 9 位必须为数字或字母 'A'";
            this.valid = false;
            return;
        }
        if (this.idNo.charAt(9) != ')') {
            this.invalidReason = "香港身份证第 10 位必须是 ')'";
            this.valid = false;
            return;
        }

        //校验位
        sum += toInt11(last);
        if (sum % 11 != 0) {
            this.invalidReason = "香港身份证校验位错误";
            this.valid = false;
            return;
        }

        this.cardType = CardType.HONGKONG;
        this.valid = true;
    }

    //澳门身份证
    private void verifyMaCau() {
        //类型
        char type = this.idNo.charAt(0);
        if (!MACAU_TYPES.containsKey(type)) {
            this.invalidReason = "澳门身份证第 1 位必须是数字";
            this.valid = false;
            return;
        }

        //格式
        for (int i = 1; i < 7; i++) {
            char ch = this.idNo.charAt(i);
            if (!isInt10(ch)) {
                this.invalidReason = String.format("澳门身份证第 %d 位必须是数字", i + 1);
                this.valid = false;
                return;
            }
        }
        if (this.idNo.charAt(7) != '(') {
            this.invalidReason = "澳门身份证第 8 位必须是 '('";
            this.valid = false;
            return;
        }
        char last = this.idNo.charAt(8);
        if (!isInt36(last)) {
            this.invalidReason = "澳门身份证第 9 位必须为数字或字母";
            this.valid = false;
            return;
        }
        if (this.idNo.charAt(9) != ')') {
            this.invalidReason = "澳门身份证第 10 位必须是 ')'";
            this.valid = false;
            return;
        }

        //校验位
        //TODO 规则不明

        this.cardType = CardType.MACAU;
        this.valid = true;
    }

    //台湾身份证
    private void verifyTaiWan() {
        //城市
        char city = this.idNo.charAt(0);
        if (!TAIWAN_CITIES.containsKey(city)) {
            this.invalidReason = "台湾身份证第 1 位必须为字母";
            this.valid = false;
            return;
        }

        //性别
        char sex = this.idNo.charAt(1);
        if (sex != '1' && sex != '2') {
            this.invalidReason = "台湾身份证第 2 位必须为 '1' 或 '2'";
            this.valid = false;
            return;
        }

        //格式
        int cityValue = TAIWAN_CITIES.get(city);
        int sum = cityValue / 10 + cityValue % 10 * 9;
        for (int i = 1; i < 9; i++) {
            char ch = this.idNo.charAt(i);
            if (!isInt10(ch)) {
                this.invalidReason = String.format("台湾身份证第 %d 位不是数字", i + 1);
                this.valid = false;
                return;
            }
            sum += toInt10(ch) * (9 - i);
        }
        char last = this.idNo.charAt(9);
        if (!isInt10(last)) {
            this.invalidReason = "台湾身份证第 10 位不是数字";
            this.valid = false;
            return;
        }

        //校验位
        sum += toInt10(last);
        if (sum % 10 != 0) {
            this.invalidReason = "台湾身份证校验位错误";
            this.valid = false;
            return;
        }

        this.cardType = CardType.TAIWAN;
        this.valid = true;
    }

    //字符串
    @Override
    public String toString() {
        return this.valid
                ? String.format("%s: %s", this.cardType, this.idNo)
                : String.format("invalid(%s): %s", this.invalidReason, this.idNo);
    }

    /**
     * 性别
     */
    public enum Gender {
        /**
         * 男
         */
        MALE,
        /**
         * 女
         */
        FEMALE
    }

    /**
     * 身份证类型
     */
    public enum CardType {
        /**
         * 大陆 15 位身份证
         */
        COMMON15,
        /**
         * 大陆 18 位身份证
         */
        COMMON18,
        /**
         * 香港身份证
         */
        HONGKONG,
        /**
         * 澳门身份证
         */
        MACAU,
        /**
         * 台湾身份证
         */
        TAIWAN
    }
}
