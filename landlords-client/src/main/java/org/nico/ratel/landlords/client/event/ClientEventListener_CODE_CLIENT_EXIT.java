package org.nico.ratel.landlords.client.event;

import java.util.Map;

import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.enums.ClientEventCode;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_CLIENT_EXIT extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		Map<String, Object> map = MapHelper.parser(data);

		Integer exitClientId = (Integer) map.get("exitClientId");

		String role = String.valueOf(map.get("exitClientNickname"));

		boolean victory = map.containsKey("victory") && Boolean.parseBoolean(String.valueOf(map.get("victory")));
		if(exitClientId == SimpleClient.id){
			victory = true;
		}

		if(victory){
			SimplePrinter.printNotice("天选之人已然诞生！他就是:" + role + "!他癫狂屠戮了所有幸存者！\n"
			+"为他的不灭功绩庆贺吧！");
		}else{
			SimplePrinter.printNotice("很遗憾，你并不是那个天选之人，你已被 " + role + " 无情击败！\n");
		}
		
		get(ClientEventCode.CODE_SHOW_OPTIONS).call(channel, data);
	}
}
