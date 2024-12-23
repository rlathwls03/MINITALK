import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

public class ChatListFrame extends JFrame {
    private JPanel contentPane;
    private String username;
    private String ipAddress;
    private String portNumber;
    private BufferedReader in;  // 추가된 부분
    private boolean buttonsCreated = false;  // 버튼 생성 여부를 추적하는 변수
    private ChatFriendListFrame lf; // ChatFriendListFrame 참조
    private ArrayList<JButton> chatRoomButtons; // 채팅방 버튼을 저장할 리스트
    private JPanel chatListPanel;  // 채팅방 버튼을 추가할 패널
    private static Point lastLocation = null; // 창의 마지막 위치를 저장
    private int nextButtonY = 60;  // lblChatListTitle 아래 시작 Y 좌표
    private PrintWriter out;
    private static Point lastChatRoomLocation = null; // 마지막 채팅방 위치를 저장
    private ArrayList<Point> chatRoomLocations = new ArrayList<>(); // 각 채팅방의 위치를 저장
    private ArrayList<ChatRoomFrame> chatRoomList = new ArrayList<>();
    
    public ChatListFrame(String username, String ipAddress, String portNumber) {
        this(username, ipAddress, portNumber, null, null, null);
    }

    public ChatListFrame(String username, String ipAddress, String portNumber, ChatFriendListFrame lf, Point location, PrintWriter out) {
        this.username = username;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.lf = lf;
        this.chatRoomButtons = new ArrayList<>(); // 버튼 리스트 초기화
        this.out = out;

        if (lastLocation != null) {
            setLocation(lastLocation); // 마지막 위치로 창 설정
        } else {
            setLocationRelativeTo(null); // 첫 번째 창은 화면 중앙에 위치
        }

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                lastLocation = getLocation(); // 창이 닫힐 때 위치 저장
            }
        });
        
        setTitle("Chat List");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 400, 600);

     // 위치를 복원합니다.
        if (location != null) {
            setLocation(location);
        } else {
            setLocationRelativeTo(null); // 위치 정보가 없으면 중앙에 배치
        }
        
        // 창 크기 변경 불가 설정
        setResizable(false);  // 창 크기 변경 불가
        
        // 메인 패널 설정
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setLayout(new BorderLayout());
        setContentPane(contentPane);

        // 좌측 메뉴 패널 생성
        JPanel menuPanel = new JPanel();
        menuPanel.setPreferredSize(new Dimension(80, 600));
        menuPanel.setBackground(new Color(240, 240, 240));
        contentPane.add(menuPanel, BorderLayout.WEST);
        menuPanel.setLayout(null);

        // 사용자 아이콘 추가
        JButton btnUserIcon = new JButton(new ImageIcon("src/images/friends_icon.png"));
        btnUserIcon.setBounds(15, 20, 50, 50);
        btnUserIcon.setFocusPainted(false);
        btnUserIcon.setBorderPainted(false);
        btnUserIcon.setContentAreaFilled(false);
        menuPanel.add(btnUserIcon);

        btnUserIcon.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 현재 창의 위치를 가져옵니다.
                Point currentLocation = getLocation();

                try {
                    // ChatFriendListFrame 위치를 업데이트하고 보여줍니다.
                    lf.setLocation(currentLocation);
                    lf.setVisible(true); // 숨겨진 창 다시 표시

                    // 현재 창을 숨깁니다.
                    setVisible(false);

                } catch (Exception ex) {
                    ex.printStackTrace(); // 문제 발생 시 로그 출력
                    JOptionPane.showMessageDialog(null, "창 전환 중 오류가 발생했습니다.");
                }
            }
        });

        // 채팅 아이콘 추가
        JButton btnChatIcon = new JButton(new ImageIcon("src/images/chat_icon.png"));
        btnChatIcon.setBounds(15, 80, 50, 50);
        btnChatIcon.setFocusPainted(false);
        btnChatIcon.setBorderPainted(false);
        btnChatIcon.setContentAreaFilled(false);
        menuPanel.add(btnChatIcon);

        
        // 우측 채팅방 목록 패널 생성
        chatListPanel = new JPanel();
        chatListPanel.setBackground(Color.WHITE);
        chatListPanel.setLayout(null); // 수직 배치
        JScrollPane scrollPane = new JScrollPane(chatListPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // 가로 스크롤바 없애기
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // 채팅방 목록 라벨 + add_room_icon 버튼을 한 라인에 배치하기 위한 패널
        JPanel chatListHeaderPanel = new JPanel();
        chatListHeaderPanel.setLayout(null);
        //chatListHeaderPanel.setPreferredSize(new Dimension(400, 60));
        // 채팅방 목록 헤더 패널의 위치와 크기 설정
        chatListHeaderPanel.setBounds(0, 0, 400, 60);

        
        // 채팅방 목록 라벨
        JLabel lblChatListTitle = new JLabel("채팅방 목록");
        lblChatListTitle.setHorizontalAlignment(SwingConstants.LEFT);
        lblChatListTitle.setFont(new Font("맑은고딕", Font.BOLD, 24));
        lblChatListTitle.setBounds(10, 10, 250, 40); // 위치 조정
        chatListHeaderPanel.add(lblChatListTitle);
        
        // 채팅방 만들기 버튼 위치 수정 (왼쪽으로 조금 옮기기)
        JButton addChattingRoomBtn = new JButton(new ImageIcon("src/images/add_room_icon.png"));
        addChattingRoomBtn.setBounds(230, 10, 50, 50); // 버튼 위치를 왼쪽으로 조금 옮김
        addChattingRoomBtn.setFocusPainted(false);
        addChattingRoomBtn.setBorderPainted(false);
        addChattingRoomBtn.setContentAreaFilled(false);
        chatListHeaderPanel.add(addChattingRoomBtn);

        // 채팅방 목록 헤더 패널을 채팅방 목록 패널에 추가
        chatListPanel.add(chatListHeaderPanel);

        // add_room_icon 클릭 시 CreateRoomFrame을 열고 friends 데이터를 전달
        addChattingRoomBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // ChatFriendListFrame에서 friends 데이터를 가져와 CreateRoomFrame에 전달
                CreateRoomFrame createRoomFrame = new CreateRoomFrame(username, lf.getFriends(), ChatListFrame.this, out);
                createRoomFrame.setVisible(true);
            }
        });
        
        new Thread(() -> {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("CREATE_CHAT_ROOM:")) {
                        String participants = message.substring("CREATE_CHAT_ROOM:".length()).trim();
                        processNewChatRoom(participants); // 새로운 채팅방 버튼 생성
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void processNewChatRoom(String roomData) {
        String[] parts = roomData.split(":");
        String roomName = ""; // 채팅방 이름
        String participants = parts[1]; // 참여자 목록
        String[] nameList = participants.split(",");
        int flag = 0;
        
        for(String name : nameList) {
           System.out.println("서버로부터 받은 이름 : " + name + ", 사용자 이름 : " + this.username);
           
           if(name.equals(this.username)) {
              flag++;
              roomName += name + ",";
           }
           else {
              System.out.println("이름이 다름");
           }
        }
        
        String croomName = roomName;
        // UI에 채팅방 버튼 추가
        if(flag > 0) {
        SwingUtilities.invokeLater(() -> {
            addChatRoomButton("채팅방: " + croomName + " (참여자: " + participants + ")");
        });
        }
        else {
           return;
        }
    }

    public void addChatRoomButton(String participants) {
        if (chatListPanel != null) {
            JButton chatRoomButton = new JButton(participants);
            chatRoomButton.setBounds(0, nextButtonY, 300, 50);
            chatRoomButton.setMaximumSize(new Dimension(300, 50));

            chatRoomButton.setBackground(Color.WHITE);
            chatRoomButton.setBorder(null);
            
            // 이름을 관리하기 위한 배열
            String[] currentName = {participants};
            // 마우스 hover 효버 기능
            chatRoomButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent e) {
                    // 커서가 버튼 위로 올라올 때
                    chatRoomButton.setBackground(new Color(0xDDDDDD));
                }

                @Override
                public void mouseExited(java.awt.event.MouseEvent e) {
                    // 커서가 버튼 밖으로 나갈 때
                    chatRoomButton.setBackground(Color.WHITE);
                }
                
                @Override
                public void mousePressed(MouseEvent e) {
                    // 마우스 우클릭 감지
                    if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                        showContextMenu(e, chatRoomButton, currentName);
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    // Windows에서는 마우스 우클릭 release 시 트리거
                    if (e.isPopupTrigger() || e.getButton() == MouseEvent.BUTTON3) {
                        showContextMenu(e, chatRoomButton, currentName);
                    }
                }
            });
            
            // 버튼 클릭 시 동작을 정의
            chatRoomButton.addActionListener(e -> {
            	Point nextLocation = getNextChatRoomLocation();
                // 채팅방 UI 열기
                ChatRoomFrame chatRoomFrame = new ChatRoomFrame("Chat Room", username, ipAddress, portNumber, currentName[0], this, nextLocation);
                chatRoomList.add(chatRoomFrame);
                chatRoomFrame.setVisible(true);
                
             // 채팅방 창의 위치를 리스트에 추가
                chatRoomLocations.add(chatRoomFrame.getLocation());
            });

            
            chatListPanel.add(chatRoomButton);
            chatRoomButtons.add(chatRoomButton);

            nextButtonY += 55; // 버튼 간격 50px
            chatListPanel.setPreferredSize(new Dimension(chatListPanel.getWidth(), nextButtonY));
            chatListPanel.revalidate();
            chatListPanel.repaint();
        } else {
            System.out.println("chatListPanel is null");
        }
    }
    
 // 우클릭 메뉴 표시 메서드
    private void showContextMenu(MouseEvent e, JButton chatRoomButton, String[] currentName) {
        JPopupMenu contextMenu = new JPopupMenu();

        // 이름 변경 메뉴 항목 추가
        JMenuItem renameMenuItem = new JMenuItem("이름 변경");
        renameMenuItem.addActionListener(ev -> {
            String newName = JOptionPane.showInputDialog(this, "새 채팅방 이름을 입력하세요:", "채팅방 이름 변경", JOptionPane.PLAIN_MESSAGE);
            if (newName != null && !newName.trim().isEmpty()) {
                chatRoomButton.setText(newName); // 버튼 텍스트 업데이트
                currentName[0] = newName; // 이름 배열 업데이트
            }
        });

        contextMenu.add(renameMenuItem);

        // 팝업 메뉴를 마우스 위치에 표시
        contextMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    
    private Point getNextChatRoomLocation() {
        if (chatRoomLocations.isEmpty()) {
            // 처음 열리는 채팅 창은 중앙에 배치
            return new Point(200, 200);
        } else {
            // 마지막 창 위치를 가져와 약간 아래쪽과 오른쪽으로 이동
            Point lastLocation = chatRoomLocations.get(chatRoomLocations.size() - 1);
            return new Point(lastLocation.x + 20, lastLocation.y + 20);
        }
    }
}

