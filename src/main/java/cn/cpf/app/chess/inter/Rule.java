package cn.cpf.app.chess.inter;

import cn.cpf.app.chess.modal.Part;
import cn.cpf.app.chess.modal.Piece;
import cn.cpf.app.chess.modal.Place;

import java.util.List;

/**
 * <b>Description : </b>
 *
 * @author CPF
 * Date: 2020/3/19 17:17
 */
public interface Rule {

    /**
     * ��� part ���� piece������ �� from �� to �������Ƿ���Ϲ���
     * <p>
     * ǰ�Ჽ��
     * 1. from != to
     * 2. from, to���궼������֮��
     * <p>
     * ���
     * 1. ��Χ�Ƿ���Ϲ���, ��, ʿ, ���Ƿ��ڹ涨��������
     * 2. �Ƿ����谭, �����ۻ������ȵ�
     * 3. �յ��Ƿ�Ϊ��λ�����ǶԷ�������
     * 4. ·���Ƿ���Ϲ���, ��, �� ��ֱ��, �����յ�
     *
     * @param pieces ����
     * @param part   ��ǰ���巽
     * @param from   ��ʼλ��
     * @param to     �����λ��
     * @return ��ǰ�����Ƿ���Ϲ���
     */
    boolean check(Piece[][] pieces, Part part, Place from, Place to);

    /**
     * @param pieces �����������
     * @param part   ��ǰ����������
     * @param place  ��ǰ��������������λ��
     * @return ��ǰ�������е���λ����
     */
    MyList<Place> find(Piece[][] pieces, Part part, Place place);

    default int checkPlace(Piece chessPiece, Part part) {
        if (chessPiece == null) {
            return 0;
        }
        if (chessPiece.part == part) {
            return -1;
        }
        return 1;
    }

    default void addPlaceIntoListWhenXyIsNull(Piece[][] chessPieces, Part part, List<Place> list, Place place, int x, int y) {
        if (chessPieces[x][y] != null) {
            return;
        }
        Piece piece = chessPieces[place.x][place.y];
        if (piece == null || piece.part != part) {
            list.add(place);
        }
    }

    /**
     * ��� part ���� chessPiece �����������һ���ߵ� place��λ��, �� place ���뵽 list �б���
     *
     * @param chessPieces ����
     * @param part        ��ǰ���巽
     * @param list        �б�
     * @param place       λ��
     */
    default void addPlaceIntoList(Piece[][] chessPieces, Part part, List<Place> list, Place place) {
        Piece piece = chessPieces[place.x][place.y];
        if (piece == null || piece.part != part) {
            list.add(place);
        }
    }

}
