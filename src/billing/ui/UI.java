package billing.ui;


import javax.swing.*;
import java.awt.*;

public class UI {
    static Font MONO = new Font("Consolas", Font.PLAIN, 13);
    static Font BOLD = new Font("Segoe UI", Font.BOLD, 14);
    static Font BIG = new Font("Segoe UI", Font.BOLD, 20);

    /** Hộp thoại báo lỗi, hiện cả thông điệp gốc từ MySQL. */
    public static void error(Component parent, Throwable e) {
        e.printStackTrace();
        String msg = e.getMessage();
        if (msg != null && msg.length() > 1200) msg = msg.substring(0, 1200) + "...";
        JTextArea ta = new JTextArea(msg);
        ta.setEditable(false);
        ta.setFont(MONO);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(650, 220));
        JOptionPane.showMessageDialog(parent, sp, "Co loi xay ra", JOptionPane.ERROR_MESSAGE);
    }
}
