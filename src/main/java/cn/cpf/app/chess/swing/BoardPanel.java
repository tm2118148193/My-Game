package cn.cpf.app.chess.swing;

import cn.cpf.app.chess.algorithm.AnalysisBean;
import cn.cpf.app.chess.conf.ChessAudio;
import cn.cpf.app.chess.conf.ChessDefined;
import cn.cpf.app.chess.conf.ChessImage;
import cn.cpf.app.chess.main.Application;
import cn.cpf.app.chess.main.CommandExecutor;
import cn.cpf.app.chess.main.Situation;
import cn.cpf.app.chess.inter.LambdaMouseListener;
import cn.cpf.app.chess.inter.MyList;
import cn.cpf.app.chess.modal.*;
import cn.cpf.app.chess.util.ListPool;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Arrays;

/**
 * <b>Description : </b> �������
 * <p>
 * <b>created in </b> 2018/8/27
 *
 * @author CPF
 * @since 1.0
 **/
@Slf4j
public class BoardPanel extends JPanel implements LambdaMouseListener {

    /**
     * ���ڱ����������ۼ�
     */
    private final transient TraceMarker traceMarker;
    /**
     * ��ǰ���忪ʼ����λ�ö�Ӧ����
     */
    private transient ChessPiece curFromPiece;
    /**
     * ����
     */
    private transient Situation situation;

    /**
     * Create the panel.
     */
    public BoardPanel() {
        setBorder(new EmptyBorder(5, 5, 5, 5));
        setLayout(null);
        // ��ʼ����Ƿ�
        traceMarker = new TraceMarker(BoardPanel.this);
        // �������¼�
        addMouseListener(this);
    }

    /**
     * ���±��
     */
    public void updateMark(Place from, Place to) {
        // ���±��
        curFromPiece = null;
        // ���ı��
        traceMarker.endedStep(from, to);
    }

    /**
     * ��ʼ�����б��
     */
    public void initMark() {
        traceMarker.initMarker();
    }

    /**
     * �������
     */
    public void init(Situation situation) {
        this.situation = situation;
        // �Ƴ��������
        this.removeAll();
        // �������
        situation.getPieceList().forEach(it -> add(it.getComp()));
        situation.getSituationRecord().getEatenPieceList().forEach(it -> add(it.getComp()));
        // ��ʼ����Ƿ�
        traceMarker.initMarker();
        repaint();
    }

    /**
     * @param e ��갴ѹ�¼�����
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        // λ��
        Place pointerPlace = ChessDefined.convertLocationToPlace(e.getPoint());
        if (pointerPlace == null) {
            return;
        }
        if (situation.winner() != null) {
            log.warn("�Ѿ�����ʤ����: {}, �޷�����", situation.winner());
            return;
        }
        // ��ǰ���巽
        @NonNull Part pointerPart = situation.getNextPart();
        // ��ǰ��������
        ChessPiece pointerPiece = situation.getChessPiece(pointerPlace);
        // ͨ����ǰ���͵�ǰλ���ж��Ƿ��������
        // step: form
        if (curFromPiece == null) {
            // ��ǰ����λ�����������Ǳ�������
            if (pointerPiece != null && pointerPiece.piece.part == pointerPart) {
                // ��������, ͬʱ��fromָ��
                curFromPiece = pointerPiece;
                traceMarker.setMarkFromPlace(pointerPlace);
                // ��ȡtoList
                MyList<Place> list = curFromPiece.piece.role.find(new AnalysisBean(situation.generatePieces()), pointerPart, pointerPlace);
                traceMarker.showMarkPlace(list);
                ChessAudio.CLICK_FROM.play();
                log.info("true -> ��ǰ����λ�����������Ǳ�������");
                final ListPool listPool = ListPool.localPool();
                listPool.addListToPool(list);
                return;
            }
            log.warn("warning -> from ����ָʾ����");
            return;
        }
        if (pointerPlace.equals(curFromPiece.getPlace())) {
            log.warn("false -> from == to");
            return;
        }
        // ��ǰ����λ�����������Ǳ�������
        if (pointerPiece != null && pointerPiece.piece.part == pointerPart) {
            assert curFromPiece.piece.part == pointerPart : "��ǰ����λ�����������Ǳ������� ֮ǰָ���˶Է�����";
            // ���� curFromPiece
            curFromPiece = pointerPiece;
            traceMarker.setMarkFromPlace(pointerPlace);
            MyList<Place> list = curFromPiece.piece.role.find(new AnalysisBean(situation.generatePieces()), pointerPart, pointerPlace);
            traceMarker.showMarkPlace(list);
            ChessAudio.CLICK_FROM.play();
            log.info("true -> ���� curFromPiece");
            ListPool.localPool().addListToPool(list);
            return;
        }
        final StepBean stepBean = StepBean.of(curFromPiece.getPlace(), pointerPlace);
        // ��������Ϲ�����ֱ�ӷ���
        final Piece[][] pieces = situation.generatePieces();
        if (!curFromPiece.piece.role.rule.check(pieces, pointerPart, stepBean.from, stepBean.to)) {
            // �����ǰָ�������Ǳ�������
            log.warn("�������������");
            return;
        }
        // �����ɳ������߳�׽, �򷵻�
        final StepBean forbidStepBean = situation.getForbidStepBean();
        if (forbidStepBean != null && forbidStepBean.from == stepBean.from && forbidStepBean.to == stepBean.to) {
            ChessAudio.MAN_MOV_ERROR.play();
            log.warn("������׽");
            return;
        }
        AnalysisBean analysisBean = new AnalysisBean(pieces);
        // ��������, �������� BOSS ����, �򷵻�
        if (!analysisBean.isBossF2FAfterStep(curFromPiece.piece, stepBean.from, stepBean.to)) {
            ChessAudio.MAN_MOV_ERROR.play();
            log.warn("BOSS�����");
            return;
        }
        /* ģ����һ����, ֮���ټ���Է�����һ���Ƿ��ܹ��Ե������� boss */
        if (analysisBean.simulateOneStep(stepBean, bean -> bean.canEatBossAfterOneAiStep(Part.getOpposite(pointerPart)))) {
            ChessAudio.MAN_MOV_ERROR.play();
            log.warn("BOSS Σ��");
            if (!Application.config().isActiveWhenBeCheck()) {
                return;
            }
        }
        // ��ǰ���������ӻ���Ϊ�Է�����, �ҷ��Ϲ���, ��������
        Object[] objects = new Object[]{stepBean.from, stepBean.to, PlayerType.PEOPLE};
        final boolean sendSuccess = Application.context().getCommandExecutor().sendCommandWhenNotRun(CommandExecutor.CommandType.LocationPiece, objects);
        if (!sendSuccess) {
            log.warn("����δ���ͳɹ�: {} ==> {}", CommandExecutor.CommandType.LocationPiece, Arrays.toString(objects));
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Image img = ChessImage.CHESS_BOARD.getImage();
        int imgWidth = img.getWidth(this);
        int imgHeight = img.getHeight(this);// ���ͼƬ�Ŀ����߶�
        int fWidth = getWidth();
        int fHeight = getHeight();// ��ô��ڵĿ����߶�
        int x = (fWidth - imgWidth) / 2;
        int y = (fHeight - imgHeight) / 2;
        // 520 576 514 567
        log.debug(String.format("%s,%s,%s,%s,%s,%s", imgWidth, imgHeight, fWidth, fHeight, x, y));
        g.drawImage(img, 0, 0, null);
    }

}
