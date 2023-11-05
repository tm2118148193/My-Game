package cn.cpf.app.chess.main;

import cn.cpf.app.chess.modal.Part;
import cn.cpf.app.chess.modal.PlayerType;
import lombok.Data;
import lombok.NonNull;

/**
 * <b>Description : </b> Ӧ������
 * <p>
 * <b>created in </b> 2021/8/31
 *
 * @author CPF
 * @since 1.0
 **/
@Data
public class AppConfig {
    /**
     * ���ַ�
     */
    private Part firstPart;
    /**
     * ��ɫ����ִ��������
     */
    private PlayerType redPlayerType;
    /**
     * ��������ִ��������
     */
    private PlayerType blackPlayerType;
    /**
     * ��ͨ�������
     */
    private int searchDeepLevel;
    /**
     * ���㱻����ʱҲ�����Ҷ�
     */
    private boolean activeWhenBeCheck;
    /**
     * �Ƿ�ʹ�ò�����
     */
    private boolean parallel;
    /**
     * �ƶ������Ƿ���ʾ����
     */
    private boolean cartoon;
    /**
     * ����ɱ�����
     */
    private int searchKillStepDeepLevel;
    /**
     * COM ���м��ʱ��
     */
    private int comIntervalTime = 600;

    /**
     * �ж� part ���� playType
     */
    public PlayerType getPlayerType(@NonNull Part part) {
        if (part == Part.RED) {
            return redPlayerType;
        } else {
            return blackPlayerType;
        }
    }
}
