package org.nico.ratel.landlords.client.event;

import java.util.Map;

import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.client.entity.User;
import org.nico.ratel.landlords.enums.ServerEventCode;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.print.SimpleWriter;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_GAME_LANDLORD_ELECT extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		Map<String, Object> map = MapHelper.parser(data);
		int turnClientId = (int) map.get("nextClientId");

		if (map.containsKey("preClientNickname")) {
			SimplePrinter.printNotice(map.get("preClientNickname") + " 拒绝了赖盖特的加冕！");
		}

		if(turnClientId == SimpleClient.id) {
			SimplePrinter.printNotice("赖盖特选中了你！按下y来加冕为王，或拒绝赖盖特的加冕！ [Y/N] ");
			String line = SimpleWriter.write(User.INSTANCE.getNickname(), "Y/N");
			if (line.equalsIgnoreCase("exit") || line.equalsIgnoreCase("e")) {
				pushToServer(channel, ServerEventCode.CODE_CLIENT_EXIT);
			} else if (line.equalsIgnoreCase("Y")) {
				pushToServer(channel, ServerEventCode.CODE_GAME_LANDLORD_ELECT, "TRUE");
			} else if (line.equalsIgnoreCase("N")) {
				pushToServer(channel, ServerEventCode.CODE_GAME_LANDLORD_ELECT, "FALSE");
			} else {
				SimplePrinter.printNotice("Invalid options");
				call(channel, data);
			}
		} else {
			SimplePrinter.printNotice("这是 " + map.get("nextClientNickname") + "的时刻，请耐心等他做出决断 !");
		}

	}

}
