package com.haiercash.spring.support;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Created by 许崇雷 on 2017-11-01.
 */
public class JSONObjectSerializer implements ObjectSerializer {
    /**
     * fastjson invokes this call-back method during serialization when it encounters a field of the
     * specified type.
     *
     * @param serializer
     * @param object     src the object that needs to be converted to Json.
     * @param fieldName  parent object field name
     * @param fieldType  parent object field type
     * @param features   parent object field serializer features
     * @throws IOException
     */
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) {
        if (object == null) {
            serializer.writeNull();
            return;
        }
        JSONObject orgJson = (JSONObject) object;
        orgJson.write(serializer.getWriter());
    }
}
