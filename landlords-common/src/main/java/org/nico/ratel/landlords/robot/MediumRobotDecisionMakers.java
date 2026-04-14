package org.nico.ratel.landlords.robot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.nico.ratel.landlords.entity.ClientSide;
import org.nico.ratel.landlords.entity.Poker;
import org.nico.ratel.landlords.entity.PokerSell;
import org.nico.ratel.landlords.enums.PokerLevel;
import org.nico.ratel.landlords.enums.SellType;
import org.nico.ratel.landlords.helper.PokerHelper;


/**
 * Hard difficulty AI with enhanced trial game algorithm
 * Improvements:
 * 1. Increased deduction depth (300 -> 5000)
 * 2. Memoization cache to avoid duplicate calculations
 * 3. Heuristic evaluation for poker value
 * 4. Smart pruning (preserve bombs, avoid breaking good combinations)
 * 5. Improved landlord selection strategy
 *
 * @author nico (enhanced to hard difficulty)
 * @date 2020-12-19 16:36
 */
public class MediumRobotDecisionMakers extends AbstractRobotDecisionMakers {

	private static final Long DEDUCE_LIMIT = 100 * 20L; // Optimized: 2000 (balance between strength and speed)
	private static final int MAX_DEPTH = 20; // Maximum recursion depth
	private static final long MAX_THINK_TIME_MS = 3000; // Maximum 3 seconds thinking time
	private static final int MAX_CACHE_SIZE = 10000; // Prevent memory overflow

	// Memoization cache for game state evaluation
	private final Map<String, CacheEntry> stateCache;

	// Timeout control
	private long thinkStartTime;

	// Inner class for cache entries
	private static class CacheEntry {
		double winRate;
		int visitCount;

		CacheEntry(double winRate, int visitCount) {
			this.winRate = winRate;
			this.visitCount = visitCount;
		}
	}

	public MediumRobotDecisionMakers() {
		this.stateCache = new HashMap<>();
	}

	@Override
	public PokerSell howToPlayPokers(PokerSell lastPokerSell, ClientSide robot) {
		// Start timer
		thinkStartTime = System.currentTimeMillis();

		// Clear cache if too large
		if(stateCache.size() > MAX_CACHE_SIZE) {
			stateCache.clear();
		}

		if(lastPokerSell != null && lastPokerSell.getSellType() == SellType.KING_BOMB) {
			return null;
		}

		List<Poker> selfPoker = PokerHelper.clonePokers(robot.getPokers());
		List<Poker> leftPoker = PokerHelper.clonePokers(robot.getPre().getPokers());
		List<Poker> rightPoker = PokerHelper.clonePokers(robot.getNext().getPokers());
		PokerHelper.sortPoker(selfPoker);
		PokerHelper.sortPoker(leftPoker);
		PokerHelper.sortPoker(rightPoker);

		List<List<Poker>> pokersList = new ArrayList<List<Poker>>();
		pokersList.add(selfPoker);
		pokersList.add(rightPoker);
		pokersList.add(leftPoker);

		List<PokerSell> sells = PokerHelper.validSells(lastPokerSell, selfPoker);
		if(sells.isEmpty()) {
			return null;
		}

		// Smart pruning: filter out obviously bad moves
		sells = filterBadSells(sells, selfPoker, lastPokerSell);

		PokerSell bestSell = null;
		Double bestScore = null;

		for(PokerSell sell: sells) {
			// Check timeout - if thinking too long, return current best
			if(System.currentTimeMillis() - thinkStartTime > MAX_THINK_TIME_MS) {
				if(bestSell != null) {
					return bestSell;
				}
				// If no best found yet, just return first valid option
				return sell;
			}

			List<Poker> pokers = PokerHelper.clonePokers(selfPoker);
			pokers.removeAll(sell.getSellPokers());

			// Win immediately if possible
			if(pokers.isEmpty()) {
				return sell;
			}

			// Evaluate this move
			pokersList.set(0, pokers);
			AtomicLong counter = new AtomicLong();
			deduce(0, sell, 1, pokersList, counter, 0);

			// Calculate score with heuristic bonus
			double deduceScore = counter.get();
			double heuristicScore = evaluatePokerValue(pokers, sell);
			double totalScore = deduceScore + heuristicScore * 0.3; // 30% weight for heuristic

			if(bestScore == null || totalScore > bestScore) {
				bestSell = sell;
				bestScore = totalScore;
			}

			pokersList.set(0, selfPoker);
		}

		return bestSell;
	}

	private Boolean deduce(int sellCursor, PokerSell lastPokerSell, int cursor, List<List<Poker>> pokersList, AtomicLong counter, int depth) {
		// Timeout check - stop if thinking too long
		if(System.currentTimeMillis() - thinkStartTime > MAX_THINK_TIME_MS) {
			return null;
		}

		// Depth limit to prevent infinite recursion
		if(depth > MAX_DEPTH) {
			return null;
		}

		if(cursor > 2) {
			cursor = 0;
		}
		if(sellCursor == cursor) {
			lastPokerSell = null;
		}

		List<Poker> original = pokersList.get(cursor);

		// Check cache for this game state
		String cacheKey = serialPokersList(pokersList) + "_" + cursor;
		CacheEntry cached = stateCache.get(cacheKey);
		if(cached != null && cached.visitCount > 5) {
			// Use cached result if we've evaluated this state enough times
			counter.addAndGet((long)(cached.winRate * 10));
			return cached.winRate > 0.5;
		}

		List<PokerSell> sells = PokerHelper.validSells(lastPokerSell, original);
		if(sells.isEmpty()) {
			if(sellCursor != cursor) {
				return deduce(sellCursor, lastPokerSell, cursor + 1, pokersList, counter, depth);
			}
		}

		// Smart pruning for non-self players
		if(cursor != 0 && sells.size() > 10) {
			sells = sells.subList(0, Math.min(10, sells.size())); // Limit opponent moves for speed
		}

		int wins = 0;
		int losses = 0;

		for(PokerSell sell: sells) {
			List<Poker> pokers = PokerHelper.clonePokers(original);
			pokers.removeAll(sell.getSellPokers());
			if(pokers.isEmpty()) {
				return cursor == 0;
			}else {
				pokersList.set(cursor, pokers);

				Boolean suc = deduce(cursor, sell, cursor + 1, pokersList, counter, depth + 1);
				if(cursor != 0) {
					pokersList.set(cursor, original);
					return suc;
				}
				if(Math.abs(counter.get()) > DEDUCE_LIMIT) {
					pokersList.set(cursor, original);
					boolean result = counter.get() > DEDUCE_LIMIT;
					// Cache the result
					double winRate = result ? 1.0 : 0.0;
					stateCache.put(cacheKey, new CacheEntry(winRate, 1));
					return result;
				}
				if(suc != null) {
					if(suc) wins++; else losses++;
					counter.addAndGet(suc ? 1 : -1);
				}
				pokersList.set(cursor, original);
			}
		}

		// Update cache with this evaluation
		if(wins + losses > 0) {
			double winRate = (double) wins / (wins + losses);
			CacheEntry oldEntry = stateCache.get(cacheKey);
			if(oldEntry != null) {
				// Update existing entry
				oldEntry.winRate = (oldEntry.winRate * oldEntry.visitCount + winRate) / (oldEntry.visitCount + 1);
				oldEntry.visitCount++;
			} else {
				stateCache.put(cacheKey, new CacheEntry(winRate, 1));
			}
		}

		return null;
	}

	private static String serialPokers(List<Poker> pokers){
		if(pokers == null || pokers.isEmpty()) {
			return "n";
		}
		StringBuilder builder = new StringBuilder();
		for(int index = 0; index < pokers.size(); index ++) {
			builder.append(pokers.get(index).getLevel().getLevel()).append(index == pokers.size() - 1 ? "" : "_");
		}
		return builder.toString();
	}

	private static String serialPokersList(List<List<Poker>> pokersList){
		StringBuilder builder = new StringBuilder();
		for(int index = 0; index < pokersList.size(); index ++) {
			List<Poker> pokers = pokersList.get(index);
			builder.append(serialPokers(pokers)).append(index == pokersList.size() - 1 ? "" : "m");
		}
		return builder.toString();
	}

	@Override
	public boolean howToChooseLandlord(List<Poker> leftPokers, List<Poker> rightPokers, List<Poker> myPokers) {
		int leftScore = PokerHelper.parsePokerColligationScore(leftPokers);
		int rightScore = PokerHelper.parsePokerColligationScore(rightPokers);
		int myScore = PokerHelper.parsePokerColligationScore(myPokers);

		// Check for bombs and kings
		int bombCount = countBombs(myPokers);
		boolean hasKings = hasKingBomb(myPokers);

		// Be more aggressive with strong hands
		if(hasKings || bombCount >= 2) {
			return myScore >= (leftScore + rightScore) / 2.0 * 0.9; // Only need 90% of average
		}

		// Be more conservative with weaker hands - need 20% advantage
		return myScore >= (leftScore + rightScore) / 2.0 * 1.2;
	}

	/**
	 * Smart pruning: Filter out obviously bad moves
	 */
	private List<PokerSell> filterBadSells(List<PokerSell> sells, List<Poker> selfPoker, PokerSell lastPokerSell) {
		if(sells.size() <= 3) {
			return sells; // Too few options, don't filter
		}

		List<PokerSell> filtered = new ArrayList<>();

		for(PokerSell sell: sells) {
			// Don't use King Bomb unless necessary (less than 5 cards or against another bomb)
			if(sell.getSellType() == SellType.KING_BOMB) {
				if(selfPoker.size() <= 5 ||
				   (lastPokerSell != null && lastPokerSell.getSellType() == SellType.BOMB)) {
					filtered.add(sell);
				}
				continue;
			}

			// Don't use regular bomb too early (unless to win or less than 8 cards)
			if(sell.getSellType() == SellType.BOMB) {
				List<Poker> remaining = PokerHelper.clonePokers(selfPoker);
				remaining.removeAll(sell.getSellPokers());
				if(remaining.isEmpty() || remaining.size() <= 3 || selfPoker.size() <= 8) {
					filtered.add(sell);
				} else if(lastPokerSell != null && lastPokerSell.getSellType() == SellType.BOMB) {
					// Counter bomb with bomb
					filtered.add(sell);
				}
				continue;
			}

			// Don't break bombs unless necessary
			if(wouldBreakBomb(sell, selfPoker)) {
				// Only break bombs if we have very few cards left
				if(selfPoker.size() <= 5) {
					filtered.add(sell);
				}
				continue;
			}

			// Accept all other moves
			filtered.add(sell);
		}

		// If we filtered everything, return original list
		return filtered.isEmpty() ? sells : filtered;
	}

	/**
	 * Evaluate the value of a poker hand after playing a certain sell
	 */
	private double evaluatePokerValue(List<Poker> remainingPokers, PokerSell sell) {
		if(remainingPokers.isEmpty()) {
			return 1000.0; // Winning is always best
		}

		double value = 0;

		// 1. Fewer remaining cards is better
		value += (20 - remainingPokers.size()) * 10;

		// 2. Check flexibility - how many valid combinations can be made
		List<PokerSell> possibleSells = PokerHelper.parsePokerSells(remainingPokers);
		value += possibleSells.size() * 2;

		// 3. Having bombs is valuable
		int bombCount = countBombs(remainingPokers);
		value += bombCount * 50;

		// 4. Having king bomb is very valuable
		if(hasKingBomb(remainingPokers)) {
			value += 100;
		}

		// 5. Having straights is good (cards work together)
		value += countStraights(remainingPokers) * 15;

		// 6. Having pairs and triples is good
		value += countPairs(remainingPokers) * 5;
		value += countTriples(remainingPokers) * 8;

		// 7. Penalty for single high cards (2s, Aces) - they're hard to play
		for(Poker poker: remainingPokers) {
			if(poker.getLevel() == PokerLevel.LEVEL_2) {
				value -= 3;
			} else if(poker.getLevel() == PokerLevel.LEVEL_A) {
				value -= 2;
			}
		}

		// 8. Bonus for playing good combinations
		if(isStraightType(sell.getSellType())) {
			value += 20; // Reward for not breaking straights
		}

		return value;
	}

	/**
	 * Count the number of bombs in a poker hand
	 */
	private int countBombs(List<Poker> pokers) {
		int count = 0;
		Map<Integer, Integer> levelCount = new HashMap<>();

		for(Poker poker: pokers) {
			int level = poker.getLevel().getLevel();
			levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
		}

		for(int cnt: levelCount.values()) {
			if(cnt >= 4) {
				count++;
			}
		}

		return count;
	}

	/**
	 * Check if hand has King Bomb (both jokers)
	 */
	private boolean hasKingBomb(List<Poker> pokers) {
		boolean hasSmallKing = false;
		boolean hasBigKing = false;

		for(Poker poker: pokers) {
			if(poker.getLevel() == PokerLevel.LEVEL_SMALL_KING) {
				hasSmallKing = true;
			}
			if(poker.getLevel() == PokerLevel.LEVEL_BIG_KING) {
				hasBigKing = true;
			}
		}

		return hasSmallKing && hasBigKing;
	}

	/**
	 * Check if playing this sell would break a bomb
	 */
	private boolean wouldBreakBomb(PokerSell sell, List<Poker> pokers) {
		if(sell.getSellType() == SellType.BOMB || sell.getSellType() == SellType.KING_BOMB) {
			return false; // Already using the bomb
		}

		Map<Integer, Integer> levelCount = new HashMap<>();
		for(Poker poker: pokers) {
			int level = poker.getLevel().getLevel();
			levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
		}

		// Check if any card in the sell is from a potential bomb
		for(Poker poker: sell.getSellPokers()) {
			int level = poker.getLevel().getLevel();
			if(levelCount.getOrDefault(level, 0) >= 4) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Count the number of possible straights
	 */
	private int countStraights(List<Poker> pokers) {
		List<PokerSell> sells = PokerHelper.parsePokerSells(pokers);
		int count = 0;
		for(PokerSell sell: sells) {
			if(isStraightType(sell.getSellType())) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Count pairs in hand
	 */
	private int countPairs(List<Poker> pokers) {
		Map<Integer, Integer> levelCount = new HashMap<>();
		for(Poker poker: pokers) {
			int level = poker.getLevel().getLevel();
			levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
		}

		int count = 0;
		for(int cnt: levelCount.values()) {
			if(cnt >= 2) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Count triples in hand
	 */
	private int countTriples(List<Poker> pokers) {
		Map<Integer, Integer> levelCount = new HashMap<>();
		for(Poker poker: pokers) {
			int level = poker.getLevel().getLevel();
			levelCount.put(level, levelCount.getOrDefault(level, 0) + 1);
		}

		int count = 0;
		for(int cnt: levelCount.values()) {
			if(cnt >= 3) {
				count++;
			}
		}
		return count;
	}

	/**
	 * Check if sell type is a straight
	 */
	private boolean isStraightType(SellType sellType) {
		return sellType == SellType.SINGLE_STRAIGHT ||
		       sellType == SellType.DOUBLE_STRAIGHT ||
		       sellType == SellType.THREE_STRAIGHT ||
		       sellType == SellType.FOUR_STRAIGHT ||
		       sellType == SellType.THREE_STRAIGHT_WITH_SINGLE ||
		       sellType == SellType.THREE_STRAIGHT_WITH_DOUBLE ||
		       sellType == SellType.FOUR_STRAIGHT_WITH_SINGLE ||
		       sellType == SellType.FOUR_STRAIGHT_WITH_DOUBLE;
	}

}
