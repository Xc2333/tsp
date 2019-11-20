package com.dyy.tsp.gateway.server.netty;

import com.alibaba.fastjson.JSONObject;
import com.dyy.tsp.core.evgb.entity.EvGBProtocol;
import com.dyy.tsp.core.handler.AbstractNettyHandler;
import com.dyy.tsp.gateway.server.handler.BusinessHandler;
import com.dyy.tsp.gateway.server.handler.RedisHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Netty核心处理器
 * created by dyy
 */
@SuppressWarnings("all")
@Service
@ChannelHandler.Sharable
public class EvGBHandler extends AbstractNettyHandler {

    private final static Logger LOGGER = LoggerFactory.getLogger(EvGBHandler.class);

    @Autowired
    private BusinessHandler businessHandler;

    @Autowired
    private RedisHandler redisHandler;

    @Override
    public void doLogic(ChannelHandlerContext ctx, EvGBProtocol protocol) {
        String message = JSONObject.toJSONString(protocol);
        LOGGER.debug("parse protocol:{}", message);
        if(!protocol.getBcc() || !protocol.getBegin()){
            LOGGER.warn("{} invalid data packet",protocol.getVin());
            return;
        }
        businessHandler.doBusiness(protocol,ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception{
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        super.exceptionCaught(ctx,cause);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        Channel channel = ctx.channel();
        if(evt instanceof IdleStateEvent){
            IdleState state = ((IdleStateEvent)evt).state();
            if(state == IdleState.READER_IDLE){
                channel.close();
            }
        }
    }
}