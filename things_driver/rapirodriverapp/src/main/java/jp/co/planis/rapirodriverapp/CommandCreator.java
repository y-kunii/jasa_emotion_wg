package jp.co.planis.rapirodriverapp;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.co.planis.iotgatewaylib.CommandResponseCreator;
import jp.co.planis.iotgatewaylib.commandresponse.CommandResponse;
import jp.co.planis.iotgatewaylib.service.AbstractConnectGatewayService;

import static android.content.ContentValues.TAG;

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

        return availableCommandList;
    }

    public interface RapiroCommand {
        void execute();
        void setValue(String value);
    }

    public static RapiroCommand convertRapiroCommand(CommandResponse.Command command) {
        switch (CommandCode.valueOf(command.command_code)) {
            case power:
                if (command.command_value.equals("on")) {
                    return new RapiroCommand() {
                        @Override
                        public void execute() {
                            RapiroController.getInstance().turnOnRapiro();
                        }

                        @Override
                        public void setValue(String value) {
                        }
                    };
                }
                if (command.command_value.equals("off")) {
                    return new RapiroCommand() {
                        @Override
                        public void execute() {
                            RapiroController.getInstance().turnOffRapiro();
                        }

                        @Override
                        public void setValue(String value) {
                        }
                    };
                }
                return null;
        }
        return null;
    }

}
