package com.haiercash.core.lang;

/**
 * Created by 许崇雷 on 2016/7/9.
 */
public final class ThrowableUtils {
    /**
     * 获取异常详细信息
     *
     * @param throwable
     * @return
     */
    public static String getString(Throwable throwable) {
        if (throwable == null)
            return StringUtils.EMPTY;
        StringBuilder builder = new StringBuilder();
        String clazzName = throwable.getClass().getName();
        builder.append(clazzName).append(": ");
        String message = throwable.getLocalizedMessage();
        if (StringUtils.isEmpty(message))
            builder.append("引发类型为“").append(clazzName).append("”的异常。");
        else
            builder.append(message);
        Throwable innerThrowable = throwable.getCause();
        if (innerThrowable != null)
            builder.append(" ---> ").append(getString(innerThrowable)).append(Environment.NewLine).append("   --- 内部异常堆栈跟踪的结尾 ---");
        StackTraceElement[] traces = throwable.getStackTrace();
        if (traces != null)
            for (StackTraceElement trace : traces)
                builder.append(Environment.NewLine).append("   在 ").append(trace);
        return builder.toString();
    }

    /**
     * 获取异常简略信息
     *
     * @param throwable
     * @return
     */
    public static String getMessage(Throwable throwable) {
        if (throwable == null)
            return StringUtils.EMPTY;
        String message = throwable.getLocalizedMessage();
        return StringUtils.isEmpty(message) ? "引发类型为“" + throwable.getClass().getName() + "”的异常。" : message;
    }
}
