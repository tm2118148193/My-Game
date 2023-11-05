package cn.cpf.app.chess.algorithm;

import cn.cpf.app.chess.conf.ChessDefined;
import cn.cpf.app.chess.inter.MyList;
import cn.cpf.app.chess.modal.Part;
import cn.cpf.app.chess.modal.Piece;
import cn.cpf.app.chess.modal.Place;
import cn.cpf.app.chess.modal.StepBean;
import cn.cpf.app.chess.util.ArrayUtils;
import lombok.NonNull;

import java.util.Set;
import java.util.function.Predicate;


/**
 * <b>Description : </b> ����ai�㷨�ķ�������, �൱��Ϊ���������һ������
 *
 * @author CPF
 * Date: 2020/3/25 17:33
 */
public class AnalysisBean {

    public final Piece[][] pieces;
    /**
     * �췽 boss λ��
     */
    private Place redBoss;
    /**
     * �ڷ� boss λ��
     */
    private Place blackBoss;
    /**
     * �췽��������
     */
    private int redPieceNum;
    /**
     * �ڷ���������
     */
    private int blackPieceNum;
    /**
     * �췽���Ӵ����ܷ�ֵ(������Boss�ķ�ֵ)
     */
    private int redPieceExistScore;
    /**
     * �ڷ����Ӵ����ܷ�ֵ(������Boss�ķ�ֵ)
     */
    private int blackPieceExistScore;
    /**
     * ˫�����Ʒ�ֵ�ܺ�(�췽Ϊ��,�ڷ�Ϊ��)
     */
    private int pieceScore;

    public AnalysisBean(@NonNull final Piece[][] rawPieceArrays) {
        this.pieces = rawPieceArrays;
        redPieceExistScore = 0;
        blackPieceExistScore = 0;
        redPieceNum = 0;
        blackPieceNum = 0;
        // �ҳ�boss, ��������������
        for (int x = 0, xLen = rawPieceArrays.length; x < xLen; x++) {
            Piece[] pieceArr = rawPieceArrays[x];
            for (int y = 0, yLen = pieceArr.length; y < yLen; y++) {
                Piece piece = pieceArr[y];
                if (piece != null) {
                    if (Part.RED == piece.part) {
                        redPieceNum++;
                        if (piece.role == Role.BOSS) {
                            redBoss = Place.of(x, y);
                        } else {
                            redPieceExistScore += piece.pieceScore.existScore;
                        }
                    } else {
                        blackPieceNum++;
                        if (piece.role == Role.BOSS) {
                            blackBoss = Place.of(x, y);
                        } else {
                            blackPieceExistScore += piece.pieceScore.existScore;
                        }
                    }
                }
            }
        }
        // �����ֵ
        this.pieceScore = calcPieceScore(rawPieceArrays);
    }

    /**
     * ����������������
     */
    public static int calcPieceScore(final Piece[][] pieces) {
        int num = 0;
        for (int x = 0; x < ChessDefined.RANGE_X; x++) {
            for (int y = 0; y < ChessDefined.RANGE_Y; y++) {
                Piece piece = pieces[x][y];
                if (piece == null) {
                    continue;
                }
                if (piece.part == Part.RED) {
                    num += piece.pieceScore.placeScores[x * 10 + 9 - y];
                    num += piece.pieceScore.existScore;
                } else {
                    num -= piece.pieceScore.placeScores[x * 10 + y];
                    num -= piece.pieceScore.existScore;
                }
            }
        }
        return num;
    }

    /**
     * ��ȡ һ����ֻ����, �����أ� �����������ķ�ֵ, �췽Ϊ��, �ڷ�Ϊ��
     *
     * @param from from λ��
     * @param to to λ��
     * @return һ����ֻ����, �����أ� �����������ķ�ֵ
     */
    public int nextStepOpportunityCost(final Place from, final Place to) {
        DebugInfo.incrementAlphaBetaTime();
        // ��ʱ��ֵ
        int invScr = 0;
        final Piece eatenPiece = pieces[to.x][to.y];
        if (eatenPiece != null) {
            // ���ǽ���, �����BOSS�ӵ�λ��
            // �����Ե������Ǻ췽, �� - ���Ե������ӵĴ���ֵ, ���Ǻڷ����෴.
            if (eatenPiece.part == Part.RED) {
                invScr -= eatenPiece.pieceScore.existScore;
                invScr -= eatenPiece.pieceScore.getPlaceScore(Part.RED, to.x, to.y);
            } else {
                invScr += eatenPiece.pieceScore.existScore;
                invScr += eatenPiece.pieceScore.getPlaceScore(Part.BLACK, to.x, to.y);
            }
            // ���·���
        }
        final Piece movePiece = pieces[from.x][from.y];
        // ����Ǻ췽, �� + �ƶ�֮������Ӵ���ֵ, - �ƶ�֮ǰ�����Ӵ���ֵ, ���Ǻڷ����෴.
        if (Part.RED == movePiece.part) {
            invScr += movePiece.pieceScore.getPlaceScore(Part.RED, to.x, to.y);
            invScr -= movePiece.pieceScore.getPlaceScore(Part.RED, from.x, from.y);
            return invScr;
        } else {
            invScr -= movePiece.pieceScore.getPlaceScore(Part.BLACK, to.x, to.y);
            invScr += movePiece.pieceScore.getPlaceScore(Part.BLACK, from.x, from.y);
            return -invScr;
        }
    }

    /**
     * ģ������
     */
    public int goForward(Place from, Place to, Piece eatenPiece) {
        final Piece movePiece = pieces[from.x][from.y];
        pieces[to.x][to.y] = movePiece;
        pieces[from.x][from.y] = null;
        if (movePiece.role == Role.BOSS) {
            updateBossPlace(movePiece.part, to);
        }
        // ��ʱ��ֵ
        int invScr = 0;
        // ����Ǻ췽, �� + �ƶ�֮������Ӵ���ֵ, - �ƶ�֮ǰ�����Ӵ���ֵ, ���Ǻڷ����෴.
        if (Part.RED == movePiece.part) {
            invScr += movePiece.pieceScore.getPlaceScore(Part.RED, to.x, to.y);
            invScr -= movePiece.pieceScore.getPlaceScore(Part.RED, from.x, from.y);
        } else {
            invScr -= movePiece.pieceScore.getPlaceScore(Part.BLACK, to.x, to.y);
            invScr += movePiece.pieceScore.getPlaceScore(Part.BLACK, from.x, from.y);
        }
        if (eatenPiece != null) {
            // ���ǽ���, �����BOSS�ӵ�λ��
            // �����Ե������Ǻ췽, �� - ���Ե������ӵĴ���ֵ, ���Ǻڷ����෴.
            final PieceScore pScore = eatenPiece.pieceScore;
            final int existScore = pScore.existScore;
            final int pieceCount = redPieceNum + blackPieceNum;
            if (eatenPiece.part == Part.RED) {
                redPieceExistScore -= existScore;
                redPieceNum--;
                invScr -= existScore;
                invScr -= pScore.getPlaceScore(Part.RED, to.x, to.y);
                /*
                 * �ӳɷ���: ���Ӵ���ֵ * (��ǰ�����Ӵ����ܷ�ֵ / ˫�����Ӵ����ܷ�ֵ) * 0.5
                 * 1. ����˻����ǲ�����, AI ���Ӿ�������Щ.
                 * 2. �������, ����Լ������������ͱȽ϶�, ��ô������������, ����Լ������������ͱȽ���, ��ô�����ǲ�����.
                 * eg: ���� red: 2000, black: 3000, ����12������, ��ʱ ��ʱ�췽 kill �ڷ� 200, ��ʱ���Ի�� 200 * 0.25 * 2000 / ( 2000 + 3000 ) = 20 ��
                 * ֮�� red: 2000, black: 2800, ����11������, ��ʱ�ڷ��� kill �췽 200, ��ʱ 200 * 0.25 * 2800 / ( 2000 + 2800 ) = 29 ��
                 */
                invScr -= (existScore * redPieceExistScore / (redPieceExistScore + blackPieceExistScore)) >> 3;
                // 2. �����ʧ�����R(������Խ��Խ��Ҫ), ǰ�����и������ӳ�, ���������������ӳ�
                if (eatenPiece.role == Role.HORSE) {
                    invScr -= (16 - pieceCount) << 1;
                }
            } else {
                blackPieceExistScore -= existScore;
                blackPieceNum--;
                invScr += existScore;
                invScr += pScore.getPlaceScore(Part.BLACK, to.x, to.y);
                /* �ò���ԭ��ͬ�� */
                invScr += (existScore * redPieceExistScore / (redPieceExistScore + blackPieceExistScore)) >> 3;
                if (eatenPiece.role == Role.HORSE) {
                    invScr += (16 - pieceCount) << 1;
                }
            }
            // ���·���
        }
        pieceScore += invScr;
        // ����, ȥ������ļӳɷ���, ������ļ�����
//        DebugInfo.checkScoreDynamicCalc(pieces, pieceScore);
        DebugInfo.incrementAlphaBetaTime();
        return invScr;
    }

    /**
     * ģ�����
     */
    public void backStep(Place from, Place to, Piece eatenPiece, int tmpScore) {
        final Piece movePiece = pieces[to.x][to.y];
        pieces[from.x][from.y] = movePiece;
        pieces[to.x][to.y] = eatenPiece;
        // �˻���һ��
        if (movePiece.role == Role.BOSS) {
            updateBossPlace(movePiece.part, from);
        }
        if (eatenPiece != null) {
            if (eatenPiece.part == Part.RED) {
                redPieceExistScore += eatenPiece.pieceScore.existScore;
                redPieceNum++;
            } else {
                blackPieceExistScore += eatenPiece.pieceScore.existScore;
                blackPieceNum++;
            }
        }
        // ���·�ֵ
        pieceScore -= tmpScore;
    }

    /**
     * ���ضԱ�����ʵ������, ����Ϊ��
     *
     * @param curPart ��ǰ���巽
     * @return ��ǰ���巽��ʵ������
     */
    public int getCurPartEvaluateScore(Part curPart) {
        if (Part.RED == curPart) {
            return pieceScore;
        } else {
            return -pieceScore;
        }
    }

    /**
     * ����������ĳһ������������
     */
    public int getPieceCount(Part curPart) {
        return curPart == Part.RED ? redPieceNum : blackPieceNum;
    }

    /**
     * @return ���̻��ж�������
     */
    public int getPieceNum() {
        return redPieceNum + blackPieceNum;
    }

    /**
     * @param place ����λ��
     * @return ��Ӧ����λ�õ����Ӷ���
     */
    public Piece getPiece(@NonNull Place place) {
        return pieces[place.x][place.y];
    }

    /**
     * ��ȡ�Է�Boss��λ��
     */
    public Place getOppoBossPlace(Part curPart) {
        return curPart == Part.RED ? blackBoss : redBoss;
    }

    /**
     * @param part     ���� Boss ���ӵ�λ��
     * @param newPlace ��λ��
     */
    public void updateBossPlace(Part part, Place newPlace) {
        if (part == Part.RED) {
            redBoss = newPlace;
        } else {
            blackBoss = newPlace;
        }
    }

    /**
     * @return boss�����ƶ��� �Ƿ�Ϊ�����
     */
    public boolean bossF2fAfterBossMove(Part curPart, Place curNextPlace) {
        Place oppPlace = curPart == Part.RED ? blackBoss : redBoss;
        // ������ boss �������겻һ��, ��ֱ�ӷ��� false
        if (curNextPlace.x != oppPlace.x) {
            return false;
        }
        return ArrayUtils.nullInMiddle(pieces[curNextPlace.x], curNextPlace.y, oppPlace.y);
    }

    /**
     * @return true : ���� boss �����, ���м�ֻ�� place һ������
     */
    public boolean isBossF2FAndWithOnlyThePlaceInMiddle(Place place) {
        // ��� ���� boss ��������� ���� ��ǰλ�ò�������boss�м�, ��ֱ�ӷ���false
        if (redBoss.x != blackBoss.x || place.x != redBoss.x || place.y > redBoss.y || place.y < blackBoss.y) {
            return false;
        }
        return ArrayUtils.oneInMiddle(pieces[redBoss.x], redBoss.y, blackBoss.y);
    }

    /**
     * @return true : ��ǰλ���� ����boss �м�(����boss��λ��)
     */
    public boolean isBossF2FAndWithThePlaceInMiddle(Place place) {
        return redBoss.x == blackBoss.x && place.x == redBoss.x && place.y <= redBoss.y && place.y >= blackBoss.y;
    }

    /**
     * ���һ�������᲻����� Boss ����� (��ǰ part ��, �� from �ߵ� to)
     *
     * @param piece ��ǰ��������
     * @param from from
     * @param to to
     * @return true: ���Ϲ���, false: �����Ϲ���
     */
    public boolean isBossF2FAfterStep(Piece piece, Place from, Place to) {
        if (Role.BOSS == piece.role) {
            return !bossF2fAfterBossMove(piece.part, to);
        } else {
            return !isBossF2FAndWithOnlyThePlaceInMiddle(from) || isBossF2FAndWithThePlaceInMiddle(to);
        }
    }

    /**
     * AI ���� curPart ��������һ�����Ƿ��ܹ��Ե��Է��� BOSS
     */
    public boolean canEatBossAfterOneAiStep(Part part) {
        final Set<StepBean> nextStepAgainEvalPlace = AlphaBeta.getEvaluatedPlace(pieces, part, 1, null);
        // �����Ĳ�����, �Ƿ�����ܳԵ� BOSS ��һ��
        for (StepBean stepBean : nextStepAgainEvalPlace) {
            final Piece piece = getPiece(stepBean.to);
            if (piece != null && piece.role == Role.BOSS) {
                return true;
            }
        }
        return false;
    }

    public boolean simulateOneStep(StepBean stepBean, Predicate<AnalysisBean> predicate) {
        final Piece eatenPiece = getPiece(stepBean.to);
        if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
            throw new IllegalStateException(eatenPiece.name() + " ���Ե���, �޷�����ִ����ȥ");
        }
        // ģ������
        final int invScr = goForward(stepBean.from, stepBean.to, eatenPiece);
        // ����
        final boolean test = predicate.test(this);
        // �˻���һ��
        backStep(stepBean.from, stepBean.to, eatenPiece, invScr);
        return test;
    }

    /**
     * AI�����, part ��һ����֮�� �Ƿ��ܹ�����Է���һ���Ե��Լ��� BOSS
     *
     * @param part ��ǰ��
     */
    public boolean canAvoidBeEatBossAfterOneAIStep(Part part) {
        final Set<StepBean> nextStepAgainEvalPlace = AlphaBeta.getEvaluatedPlace(pieces, part, 2, null);
        // �����Ĳ�����, �Ƿ�����ܳԵ� BOSS ��һ��
        for (StepBean stepBean : nextStepAgainEvalPlace) {
            final Piece eatenPiece = getPiece(stepBean.to);
            // ����� BOSS��ɫ���Ե�, ����Ҫ����ѭ���������һ��ѭ��
            if (eatenPiece != null && eatenPiece.role == Role.BOSS) {
                // ����Ե����ǶԷ��� BOSS, ��ʾ���Ա��Ȿ�� BOSS ���Ե�
                if (eatenPiece.part == part) {
                    continue;
                } else {
                    return true;
                }
            }
            // ��� stepBean ����֮��, �Է��޷��Ե��Լ��� BOSS, �򷵻�true
            if (!simulateOneStep(stepBean, bean -> bean.canEatBossAfterOneAiStep(Part.getOpposite(part)))) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return boss �Ƿ�Ϊ�����
     */
    public MyList<Place> filterPlace(MyList<Place> places) {
        return places.filter(item -> {
            Place it = (Place) item;
            return it.x == redBoss.x && it.y <= redBoss.y && it.y >= blackBoss.y;
        });
    }
}
