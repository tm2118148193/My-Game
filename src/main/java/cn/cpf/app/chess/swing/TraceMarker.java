package cn.cpf.app.chess.swing;

import cn.cpf.app.chess.conf.ChessImage;
import cn.cpf.app.chess.modal.Place;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>Description : </b> ���ڱ�������������·��
 * <p>
 * <b>created in </b> 2018/8/27
 *
 * @author CPF
 * @since 1.0
 **/
class TraceMarker {

    private final Container container;

    private final JPiece lastMarkFrom;
    private final JPiece lastMarkTo;
    /**
     * ��ǰ��������ӱ��
     */
    private final JPiece markFrom;
    /**
     * ����������ƶ��ı��
     */
    private final JPiece pointerMark;
    /**
     * ��ſ������ӵ�λ�ñ��
     */
    private final java.util.List<JPiece> toMarkList;


    TraceMarker(Container container) {
        this.container = container;
        lastMarkFrom = new JPiece(ChessImage.POINTER.getImage());
        lastMarkTo = new JPiece(ChessImage.POINTER.getImage());
        markFrom = new JPiece(ChessImage.POINTER.getImage());
        pointerMark = new JPiece(ChessImage.POINTER.getImage());
        toMarkList = new ArrayList<>();
    }

    /**
     * ��ʼ�����б��
     */
    void initMarker() {
        container.add(pointerMark.getComp());
        container.add(markFrom.getComp());
        container.add(lastMarkFrom.getComp());
        container.add(lastMarkTo.getComp());
        lastMarkFrom.hide();
        lastMarkTo.hide();
        pointerMark.hide();
        markFrom.hide();
        showMarkPlace(null);
    }

    void setMarkFromPlace(Place place) {
        markFrom.setPlaceAndShow(place);
    }

    /**
     * ����һ������, ���ñ��
     */
    void endedStep(Place from, Place to) {
        lastMarkFrom.setPlaceAndShow(from);
        lastMarkTo.setPlaceAndShow(to);
        pointerMark.hide();
        markFrom.hide();
        showMarkPlace(null);
    }

    /**
     * Ϊ �����е�����λ�� ��ʾ �������ӵı��, �������Ϊ null or �ռ��� ��, ������� �������ӵı��
     */
    void showMarkPlace(List<Place> placeList) {
        // ������� �������ӵı��
        toMarkList.forEach(JPiece::hide);
        if (placeList == null || placeList.isEmpty()) {
            return;
        }
        // ��� toMarkList �������㹻�ı��, ��ֱ�Ӵ� toMarkList ����ȡ, �����½���Ƕ����, ��ӵ� toMarkList��
        int size = toMarkList.size();
        for (int i = 0; i < placeList.size(); i++) {
            if (size <= i) {
                JPiece piece = new JPiece(ChessImage.POINTER.getImage());
                toMarkList.add(i, piece);
                container.add(piece.getComp());
            }
            JPiece jPiece = toMarkList.get(i);
            jPiece.setPlaceAndShow(placeList.get(i));
        }
    }
}
