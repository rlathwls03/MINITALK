import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class JavaChatClientMain extends JFrame {
    private JPanel contentPane;
    private JTextField txtUserName;
    private JTextField txtIpAddress;
    private JTextField txtPortNumber;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                try {
                    JavaChatClientMain frame = new JavaChatClientMain();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public JavaChatClientMain() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 300, 450);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setBackground(Color.yellow);
        contentPane.setLayout(null);

        // UI Setup
        JLabel titleLabel = new JLabel("MINITALK");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(47, 30, 200, 60);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24)); 
        contentPane.add(titleLabel);

        JLabel lblUserName = new JLabel("User Name");
        lblUserName.setHorizontalAlignment(SwingConstants.CENTER);
        lblUserName.setBounds(50, 120, 80, 30);
        contentPane.add(lblUserName);

        txtUserName = new JTextField();
        txtUserName.setHorizontalAlignment(SwingConstants.CENTER);
        txtUserName.setBounds(150, 120, 100, 30);
        contentPane.add(txtUserName);
        txtUserName.setColumns(10);

        JLabel lblIpAddress = new JLabel("IP Address");
        lblIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        lblIpAddress.setBounds(50, 170, 80, 30);
        contentPane.add(lblIpAddress);

        txtIpAddress = new JTextField();
        txtIpAddress.setHorizontalAlignment(SwingConstants.CENTER);
        txtIpAddress.setText("127.0.0.1");
        txtIpAddress.setBounds(150, 170, 100, 30);
        contentPane.add(txtIpAddress);
        txtIpAddress.setColumns(10);

        JLabel lblPortNumber = new JLabel("Port Number");
        lblPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        lblPortNumber.setBounds(50, 220, 80, 30);
        contentPane.add(lblPortNumber);

        txtPortNumber = new JTextField();
        txtPortNumber.setText("30000");
        txtPortNumber.setHorizontalAlignment(SwingConstants.CENTER);
        txtPortNumber.setBounds(150, 220, 100, 30);
        contentPane.add(txtPortNumber);
        txtPortNumber.setColumns(10);

        JButton btnConnect = new JButton("Connect");
        btnConnect.setBounds(50, 280, 200, 40);
        contentPane.add(btnConnect);
        
        // 엔터 키 눌렀을 때 Connect 버튼 클릭 효과 추가
        txtUserName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    btnConnect.doClick(); // Connect 버튼 클릭
                }
            }
        });

        btnConnect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = txtUserName.getText().trim();
                String ipAddress = txtIpAddress.getText().trim();
                String portNumber = txtPortNumber.getText().trim();

                new Thread(() -> {
                    try {
                        // 새 소켓 생성 (독립적)
                        Socket userSocket = new Socket(ipAddress, Integer.parseInt(portNumber));
                        PrintWriter userOut = new PrintWriter(userSocket.getOutputStream(), true);
                        BufferedReader userIn = new BufferedReader(new InputStreamReader(userSocket.getInputStream()));

                        // 사용자 이름 전송
                        userOut.println(username);
                        System.out.println("서버에 사용자 이름 전송: " + username);

                        // 독립적인 친구 목록 창 생성
                        ChatFriendListFrame frame = new ChatFriendListFrame(username, ipAddress, portNumber, userSocket, userOut, userIn);
                        SwingUtilities.invokeLater(() -> frame.setVisible(true)); // UI는 Event Dispatch Thread에서 실행

                        // username 필드 초기화 (메인 창 유지)
                        SwingUtilities.invokeLater(() -> txtUserName.setText(""));
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }).start();
            }
        });
    }
}