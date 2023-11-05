package cn.cpf.app.chess.conf;

import cn.cpf.app.chess.modal.Piece;
import cn.cpf.app.chess.modal.Place;
import cn.cpf.app.chess.swing.ChessPiece;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Description : </b> �Գߴ�������һ���ӿڶ���, ��ز���ֱ�Ӹ��ݲ������߼���õ�
 *
 * @author CPF
 * Date: 2020/3/18 13:28
 */
public interface ChessDefined {

    /**
     * ����X�᷶Χ
     */
    int RANGE_X = 9;
    /**
     * ����Y�᷶Χ
     */
    int RANGE_Y = 10;
    /**
     * ���̳ߴ�
     */
    Dimension boardRect = new Dimension(536, 638);
    /**
     * ���ӿ�Ⱥ͸߶�
     */
    int PIECE_WIDTH = 56;
    /**
     * ���ӿ�Ⱥ͸߶�
     */
    int PIECE_HEIGHT = 56;
    /**
     * ���� x �����ʼλ��
     */
    int X_INIT = 30 - PIECE_WIDTH / 2;
    /**
     * ���� y �����ʼλ��
     */
    int Y_INIT = 28 - PIECE_WIDTH / 2;
    /**
     * ������(�������Ϊ57.5)
     */
    int GRID_INTERVAL = 57;

    /**
     * ��������Placeλ�õõ�������Ӧ����
     */
    static Point convertPlaceToLocation(int x, int y) {
        return new Point(X_INIT + x * GRID_INTERVAL, Y_INIT + y * GRID_INTERVAL);
    }

    /**
     * ����ָ��λ�õĵ��ҵ�����Placeλ��
     */
    static Place convertLocationToPlace(Point point) {
        final int x = (point.x - X_INIT) / GRID_INTERVAL;
        final int y = (point.y - Y_INIT) / GRID_INTERVAL;
        if (x >= ChessDefined.RANGE_X || y >= ChessDefined.RANGE_Y) {
            return null;
        }
        return Place.of(x, y);
    }

    /**
     * ��ȡĬ�ϵ�һ��������������
     */
    static java.util.List<ChessPiece> geneDefaultPieceSituation() {
        List<ChessPiece> list = new ArrayList<>(32);
        // ��Ӻ�ɫ����
        list.add(new ChessPiece("��܇1", Piece.RED_CAR, Place.of(0, 9)));
        list.add(new ChessPiece("���R1", Piece.RED_HORSE, Place.of(1, 9)));
        list.add(new ChessPiece("����1", Piece.RED_ELEPHANT, Place.of(2, 9)));
        list.add(new ChessPiece("����1", Piece.RED_COUNSELOR, Place.of(3, 9)));
        list.add(new ChessPiece("��˧", Piece.RED_BOSS, Place.of(4, 9)));
        list.add(new ChessPiece("����2", Piece.RED_COUNSELOR, Place.of(5, 9)));
        list.add(new ChessPiece("����2", Piece.RED_ELEPHANT, Place.of(6, 9)));
        list.add(new ChessPiece("���R2", Piece.RED_HORSE, Place.of(7, 9)));
        list.add(new ChessPiece("��܇2", Piece.RED_CAR, Place.of(8, 9)));

        list.add(new ChessPiece("��h1", Piece.RED_CANNON, Place.of(1, 7)));
        list.add(new ChessPiece("��h2", Piece.RED_CANNON, Place.of(7, 7)));
        list.add(new ChessPiece("���1", Piece.RED_SOLDIER, Place.of(0, 6)));
        list.add(new ChessPiece("���2", Piece.RED_SOLDIER, Place.of(2, 6)));
        list.add(new ChessPiece("���3", Piece.RED_SOLDIER, Place.of(4, 6)));
        list.add(new ChessPiece("���4", Piece.RED_SOLDIER, Place.of(6, 6)));
        list.add(new ChessPiece("���5", Piece.RED_SOLDIER, Place.of(8, 6)));

        // ��Ӻ�ɫ����
        list.add(new ChessPiece("�ڳ�1", Piece.BLACK_CAR, Place.of(0, 0)));
        list.add(new ChessPiece("����1", Piece.BLACK_HORSE, Place.of(1, 0)));
        list.add(new ChessPiece("����1", Piece.BLACK_ELEPHANT, Place.of(2, 0)));
        list.add(new ChessPiece("��ʿ1", Piece.BLACK_COUNSELOR, Place.of(3, 0)));
        list.add(new ChessPiece("�ڽ�", Piece.BLACK_BOSS, Place.of(4, 0)));
        list.add(new ChessPiece("��ʿ2", Piece.BLACK_COUNSELOR, Place.of(5, 0)));
        list.add(new ChessPiece("����2", Piece.BLACK_ELEPHANT, Place.of(6, 0)));
        list.add(new ChessPiece("����2", Piece.BLACK_HORSE, Place.of(7, 0)));
        list.add(new ChessPiece("�ڳ�2", Piece.BLACK_CAR, Place.of(8, 0)));

        list.add(new ChessPiece("����1", Piece.BLACK_CANNON, Place.of(1, 2)));
        list.add(new ChessPiece("����2", Piece.BLACK_CANNON, Place.of(7, 2)));
        list.add(new ChessPiece("����1", Piece.BLACK_SOLDIER, Place.of(0, 3)));
        list.add(new ChessPiece("����2", Piece.BLACK_SOLDIER, Place.of(2, 3)));
        list.add(new ChessPiece("����3", Piece.BLACK_SOLDIER, Place.of(4, 3)));
        list.add(new ChessPiece("����4", Piece.BLACK_SOLDIER, Place.of(6, 3)));
        list.add(new ChessPiece("����5", Piece.BLACK_SOLDIER, Place.of(8, 3)));
        return list;
    }

}
