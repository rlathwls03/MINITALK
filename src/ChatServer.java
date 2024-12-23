import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class ChatServer {
    private static Set<ClientHandler> clientHandlers = new HashSet<>();  // 연결된 클라이언트를 추적하는 Set
    private static final String FILE_SAVE_DIR = "src/received_files"; // 파일 저장 경로
    private static final String LOG_FILE = "src/log/chat_log.txt";

    
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(30000);  // 서버 포트 30000
        System.out.println("서버가 시작되었습니다.");

        new File(FILE_SAVE_DIR).mkdir();

        while (true) {
            Socket clientSocket = serverSocket.accept();  // 클라이언트 연결 대기
            System.out.println("새로운 클라이언트가 접속했습니다.");

            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clientHandlers.add(clientHandler);

            // 각 클라이언트에 대해 쓰레드 시작
            new Thread(clientHandler).start();
        }
    }
    
    public static synchronized void broadcastFile(String fileName, byte[] fileData, String sender) {
        for (ClientHandler client : clientHandlers) {
            client.sendFile(fileName, fileData);
        }
    }
    
    
    public static synchronized void broadcastStatusUpdate(String username, String iconPath) {
        String message = "STATUS_UPDATE:" + username + ":" + iconPath;
        for (ClientHandler client : clientHandlers) {
            client.sendMessage(message);
        }
    }


    // 메시지를 파일에 기록하는 메서드
    private static synchronized void logMessage(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("로그 파일에 메시지를 기록하는 중 오류 발생: " + e.getMessage());
        }
    }
    
    public static synchronized void broadcastUserList() {
        StringBuilder userList = new StringBuilder();
        for (ClientHandler handler : clientHandlers) {
            userList.append(handler.getUsername()).append("\n");
        }
        
        System.out.println("브로드캐스트 중인 사용자 목록:\n" + userList); // 디버깅 출력

        
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(userList.toString());
        }
    }
    


    
    public static synchronized void broadcastChatRoom(String participants, String message) {
        System.out.println("Broadcast to: " + participants);
        for (ClientHandler client : clientHandlers) {
            if (participants.contains(client.getUsername())) {
                client.sendMessage(message);
            }
        }
    }


    public static synchronized void addClient(ClientHandler clientHandler) {
        clientHandlers.add(clientHandler);
        broadcastUserList();
    }

    public static synchronized void removeClient(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
        broadcastUserList();
    }


 // 클라이언트가 접속할 때마다 사용자 목록을 전송하는 부분
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
		private DataInputStream dataIn;
		private DataOutputStream dataOut;

        public ClientHandler(Socket socket) throws IOException {
            this.socket = socket;
            this.out = new PrintWriter(socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.dataIn = new DataInputStream(socket.getInputStream());
            this.dataOut = new DataOutputStream(socket.getOutputStream());
        }

        public void sendFile(String fileName, byte[] fileData) {
            try {
            	
            	String dataFile = "FILE_TRANSFER:" + fileName;
                //dataOut.writeUTF("FILE_TRANSFER");
                //dataOut.writeUTF(sender);
                //dataOut.writeUTF(fileName);
            	out.write(dataFile);
                dataOut.writeInt(fileData.length);
                dataOut.write(fileData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public String getUsername() {
         // TODO Auto-generated method stub
         return username;
      }

      public void sendMessage(String userList) {
         // TODO Auto-generated method stub
         if(out != null) {
            out.println(userList);
         }
      }

      @Override
      public void run() {
          try {
              username = in.readLine(); // 사용자 이름 받기
              System.out.println(username + "이(가) 접속했습니다.");
              
              synchronized (clientHandlers) {
                  broadcastUserList();
              }
              
              String message;
              while ((message = in.readLine()) != null) {
            	  String fMsg = message;
            	  String sMsg = message;
            	  
            	  if (fMsg.startsWith("FILE_TRANSFER")) {
                      // 파일 전송 처리
            		  System.out.println("File transfer start");
            		  String[] info = fMsg.split(":");
                      String fileName = info[1];
                      System.out.println(fileName);
                      long fileLength = (long)dataIn.readInt();
                      byte[] fileData = new byte[(int) fileLength];
                      dataIn.readFully(fileData);
                      
                      //out.flush();

                      // 서버에 파일 저장
                      File file = new File(FILE_SAVE_DIR + "/" + fileName);
                      try (FileOutputStream fos = new FileOutputStream(file)) {
                          fos.write(fileData);
                      }
                      System.out.println("파일 저장 완료: " + file.getAbsolutePath());

                      // 다른 클라이언트에 파일 전송
                      //broadcastFile(fileName, fileData, username);
            	  }
            	  else if(fMsg.startsWith("STATUS_UPDATE")) {
            		  
            	  }
                  String formattedMessage = message; // 메시지 형식 설정
                  
                  if(formattedMessage.startsWith("CHAT_TEXT") || formattedMessage.startsWith("CHAT_EMOTICON")||formattedMessage.startsWith("FILE_TRANSFER")){
                	  logMessage(formattedMessage); // 파일에 메시지 기록
                	  System.out.println("수신 메시지: " + formattedMessage); // 디버깅용 출력
                  }
                  
                  synchronized (clientHandlers) {                	  
                      for (ClientHandler client : clientHandlers) {
                    	  
                          if (client != this) { // 자기 자신에게는 메시지 전송 안 함
                              client.sendMessage(message);
                          }
                      }
                  }
              }
          } catch (IOException e) {
              System.err.println("클라이언트 통신 오류: " + e.getMessage());
          } finally {
              try {
                  socket.close();
                  removeClient(this); // 클라이언트 제거
              } catch (IOException e) {
                  e.printStackTrace();
              }
          }
      }

    }

}
