package cn.cpf.app.chess.main;

import cn.cpf.app.chess.conf.ChessDefined;
import cn.cpf.app.chess.modal.Part;
import cn.cpf.app.chess.modal.PlayerType;
import cn.cpf.app.chess.swing.BoardPanel;
import cn.cpf.app.chess.swing.ChessFrame;
import cn.cpf.app.chess.swing.ChessPanel;
import cn.cpf.app.chess.swing.ChessPiece;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <b>Description : </b> ��������������
 * <p>
 * <b>created in </b> 2021/8/28
 * </p>
 *
 * @author CPF
 * @since 1.0
 **/
@Slf4j
public class Application {

    private static AppContext appContext;
    private static AppConfig config;

    public static AppConfig config() {
        return config;
    }

    public static AppContext context() {
        return appContext;
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                config = new AppConfig();
                config.setFirstPart(Part.RED);
                config.setBlackPlayerType(PlayerType.COM);
                config.setRedPlayerType(PlayerType.PEOPLE);
                config.setComIntervalTime(500);
                config.setSearchDeepLevel(6);
                config.setSearchKillStepDeepLevel(0);
                config.setParallel(true);
                config.setCartoon(true);
                JFrame frame = new ChessFrame();
                frame.setVisible(true);
                // ��ȡ��������
                BoardPanel boardPanel = (BoardPanel) ((ChessPanel) frame.getContentPane()).getBoardPanel();
                List<ChessPiece> list = ChessDefined.geneDefaultPieceSituation();
                Situation situation = new Situation(list, new SituationRecord(), Application.config().getFirstPart(), LocalDateTime.now());
                appContext = new AppContext(boardPanel);
                appContext.init(situation);
            } catch (Exception e) {
                log.error("", e);
            }
        });
    }

}
