package net.stardust.base.utils;

import java.util.List;

public interface AliaseHelper {
	List<String> getAliases(String mainName);
	boolean isAliase(String mainName, String aliase);
}
