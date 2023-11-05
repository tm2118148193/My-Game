package cn.cpf.app.chess.swing;

import cn.cpf.app.chess.conf.ChessDefined;

import javax.swing.*;

/**
 * <b>Description : </b> ����������
 * <p>
 * <b>created in </b> 2018/8/27
 *
 * @author CPF
 * @since 1.0
 **/
public class ChessFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    /**
     * Create the frame.
     */
    public ChessFrame() {
        setTitle("�й�����");
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(400, 100, ChessDefined.boardRect.width, ChessDefined.boardRect.height);
        // ��Ӳ˵�
        ChessMenuBar menuBar = new ChessMenuBar();
        menuBar.setVisible(true);
        setJMenuBar(menuBar);
        // ������
        JPanel contentPane = new ChessPanel();
        contentPane.setVisible(true);
        setContentPane(contentPane);
    }

}
