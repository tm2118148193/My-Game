package cn.cpf.app.chess.main;

import cn.cpf.app.chess.modal.Part;
import cn.cpf.app.chess.modal.StepRecord;
import cn.cpf.app.chess.swing.ChessPiece;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.LinkedList;
import java.util.List;

/**
 * <b>Description : </b> �����¼��
 *
 * @author CPF
 * Date: 2020/3/19 13:56
 */
public class SituationRecord implements Serializable {

    /**
     * ��ʷ��¼
     */
    @Getter
    private final LinkedList<StepRecord> records = new LinkedList<>();
    /**
     * ���Ե������б�
     */
    @Getter
    private final transient LinkedList<ChessPiece> eatenPieceList = new LinkedList<>();
    /**
     * ���嵱ǰ�ߵĲ���
     */
    @Getter
    private int blackStep;
    /**
     * ���嵱ǰ�ߵĲ���
     */
    @Getter
    private int redStep;

    /**
     * ����������ʼ����ʱ��, �Ѿ����ڵĲ���
     * �ñ�����Ŀ���������ڳ�ʼ����ֵ�ʱ��, ��������Ϊ�Ѿ����ڵ���ʷ��¼, ����һ���ͱ��ж�Ϊ��׽, ����
     */
    private final transient int initStepNumber;

    public SituationRecord(List<StepRecord> records) {
        if (records != null) {
            records.forEach(this::addRecord);
            initStepNumber = records.size();
        } else {
            initStepNumber = 0;
        }
    }

    public SituationRecord() {
        this(null);
    }

    public void addRecord(@NonNull StepRecord stepRecord) {
        if (Part.RED == stepRecord.getPiece().part) {
            redStep++;
        } else {
            blackStep++;
        }
        if (stepRecord.getEatenPiece() != null) {
            eatenPieceList.add(stepRecord.getEatenPiece());
        }
        records.add(stepRecord);
    }

    /**
     * �������¼�¼����, ���б�����Ԫ��, ���׳��쳣
     */
    public StepRecord popRecord() {
        final StepRecord stepRecord = records.pollLast();
        if (stepRecord == null) {
            throw new EmptyStackException();
        } else if (stepRecord.getEatenPiece() != null) {
            final ChessPiece chessPiece = eatenPieceList.pollLast();
            assert chessPiece == stepRecord.getEatenPiece();
        }
        return stepRecord;
    }

    int getTotalStep() {
        return records.size();
    }

    /**
     * ͨ�������¼��, ��ȡ��׽, �����Ĳ���
     *
     * @return ��һ����ֹ�Ĳ���.
     */
    public StepRecord[] getLoopStepRecord() {
        final int length = records.size();
        List<StepRecord> list = new ArrayList<>();
        for (int n = length - 1; n >= initStepNumber ; n--) {
            final StepRecord stepRecord = records.get(n);
            if (stepRecord.getEatenPiece() != null) {
                break;
            }
            list.add(stepRecord);
        }
        // ��Ϊ�Ǻ�ڷ���������, ��ÿ�������ƶ�֮��, ��Ҫ�ƶ���ȥ, ����ʹ�� piece, from, to �����.
        // ���, һ�γ�׽, ���� ���ظ�����Ҫ4��������, ��������� �� n = 4 ��ʼ��ѯ
        for (int n = 4, len = list.size(); n < len; n++) {
            for (int i = 0, j = n; j < len; i ++, j++) {
                final StepRecord iItem = list.get(i);
                final StepRecord jItem = list.get(j);
                // ��Ϊ�˴�, û�г���, ���, ����Ҫ�Ƚ� eatPiece
                // ͬʱ, ����Ƚ� from �Ļ�, ��ô����Ҳ����ֹ��
                // ���ֻҪ�Ƚ� piece, to ��ȼ���
                if (iItem.getPiece() == jItem.getPiece() && iItem.getTo() == jItem.getTo()) {
                    if (i == n - 1) {
                        // i + 1 �ǶԷ�����һ��, i - 1 �ǶԷ�����һ��
                        return new StepRecord[]{list.get(i + 1), iItem, list.get(i - 1)};
                    }
                } else {
                    break;
                }
            }
        }
        return new StepRecord[0];
    }

}
