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
import java.util.Random;
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
    private boolean next;
    private int randnum,totalchip;
    
    /**
     * Gameserver() 
     * 构造函数，与建立服务器
     * @throws Exception
     */    
    public Gameserver()throws Exception{
        totalchip=5000;
        next=true;
        gameserver=new ServerSocket(SERVER_PORT);
    }
    
    /**
     * load()
     * 启动计时开局的线程,自身作为接收客户端连接的线程,每接收一个连接创建一个用户线程
     * @throws Exception
     */
    public void load() {
        new timer().start();

        while (next) {
            try{
            Socket socket = gameserver.accept();
            new Task(socket).start();
            }catch(Exception e){
                try{
                    gameserver.close();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                break;
            }
        }
        if(gameserver!=null)
            try{
                gameserver.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
    }
    
    /**
     * timer
     * 计时开局的线程
     */
    class timer extends Thread{
        @Override
        public void run(){
            while(next)
            {
                begin();
                broadcast("开始啦！大家快下注啦！赌大小啊！翻倍赢啊！");
                try{
                sleep(30000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                broadcast("停止下注啦！都不要动啦！马上要开啦！开！开！开！");
                broadcast("本次产生点数为"+randnum+"点");
                next=resultcal();
            }
        }
    }
     /**
     * begin
     * 开局
     */    
    void begin(){
        Random ran=new Random();
        randnum=ran.nextInt(6)+1;//随机数生成
        System.out.println(randnum+"点");
        for(Task thread: threadList ){
            thread.wagerchip=0;
            thread.sendMsg("您有"+thread.chip+"个筹码，请下注：");
        }
    }   
    /**
     * resultcal
     * 筹码计算
     */    
    boolean resultcal(){
        int detchip=0;
        if(randnum>3){
            for(Task thread: threadList){
                int userwager=thread.wagerchip;
                if(userwager>0){
                    if(thread.DorX=='D'|thread.DorX=='d'){
                        thread.sendMsg("你赢了，返还双倍共"+userwager*2+"个筹码。");
                        thread.chip+=userwager*2;
                        detchip=detchip-userwager;
                    }
                    else{
                        thread.sendMsg("你输了，"+userwager+"个筹码都归了庄家。");
                        detchip=detchip+userwager;
                        if(thread.chip==0){
                            thread.sendMsg("你输个精光，别玩儿了！");
                            thread.sendMsg("quit");
                            thread.quit();
                            broadcast(thread.username+"输个精光，被一脚踢出！");
                        }
                    }
                }
            }
        }
        else{
            for(Task thread: threadList){
                int userwager=thread.wagerchip;
                if(userwager>0){
                    if(thread.DorX=='X'|thread.DorX=='x'){
                        thread.sendMsg("你赢了，返还双倍共"+userwager*2+"个筹码。");
                        thread.chip+=userwager*2;
                        detchip=detchip-userwager;
                    }
                    else{
                        thread.sendMsg("你输了，"+userwager+"个筹码都归了庄家。");
                        detchip=detchip+userwager;
                        if(thread.chip==0){
                            thread.sendMsg("你输个精光，别玩儿了！");
                            thread.sendMsg("quit");
                            thread.quit();
                            broadcast(thread.username+"输个精光，被一脚踢出！");
                        }
                            
                    }
                }
            }
        }
        totalchip+=detchip;
        if(detchip<0){
            detchip=0-detchip;
            System.out.println("上一轮庄家输了"+detchip+"个筹码，总共剩"+totalchip+"个筹码");
        }
        else
            System.out.println("上一轮庄家赢了"+detchip+"个筹码，总共剩"+totalchip+"个筹码");
        if(totalchip<=0){
            broadcast("庄家运气怎么这么差，竟然输光了，掀桌子不玩儿了！大家散场啦！");
            for(Task thread:threadList){
                thread.sendMsg("quit");
                thread.quit();
            }
            return false;
        }
        else
            return true;
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
        public String username;
        private Socket clientsocket;
        public int chip,wagerchip;
        public char DorX;
        private Writer sendwriter;
        private BufferedReader recevreader;
        public boolean quit_flag;
        
        public Task(Socket socket){
            try{
            chip=100;
            wagerchip=0;
            quit_flag=false;
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
            while(!quit_flag){
                try{
                String msg=recevreader.readLine();
                if(username.equals(""))
                    login(msg);
                else if(msg.equals(END_MARK)){
                    sendMsg("quit");
                    if(quit_flag!=true){
                        quit(); 
                        broadcast(username+"悄悄的走了，不带走一个筹码。"); 
                    }
                    break;
                }
                else
                    game(msg);
                }catch(Exception e){
                        sendMsg("quit");
                    if(quit_flag!=true){
                        quit();
                        broadcast(username+"悄悄的走了，不带走一个筹码。"); 
                    }
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
                if(quit_flag!=true){
                   quit();
                   broadcast(username+"悄悄的走了，不带走一个筹码。"); 
                }
                
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
            try{
                if(sendwriter!= null)sendwriter.close();
                if(sendwriter!= null)recevreader.close();
            }catch(Exception ex){
                ex.printStackTrace();
            }
            quit_flag=true;
            
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
                    chip=chip-wagerchip;
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
        try{
            Gameserver server = new Gameserver();
            server.load();   
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    
}
