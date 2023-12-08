import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class EbsServer extends JFrame {
    private JTextField jTextField;
    private ServerSocket serverSocket; // 서버 클라이언트
    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;
    private JScrollPane jScrollPane;
    private JButton jButton;

    private JPanel jPanel;

    private JTextArea jtextArea;
    private JLabel jLabel;
    private boolean connectStatus; // 클라이언트 접속 여부 저장
    private boolean stopSignal; // 쓰레드 종료 신호 저장

    // ID + pw 구현해서 접속 화면 구현해야함.
    // 자료구조 사용 고려
    private String password = "1234"; // 클라이언트에서 접속용으로 사용할 패스워드

    public EbsServer() {
        OnFrame(); // 프레임
        Onchat();  // 채팅 시작
    }

    public void OnFrame() {
        setTitle("EBS Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(500, 500, 500, 400);
        getContentPane().setLayout(null);

        jtextArea = new JTextArea();
        jtextArea.setEditable(false);
        jtextArea.setBackground(Color.cyan);
        getContentPane().add(jtextArea);

        jTextField = new JTextField();
        jTextField.setBounds(0, 230, 390, 31);
        getContentPane().add(jTextField);
        jTextField.setColumns(1000);

        jButton = new JButton("전송");
        jButton.setBackground(Color.YELLOW);
        jButton.setForeground(Color.BLUE);
        jButton.setBounds(387, 230, 97, 31);
        getContentPane().add(jButton);

        JPanel jPanel = new JPanel();
        jPanel.setBounds(0, 0, 484, 22);
        getContentPane().add(jPanel);

        jLabel = new JLabel("Client 연결 상태 없음.");
        jLabel.setForeground(Color.red);
        jPanel.add(jLabel);

        jScrollPane = new JScrollPane();
        jScrollPane.setBounds(0, 23, 484, 208);
        getContentPane().add(jScrollPane);
        jScrollPane.setViewportView(jtextArea);
        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendChat(); // sendMessage()
            }
        });
        setVisible(true);
        jTextField.requestFocus();
    }

    public void Onchat() {
        try {
            jtextArea.append(" 채팅 준비중입니다. . . .");
            serverSocket = new ServerSocket(1962);
            jtextArea.append("서비스 준비 완료");

            // 연결 상태가 true가 될 때까지 대기.
            connectStatus = false;
            while (!connectStatus) {
                jtextArea.append("클라이언트 접속 대기 . . ");
                // ServerSocket 객체 accept() 호출 후 연결 대기
                socket = serverSocket.accept();
                // 접속된 클라이언트에 대한 IP 주소 정보 출력
                jtextArea.append(socket.getInetAddress() + "클라이언트에서 접속 성공");

                // dataInputStream 객체 생성하여 패스워드 가져옴
                dataInputStream = new DataInputStream(socket.getInputStream());
                String UserPw = dataInputStream.readUTF();
                jtextArea.append("접속된 비밀번호 : " + UserPw);

                // DataOutputStream 객체를 생성
                dataOutputStream = new DataOutputStream(socket.getOutputStream());
                //패스워드 비교
                if (!UserPw.equals(password)) {
                    dataOutputStream.writeBoolean(false);
                    jtextArea.append(" 클라이언트 패스워드 불일치로 접속 해제");
                } else {
                    connectStatus = true;
                    jtextArea.append(" 유저의 비밀번호가 일치합니다. ");
                    jLabel.setText(" 클라이언트 연결 완료");
                    dataOutputStream.writeBoolean(true);
                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    receiveMessage();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* 스레드 사용한 메시지 수신 처리*/
    /* stopSignal이 false면 반복해서 메세지 수신*/
    public void receiveMessage() {
        try {
            while (!stopSignal) {
                jtextArea.append("클라이언트 : " + dataInputStream.readUTF() + "\n");
            }
            dataInputStream.close();
            socket.close();
        } catch (EOFException e) {
            /* 접속 해제시 소켓 제거되면서 호출*/
            jtextArea.append("클라이언트 연결이 해제됨.");
            jLabel.setText("클라이언트 연결 안됨.");
            connectStatus = false;
        } catch (SocketException e) {
            jtextArea.append(" 서버 접속이 해제되었음.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendChat() {
        try {
            String text = jTextField.getText();
            jtextArea.append(text + "\n");

            //입력된 메세지가 "/exit" 일 경우
            if (text.equals("/exit")) {
                //textArea 에 "bye" 출력 후
                //stopSignal을 true로 설정 , 스트림 반환, 소켓 반환
                stopSignal = true;
                dataOutputStream.close();
                socket.close();

                //프로그램 종료
                System.exit(0);
            } else {
                //입력된 메세지가 "/exit"가 아닐 경우( 전송할 메세지인 경우)
                //클라이언트에게 메세지 전송
                dataOutputStream.writeUTF(text);

                //초기화 및 커서요청
                jTextField.setText("");
                jTextField.requestFocus();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new EbsServer();
    }
}

