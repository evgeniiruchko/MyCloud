package ru.geekbrains.cloud.server;

import io.netty.util.ReferenceCountUtil;
import ru.geekbrains.cloud.common.AuthFailCommand;
import ru.geekbrains.cloud.common.AuthOkCommand;
import ru.geekbrains.cloud.common.Commands;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import ru.geekbrains.cloud.common.LoginCommand;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean authorized;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Commands command = (Commands) msg;
        if (!authorized) {
            if (command.isType(Commands.CommandType.LOGIN)) {   //Если клиент не залогинен, то проверяем, пытается ли он это сделать
                LoginCommand loginCommand = (LoginCommand) msg;
                int id = SQLHandler.getIdByLoginAndPassword(loginCommand.getLogin(), loginCommand.getPassword());
                if (id == -1) {
                    ctx.writeAndFlush(new AuthFailCommand());
                }
                else {
                    ctx.writeAndFlush(new AuthOkCommand());
                    authorized = true;
                    ctx.pipeline().addLast(new ClientHandler(id));
                }
            } else {
                ReferenceCountUtil.release(msg);    //если приходит какая-то другая команда, то очищаем канал
            }
        } else {
            ctx.fireChannelRead(msg);  //не обрабатываем сообщение, передаём следующему обработчику
        }
    }

    @Override
    public void channelReadComplete (ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
