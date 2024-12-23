import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class CreateRoomFrame extends JFrame {
    private ArrayList<String> friends; // 전달받은 친구 목록
    private ChatListFrame chatListFrame; // ChatListFrame 인스턴스를 저장할 변수
    private PrintWriter out;

    public CreateRoomFrame(String username, ArrayList<String> friends, ChatListFrame chatListFrame, PrintWriter out) {
        this.friends = friends;
        this.chatListFrame = chatListFrame; // ChatListFrame 인스턴스를 받음
        this.out = out; // 서버로 메시지를 전송하기 위한 PrintWriter
        
        setTitle("채팅방 만들기");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 400, 400);
        // 부모 창의 위치를 기준으로 새 창 배치
        Point parentLocation = chatListFrame.getLocation(); // 부모 창 위치 가져오기
        System.out.println("Parent location: " + parentLocation); // 디버깅용 출력
        setLocation(parentLocation.x + 50, parentLocation.y + 50); // 부모 창에서 약간 오른쪽 아래로 배치


        // 메인 패널에 null 레이아웃 적용
        JPanel contentPane = new JPanel();
        contentPane.setLayout(null);  // Layout을 null로 설정
        setContentPane(contentPane);

        // 체크박스를 추가할 패널 설정
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(null);  // 체크박스를 수동으로 배치할 수 있게 null 레이아웃 사용
        checkBoxPanel.setBounds(12, 40, 360, 250);  // 위치와 크기 지정
        contentPane.add(checkBoxPanel);

        // 친구 목록을 체크박스로 추가
        int yPos = 10;  // 체크박스의 y 위치 초기값
        for (int i = 0; i < friends.size(); i++) {
            String friend = friends.get(i);
            JCheckBox checkBox = new JCheckBox(friend);

            // 첫 번째 요소에 대해 항상 체크하고 수정할 수 없게 설정
            if (i == 0) {
                checkBox.setSelected(true);  // 항상 체크 표시
                checkBox.setEnabled(false);  // 수정할 수 없게 설정
            }

            checkBox.setBounds(10, yPos, 200, 30);  // 체크박스의 위치와 크기 설정
            checkBoxPanel.add(checkBox);
            yPos += 40;  // 다음 체크박스 위치 조정
        }

        // "대화상대 선택" 라벨 추가
        JLabel lblNewLabel = new JLabel("대화상대 선택");
        lblNewLabel.setFont(new Font("굴림", Font.BOLD, 20));
        lblNewLabel.setBounds(127, 10, 175, 30);  // 위치와 크기 지정
        contentPane.add(lblNewLabel);

        // 방 만들기 버튼 추가
        JButton createRoomButton = new JButton("방 만들기");
        createRoomButton.setBackground(new Color(255, 255, 0));
        createRoomButton.setForeground(new Color(0, 0, 0));
        createRoomButton.setBounds(99, 313, 90, 40);  // 위치와 크기 지정
        createRoomButton.addActionListener(e -> createRoom(checkBoxPanel));
        contentPane.add(createRoomButton);
        
        JButton btnNewButton = new JButton("취소");
        btnNewButton.setBackground(new Color(255, 255, 0));
        btnNewButton.setForeground(new Color(0, 0, 0));
        btnNewButton.addActionListener(new ActionListener() {
           public void actionPerformed(ActionEvent e) {
              dispose();
           }
        });
        btnNewButton.setBounds(201, 313, 90, 40);
        contentPane.add(btnNewButton);
        
        createRoomButton.addActionListener(e -> {
            StringBuilder participants = new StringBuilder();
            for (Component c : checkBoxPanel.getComponents()) {
                if (c instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) c;
                    if (checkBox.isSelected()) {
                        participants.append(checkBox.getText()).append(",");
                    }
                }
            }

            if (participants.length() > 0) {
                participants.setLength(participants.length() - 1); // 마지막 콤마 제거
                String roomName = ""; // 임의의 채팅방 이름
                out.println("CREATE_CHAT_ROOM:"+ participants); // 서버로 요청 전송
                System.out.println("서버로 메시지 전송: "+ participants);
                }
            dispose(); // 창 닫기
        });
    }

    private void createRoom(JPanel checkBoxPanel) {
        StringBuilder selectedFriends = new StringBuilder("선택된 친구: ");
        for (Component c : checkBoxPanel.getComponents()) {
            if (c instanceof JCheckBox) {
                JCheckBox checkBox = (JCheckBox) c;
                if (checkBox.isSelected()) {
                    selectedFriends.append(checkBox.getText()).append(", ");
                }
            }
        }

        if (selectedFriends.toString().equals("선택된 친구: ")) {
            JOptionPane.showMessageDialog(this, "친구를 선택하세요.");
        } else {
            // 마지막 쉼표를 제거
            selectedFriends.setLength(selectedFriends.length() - 2);
            JOptionPane.showMessageDialog(this, selectedFriends.toString());
            dispose();
            
         // 채팅방 버튼을 채팅방 목록에 추가
            chatListFrame.addChatRoomButton(selectedFriends.toString());  // 선택된 친구 목록을 채팅방 버튼에 표시
        }
    }
}
