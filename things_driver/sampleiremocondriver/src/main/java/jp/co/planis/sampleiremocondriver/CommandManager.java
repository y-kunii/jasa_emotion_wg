package jp.co.planis.sampleiremocondriver;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import jp.co.planis.iotgatewaylib.CommandResponseCreator;
import jp.co.planis.iotgatewaylib.commandresponse.CommandResponse;

/**
 * iRemoconで実行可能なコマンドを管理するクラス
 * Created by y_akimoto on 2016/12/15.
 */
public class CommandManager {
    private static final String COMMAND_CODE_PREFIX = "iremocon_";
    private static CommandManager ourInstance = new CommandManager();

    public static CommandManager getInstance() {
        return ourInstance;
    }

    private CommandManager() {
    }

    private Context context;
    private boolean initialized = false;

    public void initialize(Context context) {
        if (!initialized) {
            this.context = context;
            this.initialized = true;
        }
    }

    /**
     * 利用可能なコマンドをセットする
     * @param commandResponseCreator
     */
    public void setAvailableCommandList(CommandResponseCreator commandResponseCreator) {
        if (!initialized) {
            throw new RuntimeException("CommandManager is not initialized.");
        }

        List<CommandResponse.ThingData.AvailableCommand> availableCommandList = new ArrayList<>();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        // コマンドを生成
        CommandResponse.ThingData.AvailableCommand availableCommand;
        int maxRemoconNum = context.getResources().getInteger(R.integer.max_remocon_num);
        for (int i = 1; i <= maxRemoconNum; i++) {
            availableCommand = commandResponseCreator.createAvailableCommandInstance();
            availableCommand.command_type = CommandResponse.CommandType.selection.name();
            availableCommand.command_code = COMMAND_CODE_PREFIX + i;

            String learningTitle = sharedPreferences.getString(LearningActivity.PREFERENSE_KEY_LEARNING_TITLE_PREFIX + i, "");
            if (learningTitle != "") {
                availableCommand.command_name = learningTitle;
                availableCommand.command_description = learningTitle;
            } else {
                availableCommand.command_name = "リモコン番号" + i;
                availableCommand.command_description = "リモコン番号" + i + "を送信します";
            }

            commandResponseCreator.addAvailableCommand(availableCommand);
        }
    }

    /**
     * コマンドを実行する
     * @param command
     */
    public void executeCommand(CommandResponse.Command command) {
        if (!initialized) {
            throw new RuntimeException("CommandManager is not initialized.");
        }

        // command_codeしか見ない
        String learningNoStr = command.command_code.replace(COMMAND_CODE_PREFIX, "");
        try {
            Integer learningNo = Integer.parseInt(learningNoStr);
            TCPIP tcpip = new TCPIP(context);
            tcpip.send("*is;" + learningNo + "\r\n", new TCPIP.SocketSendResultListener() {
                @Override
                public void onSocketSendResult(final String result) {
                    Log.d(Constants.LOG_TAG, "send result:" + result);
                }
            });
        } catch (NumberFormatException e) {
            Log.e(Constants.LOG_TAG, "CommandCodeが不正です. command_code:" + command.command_code);
        }
    }
}
