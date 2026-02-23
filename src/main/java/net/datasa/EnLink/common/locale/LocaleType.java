package net.datasa.EnLink.common.locale;

import java.util.Locale;

public enum LocaleType {
	KO("KR"),
	JA("JP");

	private final String code;

	LocaleType(String code) {
		this.code = code;
	}

	public String getCode() {
		return this.code;
	}

	public static LocaleType from(Locale locale) {
		return switch (locale.getLanguage()) {
			case "ko" -> LocaleType.KO;
			case "ja" -> LocaleType.JA;
			default -> LocaleType.KO;
		};
	}

	public static LocaleType from(String code) {
		return switch(code){
			case "ko" -> LocaleType.KO;
			case "ja" -> LocaleType.JA;
			default -> LocaleType.KO;
		};
	}
}
