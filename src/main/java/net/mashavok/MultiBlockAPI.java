package net.mashavok;

import net.fabricmc.api.ModInitializer;

import net.mashavok.tags.Util;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultiBlockAPI implements ModInitializer {


	public static final String MOD_ID = ("multiblock-api");

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static Identifier getResource(String name) {
		return new Identifier(MOD_ID, name);
	}
	public static String makeTranslationKey(String base, String name) {
		return Util.makeTranslationKey(base, getResource(name));
	}

	public static MutableText makeTranslation(String base, String name) {
		return Text.translatable(makeTranslationKey(base, name));
	}
	public static MutableText makeTranslation(String base, String name, Object... arguments) {
		return Text.translatable(makeTranslationKey(base, name), arguments);
	}

	@Override
	public void onInitialize() {
	}
}