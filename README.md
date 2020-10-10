# android与Netty结合创建聊天室

## 服务端建立

服务端所具备的功能有

- 在客户端连接上客户端时，向所有客户端发送链接已建立的消息(某某客户端已上线)。
- 在客户端断开连接时，向所有客户端发送某某已经离开聊天室的消息。
- 在客户端发送消息时，转发该消息给所有连接的客户端。

使用netty实现的步骤可以分为：

- 定义了两个处理事务类bossGroup以及workerGroup
- 定义ServerBootstrap，指定channel、处理类(ServerHandler)、选项
- 绑定端口
- 等待服务端结束

ServerHandler就是用来处理用户逻辑的地方，在继承了相关类之后，重写handlerAdded、handlerRemoved、ChannelRead等方法

- handlerAdded 当客户端建立连接之后，服务端会做什么
- handlerRemoved 当客户端连接关闭之后，服务端会做什么
- ChannelRead 当客户端发来消息，服务端会做出什么响应

1. handlerAdded

```java
 @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        for(Channel channel : channels){
            //不是当前channel，通知其它channel
            if(channel != inComing){
                channel.writeAndFlush("[系统消息]: " + inComing.remoteAddress() + "上线了!\n");
            }
        }
        //把当前的channel加入到channels
        channels.add(inComing);
    }
```

2. handlerRemoved

```java
 @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        Channel inComing = ctx.channel();
        for(Channel channel : channels){
            //不是当前channel，通知其它channel
            if(channel != inComing){
                channel.writeAndFlush("[系统消息]: " + inComing.remoteAddress() + "下线了!\n");
            }
        }
        //把当前的channel加入到channels
        channels.remove(inComing);
    }
```

3. ChannelRead

```java
@Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // System.out.println("log");
        Channel incoming = ctx.channel();
        // System.out.println(msg+"received");
        for (Channel channel : channels) {
            if (channel != incoming){
                channel.writeAndFlush("[用户 " + incoming.remoteAddress() + "]: " + msg + "\n");
            } else {
                channel.writeAndFlush("[我]: " + msg + "\n");
            }
        }
    }
```

还有更多可以重写的函数比如说ChannelActive、ExceptionCaught、ChannelInActive

## 客户端建立

在这里，我们的客户端使用android平台，客户端所具备的功能有

- 在用户登录到系统后主动与服务端建立连接，如果连接失败则需要重连算法
- 客户端能够主动发送数据给服务端，并接收服务端返回的数据
- 客户端能够对服务端的数据进行处理，从而对相应的UI进行调整

使用netty下，客户端的建立过程可以分为

- 定义处理事务组EventLoopGroup
- 定义启动类Bootstrap
- 配置bootstrap的channel、处理类(ClientHandler)、选项等
- 根据host、port进行连接并保持这个连接

ClientHandler负责用户的处理逻辑，重写ChannelActive、ChannelRead、ExceptionCaught等函数

- ChannelRead

```java
 @Override
            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                System.out.println("msg received:"+msg);
                Message message =new Message();
                Bundle bundle =new Bundle();
                bundle.putString("msg", (String) msg);
                if(((String) msg).contains("我")){
                    bundle.putString("Tag","right");
                }else {
                    bundle.putString("Tag","left");
                }
                message.setData(bundle);
                h.sendMessage(message);
            }
```

当收到服务器的消息时，如果这是我发送的消息，那么通知recyclerView在右侧新建一个气泡显示我的内容，如果不是我的消息，将会显示在左边



### 实际运行效果

客户端1：![image-20201010104337414](C:\Users\86152\AppData\Roaming\Typora\typora-user-images\image-20201010104337414.png)


