import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

public class ChatFriendListFrame extends JFrame {
   private Socket socket;
    private PrintWriter out;
   private JPanel contentPane;
   private JPanel friendsPanel;
   private ArrayList<String> friends;
   private BufferedReader in;
   int yPos = 150;
   private static Point lastLocation = null; // 창의 마지막 위치를 저장
   private ChatListFrame clf;
   private DataOutputStream dataOut;
   private DataInputStream dataIn;
   private ArrayList<JPanel> friendList = new ArrayList<>();
   
   private JPanel createFriendPanel(String friend) {
       JPanel friendPanel = new JPanel();
       friendPanel.setLayout(null);
       friendPanel.setBounds(10, yPos, 280, 60);  // 친구 패널 위치와 크기 설정
       yPos += 80;
       
       ImageIcon originalIcon = new ImageIcon("src/images/user_icon.png");
       Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
       JLabel lblIcon = new JLabel(new ImageIcon(scaledImage));
       lblIcon.setBounds(5, 5, 50, 50);
       
       friendPanel.add(lblIcon);

       JLabel lblName = new JLabel(friend);
       lblName.setFont(new Font("맑은고딕", Font.PLAIN, 16));
       lblName.setBounds(60, 10, 200, 30);
       friendPanel.add(lblName);

       JLabel lblStatus = new JLabel(new ImageIcon("src/images/online_icon.png"));
       lblStatus.setBounds(230, 20, 20, 20);
       friendPanel.add(lblStatus);
       
       
       friendList.add(friendPanel);
       System.out.println("Friend Panel Created: " + friend); // 디버깅 출력


       return friendPanel;
   }

   public ChatFriendListFrame(String username, String ipAddress, String portNumber, Socket socket, PrintWriter out, BufferedReader in) {
      this.socket = socket;
       this.out = out;
       this.in = in; // 친구 목록 초기화 (임의로 추가)
       
       

      setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // 창 닫기 동작 설정
      
       if (this.in == null) {
               System.out.println("BufferedReader is null.");
           } else {
               System.out.println("BufferedReader is not null.");
           }
   
       try {
			dataOut = new DataOutputStream(socket.getOutputStream());
	        dataIn = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
       
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
       
      friends = new ArrayList<>();
      friends.add(username); // 현재 사용자 추가
      // friends.add("유재석");

      // 프레임 설정
      setTitle(username + " - Chat");
      //setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      setBounds(100, 100, 400, 600);

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

      // 친구 아이콘 추가
      JButton btnFriends = new JButton(new ImageIcon("src/images/friends_icon.png"));
      btnFriends.setBounds(15, 20, 50, 50);
      btnFriends.setFocusPainted(false);
      btnFriends.setBorderPainted(false);
      btnFriends.setContentAreaFilled(false);
      menuPanel.add(btnFriends);

      // 채팅 아이콘 추가
      JButton btnChat = new JButton(new ImageIcon("src/images/chat_icon.png"));
      btnChat.setBounds(15, 80, 50, 50);
      btnChat.setFocusPainted(false);
      btnChat.setBorderPainted(false);
      btnChat.setContentAreaFilled(false);
      menuPanel.add(btnChat);

      // 친구 목록 패널 생성
      friendsPanel = new JPanel();
      friendsPanel.setBackground(Color.WHITE);
      friendsPanel.setLayout(null); // null 레이아웃 설정
      friendsPanel.setBounds(80, 0, 300, 600); // 위치와 크기 지정
      contentPane.add(friendsPanel, BorderLayout.CENTER);

      // 친구 목록 라벨
      JLabel lblTitle = new JLabel("친구");
      lblTitle.setHorizontalAlignment(JLabel.LEFT); // 텍스트 왼쪽 정렬
      lblTitle.setBounds(10, 20, 200, 30); // 위치와 크기 지정
      lblTitle.setFont(new Font("맑은고딕", Font.BOLD, 24)); // 폰트 크기를 크게 설정 (예: 24px)
      lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0)); // 왼쪽 여백
      friendsPanel.add(lblTitle);

      // 친구 목록의 시작 위치
      int yPos = 70;

      JPanel currentUserPanel = new JPanel();
      currentUserPanel.setLayout(null);
      currentUserPanel.setBounds(10, yPos, 280, 60); // 위치와 크기 지정
      yPos += 80;

      // 현재 사용자 프로필 추가
      // 사용자 아이콘
      ImageIcon originalIcon = new ImageIcon("src/images/user_icon.png");
      Image scaledImage = originalIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
      ImageIcon scaledIcon = new ImageIcon(scaledImage);

      JLabel lblIcon = new JLabel(scaledIcon);
      lblIcon.setBounds(5, 5, 50, 50); // 아이콘 위치와 크기
      // 마우스 클릭 이벤트 추가
      lblIcon.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
              // 파일 선택 대화상자 열기
              JFileChooser fileChooser = new JFileChooser();
              fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
              fileChooser.setDialogTitle("이미지 선택");
              int result = fileChooser.showOpenDialog(lblIcon);

              if (result == JFileChooser.APPROVE_OPTION) {
                  File selectedFile = fileChooser.getSelectedFile();
                  // 새로운 이미지 로드
                  ImageIcon newIcon = new ImageIcon(selectedFile.getAbsolutePath());
                  Image resizedImage = newIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                  lblIcon.setIcon(new ImageIcon(resizedImage));
                  
                  out.println("STATUS_UPDATE:"+username+":"+selectedFile.getAbsolutePath());
              }
          }
      });
      
      currentUserPanel.add(lblIcon);

      // 사용자 이름
      JLabel lblName = new JLabel(username);
      lblName.setFont(new Font("맑은고딕", Font.BOLD, 18));
      lblName.setBounds(60, 10, 200, 30); // 이름 위치와 크기
      currentUserPanel.add(lblName);

      friendsPanel.add(currentUserPanel);

       // 친구 목록을 업데이트하는 부분에서 in을 사용하여 데이터를 읽습니다.
      new Thread(() -> {
          try {
              if (in != null) {
                  String userList;
                  while ((userList = in.readLine()) != null) {
                     System.out.println("CFLF:" + userList);
                     if(userList.startsWith("CHAT_TEXT")) continue;
                     if(userList.startsWith("CHAT_EMOTICON")) continue;
                     if(userList.startsWith("_TEXT")) continue;
                     
                     if (userList.startsWith("FILE_TRANSFER")) {
                         // 파일 전송 데이터 처리 로직
                         String[] parts = userList.split(":");
                         String fileName = parts[1];
                         long fileSize = (long)dataIn.readInt();

                         //System.out.println("파일 전송 시작 - 파일 이름: " + fileName + ", 파일 크기: " + fileSize);

                         // 파일 데이터를 스킵
                         byte[] buffer = new byte[(int) fileSize];
                         System.out.println("파일 데이터 스킵 완료");

                         continue; // 파일 데이터 무시하고 다음 메시지로 이동
                     }
                    
                     if (userList.startsWith("CREATE_CHAT_ROOM:")) {
                        clf.processNewChatRoom(userList);
                        continue;
                      }
                     
                      // 서버에서 받은 사용자 목록이 비어 있지 않은지 확인
                      if (userList.trim().isEmpty()) {
                          System.out.println("Received empty user name, skipping.");
                          continue;  // 빈 사용자 이름은 건너뛰기
                      }

                      System.out.println("서버에서 받은 사용자 목록: " + userList);

                      // 이미 존재하는 사용자 처리
                      if (friends.contains(userList)) {
                          System.out.println("User already exists: " + userList);
                          continue;  // 이미 추가된 사용자면 넘어감
                      }

                      if (userList.startsWith("STATUS_UPDATE")||userList.startsWith("US_UPDATE")) {
                    	  System.out.println("STATUS_UPDATE_OPERATE");
                          String[] parts = userList.split(":");
                          String updatedUsername = parts[1];
                          String newIconPath = parts[2] +":"+ parts[3];
                          System.out.println(newIconPath);

                          // UI 업데이트 (SwingUtilities.invokeLater 사용)
                          SwingUtilities.invokeLater(() -> {
                              for (int i=0;i<friendList.size();i++) {
                            	  JLabel tmpLabel = (JLabel) friendList.get(i).getComponent(1);
                            	  System.out.println(tmpLabel.getText() + ", " + updatedUsername);
                                  if (tmpLabel.getText().equals(updatedUsername)) {
                                	  
                                      ImageIcon receivedIcon = new ImageIcon(newIconPath);
                                      Image imageSize = receivedIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH);
                                      ImageIcon newIcon = new ImageIcon(imageSize);
                                      //JLabel newLblIcon = new JLabel(new ImageIcon(imageSize));
                                      ((JLabel) friendList.get(i).getComponent(0)).setIcon(newIcon);
                                      
                                  }
                              }
                              friendsPanel.revalidate();
                              friendsPanel.repaint();
                          });
                          continue;
                      }
                          
                      // 새로운 친구 추가
                      friends.add(userList);
                      System.out.println("Friend added: " + userList);

                      // 패널 생성 및 UI 업데이트
                      JPanel friendPanel = createFriendPanel(userList);
                      SwingUtilities.invokeAndWait(() -> {
                          friendsPanel.add(friendPanel);
                          friendsPanel.revalidate();
                          friendsPanel.repaint();
                          System.out.println("Total friend panels: " + friendsPanel.getComponentCount());
                          System.out.println("Friend Panel Added successfully");
                          System.out.println("Panel added: " + friendPanel.isDisplayable());


                          
                          
                      });
                  }
              } else {
                  System.out.println("BufferedReader is null inside the thread.");
              }
          } catch (IOException | InterruptedException e) {
              e.printStackTrace();
          } catch (InvocationTargetException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
         }
      }).start();

      // 스크롤 추가
      JScrollPane scrollPane = new JScrollPane(friendsPanel);
      contentPane.add(scrollPane, BorderLayout.CENTER);
      this.clf = new ChatListFrame(username, ipAddress, portNumber, this, getLocation(), out);


      btnChat.addActionListener(e -> {
       // 버튼 클릭 이벤트 발생 확인
           System.out.println("Chat 아이콘 클릭됨");
           // 현재 창의 위치를 가져옵니다.
           Point currentLocation = getLocation();

           try {
               // ChatListFrame이 이미 생성되어 있는 경우
               if (this.clf == null) {
                  System.out.println("ChatListFrame 새로 생성");
                   // ChatListFrame을 새로 생성하고 위치를 전달합니다.
                   this.clf.setVisible(true); // 새 창 표시
               } else {
                   // 기존 ChatListFrame 위치를 업데이트하고 보여줍니다.
                  System.out.println("기존 ChatListFrame 위치 업데이트");
                   this.clf.setLocation(currentLocation);
                   this.clf.setVisible(true); // 숨겨진 창 다시 표시
               }

               // 현재 창을 숨깁니다.
               setVisible(false);

           } catch (Exception ex) {
               ex.printStackTrace(); // 문제 발생 시 로그 출력
               JOptionPane.showMessageDialog(null, "창 전환 중 오류가 발생했습니다.");
           }
       });

      
      addWindowListener(new java.awt.event.WindowAdapter() {
         @Override
         public void windowClosing(java.awt.event.WindowEvent e) {
             try {
                 if (socket != null && !socket.isClosed()) {
                     socket.close(); // 창 닫힐 때 소켓 닫기
                 }
                 System.out.println("User disconnected: " + getTitle()); // 창의 제목(username)을 출력
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         }
    });
}

// friends 데이터를 반환하는 메서드
   public ArrayList<String> getFriends() {
       return friends;
   }
}
