package tinygame;

import com.google.protobuf.GeneratedMessageV3;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tinygame.msg.GameMsgProtocol;

/**
 * 自定义的消息解码器
 */
public class GameMsgDecoder extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(GameMsgDecoder.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx,Object msg) {

        if (null == ctx
                || null == msg) {
            return;
        }

        //logger.info(msg+"");
        if (!(msg instanceof BinaryWebSocketFrame)) {
            return;
        }

        try {
        BinaryWebSocketFrame inputFrame = (BinaryWebSocketFrame) msg;
        ByteBuf byteBuf = inputFrame.content();

        byteBuf.readShort();//读取消息的长度
        int msgCode = byteBuf.readShort();//读取消息编号

        //拿到消息体
        byte[] msgBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(msgBody);
        GeneratedMessageV3 cmd = null;

        //根据不同的消息编码解析为对应的命令内容
        switch (msgCode) {
            case GameMsgProtocol.MsgCode.USER_ENTRY_CMD_VALUE:
                cmd = GameMsgProtocol.UserEntryCmd.parseFrom(msgBody);
                break;

            case GameMsgProtocol.MsgCode.WHO_ELSE_IS_HERE_CMD_VALUE:
                cmd = GameMsgProtocol.WhoElseIsHereCmd.parseFrom(msgBody);
                break;

            default:
                break;
        }

        if (null != cmd) {
            ctx.fireChannelRead(cmd);
        }

        } catch (Exception ex) {
            // 记录错误日志
            logger.error(ex.getMessage(), ex);
        }


    }
}
