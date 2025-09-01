package com.glygateway.service.triton.core;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Component;
import com.google.protobuf.ByteString;

@Component
public abstract class BufferCodec {

  public static ByteString encodeStringToBytes(String textToken) {
    // BYTES encoding: [uint32 / 4 bytes for data length][bytes of text] repeated;
    // little-endian
    var text_bytes = textToken.getBytes(StandardCharsets.UTF_8);
    int total = 4 + text_bytes.length;

    ByteBuffer buf = ByteBuffer.allocate(total).order(ByteOrder.LITTLE_ENDIAN);

    byte[] b = text_bytes;
    buf.putInt(b.length);
    buf.put(b);

    // Reset the pointer from writing mode to reading mode
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  public static ByteString encodeInt32ToBytes(int value) {
    // BYTES encoding: [int32 / 4 bytes]
    ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    buf.putInt(value);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  public static ByteString encodeLong64ToBytes(long value) {
    // BYTES encoding: [int64 / 8 bytes]
    ByteBuffer buf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
    buf.putLong(value);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  public static ByteString encodeFloat32ToBytes(float value) {
    // BYTES encoding: [float32 / 4 bytes]
    ByteBuffer buf = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
    buf.putFloat(value);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  public static ByteString encodeBoolToBytes(boolean value) {
    // BYTES encoding: [boolean / 1 byte]
    // 1 for true and 0 for false
    ByteBuffer buf = ByteBuffer.allocate(1).order(ByteOrder.LITTLE_ENDIAN);
    buf.put(value ? (byte) 1 : (byte) 0);
    buf.flip();
    return ByteString.copyFrom(buf);
  }

  public static String decodeBytesToString(ByteString blob) {
    ByteBuffer buf = blob.asReadOnlyByteBuffer().order(ByteOrder.LITTLE_ENDIAN);
    // Read and advance by 4 bytes (1 int)
    int len = buf.getInt();
    byte[] b = new byte[len];
    buf.get(b);
    var text_output = new String(b, StandardCharsets.UTF_8);
    return text_output;
  }

}
