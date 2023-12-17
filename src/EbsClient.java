import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.io.*;
import java.net.*;

public class EbsClient extends JFrame implements ActionListener, Runnable {
    private JLabel nickname_lb = new JLabel();
    private JTextField nickname_tf = new JTextField();
    private JButton conn_bt = new JButton();
    private JTextArea view_ta = new JTextArea();
    private JScrollPane view_jsp = new JScrollPane();
    private JTextField talk_tf = new JTextField();
    private JButton send_bt = new JButton();
    private JLabel user_lb = new JLabel();
    //private JLabel jLabel3 = new JLabel();
    private JLabel inwon_lb = new JLabel();
    private JRadioButton radio1 = new JRadioButton();
    private JRadioButton radio2 = new JRadioButton();
    private JButton end_bt = new JButton();
    private List list = new List(); //접속 리스트
    private JButton nickchange_bt = new JButton();

    private BufferedReader in;
    private OutputStream out;
    private Socket soc;
    int count = 0; // 접속 인원수


    public EbsClient() {
        try {
            initComponents();
            addListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initComponents() {
        this.setLayout(null);
        this.setSize(new Dimension(360, 480));
        this.setTitle("EbsChat");
        this.setBackground(new Color(198, 214, 255));

        nickname_lb.setText("닉네임");
        nickname_lb.setBounds(15, 10, 45, 25);
        this.add(nickname_lb);

        nickname_tf.setBounds(60, 10, 100, 25);
        this.add(nickname_tf);

        conn_bt.setText("접속");
        conn_bt.setBounds(165, 10, 60, 25);
        this.add(conn_bt);

        user_lb.setText("접속자");
        user_lb.setBounds(15, 45, 75, 10);
        this.add(user_lb);

        //접속자 리스트
        list.setBounds(15, 65, 316, 100);
        this.add(list);

        //jLabel3.setText("접속자 수");
        //jLabel3.setBounds(530, 170, 35, 25);
        //this.add(jLabel3);

        //접속자 수
        inwon_lb.setText("0");
        inwon_lb.setBounds(55, 40, 18, 20);
        inwon_lb.setBackground(new Color(198, 198, 200));
        inwon_lb.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        inwon_lb.setHorizontalAlignment(SwingConstants.CENTER);
        inwon_lb.setHorizontalTextPosition(SwingConstants.CENTER);
        this.add(inwon_lb);

        radio1.setText("귓속말");
        radio1.setBounds(80, 40, 70, 25);
        this.add(radio1);

        radio2.setText("귓속말해제");
        radio2.setBounds(150, 40, 90, 25);
        this.add(radio2);

        ButtonGroup group = new ButtonGroup();
        group.add(radio1);
        group.add(radio2);

        //채팅창
        view_jsp.setBounds(15, 170, 317, 200);
        this.getContentPane().add(view_jsp);
        view_jsp.getViewport().add(view_ta, null);

        //채팅입력창
        talk_tf.setBounds(15, 375, 250, 25);
        this.add(talk_tf);

        send_bt.setText("전송");
        send_bt.setBounds(270, 375, 60, 25);
        this.add(send_bt);


        nickchange_bt.setText("대화명 변경");
        nickchange_bt.setBounds(230, 10, 100, 25);
        this.add(nickchange_bt);

        end_bt.setText("나가기");
        end_bt.setBounds(260, 405, 70, 25);
        this.add(end_bt);
    }

    public void addListener() {
        nickname_tf.addActionListener(this);
        talk_tf.addActionListener(this);
        send_bt.addActionListener(this);
        conn_bt.addActionListener(this);
        end_bt.addActionListener(this);
        nickchange_bt.addActionListener(this);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nickname_tf || e.getSource() == conn_bt) {
            //대화명 입력 후 접속
            if (nickname_tf.getText().equals("")) {
                JOptionPane.showMessageDialog(this, "대화명 입력");
                nickname_tf.requestFocus();
                return;
            }

            try {
                soc = new Socket("localhost", 9985);
                in = new BufferedReader(
                        new InputStreamReader(soc.getInputStream(), "euc-kr"));
                out = soc.getOutputStream();
                out.write((nickname_tf.getText() + "\n").getBytes("euc-kr"));
                new Thread(this).start();//run()을 호출
            } catch (Exception e2) {
                System.out.println("접속 오류:" + e2);
            }
        } else if (e.getSource() == talk_tf || e.getSource() == send_bt) {
            //메세지 전송
            try {
                if (radio1.isSelected()) {//귓속말 메세지
                    String name = list.getSelectedItem();
                    out.write(("/s" + name + "-" + talk_tf.getText() + "\n").getBytes("euc-kr"));
                    view_ta.append(name + "님에게 귓속말이 전달되었습니다.\n");
                } else { //일반 메세지
                    out.write((talk_tf.getText() + "\n").getBytes("euc-kr"));
                }
                talk_tf.setText("");
                talk_tf.requestFocus();
            } catch (Exception e2) {
                System.out.println("메세지 전송 오류:" + e2);
            }
        } else if (e.getSource() == nickchange_bt) {
            //대화명 변경
            if (nickchange_bt.getText().equals("대화명 변경")) {
                nickchange_bt.setText("변경확인");
                nickname_tf.setEditable(true);
                nickname_tf.requestFocus();
            } else {
                nickchange_bt.setText("대화명 변경");
                nickname_tf.setEditable(false);
                try {
                    out.write(("/r" + nickname_tf.getText() + "\n").getBytes("euc-kr"));
                } catch (Exception e2) {
                    System.out.println("대화명 변경 오류:" + e2);
                }
            }
        } else if (e.getSource() == end_bt) {
            //나가기
            try {
                out.write(("/q\n").getBytes());
                in.close();
                out.close();
                soc.close();
            } catch (Exception e2) {
                System.out.println("나가기 오류:" + e2);
            } finally {
                System.exit(0);
            }
        }
    }


    @Override
    public void run() {
        while (true) {
            try {
                String msg = in.readLine(); //서버로부터 메세지 수신
                if(msg == null || msg.equals("")) return;
                if(msg.charAt(0) == '/') {
                    String userName = msg.substring(2).trim().toLowerCase();
                    if(msg.charAt(1) == 'c') { //대화명(입장) or 기존 접속자 목록
                        if(userName.contains(" ")) { // 기존 접속자 목록
                            String[] userNames = userName.split(" ");
                            for(String name : userNames) {
                                if(!contains(name)) {
                                    list.add(name, count);
                                    count++;
                                }
                            }
                        } else { // 새로운 접속자
                            if(!contains(userName)){
                                list.add(userName, count);
                                count++;
                            }
                            view_ta.append("##" + userName + "님이 입장했습니다.##\n");
                        }
                        inwon_lb.setText(String.valueOf(count));
                        nickname_tf.setEditable(false); //대화명 입력 불가
                        conn_bt.setEnabled(false);
                    } else if(msg.charAt(1) == 'q') { //퇴장
                        view_ta.append("##" + msg.substring(2) + "님이 퇴장했습니다.##\n");
                        String cname = msg.substring(2);
                        for (int i = 0; i < count; i++) {
                            if(cname.equals(list.getItem(i))) {
                                list.remove(i);
                                count--;
                                inwon_lb.setText(String.valueOf(count));
                                break;
                            }
                        }
                    } else if(msg.charAt(1) == 'r') { //대화명 변경
                        String oldName = msg.substring(2, msg.indexOf('-'));
                        String newName = msg.substring(msg.indexOf('-') + 1);
                        view_ta.append("##" + oldName + "님의 대화명이 " + newName + "으로 변경됐습니다.##\n");
                        for (int i = 0; i < count; i++) {
                            if(oldName.equals(list.getItem(i))) {
                                list.replaceItem(newName, i);
                                break;
                            }
                        }
                    }
                } else { //일반 메세지
                    view_ta.append(msg + "\n");
                }
            } catch (Exception e) {
                System.out.println("run err : " + e);
            }
        }
    }

    public boolean contains(String name) {
        for (int i = 0; i < list.getItemCount(); i++) {
            if (list.getItem(i).equals(name)) {
                return true;
            }
        }
        return false;
    }

    public static void main(String args[]) {
        EbsClient fr = new EbsClient();
        fr.getPreferredSize();
        fr.setLocation(200, 200);
        fr.setVisible(true);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}