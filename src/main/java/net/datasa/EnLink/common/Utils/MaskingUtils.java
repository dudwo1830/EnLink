package net.datasa.EnLink.common.Utils;

public final class MaskingUtils {
	public static String maskEmail(String email) {
		if (email == null)
			return "";

		int at = email.indexOf("@");
		if (at <= 1) {
			return email;
		}

		return email.charAt(0)
				+ "*".repeat(at - 2)
				+ email.charAt(at - 1)
				+ email.substring(at);
	}

}