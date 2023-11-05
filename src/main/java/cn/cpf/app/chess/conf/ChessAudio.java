package cn.cpf.app.chess.conf;

import cn.cpf.app.chess.util.Utils;
import lombok.extern.slf4j.Slf4j;
import sun.audio.AudioData;
import sun.audio.AudioDataStream;
import sun.audio.AudioPlayer;

import java.io.IOException;

/**
 * <b>Description : </b> ������Ϸ������
 * <br> ʹ�õ��� sun.audio.* �е���, ��һЩJVMƽ̨���ܲ���֧��
 *
 * <p>
 * <b>created in </b> 2021/12/21
 * </p>
 *
 * @author CPF
 * @since 1.0
 **/
@Slf4j
public enum ChessAudio {

    /**
     * from ���ʱ
     */
    CLICK_FROM("300"),
    /**
     * ����, ���ߵ��� BOSS�����, ���߱�����ʱ, �ߴ���
     */
    MAN_MOV_ERROR("301"),
    /**
     * MAN ��ɫ������������
     */
    MAN_MOVE("302"),
    /**
     * COM ����
     */
    COM_MOVE("303"),
    /**
     * MAN ���Ե� �Է�
     */
    MAN_EAT("304"),
    /**
     * COM ���Ե� �Է�
     */
    COM_EAT("305"),
    /**
     * MAN �� ����/���� �Է�
     */
    MAN_CHECK("306"),
    /**
     * COM �� ����/���� �Է�
     */
    COM_CHECK("307"),
    WIN_BGM("308"),
    LOSE_BGM("309"),
    /**
     * �� COM ��ɫ����
     */
    BE_CHECKMATED_BY_COM("310"),
    OPEN_BOARD("311")
    ;

    /**
     * ���
     */
    private final String tag;

    /**
     * AudioData ����
     */
    @SuppressWarnings("java:S3077")
    private volatile AudioData audioData;

    ChessAudio(String tag) {
        this.tag = tag;
    }

    @SuppressWarnings("java:S3077")
    private static volatile AudioDataStream audioDataStream = null;

    /**
     * ��ȡ AudioData ����, û��������
     *
     * @return AudioData ����
     */
    public AudioData getAudioData() {
        if (audioData == null) {
            synchronized (this) {
                if (audioData == null) {
                    final String name = "wave/" + tag + ".wav";
                    try {
                        final byte[] bytes = Utils.readFromResource(name);
                        log.info("��ȡ��Ƶ�ļ� " + bytes.length);
                        audioData = new AudioData(bytes);
                    } catch (IOException e) {
                        log.error("IO ��ȡ�쳣", e);
                        audioData = new AudioData(new byte[0]);
                    }
                }
            }
        }
        return audioData;
    }

    /**
     * ������Ƶ
     */
    public synchronized void play() {
        if (audioDataStream != null) {
            AudioPlayer.player.stop(audioDataStream);
        }
        audioDataStream = new AudioDataStream(getAudioData());
        AudioPlayer.player.start(audioDataStream);
    }

}
