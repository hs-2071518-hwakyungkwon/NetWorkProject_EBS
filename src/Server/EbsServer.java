package Server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class EbsServer implements Runnable{
    ServerSocket ss;
    Service service;
    ArrayList<Service> list = new ArrayList();

    public EbsServer() {
        try {
            ss = new ServerSocket(9985);
            System.out.println("채팅 서버 서비스 중...");
            new Thread(this).start();
        } catch (Exception e) {
            System.out.println("ChatServer err : " + e);
        }
    }

    @Override
    public void run() { //ChatServer의 run
        while(true) {
            try {
                Socket socket = ss.accept();
                service = new Service(socket);
                service.start();
                service.chat_name = service.in.readLine(); //접속자의 이름 읽기
                System.out.println(service.chat_name);

                // 새 클라이언트에게 기존 접속자들의 정보 전송
                StringBuilder existingUsers = new StringBuilder();
                for(Service ser : list) {
                    existingUsers.append(ser.chat_name + " ");
                }
                existingUsers.append(service.chat_name); // 새 클라이언트의 이름 추가
                service.messageSend("/c" + existingUsers.toString().trim()); // 변경된 부분

                // 기존 클라이언트에게 새 접속자의 정보 전송
                for(Service ser : list) {
                    ser.messageSend("/c" + service.chat_name);
                }

                list.add(service);
                service.messageSend("/c" + service.chat_name); // 새 클라이언트에게 자신의 정보 전송
            } catch (Exception e) {
                System.out.println("ChatServer run err :" + e);
            }
        }
    }

    //client 별 처리를 위한 내부 클래스
    class Service extends Thread {
        String chat_name;
        BufferedReader in;
        OutputStream out;
        Socket socket;

        public Service(Socket socket) {
            try {
                this.socket = socket;
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), "euc-kr"));
                out = socket.getOutputStream();
            } catch (Exception e) {
                System.out.println("Service err : " + e);
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String msg = in.readLine();
                    if (msg == null || msg.equals("")) continue;
                    if (msg.charAt(0) == '/') {
                        if (msg.charAt(1) == 'r') { //대화명 변경
                            messageAll("/r" + chat_name + "-" + msg.substring(2));
                            chat_name = msg.substring(2);
                        } else if (msg.charAt(1) == 'q') { //퇴장
                            try {
                                messageAll("/q" + chat_name);
                                list.remove(this);
                                in.close();
                                out.close();
                                socket.close();
                            } catch (Exception e) {
                                System.err.println("An error occurred while closing the socket: " + e.getMessage());
                                e.printStackTrace();
                            }
                            break;
                        } else if (msg.charAt(1) == 's') { //귓속말
                            // /s이름-메세지
                            String name = msg.substring(2, msg.indexOf('-')).trim();
                            for (Service ser : list) {
                                if (name.equals(ser.chat_name)) {
                                    ser.messageSend((chat_name + ">(귓)" + msg.substring(msg.indexOf('-') + 1)));
                                }
                            }
                        }
                    } else { //일반 메세지
                        messageAll("[" + chat_name + "] " + msg + "\n");
                    }
                } catch (Exception e) {
                    break;
                }
            }
        }

        public void messageAll(String msg) {
            try {
                for (int i = 0; i < list.size(); i++) {
                    Service ser = list.get(i);
                    ser.messageSend(msg);
                }
            } catch (Exception e) {
                System.out.println("messageAll err : " + e);
            }
        }

        public void messageSend(String msg) {
            try {
                out.write((msg + "\n").getBytes("euc-kr"));
            } catch (Exception e) {
                System.out.println("messageSend err : " + e);
            }
        }
    }

    public static void main(String[] args) {
        new EbsServer();
    }

}