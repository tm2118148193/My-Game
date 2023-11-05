package cn.cpf.app.chess.main;

import cn.cpf.app.chess.modal.Place;
import cn.cpf.app.chess.modal.PlayerType;
import cn.cpf.app.chess.swing.BoardPanel;
import com.github.cosycode.common.ext.hub.Throws;
import com.github.cosycode.common.lang.ShouldNotHappenException;
import com.github.cosycode.common.thread.CtrlLoopThreadComp;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

/**
 * <b>Description : </b> ����ִ����, ���ڴ��������е�����
 * <p>
 * <b>�ص�: </b>ÿһ��ֻ�ܹ�ִ��һ������,
 * </p>
 *
 * <p>
 * <b>created in </b> 2021/8/28
 * </p>
 *
 * @author CPF
 * @since 1.0
 **/
@Slf4j
public class CommandExecutor {

    /**
     * �첽�����߳�, ��������������
     */
    private final CtrlLoopThreadComp ctrlLoopThreadComp;
    private final BoardPanel boardPanel;
    /**
     * �Ƿ�������б��
     */
    private volatile boolean sustain;

    public CommandExecutor(BoardPanel boardPanel) {
        this.boardPanel = boardPanel;
        this.ctrlLoopThreadComp = CtrlLoopThreadComp.ofRunnable(this::loop)
                .setName("CommandExecutor")
                .catchFun(CtrlLoopThreadComp.CATCH_FUNCTION_CONTINUE);
    }

    /**
     * ��һ��������
     */
    private CommandType nextCommand;
    /**
     * ��һ��������Ĳ���
     */
    private Object nextParamObj;

    private volatile boolean isRun;

    /**
     * @param commandType ��������
     */
    public void sendCommand(@NonNull CommandType commandType) {
        sendCommand(commandType, null);
    }

    /**
     * @param commandType ��������
     * @param paramObj    �������
     */
    public synchronized void sendCommand(@NonNull CommandType commandType, Object paramObj) {
        this.nextCommand = commandType;
        this.nextParamObj = paramObj;
        sustain = false;
        this.ctrlLoopThreadComp.startOrWake();
    }

    /**
     * ֻ���� �߳�û�����е������, ������ӳɹ�
     *
     * @param commandType ��������
     * @param paramObj    �������
     * @return �Ƿ���ӳɹ�
     */
    public synchronized boolean sendCommandWhenNotRun(@NonNull CommandType commandType, Object paramObj) {
        if (isRun) {
            return false;
        }
        sendCommand(commandType, paramObj);
        return true;
    }

    private void loop() {
        final CommandType command;
        final Object paramObj;
        synchronized (this) {
            command = this.nextCommand;
            paramObj = this.nextParamObj;
            this.nextCommand = null;
            this.nextParamObj = null;
        }
        if (command != null) {
            isRun = true;
            try {
                log.debug("�����¼�[{}] start", command.getLabel());
                consumerCommand(command, paramObj);
                log.debug("�����¼�[{}] end ", command.getLabel());
            } catch (Exception e) {
                log.error("ִ������[{}]�����쳣", command.getLabel(), e);
                new Thread(() -> JOptionPane.showMessageDialog(boardPanel, e.getMessage(), e.toString(), JOptionPane.ERROR_MESSAGE)).start();
            }
        } else {
            this.ctrlLoopThreadComp.pause();
            isRun = false;
        }
    }

    /**
     * ����
     */
    private void consumerCommand(final CommandType commandType, Object paramObj) {
        switch (commandType) {
            case SuspendCallBackOrAiRun:
                break;
            case CallBackOneTime:
                Application.context().rollbackOneStep();
                break;
            case AiRunOneTime:
                if (Application.context().aiRunOneTime() != null) {
                    log.debug("�Ѿ�����ʤ��!");
                }
                break;
            case SustainCallBack:
                sustain = true;
                while (sustain) {
                    if (!Application.context().rollbackOneStep()) {
                        sustain = false;
                        break;
                    }
                    Throws.con(Application.config().getComIntervalTime(), Thread::sleep).logThrowable();
                }
                break;
            case SustainAiRun:
                sustain = true;
                while (sustain) {
                    if (Application.context().aiRunOneTime() != null) {
                        log.debug("�Ѿ�����ʤ��, AIִ����ͣ!");
                        sustain = false;
                        break;
                    }
                    Throws.con(Application.config().getComIntervalTime(), Thread::sleep).logThrowable();
                }
                break;
            case SustainAiRunIfNextIsAi:
                sustain = true;
                while (sustain) {
                    // �����һ�����ֲ��� AI, ����ͣ
                    if (!PlayerType.COM.equals(Application.config().getPlayerType(Application.context().getSituation().getNextPart()))) {
                        sustain = false;
                        log.debug("��һ�����ֲ��� AI, ��ͣ!");
                    } else if (Application.context().aiRunOneTime() != null) {
                        log.debug("�Ѿ�����ʤ��, AIִ����ͣ!");
                        sustain = false;
                    } else {
                        Throws.con(Application.config().getComIntervalTime(), Thread::sleep).logThrowable();
                    }
                }
                break;
            case LocationPiece:
                final Object[] params = (Object[]) paramObj;
                Place from = (Place) params[0];
                Place to = (Place) params[1];
                PlayerType type = (PlayerType) params[2];
                Application.context().locatePiece(from, to, type);
                sendCommand(CommandExecutor.CommandType.SustainAiRunIfNextIsAi);
                break;
            default:
                throw new ShouldNotHappenException("δ���������: " + commandType);
        }
    }

    /**
     * ����֧��ö��(��������Ӧ��ʹ��ͬһ���߳�����, һ���¼�����֮��, ��һ���¼����ܿ�ʼ����.)
     */
    @SuppressWarnings("java:S115")
    public enum CommandType {
        SuspendCallBackOrAiRun("ֹͣ����|AI����"),
        CallBackOneTime("����һ��"),
        SustainCallBack("��������"),
        AiRunOneTime("AI����һ��"),
        SustainAiRun("AI��������"),
        SustainAiRunIfNextIsAi("COM��ɫ����"),
        LocationPiece("ui��������");

        @Getter
        private final String label;

        CommandType(String label) {
            this.label = label;
        }
    }

}