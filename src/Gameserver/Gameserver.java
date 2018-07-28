/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Gameserver;

import java.util.List;
import java.util.ArrayList;
import java.net.ServerSocket;
import java.util.Set;
import java.util.HashSet;
import java.net.Socket;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Collections;
/**
 *
 * @author WATER
 */
public class Gameserver {
    private final int SERVER_PORT = 12345;
    private final String END_MARK = "quit";
    
    private Set<String> userSet = new HashSet<String>();//用户名集合
    private List<Task> threadList = new ArrayList<Task>();//用户线程集合
    private ServerSocket gameserver;
    private int randnum,totalchip;
    
    /**
     * Gameserver() 
     * 构造函数，与建立服务器
     * @throws Exception
     */    
    public Gameserver()throws Exception{
        gameserver=new ServerSocket(SERVER_PORT);
    }
    
    /**
     * load()
     * 启动计时开局的线程,自身作为接收客户端连接的线程,每接收一个连接创建一个用户线程
     * @throws Exception
     */
    public void load() throws Exception {
        new timer().start();

        while (true) {
            Socket socket = gameserver.accept();
            new Task(socket).start();
        }
    }
    
    /**
     * timer
     * 计时开局的线程
     */
    class timer extends Thread{
        @Override
        public void run(){
            while(true)
            {
                getrandom();
                broadcast("开始啦！大家快下注啦！赌大小啊！翻倍赢啊！");
                try{
                sleep(30000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                broadcast("停止下注啦！都不要动啦！马上要开啦！开！开！开！");
                broadcast("本次产生点数为"+randnum+"点");
                resultcal();
            }
        }
    }
     /**
     * getrandom
     * 随机数生成
     */    
    void getrandom(){
        ;
    }   
    /**
     * resultcal
     * 筹码计算
     */    
    void resultcal(){
        ;
    }
    /**
     * broadcast
     * 广播消息
     * @param msg
     */
    void broadcast(String msg){
        for(Task thread:threadList)
            thread.sendMsg(msg);
    }
    
    /**
     * broadnotone
     * 向一人外的所有人广播消息
     * @param msg,one
     */
    void broadnotone(String msg,Task one){
        for(Task thread: threadList)
            if(thread!=one)
                thread.sendMsg(msg);
    }
    
    /**
     * Task
     * 用户线程
     */
    class Task extends Thread{
        private String username;
        private Socket clientsocket;
        private int chip,wagerchip;
        private char DorX;
        private Writer sendwriter;
        private BufferedReader recevreader;
        
        public Task(Socket socket){
            try{
            chip=100;
            wagerchip=0;
            clientsocket=socket;
            username="";
            recevreader=new BufferedReader(new InputStreamReader(clientsocket.getInputStream(),"UTF-8"));
            sendwriter=new OutputStreamWriter(clientsocket.getOutputStream(),"UTF-8");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        
        @Override
        public void run(){
            sendMsg("连接成功，请输入用户名：");
            while(true){
                try{
                String msg=recevreader.readLine();
                if(username.equals(""))
                    login(msg);
                else if(msg.equals(END_MARK)){
                    sendMsg("quit");
                    quit();
                    break;
                }
                else
                    game(msg);
                }catch(Exception e){
                    quit();
                    break;
                }
                
            }

                
        }
                
        public void sendMsg(String msg)
        {
            try{
                sendwriter.write(msg+'\n');
                sendwriter.flush();
            }catch(Exception e){
                quit();
            }
        } 
        
        private void login(String msg){
            if(!msg.matches("\\S+"))
                sendMsg("无效输入，请重新输入用户名：");
            else if(userSet.contains(msg))
                sendMsg("用户名已经存在，请更换一个新名字：");
            else{
                username=msg;
                userSet.add(msg);
                threadList.add(this);
                sendMsg("您有100个筹码，请下注：");
            }
        }
        
        private void quit(){
            if(userSet.contains(username))userSet.remove(username);
            if(threadList.contains(this))threadList.remove(this);
            broadcast(username+"悄悄的走了，不带走一个筹码。");
            try{
                if(sendwriter!= null)sendwriter.close();
                if(sendwriter!= null)recevreader.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            
        }
        
        private void game(String msg){
            if(!msg.matches("\\d+\\s+[DdXx]"))
                sendMsg("你说啥？要按套路出牌哦！您有"+chip+"个筹码，请下注：");
            else{
                String temp[]=msg.split("\\s+");
                wagerchip=Integer.parseInt(temp[0]);
                DorX=temp[1].charAt(0);
                if(wagerchip>chip){
                    sendMsg("你行不行啊？你有那么多筹码吗？您有"+chip+"个筹码，请下注：");
                    wagerchip=0;
                }
                else{
                    if(DorX=='D'|DorX=='d')
                        broadcast(username+"下注"+temp[0]+"个"+"压大");
                    else
                        broadcast(username+"下注"+temp[0]+"个"+"压小");
                }
            }
        }
    }
    
    /**
     * main()入口
     * 运行服务器程序
     */
    public static void main() {
        try { 
            Gameserver server = new Gameserver();
            server.load();          
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}
