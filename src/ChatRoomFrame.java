import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

public class ChatRoomFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JTextField textField;
	private String username;
    private String ipAddress;
    private String portNumber;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String participants;
    private ChatListFrame chatListFrame; // ChatListFrame의 참조
    private int yPos = 0;
    private int oldPos = 0;
    private int t = 0;
    private JScrollPane scrollPane;
    private DataOutputStream dataOut;
    private JPanel panel;
    private DataInputStream dataIn;
    private HashSet<String> displayedMessages = new HashSet<>(); // 출력된 메시지 기록
    
	public ChatRoomFrame(String chatRoomName, String username, String ipAddress, String portNumber, String participants, ChatListFrame chatListFrame, Point lastLocation) {
		this.username = username;
        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        this.participants = participants;
        this.chatListFrame = chatListFrame; // 기존 ChatListFrame 참조 저장
        
		// 메인 패널 설정
		contentPane = new JPanel();
		contentPane.setBackground(new Color(128, 128, 128));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null); // 레이아웃을 null로 설정
        setContentPane(contentPane);
        
		panel = new JPanel();
		panel.setLayout(null);
		panel.setBackground(new Color(128, 128, 128)); // 배경색
	    panel.setBounds(10, 64, 353, 360); // x, y 좌표 및 크기
	    panel.setPreferredSize(new Dimension(10000,10000));
	    
        // JScrollPane을 사용하여 스크롤 가능하도록 설정
        scrollPane = new JScrollPane(panel);
        scrollPane.setBounds(10, 64, 353, 360);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        contentPane.add(scrollPane);
	    
		//contentPane.add(panel);
        
        try {
            // 서버에 연결
            socket = new Socket(ipAddress, Integer.parseInt(portNumber));
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 서버에 사용자 이름 전송
            out.println(username);
            
            try {
    			dataOut = new DataOutputStream(socket.getOutputStream());
    	        dataIn = new DataInputStream(socket.getInputStream());
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		

            // 메시지 수신 스레드 시작
            new Thread(() -> {
                try {
                    String message;
                    while ((message = in.readLine()) != null) {
                        // 채팅창에 메시지 표시
                    	 // 메시지를 JLabel로 추가
                    	System.out.println("Form:" + message);
                    	if (message.startsWith("CHAT_EMOTICON")) {
                    	    String[] text = message.split(":");
                    	    String sender = text[1].trim();
                    	    String imagePath = text[2].trim();
                    	    String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

                    	    // 메시지 고유 키 생성
                    	    String emoticonKey = sender + "::: " + imagePath + "::: " + currentTime;

                    	    // 중복 체크
                    	    if (!displayedMessages.contains(emoticonKey) && !isMessageAlreadySaved(emoticonKey)) {
                    	        displayedMessages.add(emoticonKey); // 기록

                    	        // UI에 이모티콘 추가
                    	        SwingUtilities.invokeLater(() -> addEmoticonToPanel(sender, imagePath, currentTime));

                    	        // 송신자가 내가 아닌 경우만 파일에 저장
                    	        if (!sender.equals(username)) {
                    	            saveMessageToFile(sender, imagePath, currentTime);
                    	        }
                    	    }
                    	}

                    	else if(message.startsWith("FILE_TRANSFER")) {
                    	    String[] fileInfo = message.split(":");
                    	    String fileName = fileInfo[1]; // Extract file name
                    	    System.out.println("fileName : " + fileName);
                    	    //long fileSize = (long)dataIn.readInt(); // Extract file size (in bytes)
                    	    //System.out.println(fileSize);

                    	    // Create a label to show the user the incoming file
                    	    JButton fileLabel = new JButton("Received file: " + fileName);
                    	    fileLabel.setForeground(Color.BLACK);
                    	    fileLabel.setOpaque(true);
                    	    fileLabel.setBackground(Color.LIGHT_GRAY);
                    	    fileLabel.setBounds(10, yPos, 250, 30); // Adjust as needed
                    	    
                    	    panel.add(fileLabel);
                    	    panel.revalidate();
                    	    panel.repaint();
                    	    
                    	    yPos += 35;
                    	}
                    	else if (message.startsWith("CHAT_TEXT")) {
                    	    String[] text = message.split(":");
                    	    String sender = text[1].trim();
                    	    String content = text[2].trim();
                    	    String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

                    	    // 메시지 고유 키 생성 (sender + content + time)
                    	    String messageKey = sender + "::: " + content + "::: " + currentTime;

                    	    // 중복 체크: 파일과 displayedMessages 확인
                    	    if (!displayedMessages.contains(messageKey) && !isMessageAlreadySaved(messageKey)) {
                    	        displayedMessages.add(messageKey); // 메시지 기록

                    	        // UI에 메시지 추가
                    	        SwingUtilities.invokeLater(() -> addMessageToPanel(sender, content, currentTime));

                    	        // 송신자가 내가 아닌 경우만 파일에 저장
                    	        if (!sender.equals(username)) {
                    	            saveMessageToFile(sender, content, currentTime);
                    	        }
                    	    }
                    	}



                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "서버에 연결할 수 없습니다.");
            dispose();
        }
        
        setTitle(chatRoomName);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 389, 572);
		
        // 마지막 위치를 기반으로 새 창 배치
        if (lastLocation != null) {
            setLocation(lastLocation.x + 20, lastLocation.y + 20); // 약간 오른쪽 아래로 이동
        } else {
            setLocationRelativeTo(null); // 중앙 배치
        }
        
		JPanel panel_1 = new JPanel();
		panel_1.setBounds(10, 434, 353, 91);
		contentPane.add(panel_1);
		panel_1.setLayout(null);
		
		// 메시지 입력 필드
		textField = new JTextField();
		textField.setText("메시지 입력");
		textField.setBounds(0, 0, 353, 60);
		panel_1.add(textField);
		textField.setColumns(10);

		textField.addActionListener(e -> sendMessage()); // Enter 키로 메시지 전송

		// FocusListener를 사용하여 메시지 필드 클릭 시 기존 텍스트 삭제
		textField.addFocusListener(new FocusListener() {
		    @Override
		    public void focusGained(FocusEvent e) {
		        // 텍스트 필드가 클릭되면 기존 텍스트 삭제
		        if (textField.getText().equals("메시지 입력")) {
		            textField.setText(""); // 기본 텍스트 "메시지 입력"을 지운다.
		        }
		    }

		    @Override
		    public void focusLost(FocusEvent e) {
		        // 텍스트 필드에 아무것도 입력되지 않으면 기본 텍스트 다시 삽입
		        if (textField.getText().isEmpty()) {
		            textField.setText("메시지 입력"); // 텍스트가 비어 있으면 기본 텍스트 삽입
		        }
		    }
		});

        
		// 이모티콘 버튼 이미지 아이콘 생성
		ImageIcon emoticonImage = new ImageIcon("src/images/emoticonImage.png");
		// 이미지를 버튼 크기에 맞게 조정
		Image scaledImage = emoticonImage.getImage().getScaledInstance(36, 33, java.awt.Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImage);
		
		JButton emoticonButton = new JButton(scaledIcon);
		emoticonButton.setBounds(0, 58, 36, 33);
		emoticonButton.setBorderPainted(false); // 버튼 테두리 제거
		panel_1.add(emoticonButton);
		
        emoticonButton.addActionListener(e -> {
            // 이모티콘 선택 프레임 생성 및 처리
            new EmoticonFrame(selectedEmoticon -> {
                // 선택된 이모티콘의 경로 가져오기
                String imagePath = selectedEmoticon.getDescription(); // 이모티콘 파일 경로

             // 현재 시간 가져오기
                String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

                // 1. 서버로 경로 전송
                out.println("CHAT_EMOTICON:" + username + ":" + imagePath);

                // 이모티콘을 파일에 저장
                saveMessageToFile(username, imagePath, currentTime);

                // 송신자 측 UI에 이모티콘 추가
                addEmoticonToPanel(username, imagePath, currentTime);
            }).setVisible(true);
        });
		
		// 파일 첨부 이미지 아이콘 생성
		ImageIcon fileUploadImage = new ImageIcon("src/images/fileUploadImage.png");
		// 이미지를 버튼 크기에 맞게 조정
		Image scaledImage2 = fileUploadImage.getImage().getScaledInstance(36, 33, java.awt.Image.SCALE_SMOOTH);
		ImageIcon scaledIcon2 = new ImageIcon(scaledImage2);

		JButton fileUploadButton = new JButton(scaledIcon2);
		fileUploadButton.setBounds(35, 58, 36, 33);
		fileUploadButton.setBorderPainted(false); // 버튼 테두리 제거
		fileUploadButton.addActionListener(e->sendFile());
		panel_1.add(fileUploadButton);
	
		
		// 엔터 전송 버튼
		JButton enterButton = new JButton("전송");
		enterButton.setForeground(new Color(0, 0, 0));
		enterButton.setBackground(new Color(255, 255, 0));
		enterButton.setBounds(287, 58, 66, 33);
		panel_1.add(enterButton);
		
		enterButton.addActionListener(e -> sendMessage());
		

		JPanel panel_2 = new JPanel();
		panel_2.setBackground(new Color(128, 128, 128));
		panel_2.setBounds(0, 0, 375, 63);
		contentPane.add(panel_2);
		panel_2.setLayout(null);
		
		// 채팅방 이름을 표시하는 라벨
		JLabel receiverNameLabel = new JLabel(participants);
		receiverNameLabel.setBounds(12, 14, 205, 32);
		panel_2.add(receiverNameLabel);
		
		loadMessagesFromFile(); // 저장된 메시지 불러오기
	}

	private void sendMessage() {
	    String message = textField.getText().trim();
	    if (!message.isEmpty()) {
	        // 현재 시간 가져오기
	        String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

	        // 서버로 메시지 전송
	        out.println("CHAT_TEXT:" + username + ": " + message);
	        textField.setText(""); // 입력 필드 초기화

	        saveMessageToFile(username, message, currentTime); // 메시지 저장
	        
	     // 메시지와 시간을 포함하는 HTML 라벨
	        JLabel messageLabel = new JLabel("<html><div style='text-align: right;'>" + message + 
	                                        "<br><span style='font-size: 7px; color: gray;'>" + currentTime + "</span></div></html>");
	        messageLabel.setForeground(Color.BLACK);
	        messageLabel.setOpaque(true);
	        messageLabel.setBackground(Color.YELLOW);
	        messageLabel.setBorder(new EmptyBorder(5, 10, 5, 10)); // 여백 추가

	        // 메시지 길이에 따라 동적 크기 설정
	        FontMetrics fm = messageLabel.getFontMetrics(messageLabel.getFont());
	        int textWidth = Math.min(250, fm.stringWidth(message) + 40); // 최대 250px로 제한
	        int textHeight = Math.max(50, fm.getHeight() + 10); // 메시지 높이 설정

	        // 라벨 위치 및 크기 설정 (오른쪽 정렬)
	        int xPosition = 340 - textWidth - 10; // 패널 오른쪽 여백 포함
	        messageLabel.setBounds(xPosition, yPos, textWidth, textHeight);
	        
	        yPos += textHeight + 5; // 다음 메시지를 위한 yPos 조정 (간격 포함)
	        

	        // 메시지 라벨을 패널에 추가
	        panel.add(messageLabel);
	        panel.revalidate();
	        panel.repaint();
	        
	        scrollToBottom(yPos - oldPos);
	        oldPos = yPos;
	    }
	}

	
    private void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] fileData = fis.readAllBytes();
                
                System.out.println("SENDFILE:" + file.getName());
                // 서버로 파일 전송
                out.println("FILE_TRANSFER:" + file.getName());
                dataOut.writeInt(fileData.length);
                dataOut.write(fileData);
                dataOut.flush();

                JOptionPane.showMessageDialog(this, "파일 전송 완료: " + file.getName());
                
        	    JButton fileLabel = new JButton("Sended file: " + file.getName());
        	    fileLabel.setForeground(Color.BLACK);
        	    fileLabel.setOpaque(true);
        	    fileLabel.setBackground(Color.YELLOW);
        	    fileLabel.setBounds(90, yPos, 250, 30); // Adjust as needed
        	    
        	    panel.add(fileLabel);
        	    panel.revalidate();
        	    panel.repaint();
        	    
        	    yPos += 35;
        	    
        	    scrollToBottom(yPos - oldPos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        scrollToBottom(yPos - oldPos);
        oldPos = yPos;
    }
    
    private void saveFileLocally(String fileName, byte[] fileData) {
        try {
            // 저장할 폴더 경로 설정 (프로젝트 폴더 내 resources/chatHistory)
            File resourcesDir = new File("resources/chatHistory");
            
            // 폴더가 없으면 생성
            if (!resourcesDir.exists()) {
                resourcesDir.mkdirs();
            }

            // 파일 저장 경로 설정
            FileOutputStream fileOut = new FileOutputStream(new File(resourcesDir, fileName));
            fileOut.write(fileData);
            fileOut.close();

            JOptionPane.showMessageDialog(null, "파일 저장 완료: " + resourcesDir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "파일 저장 실패: " + e.getMessage());
        }
    }

    
    // 채팅 내용을 파일에 저장. 파일명은 채팅방 참여자 이름을 기반으로 생성
    private void saveMessageToFile(String sender, String content, String time) {
        try {
            File resourcesDir = new File("resources/chatHistory");
            if (!resourcesDir.exists()) resourcesDir.mkdirs();

            String fileName = "chat_" + participants + ".txt";
            File chatLogFile = new File(resourcesDir, fileName);

            String messageKey = sender + "::: " + content + "::: " + time;

            BufferedWriter writer = new BufferedWriter(new FileWriter(chatLogFile, true));
            writer.write(messageKey);
            writer.newLine();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "메시지 저장 실패: " + e.getMessage());
        }
    }

    private boolean isMessageAlreadySaved(String messageKey) {
        String fileName = "chat_" + participants + ".txt";
        File chatLogFile = new File("resources/chatHistory", fileName);

        if (!chatLogFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(chatLogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals(messageKey)) {
                    return true; // 중복 메시지 발견
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    
    private void loadMessagesFromFile() {
        panel.removeAll(); // 패널 초기화
        yPos = 0;

        String fileName = "chat_" + participants + ".txt";
        File chatLogFile = new File("resources/chatHistory", fileName);

        if (!chatLogFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(chatLogFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
            	if (!displayedMessages.contains(line)) { // 중복 체크
                    displayedMessages.add(line); // 로드된 메시지 기록
                    String[] parts = line.split("::: ");
                    if (parts.length >= 3) {
                        String sender = parts[0];
                        String content = parts[1];
                        String time = parts[2];

                        // 이미지 경로인지 메시지인지 구분
                        if (content.endsWith(".png") || content.endsWith(".jpg")) {
                            addEmoticonToPanel(sender, content, time);
                        } else {
                            addMessageToPanel(sender, content, time);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
     // 스크롤을 맨 아래로 이동
        scrollToBottom(yPos - oldPos);
        oldPos = yPos;
    }

    private void addMessageToPanel(String sender, String content, String time) {
        JLabel messageLabel;
        if (sender.equals(username)) {
            messageLabel = new JLabel("<html><div style='text-align: right;'>" + content +
                                      "<br><span style='font-size: 7px; color: gray;'>" + time + "</span></div></html>");
            messageLabel.setBackground(Color.YELLOW);
        } else {
            messageLabel = new JLabel("<html>" + sender + ": " + content +
                                      "<br><span style='font-size: 7px; color: gray;'>" + time + "</span></html>");
            messageLabel.setBackground(Color.LIGHT_GRAY);
        }

        messageLabel.setForeground(Color.BLACK);
        messageLabel.setOpaque(true);
        messageLabel.setBorder(new EmptyBorder(5, 10, 5, 10));

        FontMetrics fm = messageLabel.getFontMetrics(messageLabel.getFont());
        int textWidth = Math.min(250, fm.stringWidth(content) + 70);
        int textHeight = Math.max(50, fm.getHeight() + 10);

        if (sender.equals(username)) {
            int xPosition = 370 - textWidth - 10;
            messageLabel.setBounds(xPosition, yPos, textWidth, textHeight);
        } else {
            messageLabel.setBounds(10, yPos, textWidth, textHeight);
        }

        yPos += textHeight + 5;
        panel.add(messageLabel);
        panel.revalidate();
        panel.repaint();
        
     // 스크롤을 맨 아래로 이동
        scrollToBottom(yPos - oldPos);
        oldPos = yPos;
    }

 // 이전 메시지를 기억하기 위한 필드
    private String lastMessage = "";
    private String lastSender = "";
    private String lastTime = "";
    
 // 중복 메시지 확인 메서드
    private boolean isDuplicateInFile(String sender, String content, String time) {
        String fileName = "chat_" + participants + ".txt";
        File chatLogFile = new File("resources/chatHistory", fileName);

        if (!chatLogFile.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(chatLogFile))) {
            String lastLine = null;
            String currentLine;

            // 마지막 줄 읽기
            while ((currentLine = reader.readLine()) != null) {
                lastLine = currentLine;
            }

            // 파일이 비어있거나 마지막 줄이 없으면 중복 아님
            if (lastLine == null) return false;

            // 마지막 줄을 ::: 기준으로 나누어 비교
            String[] parts = lastLine.split("::: ");
            if (parts.length >= 3) {
                String lastSender = parts[0];
                String lastContent = parts[1];
                String lastTime = parts[2];

                return sender.equals(lastSender) && content.equals(lastContent) && time.equals(lastTime);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    // 이모티콘을 ui에 출력하는 메서드
    private void addEmoticonToPanel(String sender, String imagePath, String time) {
        JLabel emoticonLabel = new JLabel();
        ImageIcon emoticonIcon = new ImageIcon(imagePath);
        Image scaledImage = emoticonIcon.getImage().getScaledInstance(160, 160, Image.SCALE_SMOOTH);
        emoticonLabel.setIcon(new ImageIcon(scaledImage));

        // 이모티콘 출력 위치 설정
        if (sender.equals(username)) {
            emoticonLabel.setBounds(370 - 170, yPos, 160, 160); // 오른쪽 정렬
        } else {
            emoticonLabel.setBounds(10, yPos, 160, 160); // 왼쪽 정렬
        }

        // 패널에 추가
        yPos += 170; // 이모티콘 높이만큼 yPos 조정
        panel.add(emoticonLabel);
        panel.revalidate();
        panel.repaint();
        
     // 스크롤을 맨 아래로 이동
        scrollToBottom(yPos - oldPos);
        oldPos = yPos;
    }
    // 스크롤을 가장 아래로 이동
    private void scrollToBottom(int x) {
        SwingUtilities.invokeLater(() -> {
            JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
            if(t >= 360)
            	verticalBar.setValue(t + x - 360);
            t = t + x;
        });
    }

}