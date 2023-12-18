package Login;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class JoinService extends JFrame {

    public static Map<String, String> userData;

    JTextField id;
    JTextField pw;

    JButton saveButton;
    MainLogin mainLogin;

    public JoinService() {
        userData = new HashMap<>();
        JoinFrame();
    }


    public void JoinFrame() {
        this.setTitle("회원가입");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setBounds(500, 500, 500, 400);
        this.getContentPane().setLayout((LayoutManager)null);
        saveButton = new JButton("회원가입 완료 !");
        saveButton.setBackground(Color.WHITE);
        saveButton.setForeground(Color.BLACK);
        this.saveButton.setBounds(150,70,97,31);


        id = new JTextField("ID");
        pw = new JTextField("PW");
        this.id.setBounds(0, 40, 150, 31);
        this.pw.setBounds(0, 70, 150, 31);
        this.getContentPane().add(id);
        this.getContentPane().add(pw);
        this.getContentPane().add(saveButton);
        setVisible(true);


        /* 위의 Map값을 MainLogin에서 가져오고, MainLogin에서 검증하여 EbsServer와 EbsClient를 실행시켜야함.*/

        /*회원가입 버튼 클릭시 다시 로그인 화면으로 복구 */

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                userData.put("id", id.getText());
                userData.put("pw", pw.getText());
                setVisible(false);
                mainLogin = new MainLogin();
                mainLogin.setVisible(true);
                System.out.println("ID: " + userData.get("id") + ", PW: " + userData.get("pw"));
            }
        });
//        텍스트에 입력한 값을 Map에 저장하는 로직 필요
        /*Map에 저장한 뒤 다시 로그인 화면으로 전환하여 값이 확인되면 EBSChat에 접속되는 로직 필요.*/
    }
}
