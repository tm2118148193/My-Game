package cn.cpf.app.chess.main;

import cn.cpf.app.chess.algorithm.AnalysisBean;
import cn.cpf.app.chess.algorithm.Role;
import cn.cpf.app.chess.conf.ChessDefined;
import cn.cpf.app.chess.inter.MyList;
import cn.cpf.app.chess.modal.*;
import cn.cpf.app.chess.swing.BoardPanel;
import cn.cpf.app.chess.swing.ChessPiece;
import cn.cpf.app.chess.util.JsonUtils;
import com.github.cosycode.common.lang.ShouldNotHappenException;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * <b>Description : </b> ��ǰ��ֵ�����, ˫������ʲô����, ��ʲôλ��, ��һ����˭��.
 * <p>
 * Ϊ��̨��������ǰ̨���׵Ķ���, ���Ա���̨������ {@link AppContext} & ǰ̨panel {@link BoardPanel} ����
 * </p>
 *
 * @author CPF
 * Date: 2020/3/18 14:22
 */
@Slf4j
public class Situation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ��������
     */
    private final transient ChessPiece[][] pieceArrays;
    /**
     * �췽boss, �������
     */
    @Getter
    private transient ChessPiece redBoss;
    /**
     * �ڷ�boss, �������
     */
    @Getter
    private transient ChessPiece blackBoss;

    @Getter
    private transient int redPieceNum;

    @Getter
    private transient int blackPieceNum;
    /**
     * �����¼
     */
    @Getter
    private final SituationRecord situationRecord;
    @Getter
    private final LocalDateTime situationStartTime;
    /**
     * ���ŵ������б�
     */
    @Getter
    @SuppressWarnings("java:S1948")
    private final List<ChessPiece> pieceList;
    /**
     * ��һ�����ߵ�����
     */
    @Getter
    private Part nextPart;

    public Situation(@NonNull List<ChessPiece> list, @NonNull SituationRecord situationRecord, @NonNull Part nextPart, @NonNull LocalDateTime dateTime) {
        this.situationRecord = situationRecord;
        this.nextPart = nextPart;
        this.situationStartTime = dateTime;
        // ��Ա������ʼ��
        pieceList = new ArrayList<>(list.size());
        pieceArrays = new ChessPiece[ChessDefined.RANGE_X][ChessDefined.RANGE_Y];
        redBoss = null;
        blackBoss = null;
        redPieceNum = 0;
        blackPieceNum = 0;
        // ��Ա������ֵ
        pieceList.addAll(list);
        list.forEach(it -> {
            final Place place = it.getPlace();
            final Piece piece = it.piece;
            pieceArrays[place.x][place.y] = it;
            if (Part.RED == piece.part) {
                redPieceNum++;
                if (Role.BOSS == piece.role) {
                    redBoss = it;
                }
            } else {
                blackPieceNum++;
                if (Role.BOSS == piece.role) {
                    blackBoss = it;
                }
            }
        });
    }

    public ChessPiece getChessPiece(@NonNull Place place) {
        return pieceArrays[place.x][place.y];
    }

    /**
     * ���� ChessPiece[][] �����½���һ�� Piece[][] ����
     */
    public Piece[][] generatePieces() {
        final Piece[][] pieces = new Piece[ChessDefined.RANGE_X][ChessDefined.RANGE_Y];
        for (int x = 0; x < ChessDefined.RANGE_X; x++) {
            for (int y = 0; y < ChessDefined.RANGE_Y; y++) {
                final ChessPiece chessPiece = pieceArrays[x][y];
                if (chessPiece != null) {
                    pieces[x][y] = chessPiece.piece;
                }
            }
        }
        return pieces;
    }

    /**
     * @return ��ȡ��Ҫ��ֹ�����岽��(��׽ & ����)
     */
    public StepBean getForbidStepBean() {
        // �ж��Ƿ������Ҫ��ֹ�ߵ���·(��׽, ����)
        final StepRecord[] loopStepArr = getSituationRecord().getLoopStepRecord();
        if (loopStepArr == null || loopStepArr.length == 0) {
            return null;
        }
        // �Է���һ��, ������һ��, �Է���һ��
        final StepRecord before = loopStepArr[0];
        final StepRecord current = loopStepArr[1];
        final StepRecord after = loopStepArr[2];
        final ChessPiece chessPiece = getChessPiece(current.getFrom());
        assert chessPiece != null && chessPiece.piece == current.getPiece() : "current: " + current;
        AnalysisBean bean = new AnalysisBean(generatePieces());
        final Piece currentPiece = current.getPiece();
        final Part curPart = currentPiece.part;
        assert curPart == getNextPart() : "curPart != getNextPart()";
        // A, B ����, ����������������򹹳ɳ�׽, ������׽
        // 1. ���� to λ��, �ܳԵ��Է���һ���� from λ��
        // 2. ���� from λ��, �޷��Ե��Է���һ���� from λ��
        // 2. ���� to λ��, �޷��Ե��Է���һ���� to λ��
        if (currentPiece.role.find(bean, curPart, current.getTo()).contains(after.getFrom())
                && !currentPiece.role.find(bean, curPart, current.getFrom()).contains(after.getFrom())
                && !currentPiece.role.find(bean, curPart, current.getTo()).contains(after.getTo())) {
            return StepBean.of(current.getFrom(), current.getTo());
        }
        // A, B ����, ����������������򹹳ɳ���, ������
        // 1. �Է���һ���� to λ��, �ܳԵ� ������ from λ��
        // 2. �Է���һ���� to λ��, �޷��Ե������� to λ��
        final Part opposite = Part.getOpposite(curPart);
        final MyList<Place> beforeRange = before.getPiece().role.find(bean, opposite, before.getTo());
        if (beforeRange.contains(current.getFrom()) && !beforeRange.contains(current.getTo())) {
            return null;
        }
        // ������ǳ���, ��������ͨѭ��
        // ���������ж�, Ҳ����Ƕ������, Ҳ����������������Ļ�, �ᵼ�±���Σ��
        // ����ͨѭ���ᵼ��һֱ������, ����һֱ�����ӵĻ�, ��ֱ����ͣ COM ����.
        log.warn("��������ͨѭ�������ǳ���");
        return null;
    }

    /**
     * ����Ѿ���ʤ����, �򷵻�ʤ����, ���򷵻� null
     *
     * @return ʤ����
     */
    public Part winner() {
        boolean isRedBossExist = false;
        boolean isBlankBossExist = false;
        for (int x = 0; x < ChessDefined.RANGE_X; x++) {
            for (int y = 0; y < ChessDefined.RANGE_Y; y++) {
                final ChessPiece chessPiece = pieceArrays[x][y];
                if (chessPiece != null && chessPiece.piece.role == Role.BOSS) {
                    if (chessPiece.piece.part == Part.BLACK) {
                        isBlankBossExist = true;
                    } else {
                        isRedBossExist = true;
                    }
                }
            }
        }
        if (isRedBossExist) {
            return isBlankBossExist ? null : Part.RED;
        } else if (isBlankBossExist) {
            return Part.BLACK;
        } else {
            throw new ShouldNotHappenException("���� Boss ��������");
        }
    }

    /**
     * �ƶ�����
     *
     * @param from Դλ��
     * @param to   Ŀ��λ��
     * @return ���Ŀ��λ���� Boss ��ɫ, �򷵻� ���� boss ��ɫ������
     */
    Piece movePiece(Place from, Place to) {
        final ChessPiece fromPiece = getChessPiece(from);
        final ChessPiece eatenPiece = getChessPiece(to);
        Objects.requireNonNull(fromPiece, "�Ҳ����ƶ�������");
        // �ж��Ƿ��ǳ���, ������ӱ��Ե�, �������ƶ��б�
        if (eatenPiece != null) {
            pieceList.remove(eatenPiece);
            // ui ����
            log.info("move {} -> {}, {} eat {}", from, to, fromPiece.name, eatenPiece.name);
        }
        // ������������
        pieceArrays[from.x][from.y] = null;
        pieceArrays[to.x][to.y] = fromPiece;
        /* ui �ƶ�from����, ���ر��Ե������� */
        fromPiece.movePlace(to);
        if (eatenPiece != null) {
            eatenPiece.hide();
        }
        // ��Ӽ�¼
        situationRecord.addRecord(new StepRecord(fromPiece.piece, from, to, eatenPiece));
        // �������
        nextPart = Part.getOpposite(nextPart);
        // �������߳��ж��Ƿ�ʤ��, ������
        return eatenPiece == null ? null : eatenPiece.piece;
    }

    /**
     * ����һ����
     *
     * @return �����Ĳ����¼
     */
    StepRecord rollbackOneStep() {
        Objects.requireNonNull(situationRecord, "situationRecord shouldn't be null");
        List<StepRecord> list = situationRecord.getRecords();
        if (list.isEmpty()) {
            log.warn("������ʷ��¼Ϊ��, �Ѿ����˵���ʼ״̬!");
            return null;
        }
        // ������¼
        final StepRecord stepRecord = situationRecord.popRecord();
        final Place from = stepRecord.getFrom();
        final Place to = stepRecord.getTo();
        final ChessPiece eatenPiece = stepRecord.getEatenPiece();
        Optional.ofNullable(pieceArrays[from.x][from.y]).ifPresent(e -> {
            throw new IllegalStateException("�˴�����������: " + e);
        });
        // ����from
        final ChessPiece movePiece = getChessPiece(to);
        pieceArrays[from.x][from.y] = movePiece;
        /* ���б��Ե�������, �򸴻�, �ƶ��б� */
        if (eatenPiece != null) {
            pieceList.add(eatenPiece);
        }
        pieceArrays[to.x][to.y] = eatenPiece;
        // ui�ƶ����ӷŵ������, ����ʾ���Ե������Ӻ��ƶ�
        if (eatenPiece != null) {
            eatenPiece.setPlaceAndShow(to);
        }
        movePiece.movePlace(from);
        // �������
        nextPart = Part.getOpposite(nextPart);
        return stepRecord;
    }

    @Override
    public String toString() {
        return "Situation:" + hashCode() + "=" + JsonUtils.toJson(this);
    }
}
