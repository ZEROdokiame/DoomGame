package org.nico.ratel.landlords.client.event;

import java.util.List;
import java.util.Map;

import org.nico.noson.Noson;
import org.nico.noson.entity.NoType;
import org.nico.ratel.landlords.entity.Poker;
import org.nico.ratel.landlords.enums.ClientType;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_SHOW_POKERS extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {

		Map<String, Object> map = MapHelper.parser(data);

		lastSellClientNickname = (String) map.get("clientNickname");
		lastSellClientType = (String) map.get("clientType");
		try{
            lastSellClientType = ClientType.valueOf(lastSellClientType).zh();
        }catch(Exception ignore){}
		SimplePrinter.printNotice("是致胜一手还是自我毁灭之道？");
		SimplePrinter.printNotice(lastSellClientNickname + "[" + lastSellClientType + "] 使用了:");
		lastPokers = Noson.convert(map.get("pokers"), new NoType<List<Poker>>() {});
		SimplePrinter.printPokers(lastPokers);

		if (map.containsKey("sellClientNickname")) {
			SimplePrinter.printNotice("下一个传奇人物是 " + map.get("sellClientNickname") + "！见证他的技艺吧！");
		}
	}

}
