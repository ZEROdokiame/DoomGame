package org.nico.ratel.landlords.client.event;

import org.nico.noson.Noson;
import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.enums.ServerEventCode;
import org.nico.ratel.landlords.features.Features;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;

import io.netty.channel.Channel;
import org.nico.ratel.landlords.utils.JsonUtils;

import java.util.HashMap;
import java.util.Map;

public class ClientEventListener_CODE_CLIENT_CONNECT extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		SimplePrinter.printNotice("正在连接. 欢迎来到地狱!\n"+
				"前情提要:传说只有被选中的人类才能对抗恶魔。\n"+
				"恶魔赖盖特正在寻找对抗地狱之王的力量，他将加冕一位地狱幸存者，加冕者被称为赖盖特之赐\n"+
				"赖盖特之赐将获得更多手牌来对抗其余的两名幸存者，战到最后的人才能获得胜利");
		SimpleClient.id = Integer.parseInt(data);

		Map<String, Object> infos = new HashMap<>();
		infos.put("version", SimpleClient.VERSION);
		pushToServer(channel, ServerEventCode.CODE_CLIENT_INFO_SET, Noson.reversal(infos));
	}

}
