package org.nico.ratel.landlords.client.event;

import java.util.Map;

import org.nico.ratel.landlords.client.SimpleClient;
import org.nico.ratel.landlords.enums.ClientEventCode;
import org.nico.ratel.landlords.enums.ClientType;
import org.nico.ratel.landlords.helper.MapHelper;
import org.nico.ratel.landlords.print.SimplePrinter;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_CLIENT_EXIT extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		Map<String, Object> map = MapHelper.parser(data);

		Integer exitClientId = (Integer) map.get("exitClientId");

		String winner = String.valueOf(map.get("exitClientNickname"));

		boolean victory = map.containsKey("victory") && Boolean.parseBoolean(String.valueOf(map.get("victory")));
		if(exitClientId != null && exitClientId == SimpleClient.id){
			victory = true;
		}

		// 获取获胜阵营名称
		String winnerTypeStr = String.valueOf(map.get("winnerType"));
		String factionName;
		try {
			factionName = ClientType.valueOf(winnerTypeStr).zh();
		} catch (Exception e) {
			factionName = winnerTypeStr;
		}

		SimplePrinter.printNotice("\n========== 对局结束 ==========");
		SimplePrinter.printNotice("获胜阵营：【" + factionName + "】");
		SimplePrinter.printNotice(winner + " 出完了所有手牌！");

		if (victory) {
			if (ClientType.LANDLORD.name().equals(winnerTypeStr)) {
				SimplePrinter.printNotice("理所应当！你用赖盖特的恶魔之力让那些凡人见证了何为恐惧！");
			} else {
				SimplePrinter.printNotice("险中求胜！团结的意志坚不可摧，你们击败了赖盖特的恶魔！");
			}
		} else {
			if (ClientType.LANDLORD.name().equals(winnerTypeStr)) {
				SimplePrinter.printNotice("赖盖特之赐太过强大，你们没能逆转人类的结局。");
			} else {
				SimplePrinter.printNotice("地狱幸存者联手将你击败，人类的光辉与意志不朽。");
			}
		}
		SimplePrinter.printNotice("==============================\n");

		get(ClientEventCode.CODE_SHOW_OPTIONS).call(channel, data);
	}
}
