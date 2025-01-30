package com.example.datadelorean.serialization;

import com.example.datadelorean.model.CustomerEvent;
import org.apache.flink.api.common.typeinfo.TypeInfoFactory;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.typeutils.PojoField;
import org.apache.flink.api.java.typeutils.PojoTypeInfo;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CustomerEventTypeInfoFactory extends TypeInfoFactory<CustomerEvent> {

    @Override
    public TypeInformation<CustomerEvent> createTypeInfo(Type t, Map<String, TypeInformation<?>> genericParameters) {
        List<PojoField> fields = new ArrayList<>();
        
        fields.add(new PojoField(getField(CustomerEvent.class, "eventId"), UUIDTypeInfo.INSTANCE));
        fields.add(new PojoField(getField(CustomerEvent.class, "customerId"), Types.STRING));
        fields.add(new PojoField(getField(CustomerEvent.class, "eventType"), Types.STRING));
        fields.add(new PojoField(getField(CustomerEvent.class, "timestamp"), Types.INSTANT));
        fields.add(new PojoField(getField(CustomerEvent.class, "data"), Types.STRING));
        fields.add(new PojoField(getField(CustomerEvent.class, "schemaVersion"), Types.INT));

        return new PojoTypeInfo<>(CustomerEvent.class, fields);
    }

    private static java.lang.reflect.Field getField(Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Could not find field " + fieldName + " in class " + clazz.getName(), e);
        }
    }
}