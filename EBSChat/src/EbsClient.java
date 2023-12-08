import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class EbsClient extends JFrame {
    private JTextField jTextField;
    private JTextArea jtextArea;
    private JLabel jLabel;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private DataInputStream dataInputStream;
    private boolean connectStatus; // 클라이언트 접속 여부 저장
    private boolean stopSignal; // 쓰레드 종료 신호 저장
    private JButton jButton;

    private JScrollPane jScrollPane;
    public EbsClient() {
        OnFrame();
    }

    private void OnFrame() {
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

        jScrollPane = new JScrollPane();
        jScrollPane.setBounds(0, 23, 484, 208);
        getContentPane().add(jScrollPane);
        jScrollPane.setViewportView(jtextArea);

        jLabel = new JLabel("서버 연결 상태 : 미연결");
        jLabel.setForeground(Color.red);
        jPanel.add(jLabel);

        jTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendChat();
            }
        });
        setVisible(true);
        onChat();
        jtextArea.requestFocus(); // 텍스트 필드에 커서.
    }

    public void onChat() {
        String password= ReadyForConnect();
        boolean connectResult=connectServer(password);

        //서버접속 결과 판별하여 패스워드 일치할 때까지 패스워드 입력 후 접속 시도
        while(!connectResult) {

            password=ReadyForConnect();
            connectResult=connectServer(password);

        }
        jtextArea.append("서버접속 패스워드 일치\n");

        //멀티쓰레딩 구현하여 receiveMessage() 메서드 호출

        new Thread(new Runnable() {

            @Override
            public void run() {
                receiveMessage();
            }
        }).start();

    }

    private String ReadyForConnect() {
        String password = JOptionPane.showInputDialog(this, "패스워드 입력");

        while (password != null && password.equals("")) {
            JOptionPane.showMessageDialog(this, "패스워드 입력 필수!", "경고", JOptionPane.ERROR_MESSAGE);
            password = JOptionPane.showInputDialog(this,"패스워드 입력");

        }

        // 취소 버튼 눌렀을 경우 (null 입력 시)
        // ConfurmDialog를 사용하여 "종료하시겠습니까?" 질문에 예/아니오 선택받기
        if (password == null) {
            int confirm = JOptionPane.showConfirmDialog(this, "종료하시겠습니까?", "종료 확인", JOptionPane.YES_NO_OPTION);
//                    System.out.println(confirm);
            // 선택된 버튼의 값을 JOptionPane.XXX_OPTION 상수와 비교
            if (confirm == JOptionPane.YES_OPTION) {// 예(Y) 선택 시 현재 프로그램 종료
                System.exit(0);// 프로그램 강제 종료(정상적인 강제 종료)

            }

            return null;
        }
        //----------------------------------------

        return password;
    }

    public boolean connectServer(String pw) {
        try {
            jtextArea.append("서버에 접속을 시도 중입니다....\n");

            // socket 객체를 생성하여 IP 주소와 포트번호 전달->서버 접속시도
            socket = new Socket("localhost", 1962);

            // DataOutputStream 객체 생성 후 입력되는 패스워드 넘겨주기

            dataOutputStream = new DataOutputStream(socket.getOutputStream());
            dataOutputStream.writeUTF(pw);

            jtextArea.append("서버 접속 완료\n");

            jtextArea.append("패스워드 확인중...\n");

            // DataInputStream 객체 생성 후 전달받은 접속요청 결과 출력
            dataInputStream = new DataInputStream(socket.getInputStream());
            boolean result = dataInputStream.readBoolean();

            //전달받은 접속요청 결과 판별
            if(!result) {
                jtextArea.append("패스워드 불일치로 연결 실패\n");
                socket.close();//소켓 반환
                return false;
            }else {
                jLabel.setText("서버 연결 상태 : 연결됨\n");
                connectStatus=true;
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }

    public void receiveMessage() {
        try {
            while(!stopSignal) {
                //클라이언트가 writeUTF() 메서드로 전송한 메세지를 입력받기
                jtextArea.append("서버 : "+dataInputStream.readUTF()+"\n");
            }
            //stopSignal 이 true 가 되면 메세지 수신 종료되므로 dis와 socket 반환
            dataInputStream.close();
            socket.close();
        }catch(EOFException e){
            //상대방이 접속 해제할 경우 소켓이 제거되면서 호출되는 예외
            jtextArea.append("서버 접속이 해제되었습니다.\n");
            jLabel.setText("서버 연결 상태 : 미연결");
            connectStatus=false;
        }catch(SocketException e) {
            jtextArea.append("서버 접속이 해제되었습니다.\n");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void sendChat() {
        try {
            String text = jTextField.getText();
            jtextArea.append(text + "\n");

            //입력된 메세지가 "/exit" 일 경우
            if(text.equals("/exit")) {
                //textArea 에 "bye" 출력 후
                //stopSignal을 true로 설정 , 스트림 반환, 소켓 반환
                stopSignal=true;
                dataOutputStream.close();
                socket.close();

                //프로그램 종료
                System.exit(0);
            }else {
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
        new EbsClient();
    }
    }

