package Login;

import Client.EbsClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import static Login.JoinService.userData;

public class MainLogin extends JFrame {
    public JoinService joinService;
    protected JTextArea jTextArea;
    protected JTextField id;
    protected JTextField pw;

    protected JButton LoginButton;
    protected JButton JoinButton;

    private boolean pass = false;


    /* Main에서 MainLgoin 객체를 생성하면 로그인 화면이 생성되고 -> 채팅방이 접속되도록 구현해야한다. */
    public MainLogin() {
        OnLogin();
        MemberJoinService();
        MemberLoginService();

    }

    /* 서버가 처음 연결될 시 로그인 화면이 생성된다.*/
    private void OnLogin() {
        this.setTitle("EBSChat Login");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(500, 500, 500, 400);
        this.getContentPane().setLayout(null);


        id = new JTextField("ID");
        pw = new JTextField("PW");
        this.id.setBounds(0, 40, 150, 31);
        this.pw.setBounds(0, 70, 150, 31);
        this.getContentPane().add(id);
        this.getContentPane().add(pw);
        LoginButton = new JButton("로그인");
        JoinButton = new JButton("회원가입");
        getContentPane().add(JoinButton);

        JoinButton.setBackground(Color.WHITE);
        JoinButton.setForeground(Color.BLACK);
        this.JoinButton.setBounds(150, 70, 97, 31);

        LoginButton.setBackground(Color.WHITE);
        LoginButton.setForeground(Color.BLACK);
        this.LoginButton.setBounds(150, 40, 97, 31);

        getContentPane().add(LoginButton);
        setVisible(true);
    }

    /* 회원가입 클릭시 실행될 이벤트 */
    /* 회원가입 컨테이너를 새로 띄우고, ID,와 PW를 데이터로 받아서 JoinService의 자료구조에 저장.*/


    public void MemberJoinService() {
        JoinButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                new JoinService();
            }
        });
    }

    public void MemberLoginService() {
        LoginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (id.getText().equals(userData.get("id")) && pw.getText().equals(userData.get("pw"))) {
                    JOptionPane.showMessageDialog(null, "로그인 성공", "로그인 성공", JOptionPane.INFORMATION_MESSAGE);
                    setVisible(false);
                    EbsClient ebsClient = new EbsClient();
                } else {
                    System.out.println(" ID와 PW가 틀렸습니다.");
                    JOptionPane.showMessageDialog(null, "아이디와 패스워드가 틀렸습니다.", "로그인 실패", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

}



