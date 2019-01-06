package com.fantj.springbootjpa.netty;

public class Main {
    public static void main(String[] args) {
        new WebSocketServer("192.168.1.33",9999).start();
//        new WebSocketServer("127.0.0.1",9999).start();
//        new WebSocketServer("192.168.43.186",9999).start();
    }
}
