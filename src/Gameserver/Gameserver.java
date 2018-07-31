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
    private final String END_MARK = "quit";//用户主动退出标志
    
    private Set<String> userSet = new HashSet<String>();//用户名集合
    private List<Task> threadList = new ArrayList<Task>();//用户线程集合
    private ServerSocket gameserver;
    private boolean stopwager;//是否停止下注标志，是为true，否为false
    private int randnum,totalchip;//randum为产生的随机数，totalchip为庄家筹码
    
    /**
     * Gameserver() 
     * 构造函数，与建立服务器
     * @throws Exception
     */    
    public Gameserver()throws Exception{
        totalchip=5000;
        //totalchip=200;
        stopwager=false;
        gameserver=new ServerSocket(SERVER_PORT);
    }
    
    /**
     * load()
     * 启动计时开局的线程,自身作为接收客户端连接的线程,每接收一个连接创建一个用户线程
     * @throws Exception
     */
    public void load() {
        new timer().start();
        try{
        while (true) {
            Socket socket = gameserver.accept();
            new Task(socket).start();

        }
        }catch(Exception e){
            quitserver();
        }
    }
    /**
     * quitserver
     * 退出服务器
     */
    private void quitserver(){
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
            boolean next=true;
            while(next)
            {
                begin();
                stopwager=false;
                try{
                sleep(30000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                stopwager=true;
                broadcast("停止下注啦！都不要动啦！马上要开啦！开！开！开！");
                broadcast("本轮产生点数为"+randnum+"点");
                next=resultcal();
            }
            quitserver();
        }
    }
     /**
     * begin
     * 开局
     */    
    void begin(){
        Random ran=new Random();
        randnum=ran.nextInt(6)+1;//随机数生成
        System.out.println("-----------------------------------");
        System.out.println(randnum+"点");
        broadcast("-----------------------------------");
        broadcast("开始啦！大家快下注啦！赌大小啊！翻倍赢啊！");
        List<Task> threadListtemp = new ArrayList<Task>();
        threadListtemp.addAll(threadList);
        for(Task thread: threadListtemp ){
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
        List<Task> threadListtemp = new ArrayList<Task>();
        threadListtemp.addAll(threadList);
        if(randnum>3){
            for(Task thread: threadListtemp){
                int userwager=thread.wagerchip;
                if(userwager>0){
                    if(thread.DorX=='D'|thread.DorX=='d'){
                        thread.sendMsg("你赢了，返还双倍共"+userwager*2+"个筹码。");
                        thread.chip+=userwager;
                        detchip=detchip-userwager;
                    }
                    else{
                        thread.sendMsg("你输了，"+userwager+"个筹码都归了庄家。");
                        detchip=detchip+userwager;
                        thread.chip-=userwager;
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
            for(Task thread: threadListtemp){
                int userwager=thread.wagerchip;
                if(userwager>0){
                    if(thread.DorX=='X'|thread.DorX=='x'){
                        thread.sendMsg("你赢了，返还双倍共"+userwager*2+"个筹码。");
                        thread.chip+=userwager;
                        detchip=detchip-userwager;
                    }
                    else{
                        thread.sendMsg("你输了，"+userwager+"个筹码都归了庄家。");
                        detchip=detchip+userwager;
                        thread.chip-=userwager;
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
            threadListtemp.clear();
            threadListtemp.addAll(threadList);
            for(Task thread: threadListtemp){
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
        List<Task> threadListtemp = new ArrayList<Task>();
        threadListtemp.addAll(threadList);
        for(Task thread:threadList)
            thread.sendMsg(msg);
    }
    
    /**
     * broadnotone
     * 向一人外的所有人广播消息
     * @param msg,one
     */
    void broadnotone(String msg,Task one){
        List<Task> threadListtemp = new ArrayList<Task>();
        threadListtemp.addAll(threadList);
        for(Task thread: threadList)
            if(thread!=one)
                thread.sendMsg(msg);
    }
    
    /**
     * Task
     * 用户线程
     */
    class Task extends Thread{
        public String username;//用户名
        private Socket clientsocket;
        public int chip,wagerchip;//chip为该用户所有筹码，wagerchip为该用户所压筹码
        public char DorX;//下注大小，D、d为大，X、x为小
        private Writer sendwriter;//发送消息字符输出流
        private BufferedReader recevreader;//接收消息字符输入流；
        public boolean quit_flag;//是否已退出标志，是为true，否为false
        
        /**
         * Task
         * 构造函数,初始化参数
         * @param socket 
         */
        public Task(Socket socket){
            try{
            chip=100;
            wagerchip=0;
            quit_flag=false;
            clientsocket=socket;
            username="";//用户命名之前初始化为空字符串
            recevreader=new BufferedReader(new InputStreamReader(clientsocket.getInputStream(),"UTF-8"));
            sendwriter=new OutputStreamWriter(clientsocket.getOutputStream(),"UTF-8");
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        /**
         * run
         * 根据用户发来的数据，执行login()，quit()或者game()
         */
        @Override
        public void run(){
            sendMsg("连接成功，请输入用户名：");
            while(true){
                try{
                    String msg=recevreader.readLine();
                    if(username.equals(""))//判断是否未命名
                        login(msg);
                    else if(msg.equals(END_MARK)){//判断是否用户主动退出
                        sendMsg("quit");
                        if(quit()){
                            broadcast(username+"悄悄的走了，不带走一个筹码。"); 
                        }
                        break;
                    }
                    else{//否则执行游戏函数
                        if(!stopwager)
                            game(msg);
                        else
                            sendMsg("即将开盘，停止下注");
                    }
                }catch(Exception e){
                    if(quit()){
                        broadcast(username+"悄悄的走了，不带走一个筹码。"); 
                    }
                    break;
                }           
            }
        }
        /**
         * sendMsg
         * 向该用户发送消息
         * @param msg 
         */        
        public void sendMsg(String msg)
        {
            try{
                sendwriter.write(msg+'\n');
                sendwriter.flush();
            }catch(Exception e){
                if(quit())
                   broadcast(username+"悄悄的走了，不带走一个筹码。"); 
                
            }
        } 
        /**
         * login
         * 根据输入用户名执行登陆操作
         * @param msg 
         */
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
        /**
         * quit
         * 退出函数，返回值为之前是否已经退出
         * @return 
         */
        private synchronized boolean quit(){ 
            if(quit_flag!=true){
                quit_flag=true;
                if(userSet.contains(username))userSet.remove(username);
                if(threadList.contains(this))threadList.remove(this);
                try{
                    sendwriter.close();
                    recevreader.close();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
                return true;
            }
            else
                return false;

            
        }
        
        /**
         * game
         * 游戏函数，根据输入压铸筹码
         * @param msg 
         */
        private void game(String msg){
            if(!msg.matches("\\d+\\s+[DdXx]"))//判断是否符合语法规则
                sendMsg("你说啥？要按套路出牌哦！您有"+chip+"个筹码，请下注：");
            else{
                String temp[]=msg.split("\\s+");//根据空格分割字符串
                wagerchip=Integer.parseInt(temp[0]);
                DorX=temp[1].charAt(0);
                if(wagerchip>chip){
                    sendMsg("你行不行啊？你有那么多筹码吗？您有"+chip+"个筹码，请下注：");
                    wagerchip=0;
                }
                else{
                    if(DorX=='D'|DorX=='d')
                        broadcast(username+"下注"+temp[0]+"个，"+"压大");
                    else
                        broadcast(username+"下注"+temp[0]+"个，"+"压小");
                }
            }
        }
    }
    
    /**
     * main()入口
     * 运行服务器程序
     * @param args
     */
    public static void main(String[] args) {
        try{
            Gameserver server = new Gameserver();
            server.load();   
        }catch(Exception e){
            e.printStackTrace();
        }

    }
    
}
