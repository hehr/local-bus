package com.hehr.lib.protocol;

import android.util.Log;

import com.hehr.lib.protocol.multipart.Multipart;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MultipartDecoder extends ByteToMessageDecoder {

    private static final String TAG = "MultipartDecoder";

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

        if (in.readableBytes() <= 4) {
            return;
        }

        int length = in.readInt();

        if (length < 20) {
            Log.d(TAG, "drop illegal multipart .");
            return;
        }

        byte[] dst = new byte[length];

        in.readBytes(dst);

        out.add(Multipart.decode(dst));

    }

}
