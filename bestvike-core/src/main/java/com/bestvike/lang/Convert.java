package com.bestvike.lang;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by 许崇雷 on 2016/6/17.
 * 类型转换
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class Convert {
    private static final boolean BOOLEAN_DEFAULT = false;
    private static final byte BYTE_DEFAULT = 0;
    private static final short SHORT_DEFAULT = 0;
    private static final int INT_DEFAULT = 0;
    private static final long LONG_DEFAULT = 0L;
    private static final float FLOAT_DEFAULT = 0f;
    private static final double DOUBLE_DEFAULT = 0d;
    private static final BigDecimal DECIMAL_DEFAULT = BigDecimal.ZERO;
    private static final char CHAR_DEFAULT = 0;
    private static final String STRING_DEFAULT = StringUtils.EMPTY;

    private static final ThreadLocal<SimpleDateFormat> FORMAT_DATE = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd"));
    private static final ThreadLocal<SimpleDateFormat> FORMAT_DATE_TIME = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    private static final ThreadLocal<SimpleDateFormat> FORMAT_DATE_TIME_MS = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS"));

    //调用对象的方法,无参数
    @SuppressWarnings("unchecked")
    private static <T> T invoke(Object value, String methodName) throws Exception {
        Method method = value.getClass().getMethod(methodName);
        return (T) method.invoke(value);
    }

    //创建类型的实例,一个参数
    private static <T> T create(Class<T> clazz, Object arg0) throws Exception {
        Constructor<T> constructor = clazz.getConstructor(arg0.getClass());
        return constructor.newInstance(arg0);
    }

    //获取最后一个点后的字符串
    private static String getShortClassName(String clazzName) {
        int dotIndex = clazzName.lastIndexOf(".");
        if (dotIndex > -1)
            clazzName = clazzName.substring(dotIndex + 1);
        return clazzName;
    }


    //region as 拆箱,失败返回 null

    public static Boolean asBoolean(Object value) {
        try {
            return (Boolean) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Byte asByte(Object value) {
        try {
            return (Byte) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Short asShort(Object value) {
        try {
            return (Short) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer asInteger(Object value) {
        try {
            return (Integer) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Long asLong(Object value) {
        try {
            return (Long) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Float asFloat(Object value) {
        try {
            return (Float) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Double asDouble(Object value) {
        try {
            return (Double) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal asDecimal(Object value) {
        try {
            return (BigDecimal) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Character asCharacter(Object value) {
        try {
            return (Character) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static String asString(Object value) {
        try {
            return (String) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date asDate(Object value) {
        try {
            return (Date) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static Timestamp asTimestamp(Object value) {
        try {
            return (Timestamp) value;
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T asType(Object value, Class<T> clazz) {
        if (clazz == null)
            throw new NullPointerException("clazz can not be null");

        //noinspection unchecked
        return clazz.isInstance(value) ? (T) value : null;
    }

    //endregion


    //region to 转换,失败抛出异常

    public static boolean toBoolean(Object value) {
        if (value instanceof Boolean)
            return (Boolean) value;
        String str = toString(value);
        switch (str.length()) {
            case 1: {
                char ch0 = str.charAt(0);
                if (ch0 == 'y' || ch0 == 'Y' || ch0 == 't' || ch0 == 'T')
                    return true;
                if (ch0 == 'n' || ch0 == 'N' || ch0 == 'f' || ch0 == 'F')
                    return false;
                break;
            }
            case 2: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                if ((ch0 == 'o' || ch0 == 'O') && (ch1 == 'n' || ch1 == 'N'))
                    return true;
                if ((ch0 == 'n' || ch0 == 'N') && (ch1 == 'o' || ch1 == 'O'))
                    return false;
                break;
            }
            case 3: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char ch2 = str.charAt(2);
                if ((ch0 == 'y' || ch0 == 'Y') && (ch1 == 'e' || ch1 == 'E') && (ch2 == 's' || ch2 == 'S'))
                    return true;
                if ((ch0 == 'o' || ch0 == 'O') && (ch1 == 'f' || ch1 == 'F') && (ch2 == 'f' || ch2 == 'F'))
                    return false;
                break;
            }
            case 4: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char ch2 = str.charAt(2);
                char ch3 = str.charAt(3);
                if ((ch0 == 't' || ch0 == 'T') && (ch1 == 'r' || ch1 == 'R') && (ch2 == 'u' || ch2 == 'U') && (ch3 == 'e' || ch3 == 'E'))
                    return true;
                break;
            }
            case 5: {
                char ch0 = str.charAt(0);
                char ch1 = str.charAt(1);
                char ch2 = str.charAt(2);
                char ch3 = str.charAt(3);
                char ch4 = str.charAt(4);
                if ((ch0 == 'f' || ch0 == 'F') && (ch1 == 'a' || ch1 == 'A') && (ch2 == 'l' || ch2 == 'L') && (ch3 == 's' || ch3 == 'S') && (ch4 == 'e' || ch4 == 'E'))
                    return true;
                break;
            }
        }
        throw new ClassCastException(String.format("can not cast '%s' to boolean", value));
    }

    public static byte toByte(Object value) {
        return value instanceof Byte ? (Byte) value : Byte.parseByte(toString(value));
    }

    public static short toShort(Object value) {
        return value instanceof Short ? (Short) value : Short.parseShort(toString(value));
    }

    public static int toInteger(Object value) {
        return value instanceof Integer ? (Integer) value : Integer.parseInt(toString(value));
    }

    public static long toLong(Object value) {
        return value instanceof Long ? (Long) value : Long.parseLong(toString(value));
    }

    public static float toFloat(Object value) {
        return value instanceof Float ? (Float) value : Float.parseFloat(toString(value));
    }

    public static double toDouble(Object value) {
        return value instanceof Double ? (Double) value : Double.parseDouble(toString(value));
    }

    public static BigDecimal toDecimal(Object value) {
        return value instanceof BigDecimal ? (BigDecimal) value : new BigDecimal(toString(value));
    }

    public static char toCharacter(Object value) {
        if (value instanceof Character)
            return (Character) value;
        String str = toString(value);
        if (str.length() == 1)
            return str.charAt(0);
        throw new ClassCastException(String.format("can not cast '%s' to char", value));
    }

    public static String toString(Object value) {
        if (value == null)
            return StringUtils.EMPTY;
        String str = value.toString();
        return str == null ? StringUtils.EMPTY : str;
    }

    public static Date toDate(Object value) {
        if (value instanceof Date)
            return (Date) value;
        String str = toString(value);
        if (StringUtils.isEmpty(str))
            throw new ClassCastException(String.format("can not cast '%s' to Date", value));
        Long num = nullLong(value);
        try {
            return num == null ? (str.length() < 11 ? FORMAT_DATE.get().parse(str) : (str.length() < 20 ? FORMAT_DATE_TIME.get().parse(str) : FORMAT_DATE_TIME_MS.get().parse(str))) : new Date(num);
        } catch (Exception e) {
            throw new ClassCastException(String.format("can not convert '%s' to Date", value));
        }
    }

    public static Timestamp toTimestamp(Object value) {
        if (value instanceof Timestamp)
            return (Timestamp) value;
        String str = toString(value);
        if (StringUtils.isEmpty(str))
            throw new ClassCastException(String.format("can not cast '%s' to Timestamp", value));
        Long num = nullLong(str);
        try {
            return new Timestamp(num == null ? (str.length() < 11 ? FORMAT_DATE.get().parse(str) : (str.length() < 20 ? FORMAT_DATE_TIME.get().parse(str) : FORMAT_DATE_TIME_MS.get().parse(str))).getTime() : num);
        } catch (Exception e) {
            throw new ClassCastException(String.format("can not convert '%s' to Timestamp", value));
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T toType(Object value, Class<T> clazz) {
        if (clazz == null)
            throw new NullPointerException("clazz can not be null");

        if (clazz.isInstance(value))
            return (T) value;

        Object ret = null;
        if (clazz.equals(boolean.class) || clazz.equals(Boolean.class))
            ret = toBoolean(value);
        else if (clazz.equals(byte.class) || clazz.equals(Byte.class))
            ret = toByte(value);
        else if (clazz.equals(short.class) || clazz.equals(Short.class))
            ret = toShort(value);
        else if (clazz.equals(int.class) || clazz.equals(Integer.class))
            ret = toInteger(value);
        else if (clazz.equals(long.class) || clazz.equals(Long.class))
            ret = toLong(value);
        else if (clazz.equals(float.class) || clazz.equals(Float.class))
            ret = toFloat(value);
        else if (clazz.equals(double.class) || clazz.equals(Double.class))
            ret = toDouble(value);
        else if (clazz.equals(char.class) || clazz.equals(Character.class))
            ret = toCharacter(value);
        else if (clazz.equals(String.class))
            ret = toString(value);
        else if (Timestamp.class.isAssignableFrom(clazz))
            ret = toTimestamp(value);
        else if (Date.class.isAssignableFrom(clazz))
            ret = toDate(value);

        if (ret != null)
            return (T) ret;

        try {
            String shortClassName = getShortClassName(clazz.getName());
            return invoke(value, StringUtils.decapitalize(shortClassName) + "Value");
        } catch (Exception ignored) {
        }

        try {
            return create(clazz, value);
        } catch (Exception ignored) {
        }

        throw new ClassCastException("can not convert \"" + value + "\" to " + clazz.getName());
    }

    //endregion


    //region null 转换,失败返回 null

    public static Boolean nullBoolean(Object value) {
        try {
            return toBoolean(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Byte nullByte(Object value) {
        try {
            return toByte(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Short nullShort(Object value) {
        try {
            return toShort(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer nullInteger(Object value) {
        try {
            return toInteger(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Long nullLong(Object value) {
        try {
            return toLong(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Float nullFloat(Object value) {
        try {
            return toFloat(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Double nullDouble(Object value) {
        try {
            return toDouble(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static BigDecimal nullDecimal(Object value) {
        try {
            return toDecimal(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Character nullCharacter(Object value) {
        try {
            return toCharacter(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static String nullString(Object value) {
        try {
            String str = toString(value);
            return StringUtils.isEmpty(str) ? null : str;
        } catch (Exception e) {
            return null;
        }
    }

    public static Date nullDate(Object value) {
        try {
            return toDate(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static Timestamp nullTimestamp(Object value) {
        try {
            return toTimestamp(value);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T nullType(Object value, Class<T> clazz) {
        try {
            return toType(value, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    //endregion


    //region default 转换,失败返回默认值

    public static boolean defaultBoolean(Object value) {
        return defaultBoolean(value, BOOLEAN_DEFAULT);
    }

    public static byte defaultByte(Object value) {
        return defaultByte(value, BYTE_DEFAULT);
    }

    public static short defaultShort(Object value) {
        return defaultShort(value, SHORT_DEFAULT);
    }

    public static int defaultInteger(Object value) {
        return defaultInteger(value, INT_DEFAULT);
    }

    public static long defaultLong(Object value) {
        return defaultLong(value, LONG_DEFAULT);
    }

    public static float defaultFloat(Object value) {
        return defaultFloat(value, FLOAT_DEFAULT);
    }

    public static double defaultDouble(Object value) {
        return defaultDouble(value, DOUBLE_DEFAULT);
    }

    public static BigDecimal defaultDecimal(Object value) {
        return defaultDecimal(value, DECIMAL_DEFAULT);
    }

    public static char defaultCharacter(Object value) {
        return defaultCharacter(value, CHAR_DEFAULT);
    }

    public static String defaultString(Object value) {
        return defaultString(value, STRING_DEFAULT);
    }

    public static Date defaultDate(Object value) {
        return defaultDate(value, DateUtils.now());
    }

    public static Timestamp defaultTimestamp(Object value) {
        return defaultTimestamp(value, DateUtils.now());
    }

    public static <T> T defaultType(Object value, Class<T> clazz) {
        return defaultType(value, null, clazz);
    }

    //endregion


    //region default 转换,失败返回 指定值

    public static boolean defaultBoolean(Object value, boolean defaultValue) {
        try {
            return toBoolean(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static byte defaultByte(Object value, byte defaultValue) {
        try {
            return toByte(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static short defaultShort(Object value, short defaultValue) {
        try {
            return toShort(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static int defaultInteger(Object value, int defaultValue) {
        try {
            return toInteger(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static long defaultLong(Object value, long defaultValue) {
        try {
            return toLong(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static float defaultFloat(Object value, float defaultValue) {
        try {
            return toFloat(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static double defaultDouble(Object value, double defaultValue) {
        try {
            return toDouble(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static BigDecimal defaultDecimal(Object value, BigDecimal defaultValue) {
        try {
            return toDecimal(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static char defaultCharacter(Object value, char defaultValue) {
        try {
            return toCharacter(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static String defaultString(Object value, String defaultValue) {
        try {
            String str = toString(value);
            return StringUtils.isEmpty(str) ? defaultValue : str;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Date defaultDate(Object value, Date defaultValue) {
        try {
            return toDate(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static Timestamp defaultTimestamp(Object value, Timestamp defaultValue) {
        try {
            return toTimestamp(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static <T> T defaultType(Object value, T defaultValue, Class<T> clazz) {
        if (clazz == null)
            throw new NullPointerException("clazz can not be null");

        if (defaultValue != null && !clazz.isInstance(defaultValue))
            throw new ClassCastException("defaultValue:\"" + defaultValue + "\" must be instance of " + clazz.getName());

        try {
            return toType(value, clazz);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    //endregion
}
