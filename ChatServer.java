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
				if(linee.contains("fuck")||linee.contains("shit")||linee.contains("fool")||linee.contains("silly")||linee.contains("hell"))//5가지의 경우에서 문장이 포함 된 것이 있다면 쓰지말라고 경고를 준다. 대소문자 구분없이 만들었다.
					dont();
				else if(line.equals("/quit"))
					break;
				else if(line.equals("/userlist")){//userlist를 입력하면
					send_userlist();//send_userlist메소드 실행
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
			Iterator iter2 = hm.keySet().iterator();//새로운iter2를 만든다. 위의 iter과는 다르게 value값만이 아닌 키값도 같이 받아온다.
			while(iter.hasNext()){
				String keys = (String)iter2.next();//iter와 같은 순으로 넘긴다
				PrintWriter pw = (PrintWriter)iter.next();
				if(id==keys){//현 아이디와 key값이 겹치면 보내지 않는 것으로 자신이 보낸 문장은 자신에게 나타나지 않는다.
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
			Object obj = hm.get(id);//아이디에 해당하는 value인 pw를 가져옴
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println("Users id are "+ hm.size() +" : "+hm.keySet());//접속한 사용자들의 id들과 그 수를 출력
				pw.flush();//해당 아이디에게만 보여짐
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
