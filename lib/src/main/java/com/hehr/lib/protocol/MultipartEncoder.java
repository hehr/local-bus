package com.hehr.lib.protocol;

import com.hehr.lib.IBus;
import com.hehr.lib.protocol.multipart.Multipart;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;


public class MultipartEncoder extends MessageToByteEncoder<Multipart> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Multipart multipart, ByteBuf out) throws Exception {

        byte[] content = multipart.encode();//报文编码

        out.writeInt(content.length)//写入长度
                .writeBytes(content)//报文正体
                .writeBytes(IBus.DELIMITER.getBytes());//分隔符


    }

}
