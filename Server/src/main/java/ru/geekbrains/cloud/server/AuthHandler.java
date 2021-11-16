package ru.geekbrains.cloud.server;

import io.netty.util.ReferenceCountUtil;
import org.apache.commons.codec.digest.DigestUtils;
import ru.geekbrains.cloud.common.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class AuthHandler extends ChannelInboundHandlerAdapter {
    private boolean authorized;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Commands command = (Commands) msg;
        System.out.println("Пришло сообщение");
        if (!authorized) {
            if (command.isType(Commands.CommandType.LOGIN)) {   //Если клиент не залогинен, то проверяем, пытается ли он это сделать
                LoginCommand loginCommand = (LoginCommand) msg;
                System.out.println(DigestUtils.md5Hex(loginCommand.getPassword()));
                int id = SQLHandler.getIdByLoginAndPassword(loginCommand.getLogin(), DigestUtils.md5Hex(loginCommand.getPassword()));
                if (id == -1) {
                    ctx.writeAndFlush(new AuthFailCommand());
                }
                else {
                    ctx.writeAndFlush(new AuthOkCommand());
                    authorized = true;
                    ctx.pipeline().addLast(new ClientHandler(id));
                }
            } else if (command.isType(Commands.CommandType.REGISTRATION)) {
                RegistrationCommand registrationCommand = (RegistrationCommand) msg;
                boolean isReg = SQLHandler.tryToRegister(registrationCommand.getLogin(), registrationCommand.getPassword());
                if (isReg) {
                    System.out.println("Успешная регистрация");
                } else {
                    System.out.println("Логин" + registrationCommand.getLogin() + "уже занят");
                }
            } else
                ReferenceCountUtil.release(msg);    //если приходит какая-то другая команда, то очищаем канал
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
