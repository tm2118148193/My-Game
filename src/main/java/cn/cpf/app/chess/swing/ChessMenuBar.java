package cn.cpf.app.chess.swing;

import cn.cpf.app.chess.conf.ChessDefined;
import cn.cpf.app.chess.main.*;
import cn.cpf.app.chess.inter.LambdaMouseListener;
import cn.cpf.app.chess.modal.PlayerType;
import cn.cpf.app.chess.util.JsonUtils;
import com.github.cosycode.common.util.io.FileSystemUtils;
import com.github.cosycode.common.util.io.IoUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * <b>Description : </b> ���˵�
 * <p>
 * <b>created in </b> 2018/9/8
 * </p>
 *
 * @author CPF
 **/
@Slf4j
public class ChessMenuBar extends JMenuBar {

    private static final long serialVersionUID = 1L;

    public void sendCommand(CommandExecutor.CommandType commandType) {
        Application.context().getCommandExecutor().sendCommand(commandType);
    }

    public ChessMenuBar() {
        addSettingMenu();
        addDebugMenu();
        addMenuToMenuBar("��ֹ������AI����", e -> sendCommand(CommandExecutor.CommandType.SuspendCallBackOrAiRun));
    }

    private void addSettingMenu() {
        JMenu muSetting = new JMenu("����");
        add(muSetting);

        addItemToMenu(muSetting, "���¿���", e -> {
            List<ChessPiece> list = ChessDefined.geneDefaultPieceSituation();
            Application.context().init(new Situation(list, new SituationRecord(), Application.config().getFirstPart(), LocalDateTime.now()));
        });

        muSetting.add(new JSeparator());

        addItemToMenu(muSetting, "�������", e -> {
            try {
                Situation situation = Application.context().getSituation();
                final String str = JsonUtils.toJson(situation);
                String fileName = String.format("situation-%s-%s.chess.bin", situation.getSituationStartTime().toEpochSecond(ZoneOffset.of("+8")), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd-HHmmss")));
                final String userDir = System.getProperty("user.dir");
                final File saveDir = new File(userDir + File.separator + "save");
                FileSystemUtils.insureFileDirExist(saveDir);
                File file = new File(saveDir.getPath() + File.separator + fileName);
                assert !file.exists() : "�ļ��Ѵ���";
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(str);
                    writer.flush();
                }
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        });

        addItemToMenu(muSetting, "�������", e -> {
            final String userDir = System.getProperty("user.dir");
            File file = new File(userDir + File.separator + "save/");
            if (!file.exists() || !file.isDirectory()) {
                file = file.getParentFile();
            }
            JFileChooser jfc = new JFileChooser(file);
            jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            jfc.showOpenDialog(muSetting);
            File f = jfc.getSelectedFile();
            if (f != null) {
                try {
                    final String json = IoUtils.readFile(f).trim();
                    final Situation situation = JsonUtils.fromJson(json, Situation.class);
                    Application.context().init(situation);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

        muSetting.add(new JSeparator());

        /* ����ƶ����Ӷ���ѡ��� */
        JCheckBoxMenuItem cm = new JCheckBoxMenuItem("�ƶ����Ӷ���");
        addLambdaMouseListener(cm, e -> Application.config().setCartoon(cm.getState()));
        muSetting.add(cm);
        // ��ʼ��ֵ
        cm.setState(Application.config().isCartoon());

        muSetting.add(new JSeparator());

        /* ��� AI ����ģʽѡ��� */
        JCheckBoxMenuItem checkMod = new JCheckBoxMenuItem("������ʱҲ�����Ҷ�");
        addLambdaMouseListener(checkMod, e -> Application.config().setActiveWhenBeCheck(checkMod.getState()));
        muSetting.add(checkMod);
        // ��ʼ��ֵ
        checkMod.setState(Application.config().isActiveWhenBeCheck());

        muSetting.add(new JSeparator());

        /* ��� AI ����ģʽѡ��� */
        JCheckBoxMenuItem aiCalcMod = new JCheckBoxMenuItem("��˲��м���");
        addLambdaMouseListener(aiCalcMod, e -> Application.config().setParallel(aiCalcMod.getState()));
        muSetting.add(aiCalcMod);
        // ��ʼ��ֵ
        aiCalcMod.setState(Application.config().isParallel());

        muSetting.add(new JSeparator());
        /* �����Ϸģʽ��ѡ��ť�� */
        ButtonGroup playModalGroup = new ButtonGroup();
        JRadioButtonMenuItem rmManAi = new JRadioButtonMenuItem("�˻�����", true);
        JRadioButtonMenuItem rmManMan = new JRadioButtonMenuItem("���˶���");
        JRadioButtonMenuItem rmAiAi = new JRadioButtonMenuItem("��������");
        addLambdaMouseListener(rmManAi, e -> {
            AppConfig config = Application.config();
            config.setBlackPlayerType(PlayerType.COM);
            config.setRedPlayerType(PlayerType.PEOPLE);
        });
        addLambdaMouseListener(rmManMan, e -> {
            AppConfig config = Application.config();
            config.setBlackPlayerType(PlayerType.PEOPLE);
            config.setRedPlayerType(PlayerType.PEOPLE);
        });
        addLambdaMouseListener(rmAiAi, e -> {
            AppConfig config = Application.config();
            config.setBlackPlayerType(PlayerType.COM);
            config.setRedPlayerType(PlayerType.COM);
        });
        playModalGroup.add(rmManAi);
        playModalGroup.add(rmManMan);
        playModalGroup.add(rmAiAi);
        muSetting.add(rmManAi);
        muSetting.add(rmManMan);
        muSetting.add(rmAiAi);

        muSetting.add(new JSeparator());
        /* �����Ѷȵ�ѡ��ť�� */
        ButtonGroup searchDeepBtnGroup = new ButtonGroup();
        final int searchDeepLevel = Application.config().getSearchDeepLevel();
        JRadioButtonMenuItem rmDeep4 = new JRadioButtonMenuItem("�������-4", searchDeepLevel == 4);
        JRadioButtonMenuItem rmDeep6 = new JRadioButtonMenuItem("�������-6", searchDeepLevel == 6);
        JRadioButtonMenuItem rmDeep8 = new JRadioButtonMenuItem("�������-8", searchDeepLevel == 8);
        addLambdaMouseListener(rmDeep4, e -> Application.config().setSearchDeepLevel(4));
        addLambdaMouseListener(rmDeep6, e -> Application.config().setSearchDeepLevel(6));
        addLambdaMouseListener(rmDeep8, e -> Application.config().setSearchDeepLevel(8));
        searchDeepBtnGroup.add(rmDeep4);
        searchDeepBtnGroup.add(rmDeep6);
        searchDeepBtnGroup.add(rmDeep8);
        muSetting.add(rmDeep4);
        muSetting.add(rmDeep6);
        muSetting.add(rmDeep8);
        // ��ʼ��
    }

    private void addDebugMenu() {
        JMenu muDebug = new JMenu("����");
        add(muDebug);
        addItemToMenu(muDebug, "��ֹ������AI����", e -> sendCommand(CommandExecutor.CommandType.SuspendCallBackOrAiRun));
        addItemToMenu(muDebug, "����һ��", e -> sendCommand(CommandExecutor.CommandType.CallBackOneTime));
        addItemToMenu(muDebug, "��������", e -> sendCommand(CommandExecutor.CommandType.SustainCallBack));
        addItemToMenu(muDebug, "AI����һ��", e -> sendCommand(CommandExecutor.CommandType.AiRunOneTime));
        addItemToMenu(muDebug, "AI��������", e -> sendCommand(CommandExecutor.CommandType.SustainAiRun));
        addItemToMenu(muDebug, "COM��ɫ����", e -> sendCommand(CommandExecutor.CommandType.SustainAiRunIfNextIsAi));
    }

    /**
     * �ڲ˵� JMenu �����һ��ѡ����, ��ǩΪ label, ��������¼� listener
     *
     * @param jMenu    �˵�
     * @param label    ��ǩ
     * @param listener ����ִ���߼�
     */
    private void addItemToMenu(@NonNull JMenu jMenu, String label, @NonNull LambdaMouseListener listener) {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.addMouseListener(listener);
        jMenu.add(menuItem);
    }

    /**
     * �ڵ�ǰ�˵������� �����һ��ѡ����, ��ǩΪ label, ��������¼� listener
     *
     * @param label    ��ǩ
     * @param listener ����ִ���߼�
     */
    private void addMenuToMenuBar(String label, @NonNull LambdaMouseListener listener) {
        JMenuItem menuItem = new JMenuItem(label);
        menuItem.addMouseListener(listener);
        add(menuItem);
    }

    private void addLambdaMouseListener(Component component, @NonNull LambdaMouseListener listener) {
        component.addMouseListener(listener);
    }
}
