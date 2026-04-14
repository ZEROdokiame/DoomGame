package org.nico.ratel.landlords.client.event;

import io.netty.channel.Channel;
import org.nico.ratel.landlords.channel.ChannelUtils;
import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.client.entity.User;
import org.nico.ratel.landlords.enums.ServerEventCode;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.print.SimpleWriter;

import java.util.Map;

public class ClientEventListener_CODE_GAME_READY extends ClientEventListener {
    @Override
    public void call(Channel channel, String data) {
        Map<String, Object> map = MapHelper.parser(data);
        if (SimpleClient.id == (int) map.get("clientId")) {
            SimplePrinter.printNotice("你已准备就绪。");
            return;
        }
        SimplePrinter.printNotice(map.get("clientNickName").toString() + " 已准备就绪。");
    }

    static void gameReady(Channel channel) {
        SimplePrinter.printNotice("\n是否继续下一局？ [Y/N]");
        String line = SimpleWriter.write(User.INSTANCE.getNickname(), "notReady");
        if (line.equals("Y") || line.equals("y")) {
            ChannelUtils.pushToServer(channel, ServerEventCode.CODE_GAME_READY, "");
            return;
        }
        ChannelUtils.pushToServer(channel, ServerEventCode.CODE_CLIENT_EXIT, "");
    }
}
