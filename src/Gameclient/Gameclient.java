/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Gameclient;
import java.net.Socket;
import java.io.Writer;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
/**
 *
 * @author WATER
 */
public class Gameclient {
    
    private final String SERVER_IP="127.0.0.1";
    private final int SERVER_PORT=12345;
    private final String END_MARK="quit";//聊天室退出标识
    
    private Socket gamesocket;
    private Writer sendwriter;//发送消息字符输出流
    private BufferedReader keyin;//接收键盘字符输入流
    private BufferedReader recevreader;//接收消息字符输入流；
    private boolean quit_flag;
    
    /**
     * Gameclient() 
     * 构造函数
     * 与服务器建立连接
     * @throws Exception
     */
    public Gameclient() throws Exception{
        quit_flag=false;
        gamesocket=new Socket(SERVER_IP,SERVER_PORT);
    }
    
    /**
     * load() 
     * 启动监听接收消息并显示的线程，同时自身作为循环输入信息，并且将消息发送出去的线程
     * @throws Exceptiom 
     */
    public void load() {
        ReceiveMsgTask Task=new ReceiveMsgTask();//启动接收监听线程；
        Task.start();
        try{
            sendwriter=new OutputStreamWriter(gamesocket.getOutputStream(),"UTF-8");
            keyin=new BufferedReader (new InputStreamReader(System.in));
            String inputMsg="";
            while(!quit_flag)
            {
                inputMsg=keyin.readLine();
                sendwriter.write(inputMsg+'\n');
                sendwriter.flush();  
            }
        }catch(Exception e){
            if(quit_flag!=true)
                quit();
        }
    }
    
    /**
     * ReceiveMsgTask()
     * 监听接收消息并显示的线程
     */
    class ReceiveMsgTask extends Thread {
        @Override
        public void run(){
            try{
                recevreader=new BufferedReader(new InputStreamReader(gamesocket.getInputStream(),"UTF-8"));
                while(!quit_flag){
                     String Msgbuff=recevreader.readLine();
                     if(!END_MARK.equals(Msgbuff))
                         System.out.println(Msgbuff);
                     else{
                         if(quit_flag!=true)
                            quit();
                         break;
                     }
                         
                }
            }catch(Exception e){
              if(quit_flag!=true)
                quit();
            }
        }
    }
    /**
     * quit()
     * 退出函数
     */
    void quit(){
        try{
                if(recevreader!=null)recevreader.close();
                if(sendwriter!=null)sendwriter.close();
                if(keyin!=null)keyin.close();
                if(gamesocket!=null)gamesocket.close();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
        quit_flag=true;
    }
    
    
    /**
     * main()
     * 运行客户端程序
     * @param args
     */
    public static void main(String[] args){
        try{
            Gameclient client=new Gameclient();
            client.load();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
}
