package com.haiercash.core.lang;

import com.bestvike.linq.exception.InvalidOperationException;

/**
 * Created by 许崇雷 on 2017-11-24.
 */
public abstract class Cloneable<T> implements java.lang.Cloneable {
    @Override
    @SuppressWarnings({"unchecked", "CloneDoesntDeclareCloneNotSupportedException"})
    public T clone() {
        try {
            return (T) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InvalidOperationException("clone() method is not supported.");
        }
    }
}
