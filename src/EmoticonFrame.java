import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class EmoticonFrame extends JFrame {
    private static final long serialVersionUID = 1L;

    public interface EmoticonSelectionListener {
        void onEmoticonSelected(ImageIcon emoticon);
    }

    public EmoticonFrame(EmoticonSelectionListener listener) {
        setTitle("이모티콘 선택");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(300, 300);
        setLayout(new GridLayout(3, 3, 10, 10)); // 3x3 그리드, 간격 10px
        setLocationRelativeTo(null); // 화면 중앙 배치

        // 이모티콘 추가
        String[] emoticonPaths = {
            "src/images/emoticon1.png",
            "src/images/emoticon2.png",
            "src/images/emoticon3.png",
            "src/images/emoticon4.png",
            "src/images/emoticon5.png",
            "src/images/emoticon6.png",
            "src/images/emoticon7.png",
            "src/images/emoticon8.png",
            "src/images/emoticon9.png"
        };

        for (String path : emoticonPaths) {
            ImageIcon emoticonIcon = new ImageIcon(path);
            JButton emoticonButton = new JButton(new ImageIcon(emoticonIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH)));
            emoticonButton.setBorderPainted(false); // 버튼 테두리 제거
            emoticonButton.setContentAreaFilled(false); // 배경 제거
            emoticonButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    listener.onEmoticonSelected(new ImageIcon(path));
                    dispose(); // 이모티콘 창 닫기
                }
            });
            add(emoticonButton);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EmoticonFrame(emoticon -> {
                System.out.println("선택된 이모티콘: " + emoticon);
            }).setVisible(true);
        });
    }
}
