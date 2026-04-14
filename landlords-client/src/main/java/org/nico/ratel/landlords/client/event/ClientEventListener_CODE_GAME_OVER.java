package org.nico.ratel.landlords.client.event;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.nico.noson.Noson;
import org.nico.noson.entity.NoType;
import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.entity.Poker;
import org.nico.ratel.landlords.enums.ClientType;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_GAME_OVER extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		Map<String, Object> map = MapHelper.parser(data);
		String typeZh = map.get("winnerType").toString();
		try{
			typeZh = ClientType.valueOf(typeZh).zh();
		}catch(Exception ignore){}
		SimplePrinter.printNotice("\n" + map.get("winnerNickname") + "[" + typeZh + "]" + " 赢得了这场对决！");

		if (map.containsKey("scores")){
			List<Map<String, Object>> scores = Noson.convert(map.get("scores"), new NoType<List<Map<String, Object>>>() {});
			for (Map<String, Object> score : scores) {
				if (! Objects.equals(score.get("clientId"), SimpleClient.id)) {
					SimplePrinter.printNotice(score.get("nickName").toString() + " 的剩余手牌：");
					SimplePrinter.printPokers(Noson.convert(score.get("pokers"), new NoType<List<Poker>>() {}));
				}
			}
			SimplePrinter.printNotice("\n");
			// 打印分数
			for (Map<String, Object> score : scores) {
				String scoreInc = score.get("scoreInc").toString();
				String scoreTotal = score.get("score").toString();
				if (SimpleClient.id != (int) score.get("clientId")) {
					SimplePrinter.printNotice(score.get("nickName").toString() + " 本局得分 " + scoreInc + "，总分 " + scoreTotal);
				} else {
					SimplePrinter.printNotice("你的本局得分 " + scoreInc + "，总分 " + scoreTotal);
				}
			}
			ClientEventListener_CODE_GAME_READY.gameReady(channel);
		}
	}
}
