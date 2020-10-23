package com.hehr.lib.netty;


import android.text.TextUtils;
import android.util.Log;

import com.hehr.lib.IBus;
import com.hehr.lib.protocol.multipart.Multipart;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;


@ChannelHandler.Sharable
public class EventDispatcher extends SimpleChannelInboundHandler<ByteBuf> {

    private static final String TAG = "bus";

    private EventDispatcher() {
    }

    private Map<String, Channel> innerLst = new HashMap<>();

    /**
     * subscribed topics lst
     */
    private Map<String, Set<String>> subscribeLst = new HashMap<>();


    /**
     * 分发处理报文
     *
     * @param ctx
     * @param dst
     * @throws UnsupportedEncodingException
     * @throws JSONException
     */
    private void dispatch(ChannelHandlerContext ctx, byte[] dst, int length) throws UnsupportedEncodingException, JSONException {

        Multipart multipart = Multipart.decode(dst, false);//半拆包

        final String name = multipart.getName();
        String topic = multipart.getTopic();
        int type = multipart.getType();
        switch (IBus.Type.findTypeByInteger(type)) {
            case join:
                if (innerLst != null) {
                    Log.i(TAG, " >>> " + name + " <<< " + " joined ");
                    innerLst.put(name, ctx.channel());
                }
                break;
            case subscribe:
                if (!TextUtils.isEmpty(topic) && innerLst.containsKey(name)) {
                    Log.i(TAG, " >>> " + name + " <<< " + " subscribed " + topic);
                    if (subscribeLst.containsKey(topic)) {
                        subscribeLst.get(topic).add(name);
                    } else {
                        subscribeLst.put(topic, new HashSet<String>() {{
                            add(name);
                        }});
                    }
                } else {
                    Log.e(TAG, " not found name " + " >>> " + name + " <<< ");
                }
                break;
            case broadcast:
                if (subscribeLst.containsKey(topic)) {
                    Set<String> targets = subscribeLst.get(topic);
                    for (String target : targets) {
                        if (innerLst.containsKey(target)) {
                            Log.d(TAG, " >>> bus-" + target + " <<< " + " received topic " + topic + " from " + " >>> " + name + " <<< ");
                            Channel channel = innerLst.get(target);
                            if (channel.isActive() && channel.isWritable()) {
                                ByteBuf buf = channel.alloc().buffer();
                                buf.writeInt(length);
                                buf.writeBytes(dst);
                                buf.writeBytes(IBus.DELIMITER.getBytes());
                                channel.writeAndFlush(buf);//转发原始报文
                            } else {
                                Log.e(TAG, name + " channel is close , drop topic " + topic);
                            }

                        } else {
                            Log.e(TAG, " discard " + name + " , " + multipart);
                        }
                    }
                } else {
                    Log.e(TAG, " no client subscribe this topic " + " >>> " + topic + " <<< ");
                }
                break;
            case unsubscribe:
                if (!TextUtils.isEmpty(name) && !TextUtils.isEmpty(topic)) {
                    if (subscribeLst.containsKey(topic)) {
                        Log.i(TAG, " >>> " + name + " <<< " + " unsubscribe " + topic);
                        subscribeLst.get(topic).remove(name);
                        if (subscribeLst.get(topic).isEmpty()) {
                            subscribeLst.remove(topic);
                        }
                    } else {
                        Log.e(TAG, topic + " have not subscribed. ");
                    }
                }
                break;
            case exit:
                if (innerLst.containsKey(name) && multipart.getType() == IBus.Type.exit.value) {
                    Log.w(TAG, " >>> " + name + " <<< " + " exit ...");
                    for (Iterator<String> iteratorKey = subscribeLst.keySet().iterator(); iteratorKey.hasNext(); ) {
                        String top = iteratorKey.next();
                        Set<String> set = subscribeLst.get(top);
                        for (Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                            String client = iterator.next();
                            if (TextUtils.equals(name, client)) {
                                Log.d(TAG, "remove have subscribe topic " + top + " client " + client);
                                iterator.remove();
                            }
                        }
                        if (set.isEmpty()) {
                            Log.d(TAG, "remove topic from subscribe list " + top);
                            subscribeLst.remove(top);
                        }
                    }
                    innerLst.get(name).close();
                    innerLst.remove(name);
                }
                break;
            default:
                Log.e(TAG, "not support operate type code : " + multipart.getType());
                throw new IllegalStateException("unknown bus server type");
        }
    }


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf in) throws Exception {

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

        dispatch(ctx, dst, length);

    }

    private static class SingleHolder {
        private static EventDispatcher INSTANCE = new EventDispatcher();
    }

    public static EventDispatcher getInstance() {
        return SingleHolder.INSTANCE;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    Log.e(TAG, "writer_idle over.");
                    break;
                case READER_IDLE:
                    Log.e(TAG, "reader_idle over.");
                    break;
                case ALL_IDLE:
                    Log.e(TAG, "all_idle over.");
                default:
                    break;
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, "client inactive: " + ctx.channel().remoteAddress());
        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        Log.d(TAG, "client active: " + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }


}
