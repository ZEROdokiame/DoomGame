package org.nico.ratel.landlords.utils;

import org.nico.ratel.landlords.entity.Poker;

import java.util.*;

public class LastCardsUtils {

    /**
     * 按游戏内部大小顺序排列的牌面名称列表。
     * 
     * 说明：
     *  - 小王、大王在系统内部分别命名为 {@code W}（王） 和 {@code S}（神）。
     */
    private static final List<String> defSort = new ArrayList<String>(){{
        add("3");
        add("4");
        add("5");
        add("6");
        add("7");
        add("8");
        add("9");
        add("10");
        add("J");
        add("Q");
        add("K");
        add("A");
        add("2");
        add("W"); // 小王（王）
        add("S"); // 大王（神）
    }};

    public static String getLastCards(List<List<Poker>> pokers){
        StringBuffer lastCards = new StringBuffer();
        Map<String, Integer> lastCardMap = initLastCards();
        for(int i = 0; i < pokers.size(); i++){
            List<Poker> pokerList = pokers.get(i);
            for(int a = 0; a < pokerList.size(); a++){
                Poker poker = pokerList.get(a);
                if(poker != null && poker.getLevel() != null){
                    String levelName = poker.getLevel().getName();
                    // 使用 getOrDefault 防止空指针异常，如果该牌面尚未统计则默认值为 0
                    lastCardMap.put(levelName, lastCardMap.getOrDefault(levelName, 0) + 1);
                }
            }
        }
        for(int i = 0; i < defSort.size(); i++){
            String key = defSort.get(i);
            // 显示时将 W/S 转换为 王/神
            String displayKey = key;
            if("W".equals(key)) displayKey = "王";
            else if("S".equals(key)) displayKey = "神";

            lastCards.append(displayKey + "["+lastCardMap.get(key)+"] ");
        }

        return lastCards.toString();
    }


    private static Map<String, Integer> initLastCards(){
        Map<String, Integer> lastCardMap = new HashMap<>();
        lastCardMap.put("A",0);
        lastCardMap.put("2",0);
        lastCardMap.put("3",0);
        lastCardMap.put("4",0);
        lastCardMap.put("5",0);
        lastCardMap.put("6",0);
        lastCardMap.put("7",0);
        lastCardMap.put("8",0);
        lastCardMap.put("9",0);
        lastCardMap.put("10",0);
        lastCardMap.put("J",0);
        lastCardMap.put("Q",0);
        lastCardMap.put("K",0);
        lastCardMap.put("W",0);
        lastCardMap.put("S",0);
        return lastCardMap;
    }
}
