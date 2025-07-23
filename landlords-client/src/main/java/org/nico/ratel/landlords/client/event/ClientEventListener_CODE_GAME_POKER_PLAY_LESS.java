package org.nico.ratel.landlords.client.event;

import org.nico.ratel.landlords.enums.ServerEventCode;
import org.nico.ratel.landlords.enums.ClientType;
import org.nico.ratel.landlords.print.SimplePrinter;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_GAME_POKER_PLAY_LESS extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		SimplePrinter.printNotice("软弱无力！你选择的手牌无法战胜对手！");

		if(lastPokers != null) {
			SimplePrinter.printNotice("是致胜一手还是自我毁灭之道？");
			String typeZh = lastSellClientType;try{typeZh = ClientType.valueOf(typeZh).zh();}catch(Exception ignore){}
			SimplePrinter.printNotice(lastSellClientNickname + "[" + typeZh + "] 使用了:");
			SimplePrinter.printPokers(lastPokers);
		}

		pushToServer(channel, ServerEventCode.CODE_GAME_POKER_PLAY_REDIRECT);
	}

}
