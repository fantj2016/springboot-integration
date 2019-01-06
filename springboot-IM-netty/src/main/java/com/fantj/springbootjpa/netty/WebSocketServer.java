package com.fantj.springbootjpa.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class WebSocketServer implements MyHttpService, MyWebSocketService {



    /**
     * 握手用的 变量
     */
    private static final AttributeKey<WebSocketServerHandshaker> ATTR_HAND_SHAKER = AttributeKey.newInstance("ATTR_KEY_CHANNEL_ID");

    private static final int MAX_CONTENT_LENGTH = 65536;

    /**
     * 请求类型常量
     */
    private static final String WEBSOCKET_UPGRADE = "websocket";
    private static final String WEBSOCKET_CONNECTION = "Upgrade";
    private static final String WEBSOCKET_URI_ROOT_PATTERN = "ws://%s:%d";

    /**
     * 用户字段
     */
    private String host;
    private int port;

    /**
     * 保存 所有的连接
     */
    private Map<ChannelId, Channel> channelMap = new HashMap<>();
    private final String WEBSOCKET_URI_ROOT;

    public WebSocketServer(String host, int port) {
        this.host = host;
        this.port = port;
        // 将 ip 和端口 按照格式 赋值给 uri
        WEBSOCKET_URI_ROOT = String.format(WEBSOCKET_URI_ROOT_PATTERN, host, port);
    }

    public void start(){
        // 实例化 nio监听事件池
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        // 实例化 nio工作线程池
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        // 启动器
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup,workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pl = channel.pipeline();
                // 保存 该channel 到map中
                channelMap.put(channel.id(),channel);
                log.info("new channel {}",channel);
                channel.closeFuture().addListener((ChannelFutureListener) channelFuture -> {
                    log.info("channel close future  {}",channelFuture);
                    //关闭后 从map中移除
                    channelMap.remove(channelFuture.channel().id());
                });
                //添加 http 编解码
                pl.addLast(new HttpServerCodec());
                // 聚合器
                pl.addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
                // 支持大数据流
                pl.addLast(new ChunkedWriteHandler());
                // 设置 websocket 服务处理方式
                pl.addLast(new WebSocketServerHandler(WebSocketServer.this, WebSocketServer.this));
            }
        });
        /**
         * 实例化完毕后，需要完成端口绑定
         */
        try {
            ChannelFuture channelFuture = bootstrap.bind(host,port).addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isSuccess()){
                    log.info("webSocket started");
                }
            }).sync();
            channelFuture.channel().closeFuture().addListener((ChannelFutureListener) channelFuture12 ->
                    log.info("server channel {} closed.", channelFuture12.channel())).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("绑定端口失败");
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        log.info("webSocket shutdown");
    }

    @Override
    public void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        //判断是不是 socket 请求
        if (isWebSocketUpgrade(request)){
            //如果是webSocket请求
            log.info("请求是webSocket协议");
            // 获取子协议
            String subProtocols = request.headers().get(HttpHeaderNames.SEC_WEBSOCKET_PROTOCOL);
            //握手工厂 设置 uri+协议+不允许扩展
            WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(WEBSOCKET_URI_ROOT,subProtocols,false);
            // 从工厂中实例化一个 握手请求
            WebSocketServerHandshaker handshaker = handshakerFactory.newHandshaker(request);
            if (handshaker == null){
                //握手失败：不支持的协议
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            }else {
                //响应请求:将 握手转交给 channel处理
                handshaker.handshake(ctx.channel(),request);
                //将 channel 与 handshaker 绑定
                ctx.channel().attr(ATTR_HAND_SHAKER).set(handshaker);
            }
            return;
        }else {
            // 不处理 HTTP 请求
            log.info("不处理 HTTP 请求");
        }
    }

    @Override
    public void handleFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        /**
         * text frame handler
         */
        if (frame instanceof TextWebSocketFrame){
            String text = ((TextWebSocketFrame) frame).text();
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(text);
            log.info("receive textWebSocketFrame from channel: {} , 目前一共有{}个在线",ctx.channel(),channelMap.size());
            //发给其它的 channel  (群聊功能)
            for (Channel ch: channelMap.values()){
                if (ch.equals(ctx.channel())){
                    continue;
                }
                //将 text frame 写出
                ch.writeAndFlush(textWebSocketFrame);
                log.info("消息已发送给{}",ch);
                log.info("write text: {} to channel: {}",textWebSocketFrame,ctx.channel());
            }
            return;
        }
        /**
         * ping frame , 回复  pong frame
         */
        if (frame instanceof PingWebSocketFrame){
            log.info("receive pingWebSocket from channel: {}",ctx.channel());
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        /**
         * pong frame, do nothing
         */
        if (frame instanceof PongWebSocketFrame){
            log.info("receive pongWebSocket from channel: {}",ctx.channel());
            return;
        }
        /**
         * close frame, close
         */
        if (frame instanceof CloseWebSocketFrame){
            log.info("receive closeWebSocketFrame from channel: {}", ctx.channel());
            //获取到握手信息
            WebSocketServerHandshaker handshaker = ctx.channel().attr(ATTR_HAND_SHAKER).get();
            if (handshaker == null){
                log.error("channel: {} has no handShaker", ctx.channel());
                return;
            }
            handshaker.close(ctx.channel(),((CloseWebSocketFrame) frame).retain());
            return;
        }
        /**
         * 剩下的都是 二进制 frame ，忽略
         */
        log.warn("receive binary frame , ignore to handle");
    }

    /**
     * 判断是否是 webSocket 请求
     */
    private boolean isWebSocketUpgrade(FullHttpRequest req) {
        HttpHeaders headers = req.headers();
        return req.method().equals(HttpMethod.GET)
                && headers.get(HttpHeaderNames.UPGRADE).contains(WEBSOCKET_UPGRADE)
                && headers.get(HttpHeaderNames.CONNECTION).contains(WEBSOCKET_CONNECTION);
    }
}
