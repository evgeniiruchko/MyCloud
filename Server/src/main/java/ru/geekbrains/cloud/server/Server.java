package ru.geekbrains.cloud.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

public class Server {
    private final int PORT;

    public Server(int port) {
        PORT = port;
    }

    public void start() throws InterruptedException {
        //инициируем пул потоков для приема входящих подключений
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //инициируем пул потоков для обработки потоков данных
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //Подключаемся к базе данных
        SQLHandler.connect();
        //Настройка сервера перед запуском
        try {
            ServerBootstrap server = new ServerBootstrap();
            server
                    .group(bossGroup, workerGroup)
                    /*Указываем использование класса NioServerSocketChannel для создания канала после того,
                        как принято входящее соединение.*/
                    .channel(NioServerSocketChannel.class)
                    /*Указываем обработчик, который будем использовать для открытого канала (Channel).
                    ChannelInitializer помогает пользователю сконфигурировать новый канал.*/
                    .childHandler(new ChannelInitializer<NioServerSocketChannel>() {
                        //обработка сообщений
                        // для входящих - сверху вниз, для исходящих снизу вверх
                        @Override
                        protected void initChannel(NioServerSocketChannel nioServerSocketChannel) {
                            nioServerSocketChannel.pipeline().addLast(
                                    //десериализатор netty входящего потока байтов в объект сообщения
                                    new ObjectDecoder(50 * 1024 * 1024, ClassResolvers.cacheDisabled(null)),
                                    //сериализатор netty объекта сообщения в исходящии поток байтов
                                    new ObjectEncoder(),
                                    //входящий обработчик команд по управлению сетевым хранилищем
                                    new AuthHandler()
                            );
                        }
                    })
                    //Опции для обрабатываемых каналов
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
                // Начинаем принимать входящие сообщения
                ChannelFuture channelFuture = server.bind(PORT).sync();
                System.out.println("Сервер запущен");
                channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            SQLHandler.disconnect();
        }
    }

}
