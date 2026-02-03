package net.datasa.EnLink.common.language;

import java.util.Locale;

public enum LanguageType {
	KO("KR"),
	JP("JP");

	private final String code;

	LanguageType(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public static LanguageType from(Locale locale) {
		return switch (locale.getLanguage()) {
			case "ko" -> LanguageType.KO;
			case "ja" -> LanguageType.JP;
			default -> LanguageType.KO;
		};
	}
}
