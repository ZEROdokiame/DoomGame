package org.nico.ratel.landlords.client.event;

import org.nico.ratel.landlords.client.entity.User;
import org.nico.ratel.landlords.enums.ClientEventCode;
import org.nico.ratel.landlords.helper.PokerHelper;
import org.nico.ratel.landlords.print.SimplePrinter;
import org.nico.ratel.landlords.print.SimpleWriter;
import org.nico.ratel.landlords.utils.OptionsUtils;

import io.netty.channel.Channel;

public class ClientEventListener_CODE_SHOW_OPTIONS_SETTING extends ClientEventListener {

	@Override
	public void call(Channel channel, String data) {
		SimplePrinter.printNotice("设置: ");
		SimplePrinter.printNotice("1. 方角卡牌（默认）");
		SimplePrinter.printNotice("2. 圆角卡牌");
		SimplePrinter.printNotice("3. 纯文字（带花色）");
		SimplePrinter.printNotice("4. 纯文字（无花色）");
		SimplePrinter.printNotice("5. Unicode 卡牌");

		SimplePrinter.printNotice("请选择显示风格！ (输入 [BACK] 返回主菜单)");
		String line = SimpleWriter.write(User.INSTANCE.getNickname(), "setting");

		if (line.equalsIgnoreCase("BACK")) {
			get(ClientEventCode.CODE_SHOW_OPTIONS).call(channel, data);
		} else {
			int choose = OptionsUtils.getOptions(line);

			if (choose >= 1 && choose <= 5) {
				PokerHelper.pokerPrinterType = choose - 1;
				get(ClientEventCode.CODE_SHOW_OPTIONS).call(channel, data);
			} else {
				SimplePrinter.printNotice("无效选项，请重新选择：");
				call(channel, data);
			}
		}
	}


}
