package cn.cpf.app.chess.main;

import cn.cpf.app.chess.algorithm.AlphaBeta;
import cn.cpf.app.chess.algorithm.AnalysisBean;
import cn.cpf.app.chess.algorithm.DebugInfo;
import cn.cpf.app.chess.algorithm.Role;
import cn.cpf.app.chess.conf.ChessAudio;
import cn.cpf.app.chess.modal.*;
import cn.cpf.app.chess.swing.BoardPanel;
import cn.cpf.app.chess.swing.ChessPiece;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 * <b>Description : </b> ����ui���̨���ݽ���, �Լ������Կ���
 * <p>
 * <b>created in </b> 2021/8/27
 *
 * @author CPF
 * @since 0.1
 **/
@Slf4j
public class AppContext {

    private final BoardPanel boardPanel;

    @Getter
    private final CommandExecutor commandExecutor;

    @Getter
    private Situation situation;

    private static final Random random = new Random();

    AppContext(final BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
        commandExecutor = new CommandExecutor(boardPanel);
    }

    /**
     * ��ֳ�ʼ��
     *
     * @param situation �������
     */
    public void init(Situation situation) {
        ChessAudio.OPEN_BOARD.play();
        this.situation = situation;
        boardPanel.init(situation);
        getCommandExecutor().sendCommand(CommandExecutor.CommandType.SustainAiRunIfNextIsAi);
    }

    /**
     * @return AI ������һ������λ��
     *
     * @param pieces     �������
     * @param part       ��ǰ���巽
     * @param forbidStep ��ֹ�Ĳ���(��׽����)
     */
    public static StepBean computeStepBean(@NonNull final Piece[][] pieces, @NonNull final Part part, final StepBean forbidStep) {
        log.info("com run start");
        DebugInfo.initAlphaBetaTime();
        long t = System.currentTimeMillis();
        final AnalysisBean analysisBean = new AnalysisBean(pieces);
        // ������������, ��̬�����������
        final int deep = Application.config().getSearchDeepLevel() + AlphaBeta.searchDeepSuit(analysisBean.getPieceNum());
        // ����
        final Set<StepBean> evaluatedPlaceSet;
        if (Application.config().isParallel()) {
            evaluatedPlaceSet = AlphaBeta.getEvaluatedPlaceWithParallel(pieces, part, deep, forbidStep);
        } else {
            evaluatedPlaceSet = AlphaBeta.getEvaluatedPlace(pieces, part, deep, forbidStep);
        }
        // ���ѡ��һ����õ�һ��
        final StepBean stepBean;
        if (evaluatedPlaceSet.isEmpty()) {
            stepBean = null;
            log.info("evaluated û�����ˣ� Set == > {}", evaluatedPlaceSet);
        } else if (evaluatedPlaceSet.size() > 1) {
            int ran = random.nextInt(evaluatedPlaceSet.size());
            stepBean = (StepBean) evaluatedPlaceSet.toArray()[ran];
            log.info("evaluated Set == > {}", evaluatedPlaceSet);
            log.info("evaluated == > {}", stepBean);
        } else {
            stepBean = (StepBean) evaluatedPlaceSet.toArray()[0];
        }
        log.info("time: {}", (System.currentTimeMillis() - t));
        DebugInfo.logEnd();
        log.info("com run stop");
        return stepBean;
    }

    /**
     * ���Ӻ���, ���������Ӻ��ʤ�����Ȳ����߼�.
     * @return ��ʤ��, �����ֵ��ʾ��ʤ���Ѿ�����, ��Ϸ����. ���Ϊnull ��ʾ��Ϸ���Լ���.
     */
    public Part locatePiece(@NonNull Place from, @NonNull Place to, @NonNull PlayerType playerType) {
        final ChessPiece fromPiece = situation.getChessPiece(from);
        Objects.requireNonNull(fromPiece, "�Ҳ����ƶ�������");
        final Part curPart = fromPiece.piece.part;
        Piece eatenPiece = situation.movePiece(from, to);
        boardPanel.updateMark(from, to);
        // �ж��Ƿ�Ե��˶Է��� Boss
        if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
            if (PlayerType.PEOPLE == playerType) {
                ChessAudio.WIN_BGM.play();
            } else {
                ChessAudio.LOSE_BGM.play();
            }
            JOptionPane.showMessageDialog(boardPanel, curPart.name() + "ʤ��", "��Ϸ����", JOptionPane.INFORMATION_MESSAGE);
            log.info("��Ϸ���� ==> {} ʤ��", curPart.name());
            return curPart;
        }
        AnalysisBean bean = new AnalysisBean(situation.generatePieces());
        /* �ж϶Է��Ƿ�����: AI �������۶Է���ô��, ���޷����ⱻ�Ե� BOSS, �����Է��Ѿ����� */
        if (!bean.canAvoidBeEatBossAfterOneAIStep(Part.getOpposite(curPart))) {
            if (PlayerType.PEOPLE == playerType) {
                ChessAudio.WIN_BGM.play();
            } else {
                ChessAudio.BE_CHECKMATED_BY_COM.play();
            }
            JOptionPane.showMessageDialog(boardPanel, curPart.name() + "ʤ��", "��Ϸ����", JOptionPane.INFORMATION_MESSAGE);
            log.info("��Ϸ���� ==> {} ʤ��", curPart.name());
            return curPart;
        }
        // �Ƿ񽫾�: AI ���� curPart �� ����һ���Ƿ��ܹ��Ե��Է��� BOSS
        final boolean canEatBossNextStep = bean.canEatBossAfterOneAiStep(curPart);
        // ���ݲ�ͬ�����������
        if (PlayerType.PEOPLE == playerType) {
            if (canEatBossNextStep) {
                ChessAudio.MAN_CHECK.play();
            } else if (eatenPiece == null){
                ChessAudio.MAN_MOVE.play();
            } else {
                ChessAudio.MAN_EAT.play();
            }
        } else {
            if (canEatBossNextStep) {
                ChessAudio.COM_CHECK.play();
            } else if (eatenPiece == null){
                ChessAudio.COM_MOVE.play();
            } else {
                ChessAudio.COM_EAT.play();
            }
        }
        return null;
    }

    /**
     * @return ���� true: �����ɹ�; ����false: ����ʧ��, �Ѿ�û�м�¼, �޷��ټ�������
     */
    public boolean rollbackOneStep() {
        final StepRecord stepRecord = situation.rollbackOneStep();
        if (stepRecord == null) {
            boardPanel.initMark();
        } else {
            boardPanel.updateMark(stepRecord.getFrom(), stepRecord.getTo());
        }
        return stepRecord != null;
    }

    /**
     * AI ����һ��, ���л�ʤ��, �򷵻ػ�ʤ��
     *
     * @return ��ʤ��, �����ֵ��ʾ��ʤ���Ѿ�����, ��Ϸ����. ���Ϊnull ��ʾ��Ϸ���Լ���.
     */
    public Part aiRunOneTime() {
        final Part winner = situation.winner();
        if (winner != null) {
            log.warn("ʤ����[{}]�Ѿ�����, �޷��ٴ� AI ����", winner);
            return winner;
        }
        final Part nextPart = situation.getNextPart();
        // �ж��Ƿ������Ҫ��ֹ�ߵ���·(��׽, ����)
        final StepBean forbidStepBean = situation.getForbidStepBean();
        // �������һ����
        StepBean evaluatedStepBean = computeStepBean(situation.generatePieces(), nextPart, forbidStepBean);
        if (evaluatedStepBean == null) {
            log.warn("[{}]û��������, �޷��ٴ� AI ����", nextPart);
            return Part.getOpposite(nextPart);
        }
        return locatePiece(evaluatedStepBean.from, evaluatedStepBean.to, PlayerType.COM);
    }

}
