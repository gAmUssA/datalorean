package com.example.datadelorean.serialization;

import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.common.typeutils.TypeSerializer;

import java.util.UUID;

public class UUIDTypeInfo extends TypeInformation<UUID> {

    public static final UUIDTypeInfo INSTANCE = new UUIDTypeInfo();

    @Override
    public boolean isBasicType() {
        return true;
    }

    @Override
    public boolean isTupleType() {
        return false;
    }

    @Override
    public int getArity() {
        return 1;
    }

    @Override
    public int getTotalFields() {
        return 1;
    }

    @Override
    public Class<UUID> getTypeClass() {
        return UUID.class;
    }

    @Override
    public boolean isKeyType() {
        return true;
    }

    @Override
    public TypeSerializer<UUID> createSerializer(ExecutionConfig config) {
        return UUIDSerializer.INSTANCE;
    }

    @Override
    public String toString() {
        return "UUID";
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UUIDTypeInfo;
    }

    @Override
    public int hashCode() {
        return UUID.class.hashCode();
    }

    @Override
    public boolean canEqual(Object obj) {
        return obj instanceof UUIDTypeInfo;
    }
}