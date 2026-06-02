/**
 * Game balance constants — numeric values synced from project.config.json via sync_project.py.
 * @see projectConfig.ts (generated)
 */
export {
  DAILY_FREE_PACKS,
  DAILY_FREE_SLOT_SPINS,
  DAILY_REWARDED_PACK_LIMIT,
  DAILY_SLOT_PACK_REWARD_CAP,
  REWARDED_SLOT_SPINS,
  SIGNUP_FREE_PACKS,
  STICKERS_PER_PACK,
} from "./projectConfig";

export const RARITY_WEIGHTS: Record<string, number> = {
  common: 70,
  rare: 20,
  epic: 8,
  legendary: 2,
};

export const RARITY_FALLBACK_ORDER = ["legendary", "epic", "rare", "common"];
