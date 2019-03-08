package jp.co.planis.samplehuedriver;

import java.util.ArrayList;
import java.util.List;

import jp.co.planis.iotgatewaylib.CommandResponseCreator;
import jp.co.planis.iotgatewaylib.commandresponse.CommandResponse;

/**
 * HueDriverで使用できるコマンドを生成するクラス
 * Created by y_akimoto on 2016/10/05.
 */
public class CommandCreator {

    private enum CommandCode {
        power,
        change_color_random,
        change_color_white,
        change_color_red,
        change_color_blue,
        change_color_green
    };

    /**
     * 利用可能なコマンドのリストを生成する
     * @param commandResponseCreator
     * @return
     */
    public static List<CommandResponse.ThingData.AvailableCommand> createAvailableCommandList(CommandResponseCreator commandResponseCreator) {
        List<CommandResponse.ThingData.AvailableCommand> availableCommandList = new ArrayList<>();

        // コマンドを生成
        CommandResponse.ThingData.AvailableCommand availableCommand;
        // ライト ON/OFF
        availableCommand = commandResponseCreator.createAvailableCommandInstance();
        availableCommand.command_type = CommandResponse.CommandType.selection.name();
        availableCommand.command_name = "ライト切り替え";
        availableCommand.command_code = CommandCode.power.name();
        availableCommand.command_description = "ライトのオン/オフ";
        List<CommandResponse.ThingData.AvailableCommand.CommandSelection> commandSelectionList = new ArrayList<>();
        CommandResponse.ThingData.AvailableCommand.CommandSelection commandSelection;
        {
            commandSelection = availableCommand.new CommandSelection();
            commandSelection.option_name = "オン";
            commandSelection.option_value = "on";
            commandSelection.option_description = "ライトをつけます";
            commandSelectionList.add(commandSelection);

            commandSelection = availableCommand.new CommandSelection();
            commandSelection.option_name = "オフ";
            commandSelection.option_value = "off";
            commandSelection.option_description = "ライトを消します";
            commandSelectionList.add(commandSelection);
        }
        availableCommand.command_selection = commandSelectionList.toArray(new CommandResponse.ThingData.AvailableCommand.CommandSelection[0]);
        availableCommandList.add(availableCommand);

        // 色を変える→ランダム
        availableCommand = commandResponseCreator.createAvailableCommandInstance();
        availableCommand.command_name = "ランダム点灯";
        availableCommand.command_type = CommandResponse.CommandType.color.name();
        availableCommand.command_code = CommandCode.change_color_random.name();
        availableCommand.command_value = "";
        availableCommandList.add(availableCommand);

        // 色を変える→白
        availableCommand = commandResponseCreator.createAvailableCommandInstance();
        availableCommand.command_name = "白に変更！";
        availableCommand.command_type = CommandResponse.CommandType.color.name();
        availableCommand.command_code = CommandCode.change_color_white.name();
        availableCommand.command_value = "#ffffff";
        availableCommandList.add(availableCommand);

        // 色を変える→赤
        availableCommand = commandResponseCreator.createAvailableCommandInstance();
        availableCommand.command_name = "警告";
        availableCommand.command_type = CommandResponse.CommandType.color.name();
        availableCommand.command_code = CommandCode.change_color_red.name();
        availableCommand.command_value = "#ff0000";
        availableCommandList.add(availableCommand);

        // 色を変える→青
        availableCommand = commandResponseCreator.createAvailableCommandInstance();
        availableCommand.command_name = "青に変更";
        availableCommand.command_type = CommandResponse.CommandType.color.name();
        availableCommand.command_code = CommandCode.change_color_blue.name();
        availableCommand.command_value = "#0000ff";
        availableCommandList.add(availableCommand);

        // 色を変える→緑
        availableCommand = commandResponseCreator.createAvailableCommandInstance();
        availableCommand.command_name = "正常";
        availableCommand.command_type = CommandResponse.CommandType.color.name();
        availableCommand.command_code = CommandCode.change_color_green.name();
        availableCommand.command_value = "#00ff00";
        availableCommandList.add(availableCommand);

        return availableCommandList;
    }

    public interface HueCommand {
        void execute();
        void setValue(String value);
    }

    /**
     * コマンドをHueのコントロールの指令に変換する
     * @param command
     * @return
     */
    public static HueCommand convertHueCommand(CommandResponse.Command command) {
        switch (CommandCode.valueOf(command.command_code)) {
            case power:
                if (command.command_value.equals("on")){
                    return new HueCommand() {
                        @Override
                        public void execute() {
                            HueController.getInstance().turnOnLights();
                        }

                        @Override
                        public void setValue(String value) {}
                    };
                } else if (command.command_value.equals("off")) {
                    return new HueCommand() {
                        @Override
                        public void execute() {
                            HueController.getInstance().turnOffLights();
                        }

                        @Override
                        public void setValue(String value) {}
                    };
                }
                return null;

            case change_color_random:
                return new HueCommand() {
                    @Override
                    public void execute() {
                        HueController.getInstance().randomLights();
                    }

                    @Override
                    public void setValue(String value) {

                    }
                };

            case change_color_white:
            case change_color_red:
            case change_color_blue:
            case change_color_green:
                HueCommand hueCommand = new HueCommand() {
                    String value = null;
                    @Override
                    public void execute() {
                        HueController.getInstance().changeColor(this.value);
                    }

                    @Override
                    public void setValue(String value) {
                        this.value = value;
                    }
                };

                hueCommand.setValue(command.command_value);
                return hueCommand;

            default:
                return null;
        }
    }

}
