package dev.wiji.instancemanager.guilds.enums;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

public enum DyeColor {
	WHITE(0, 15),
	ORANGE(1, 14),
	MAGENTA(2, 13),
	LIGHT_BLUE(3, 12),
	YELLOW(4, 11),
	LIME(5, 10),
	PINK(6, 9),
	GRAY(7, 8),
	SILVER(8, 7),
	CYAN(9, 6),
	PURPLE(10, 5),
	BLUE(11, 4),
	BROWN(12, 3),
	GREEN(13, 2),
	RED(14, 1),
	BLACK(15, 0);

	private final byte woolData;
	private final byte dyeData;
	private static final DyeColor[] BY_WOOL_DATA = values();
	private static final DyeColor[] BY_DYE_DATA = values();

	private DyeColor(int var3, int var4) {
		this.woolData = (byte) var3;
		this.dyeData = (byte) var4;
	}

	@Deprecated
	public byte getData() {
		return this.getWoolData();
	}

	@Deprecated
	public byte getWoolData() {
		return this.woolData;
	}

	@Deprecated
	public byte getDyeData() {
		return this.dyeData;
	}

	@Deprecated
	public static DyeColor getByData(byte var0) {
		return getByWoolData(var0);
	}

	@Deprecated
	public static DyeColor getByWoolData(byte var0) {
		int var1 = 255 & var0;
		return var1 >= BY_WOOL_DATA.length ? null : BY_WOOL_DATA[var1];
	}

	@Deprecated
	public static DyeColor getByDyeData(byte var0) {
		int var1 = 255 & var0;
		return var1 >= BY_DYE_DATA.length ? null : BY_DYE_DATA[var1];
	}

	static {
		Builder var0 = ImmutableMap.builder();
		Builder var1 = ImmutableMap.builder();
		DyeColor[] var2 = values();
		int var3 = var2.length;
	}
}
