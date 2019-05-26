//https://github.com/KimSeWon/SimpleChat.git

import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection....");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			}
			initFlag = true;
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String xx = "fuck shit damn silly fool";
			String line = null;
			while((line = br.readLine()) != null){
				String linee = line.toLowerCase();
				if(linee.contains("fuck")||linee.contains("shit")||linee.contains("fool")||linee.contains("silly")||linee.contains("hell"))//I use contains to avoid 5 words.
					dont();
				else if(line.equals("/quit"))
					break;
				else if(line.equals("/userlist")){//if you chat userlist
					send_userlist();//send_userlist method start
				}
				else if(line.indexOf("/to ") == 0){
					sendmsg(line);
				}else
					broadcast(id + " : " + line);
			}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	public void broadcast(String msg){
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			Iterator iter2 = hm.keySet().iterator();//This is new iter2 using hm.
			while(iter.hasNext()){
				String keys = (String)iter2.next();//iter2 also get next.
				PrintWriter pw = (PrintWriter)iter.next();
				if(id==keys){//if key=id cannot print msg
				}
				else{
					pw.println(msg);
					pw.flush();
				}
			}
		}
	}// broadcast
	public void send_userlist(){
		synchronized(hm){
			Object obj = hm.get(id);//get value of id's pw
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("Users id are "+ hm.size() +" : "+hm.keySet());//get how many people and ID
				pw.flush();
			}
		}
	}
	public void dont(){
		synchronized(hm){
			Object obj = hm.get(id);
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("don't use this!");
				pw.flush();
			}
		}
	}
}
