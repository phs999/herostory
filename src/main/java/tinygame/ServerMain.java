package tinygame;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerMain {

    private static final Logger logger = LoggerFactory.getLogger(ServerMain.class);
    public static void main(String[] args) {
        PropertyConfigurator.configure(ServerMain.class.getClassLoader().getResourceAsStream("log4j.properties"));

        EventLoopGroup bossGroup = new NioEventLoopGroup();//负责建立连接
        EventLoopGroup workerGroup = new NioEventLoopGroup();///负责收发消息

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup,workerGroup);
        b.channel(NioServerSocketChannel.class);// 服务器信道的处理方式
        b.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
              socketChannel.pipeline().addLast(
                      new HttpServerCodec(),// Http 服务器编解码器
                      new HttpObjectAggregator(65535),// 内容长度限制
                      new WebSocketServerProtocolHandler("/websocket"),// WebSocket 协议处理器, 在这里处理握手等消息
                      new GameMgsHandler()// 自定义的消息处理器
              );

              b.option(ChannelOption.SO_BACKLOG,128);
              b.childOption(ChannelOption.SO_KEEPALIVE,true);

            }
        });



        try {
            // 绑定 12345 端口,
            // 注意: 实际项目中会使用 args 中的参数或者配置文件来指定端口号
            ChannelFuture channelFuture = b.bind(12345).sync();

            if (channelFuture.isSuccess()){
                logger.info("游戏服务器启动成功");
            }

            // 等待服务器信道关闭,
            // 也就是不要立即退出应用程序, 让应用程序可以一直提供服务
            channelFuture.channel().closeFuture().sync();//阻塞

        }catch (Exception e){
            logger.error(e.getMessage(),e);

        }finally {

            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
