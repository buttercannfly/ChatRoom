package com.example.chatroom.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.chatroom.Adapter.ItemAdapter;
import com.example.chatroom.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import static java.lang.Thread.sleep;

public class ChatRoom extends AppCompatActivity {
    public final static int RIGHT = 2;
    public final static int LEFT = 1;
    public static String USER = "";
    public ChatClient chatClient;
    RecyclerView recyclerView;
    ItemAdapter itemAdapter;
    EditText ed ;
    Button bt;
    int i = 0;
    Handler h;
    Bootstrap bootstrap;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        Intent intent = getIntent();
        USER = intent.getStringExtra("username");
        initview();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    initClient();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        h =new Handler(){
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                String str = (String) bundle.get("msg");
                String position = (String)bundle.get("Tag");
                if(position.equals("left")){
                    itemAdapter.addItem(str,  LEFT);
                }else{
                    itemAdapter.addItem(str, RIGHT);
                }
                recyclerView.smoothScrollToPosition(i);
                i++;
                }
        };
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatClient.sendMsg(ed.getText().toString());
                ed.setText("");
            }
        });
    }

    private void initClient() throws InterruptedException {
        String host = "202.38.86.126";
        int port = 7654;
        chatClient = new ChatClient(host,port);
        chatClient.run();
    }

    private void initview() {
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new ItemAdapter(this);
        recyclerView.setAdapter(itemAdapter);
        ed = findViewById(R.id.ed);
        bt = findViewById(R.id.bt);
    }
    class ChatClient {

        private String host;
        private int port;
        private ChannelFuture channelFuture;

        ChatClient(String host, int port) {
            this.host = host;
            this.port = port;
        }

        void run() throws InterruptedException {
            //客户端用Bootstrap来启动
            EventLoopGroup group = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .option(ChannelOption.SO_KEEPALIVE,true)
                        .handler(new ChatClientInitializer());
            channelFuture = bootstrap.connect(host,port).sync();
            channelFuture.channel().closeFuture().sync();
        }
        void sendMsg(String msg){
            if(channelFuture!=null){
                channelFuture.channel().writeAndFlush(msg);
            }
        }

        private class ChatClientInitializer extends ChannelInitializer<SocketChannel> {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                //获取通道，设置通道的初始化属性
                ChannelPipeline pipeline = socketChannel.pipeline();

//                pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
                pipeline.addLast("decoder", new StringDecoder());
                pipeline.addLast("encoder", new StringEncoder());
                pipeline.addLast(new ChatClientHandler());
            }
        }
        private class ChatClientHandler extends ChannelInboundHandlerAdapter{

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

            @Override
            public void channelActive(final ChannelHandlerContext ctx) throws Exception {

            }
        }
    }

}
