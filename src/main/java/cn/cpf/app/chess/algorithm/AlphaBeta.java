package cn.cpf.app.chess.algorithm;

import cn.cpf.app.chess.conf.ChessDefined;
import cn.cpf.app.chess.inter.MyList;
import cn.cpf.app.chess.modal.Part;
import cn.cpf.app.chess.modal.Piece;
import cn.cpf.app.chess.modal.Place;
import cn.cpf.app.chess.modal.StepBean;
import cn.cpf.app.chess.util.ArrayUtils;
import cn.cpf.app.chess.util.ListPool;
import com.github.cosycode.common.ext.bean.DoubleBean;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <b>Description : </b> AI ������ֵ�����㷨
 * <p>
 * <br>�Ը�����������һ����ֵ���������������ֵ��Զ�Ǵ�һ�����������������۵ģ��췽����ʱ��һ���������ڷ�����ʱ��һ��������
 * <br>��ͨ��������ΪMax����������ֵ�������ʾ�Լ��������������ڶԷ�Min��˵������ѡ���ֵС���ŷ���
 * <br>
 * <br>��Negamax�����������AlphaBeta�е��������������ֵ�˭���������еġ�
 * <br>��Minimax����AlphaBeta�㷨�У��ֺ췽����ʱ������ֵΪ 100���ֺڷ���������ֵ����100��
 * <br>����Negamax����AlphaBeta�㷨�У��ֺ췽����ʱ������ֵΪ 100���ֺڷ�����ʱ����ֵҪΪ-100��
 * </p>
 * <p>
 * <b>created in </b> 2017/12/22
 * </p>
 *
 * @author CPF
 **/
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class AlphaBeta {

    private static final int MAX = 100_000_000;
    /**
     * ����Ҫ��֤ Min + Max = 0, ������΢������Ĳ�඼���ܵ��·�������
     */
    private static final int MIN = -MAX;

    /**
     * ������������, ��̬�����������
     *
     * @param pieceNum ��������
     * @return ����������Ȳ�ֵ
     */
    public static int searchDeepSuit(final int pieceNum) {
        // ������������, ��̬�����������
        if (pieceNum > 20) {
            return -2;
        } else if (pieceNum <= 4) {
            return 4;
        } else if (pieceNum <= 8) {
            return 2;
        }
        return 0;
    }

    /**
     * ���ɴ�ѡ���б����ǿ������ӵĿ�λ, ��� deep > 2 ������������������.
     *
     * @param analysisBean ���̷�������
     * @param curPart      ��ǰ���巽
     * @param deep         �������
     * @return �������ӵĿ�λ����
     */
    private static MyList<StepBean> geneNestStepPlaces(final AnalysisBean analysisBean, final Part curPart, final int deep) {
        final Piece[][] pieces = analysisBean.pieces;
        // �Ƿ�ɱ��
        MyList<StepBean> stepBeanList = ListPool.localPool().getAStepBeanList();
        for (int x = 0; x < ChessDefined.RANGE_X; x++) {
            for (int y = 0; y < ChessDefined.RANGE_Y; y++) {
                final Piece fromPiece = pieces[x][y];
                if (fromPiece != null && fromPiece.part == curPart) {
                    final Place from = Place.of(x, y);
                    // TO DO �����´˴���������ϵ����� �ڼ���ʱ �Ƿ����Ż��ռ�.
                    final MyList<Place> list = fromPiece.role.find(analysisBean, curPart, from);
                    if (list.isEmpty()) {
                        ListPool.localPool().addListToPool(list);
                        continue;
                    }
                    final Object[] elementData = list.eleTemplateDate();
                    for (int i = 0, len = list.size(); i < len; i++) {
                        stepBeanList.add(StepBean.of(from, (Place) elementData[i]));
                    }
                    ListPool.localPool().addListToPool(list);
                }
            }
        }
        // �Ƿ�����, ���������ȴ���2, ��Խ����������
        // �����Ľ��, ���뼫��Сֵ�����㷨ʱ, ���ױ���֦.
        if (deep > 2) {
            orderStep(analysisBean, stepBeanList, curPart);
        }

        return stepBeanList;
    }

    /**
     * �� ��λ�б� ��������, �����Ŀ�λ�б�, ���뼫��Сֵ�����㷨ʱ, ���ױ���֦.
     *
     * @param analysisBean ���̷�������
     * @param stepBeanList �������ӵĿ�λ�б�
     * @param curPart      ��ǰ���巽
     */
    private static void orderStep(final AnalysisBean analysisBean, final MyList<StepBean> stepBeanList, final Part curPart) {
        final Piece[][] srcPieces = analysisBean.pieces;
        // ����ѭ��֮ǰ�����ѭ����ʹ�ó���
        MyList<DoubleBean<Integer, StepBean>> bestPlace = ListPool.localPool().getADoubleBeanList();
        // �Է�����
        final Part oppositeCurPart = Part.getOpposite(curPart);
        int best = MIN;

        final Object[] objects = stepBeanList.eleTemplateDate();
        for (int i = 0; i < stepBeanList.size(); i++) {
            final StepBean item = (StepBean) objects[i];
            final Place to = item.to;
            // ����
            final Piece eatenPiece = srcPieces[to.x][to.y];
            int score;
            // �ж��Ƿ�ʤ��
            if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
                score = MAX;
            } else {
                // ����
                final int invScr = analysisBean.goForward(item.from, to, eatenPiece);
                DebugInfo.incrementAlphaBetaOrderTime();
                // ����
                score = negativeMaximumWithNoCut(analysisBean, oppositeCurPart, -best);
                // �˻���һ��
                analysisBean.backStep(item.from, to, eatenPiece, invScr);
            }
            // ������ӽ����еķ���
            bestPlace.add(new DoubleBean<>(score, item));
            if (score > best) { // �ҵ�һ�����õķ֣��Ͱ���ǰ���λ��ȫ�����
                best = score;
            }
        }
        /* ����󷵻� */
        // ������������ȷ��, ������Ч��������
        bestPlace.sort((o1, o2) -> o2.getO1() - o1.getO1());

        stepBeanList.clear();
        bestPlace.forEach(dou -> stepBeanList.add(dou.getO2()));

        ListPool.localPool().addListToDoubleBeanListPool(bestPlace);
    }


    /**
     * ������ֵ�����㷨(������֦�㷨)
     *
     * @param analysisBean ���Ʒ�������
     * @param curPart      ��ǰ���巽
     * @return ������ֵ�����㷨�����ֵ
     */
    private static int negativeMaximumWithNoCut(AnalysisBean analysisBean, Part curPart, int alphaBeta) {
        // 1. ��ʼ����������
        final Piece[][] pieces = analysisBean.pieces;
        int best = MIN;
        // 2. ���ɴ�ѡ���б����ǿ������ӵ��б�
        MyList<StepBean> stepBeanList = geneNestStepPlaces(analysisBean, curPart, 1);

        final Object[] objects = stepBeanList.eleTemplateDate();
        for (int i = 0, len = stepBeanList.size(); i < len; i++) {
            final StepBean item = (StepBean) objects[i];
            Place from = item.from;
            Place to = item.to;
            // ����
            Piece eatenPiece = pieces[to.x][to.y];
            int score;
            // �ж��Ƿ�ʤ��
            if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
                score = MAX;
            } else {
                // ����
                final int invScr = analysisBean.goForward(from, to, eatenPiece);
                DebugInfo.incrementAlphaBetaOrderTime();
                score = analysisBean.getCurPartEvaluateScore(curPart);
                // �˻���һ��
                analysisBean.backStep(from, to, eatenPiece, invScr);
            }
            if (score > best) { // �ҵ�һ�����õķ֣��͸��·���
                best = score;
            }
            if (score > alphaBeta) { // alpha��֦
                break;
            }
        }
        ListPool.localPool().addListToStepBeanListPool(stepBeanList);
        return -best;
    }


    /**
     * �������ǵ���(max��)thisSide, ż������human(min��)otherSide
     *
     * @param srcPieces ����
     * @param curPart   ��ǰ���巽
     * @param deep      �������
     * @param forbidStep ��ֹ�Ĳ���(��׽����)
     * @return ��һ����λ��
     */
    public static Set<StepBean> getEvaluatedPlace(final Piece[][] srcPieces, final Part curPart, final int deep, final StepBean forbidStep) {
        // 1. ��ʼ����������
        final AnalysisBean analysisBean = new AnalysisBean(srcPieces);
        // 2. ��ȡ�������ӵĿ�λ�б�
        MyList<StepBean> stepBeanList = geneNestStepPlaces(analysisBean, curPart, deep);
        // 3. �Ƴ������µ���
        stepBeanList.remove(forbidStep);
        // ����ѭ��֮ǰ�����ѭ����ʹ�ó���
        Set<StepBean> bestPlace = new HashSet<>();
        int best = MIN;
        // �Է�����
        final Part oppositeCurPart = Part.getOpposite(curPart);
        // ��һ���
        final int nextDeep = deep - 1;
        log.debug("size : {}, content: {}", stepBeanList.size(), stepBeanList);
        final Object[] objects = stepBeanList.eleTemplateDate();
        for (int i = 0, len = stepBeanList.size(); i < len; i++) {
            StepBean item = (StepBean) objects[i];
            final Place to = item.to;
            // ����
            final Piece eatenPiece = srcPieces[to.x][to.y];
            int score;
            // �ж��Ƿ�ʤ��
            if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
                // ����Խ��, ��ֵԽ��
                score = MAX + deep;
            } else {
                // ����
                final int invScr = analysisBean.goForward(item.from, to, eatenPiece);
                // ����
                if (deep <= 1) {
                    score = analysisBean.getCurPartEvaluateScore(curPart);
                } else {
                    score = negativeMaximum(analysisBean, oppositeCurPart, nextDeep, -best);
                }
                // �˻���һ��
                analysisBean.backStep(item.from, to, eatenPiece, invScr);
            }
            if (score == best) { // �ҵ���ͬ�ķ���, �������һ��
                bestPlace.add(item);
            }
            if (score > best) { // �ҵ�һ�����õķ֣��Ͱ���ǰ���λ��ȫ�����
                best = score;
                bestPlace.clear();
                bestPlace.add(item);
            }
        }
        ListPool.end();
        ListPool.localPool().addListToStepBeanListPool(stepBeanList);
        return bestPlace;
    }

    /**
     * �������ǵ���(max��)thisSide, ż������human(min��)otherSide
     *
     * @param srcPieces ����
     * @param curPart   ��ǰ���巽
     * @param deep      �������
     * @param forbidStep ��ֹ�Ĳ���(��׽����)
     * @return ��һ����λ��
     */
    public static Set<StepBean> getEvaluatedPlaceWithParallel(final Piece[][] srcPieces, final Part curPart, final int deep, final StepBean forbidStep) {
        // 1. ��ʼ����������
        final AnalysisBean srcAnalysisBean = new AnalysisBean(srcPieces);
        // 2. ��ȡ�������ӵĿ�λ�б�
        MyList<StepBean> stepBeanList = geneNestStepPlaces(srcAnalysisBean, curPart, deep);
        // 3. �Ƴ������µ���
        stepBeanList.remove(forbidStep);
        // ����ѭ��֮ǰ�����ѭ����ʹ�ó���
        final Set<StepBean> bestPlace = new HashSet<>();
        final AtomicInteger best = new AtomicInteger(MIN);
        // �Է�����
        final Part oppositeCurPart = Part.getOpposite(curPart);
        // ��һ���
        final int nextDeep = deep - 1;
        log.debug("size : {}, content: {}", stepBeanList.size(), stepBeanList);

        Arrays.stream(stepBeanList.toArray()).parallel().filter(Objects::nonNull).map(StepBean.class::cast).forEach(item -> {
            log.debug("������ ==> Thread : {}", Thread.currentThread().getId());
            final Piece[][] pieces = ArrayUtils.deepClone(srcPieces);
            final AnalysisBean analysisBean = new AnalysisBean(pieces);

            final Place to = item.to;
            // ����
            final Piece eatenPiece = pieces[to.x][to.y];
            int score;
            // �ж��Ƿ�ʤ��
            if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
                // ����Խ��, ��ֵԽ��
                score = MAX + deep;
            } else {
                // ����
                final int invScr = analysisBean.goForward(item.from, to, eatenPiece);
                // ����
                if (deep <= 1) {
                    score = analysisBean.getCurPartEvaluateScore(curPart);
                } else {
                    score = negativeMaximum(analysisBean, oppositeCurPart, nextDeep, -best.get());
                }
                // �˻���һ��
                analysisBean.backStep(item.from, to, eatenPiece, invScr);
            }
            if (score == best.get()) { // �ҵ���ͬ�ķ���, �������һ��
                synchronized (bestPlace) {
                    bestPlace.add(item);
                }
            }
            if (score > best.get()) { // �ҵ�һ�����õķ֣��Ͱ���ǰ���λ��ȫ�����
                best.set(score);
                synchronized (bestPlace) {
                    bestPlace.clear();
                    bestPlace.add(item);
                }
            }
            ListPool.end();
        });
        ListPool.localPool().addListToStepBeanListPool(stepBeanList);
        ListPool.end();
        return bestPlace;
    }


    /**
     * ������ֵ�����㷨
     *
     * @param analysisBean ���Ʒ�������
     * @param curPart      ��ǰ���巽
     * @param deep         �������
     * @param alphaBeta    alphaBeta ��֦��ֵ
     * @return ������ֵ�����㷨�����ֵ
     */
    private static int negativeMaximum(AnalysisBean analysisBean, Part curPart, int deep, int alphaBeta) {
        // 1. ��ʼ����������
        final Piece[][] pieces = analysisBean.pieces;
        int best = MIN;
        // �Է�����
        final Part oppositeCurPart = Part.getOpposite(curPart);
        // ��һ���
        final int nextDeep = deep - 1;
        // 2. ���ɴ�ѡ���б����ǿ������ӵ��б�
        final MyList<StepBean> stepBeanList = geneNestStepPlaces(analysisBean, curPart, deep);

        final Object[] objects = stepBeanList.eleTemplateDate();
        for (int i = 0, len = stepBeanList.size(); i < len; i++) {
            final StepBean item = (StepBean) objects[i];
            Place from = item.from;
            Place to = item.to;
            // ����
            Piece eatenPiece = pieces[to.x][to.y];
            int score;
            // �ж��Ƿ�ʤ��
            if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
                // ����Խ��, ��ֵԽ��
                score = MAX + deep;
            } else {
                // ����
                final int invScr = analysisBean.goForward(from, to, eatenPiece);
                // ����
                if (deep <= 1) {
                    score = analysisBean.getCurPartEvaluateScore(curPart);
                } else {
                    score = negativeMaximum(analysisBean, oppositeCurPart, nextDeep, -best);
                }
                // �˻���һ��
                analysisBean.backStep(from, to, eatenPiece, invScr);
            }
            if (score > best) { // �ҵ�һ�����õķ֣��͸��·���
                best = score;
            }
            if (score > alphaBeta) { // alpha��֦
                break;
            }
        }
        ListPool.localPool().addListToStepBeanListPool(stepBeanList);
        return -best;
    }

}
