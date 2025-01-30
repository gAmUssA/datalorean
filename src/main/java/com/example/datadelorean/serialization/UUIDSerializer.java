package com.example.datadelorean.serialization;

import org.apache.flink.api.common.typeutils.SimpleTypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.TypeSerializerSnapshot;
import org.apache.flink.api.common.typeutils.base.TypeSerializerSingleton;
import org.apache.flink.core.memory.DataInputView;
import org.apache.flink.core.memory.DataOutputView;

import java.io.IOException;
import java.util.UUID;

public class UUIDSerializer extends TypeSerializerSingleton<UUID> {

    public static final UUIDSerializer INSTANCE = new UUIDSerializer();

    @Override
    public boolean isImmutableType() {
        return true;
    }

    @Override
    public UUID createInstance() {
        return new UUID(0L, 0L);
    }

    @Override
    public UUID copy(UUID from) {
        return from;
    }

    @Override
    public UUID copy(UUID from, UUID reuse) {
        return from;
    }

    @Override
    public int getLength() {
        return 16; // 2 longs, 8 bytes each
    }

    @Override
    public void serialize(UUID record, DataOutputView target) throws IOException {
        target.writeLong(record.getMostSignificantBits());
        target.writeLong(record.getLeastSignificantBits());
    }

    @Override
    public UUID deserialize(DataInputView source) throws IOException {
        long most = source.readLong();
        long least = source.readLong();
        return new UUID(most, least);
    }

    @Override
    public UUID deserialize(UUID reuse, DataInputView source) throws IOException {
        return deserialize(source);
    }

    @Override
    public void copy(DataInputView source, DataOutputView target) throws IOException {
        target.writeLong(source.readLong());
        target.writeLong(source.readLong());
    }

    @Override
    public TypeSerializerSnapshot<UUID> snapshotConfiguration() {
        return new UUIDSerializerSnapshot();
    }

    public static class UUIDSerializerSnapshot extends SimpleTypeSerializerSnapshot<UUID> {
        public UUIDSerializerSnapshot() {
            super(UUIDSerializer::new);
        }
    }
}